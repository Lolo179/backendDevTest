# AGENTS.md

## Role

Act as a Senior Backend Engineer working on a Spring Boot backend technical assessment.

## Goal

Implement only `yourApp`, a Spring Boot application exposing:

`GET /product/{productId}/similar`

The application must run on port `5000` and consume the provided mocks at `http://localhost:3001`.

## Repository boundaries

Do not modify the provided assessment files unless explicitly requested:

- `docker-compose.yml`
- `similarProducts.yaml`
- `existingApis.yaml`
- `shared/k6`
- `shared/simulado`
- `shared/grafana`

The provided files define the evaluation environment.

## Technical approach

Use a simple layered architecture:

- Controller
- Service
- HTTP Client
- Configuration
- Error Handling
- Tests

Avoid overengineering. Do not introduce database, messaging, Kafka, JPA, heavy hexagonal architecture or AsyncAPI.

## API style

The public API is synchronous REST over HTTP and is documented with OpenAPI.

Do not use AsyncAPI. Internal concurrency does not change the public contract.

## HTTP client strategy

Preferred approach:

- Spring MVC application
- `RestClient` for downstream HTTP calls
- `CompletableFuture` for concurrent product detail calls
- dedicated configurable executor
- configured connect and read timeouts

## Resilience strategy

- `/product/{productId}/similarids` is mandatory.
- If `/similarids` fails with 404, return 404.
- If `/similarids` fails with 5xx or timeout, return a controlled downstream error.
- Product detail calls `/product/{similarId}` are best-effort.
- If an individual product detail fails with 404, 5xx or timeout, omit it.
- Return successfully retrieved products preserving the original similarity order.

## Development workflow

Before implementing a task:

1. Read `ARCHITECTURE.md`.
2. Read `DECISIONS.md`.
3. Check `TASKS.md`.
4. Implement only the current task.
5. Keep changes small.
6. Run available tests after meaningful changes.
7. Update `TASKS.md` when a task is completed.

## Code quality rules

- Keep code simple and readable.
- Prefer explicit names over clever abstractions.
- Keep business orchestration out of controllers.
- Do not hardcode downstream URLs in business logic.
- Preserve product order from `/similarids`.
- Avoid `parallelStream`.
- Avoid unbounded concurrency.

## Testing rules

- All new or modified tests must use explicit sections:
	- `// given`
	- `// when`
	- `// then`
- Test names must describe observable behavior, not implementation details.
- Prefer expressive assertions (AssertJ and MockMvc matchers).
- Do not test irrelevant internal details.
- Do not add sleeps or fragile concurrency tests.
- Controller tests must validate HTTP contract, status and basic response shape.
- Service tests must validate functional rules: ordering, partial response and propagation of mandatory-call failures.
- Mapper tests must validate field-by-field transformation.