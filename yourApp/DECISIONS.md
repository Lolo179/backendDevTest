# DECISIONS.md

## Decision 001 - Use OpenAPI REST, not AsyncAPI

The public contract is synchronous REST over HTTP.

The provided contracts are OpenAPI documents and all interactions are request/response HTTP calls.

AsyncAPI is not appropriate because there is no event broker, topic, queue or publish/subscribe workflow.

Internal concurrency does not change the external API contract.

## Decision 002 - Run the application locally on port 5000

The provided `docker-compose.yml` starts mocks, k6, InfluxDB and Grafana, but not the application under assessment.

The k6 script calls:

`http://host.docker.internal:5000`

Therefore the Spring Boot application will run locally on port `5000`.

## Decision 003 - Do not modify provided assessment infrastructure

The provided Docker Compose, mocks, k6 scripts, Grafana configuration and OpenAPI contracts represent the evaluation environment.

They should remain unchanged unless explicitly required.

## Decision 004 - Use RestClient

Use Spring `RestClient` for downstream HTTP calls.

Reason:

- The application is simple and Spring MVC oriented.
- RestClient keeps the implementation readable.
- WebClient would add reactive complexity that is not required.
- Concurrency can be achieved with `CompletableFuture`.

## Decision 005 - Fetch product details concurrently

The product detail calls are independent.

Fetching them sequentially would multiply latency, especially for the slow and very slow mock scenarios.

Use `CompletableFuture` with a dedicated executor.

## Decision 006 - Use controlled concurrency

Do not use `parallelStream`.

Use a dedicated executor with configurable max concurrency.

This avoids relying on the common ForkJoinPool and gives better control under k6 load.

## Decision 007 - Configure timeouts

The mocks include delayed product detail responses:

- `/product/100` delays around 1000 ms
- `/product/1000` delays around 5000 ms
- `/product/10000` delays around 50000 ms

The application must not wait indefinitely for very slow downstream responses.

Use configurable connect and read timeouts.

Initial target:

- connect timeout: 500 ms
- read timeout: 1500 ms

## Decision 008 - Treat similar IDs as mandatory

The `/product/{productId}/similarids` call defines the list of similar products.

If this call fails, the use case cannot be resolved correctly.

Policy:

- 404 from `/similarids` should become 404.
- 5xx or timeout from `/similarids` should become a controlled downstream error.

## Decision 009 - Treat product details as best-effort

Product detail calls are independent enrichment calls.

If one product detail call fails with 404, 5xx or timeout, omit that product and continue.

This improves resilience and matches the provided k6 scenarios.

## Decision 010 - Preserve similarity order

The `/similarids` response is ordered by similarity.

The final response must preserve the order of the original IDs for all successfully retrieved products.

Concurrency must not alter the functional ordering.

## Decision 011 - Use BigDecimal for price

The contract defines `price` as a number.

The application will represent price with `BigDecimal` to avoid floating point precision issues for monetary values.

## Decision 012 - Keep the architecture simple

Use a simple layered architecture:

- Controller
- Service
- HTTP Client
- Configuration
- Error Handling
- Tests

Do not introduce database, messaging, JPA, Kafka or heavy domain abstractions.