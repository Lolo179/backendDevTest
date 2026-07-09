# TASKS.md

## TASK-000 - Repository setup and assessment analysis

Status: DONE

Goal:

- Create personal repository.
- Preserve provided assessment files.
- Understand README, contracts, Docker Compose, k6 script and mocks.
- Define initial implementation strategy.

Notes:

- The application will be implemented under `yourApp/`.
- Provided infrastructure files should not be modified.

## TASK-001 - Add project guidance documentation

Status: DONE

Goal:

Create the documentation files used to guide AI-assisted implementation:

- `AGENTS.md`
- `ARCHITECTURE.md`
- `DECISIONS.md`
- `TASKS.md`

Acceptance criteria:

- Documents explain the target architecture.
- Documents explain the resilience and performance decisions.
- Documents state that provided assessment infrastructure must not be modified.
- Documents define a task-by-task implementation workflow.
- No Java business code is implemented in this task.

## TASK-002 - Create Spring Boot application skeleton

Status: DONE

Goal:

Create an executable Spring Boot project under `yourApp/`.

Requirements:

- Java 21.
- Maven.
- Spring Boot Web.
- Spring Boot Test.
- No business logic yet.

Acceptance criteria:

- `mvn test` or `./mvnw test` runs successfully.
- The project has a clear package structure.
- No provided assessment infrastructure files are modified.

## TASK-003 - Configure application properties

Status: DONE

Goal:

Configure:

- server port `5000`
- product API base URL
- connect timeout
- read timeout
- max concurrency for product detail calls

Initial values:

- `external.product-api.base-url=http://localhost:3001`
- `external.product-api.connect-timeout=500ms`
- `external.product-api.read-timeout=1500ms`
- `similar-products.client.max-concurrency=100`

Acceptance criteria:

- Configuration is externalized.
- Properties are type-safe where useful.
- Application context loads.

## TASK-004 - Add internal Product model

Status: DONE

Goal:

Create the internal `Product` model used by service/client layers.

Fields:

- `id`
- `name`
- `price`
- `availability`

Acceptance criteria:

- The model matches the public API response shape.
- `price` uses `BigDecimal`.
- No unnecessary DTO layers are introduced.

## TASK-005 - Generate public API interfaces from OpenAPI

Status: DONE

Goal:

Adopt API First for the public contract using OpenAPI Generator over `similarProducts.yaml`.

Acceptance criteria:

- OpenAPI Generator configuration is added for server-side generation.
- Generation is interface-focused (`interfaceOnly`) if supported by selected generator.
- No full application scaffold is generated.
- Generated API interfaces can be implemented by the Spring controller.
- No downstream client is generated from `existingApis.yaml`.

## TASK-006 - Implement Product API client

Status: DONE

Goal:

Implement a client for the provided mock API.

Methods:

- `getSimilarIds(String productId)`
- `getProduct(String productId)`

Acceptance criteria:

- Uses configured base URL.
- Uses configured timeouts.
- Maps 404 and downstream failures to controlled exceptions.
- Does not expose RestClient details to the service.
- Normalizes similar IDs from downstream payload to `List<String>`.
- Adds mapping from internal `Product` to generated API `ProductDetail` through MapStruct.

Notes:

- Implement manually with `RestClient`.
- Do not generate downstream client from `existingApis.yaml`.

## TASK-007 - Implement Similar Products service

Status: DONE

Goal:

Implement the use case orchestration.

Acceptance criteria:

- Calls `/similarids` first.
- Returns empty list if there are no similar IDs.
- Fetches product details concurrently.
- Uses a dedicated executor.
- Omits failed individual product details.
- Preserves the original similarity order.

## TASK-008 - Implement REST controller

Status: DONE

Goal:

Expose:

`GET /product/{productId}/similar`

Acceptance criteria:

- Returns a JSON array of product details.
- Implements the generated API interface from `similarProducts.yaml`.
- Delegates to the service.
- Contains no business orchestration logic.

Notes:

- Controller tests should use explicit Given / When / Then structure for readability.

## TASK-009 - Implement error handling

Status: DONE

Goal:

Add global exception handling.

Acceptance criteria:

- Product not found from mandatory `/similarids` maps to 404.
- Downstream technical failures from mandatory `/similarids` map to a controlled error.
- Unexpected errors do not leak stack traces.

Validation:

- `mvn test` passed successfully after Global Error Handling verification.

## TASK-010 - Add tests

Status: DONE

Goal:

Add focused tests for service, controller and configuration/client behavior.

Minimum cases:

- Happy path preserves order.
- Empty similar IDs returns empty list.
- Individual product 404 is omitted.
- Individual product 5xx is omitted.
- Individual product timeout is omitted if practical to test.
- Mandatory similar IDs failure propagates.
- Controller returns expected status.

Validation:

- `mvn test` passed successfully after expanding service and ProductApiRestClient coverage.

## TASK-011 - Manual verification with mocks

Status: DONE

Goal:

Run the app against the provided mocks.

Commands:

- `docker-compose up -d simulado influxdb grafana`
- `curl http://localhost:3001/product/1/similarids`
- `curl http://localhost:5000/product/1/similar`
- `curl http://localhost:5000/product/2/similar`
- `curl http://localhost:5000/product/3/similar`
- `curl http://localhost:5000/product/4/similar`
- `curl http://localhost:5000/product/5/similar`

Expected behavior:

- Product 1 returns products 2, 3, 4.
- Product 2 returns products 3 and 100 with the initial timeout strategy.
- Product 3 returns product 100 with the initial timeout strategy.
- Product 4 returns products 1 and 2.
- Product 5 returns products 1 and 2.

Validation:

- `mvn test` passed successfully after documenting manual verification workflow.

## TASK-012 - Run k6 performance test

Status: DONE

Goal:

Run the provided k6 load test.

Command:

`docker-compose run --rm k6 run scripts/test.js`

Acceptance criteria:

- Application remains responsive.
- Slow and very slow scenarios do not block indefinitely.
- Not found and error scenarios are handled gracefully.
- Grafana dashboard can be inspected.

Validation:

- `mvn test` passed successfully after documenting Postman and k6 verification workflow.
- k6 command and dashboard were documented for manual execution.

## TASK-013 - Final README

Status: DONE

Goal:

Document how to run and explain technical decisions for reviewers.

Acceptance criteria:

- Explains application purpose.
- Explains how to run mocks.
- Explains how to run the app.
- Explains how to run k6.
- Explains concurrency, timeouts and partial response policy.

Validation:

- `mvn test` passed successfully after final README update.
- README includes Postman and k6 execution guidance and observed k6 behavior.

## TASK-015 - Future improvement: load tuning observation

Status: TODO

Goal:

Review optional timeout and concurrency tuning under sustained load if occasional 502 responses appear in `error` scenario.

Notes:

- Keep current functional policy and architecture.
- Treat as post-delivery improvement, not a blocker for current submission.

## TASK-014 - Refactor package structure to lightweight hexagonal architecture

Status: DONE

Goal:

Refactor package structure to:

- `domain.model`
- `application.port`
- `application.service`
- `infrastructure.inbound.rest`
- `infrastructure.outbound.productapi`
- `infrastructure.config`
- `shared.error`

Acceptance criteria:

- Refactor is structural only (no functional behavior changes).
- Public API contract and error policy remain unchanged.
- Tests pass after the refactor.

Validation:

- `mvn test` passed successfully after package and import updates.