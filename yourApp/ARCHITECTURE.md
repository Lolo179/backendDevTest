# ARCHITECTURE.md

## Overview

This application implements the backend service under assessment.

It exposes:

`GET /product/{productId}/similar`

The endpoint returns the details of products similar to the requested product.

The provided external mock API runs at:

`http://localhost:3001`

External endpoints:

- `GET /product/{productId}/similarids`
- `GET /product/{productId}`

## Runtime topology

The provided Docker Compose starts:

- `simulado`: mock API, exposed as `localhost:3001`
- `k6`: load test runner
- `influxdb`: k6 metrics storage
- `grafana`: k6 dashboard

The Spring Boot application runs locally on port `5000`.

k6 calls the local application through:

`http://host.docker.internal:5000`

## Request flow

1. The client calls `GET /product/{productId}/similar`.
2. The controller delegates to the service.
3. The service calls `/product/{productId}/similarids`.
4. The service receives an ordered list of similar product IDs.
5. The service fetches each product detail concurrently.
6. Failed individual product detail calls are omitted.
7. The service returns the successful products preserving the original order.
8. The controller returns a JSON array.

## Layers

### Controller

Responsibilities:

- Expose the REST endpoint.
- Implement the generated server API interface from `similarProducts.yaml`.
- Map path variables.
- Delegate to the service.
- Convert internal `Product` models to generated API `ProductDetail` models through a mapper.
- Return the response body.

The controller must not contain orchestration logic.

### Service

Responsibilities:

- Coordinate the use case.
- Fetch similar product IDs.
- Fetch product details concurrently.
- Preserve ordering.
- Apply partial failure policy.

The service works with the internal `Product` model.

### HTTP Client

Responsibilities:

- Call the external mock API.
- Encapsulate URL construction and HTTP mapping.
- Convert downstream failures into controlled exceptions.
- Keep explicit control of timeout and error behavior.

The downstream client is implemented manually with `RestClient`.

`existingApis.yaml` is used as reference contract, but its client is not code-generated.

The downstream client deserializes into internal `Product` while contracts remain shape-compatible.

## Mapping strategy

Mapping from internal `Product` to generated API `ProductDetail` is explicit in the API layer.

MapStruct is used to generate compile-time mapping code with type-safe signatures.

### Configuration

Responsibilities:

- Application port.
- Downstream base URL.
- Connect/read timeouts.
- Product detail concurrency settings.

### Error Handling

Responsibilities:

- Convert controlled application exceptions into HTTP responses.
- Avoid leaking stack traces or implementation details.

## Concurrency model

Product detail calls are independent and should be executed concurrently.

Use `CompletableFuture` with a dedicated executor.

Do not use `parallelStream`, because it relies on the common ForkJoinPool and provides less control under load.

## Resilience model

The similar IDs call is mandatory because it defines which products must be retrieved.

Product detail calls are best-effort because each product detail is an independent enrichment.

If a product detail fails, the application omits that item and returns the remaining products.

## Ordering

The list returned by `/similarids` is ordered by similarity.

The final response must preserve that order for successfully retrieved products.

## Contract-first strategy

The public API is API First.

`similarProducts.yaml` is the source of truth for public endpoint signatures.

OpenAPI Generator is used to generate server API interfaces (preferably `interfaceOnly`).

The controller implements those generated interfaces to reduce drift between code and contract.