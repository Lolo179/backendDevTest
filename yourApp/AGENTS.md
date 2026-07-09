# AGENTS

## Objetivo del agente
Guiar la implementación backend de `GET /product/{productId}/similar` sin sobreingeniería y respetando los contratos entregados.

## Alcance
- Implementar API en puerto `5000`.
- Consumir mocks externos en `http://localhost:3001`.
- Orquestar llamadas concurrentes para obtener detalles de productos similares.
- Mantener orden original de `similarids` en la respuesta final.

## Restricciones
- No modificar archivos entregados de mocks, k6, grafana ni `docker-compose.yaml`.
- No introducir AsyncAPI.
- Priorizar simplicidad, legibilidad y comportamiento determinista.

## Criterios de implementación
- Cliente externo REST síncrono.
- Orquestación interna con `CompletableFuture` + `Executor` dedicado.
- Timeouts por llamada y manejo de errores individuales con respuesta parcial.
- Logging y métricas básicas para trazabilidad.

## Definición de terminado (DoD)
- Endpoint funcional en puerto `5000`.
- Orden de respuesta preservado.
- Fallos parciales no rompen toda la respuesta.
- Pruebas mínimas de controlador y servicio de agregación.
- Documentación alineada con `ARCHITECTURE.md`, `DECISIONS.md` y `TASKS.md`.
