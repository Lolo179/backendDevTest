# TASKS

## TASK-001 - Crear esqueleto Maven Spring Boot en yourApp (COMPLETADA)
- [x] Estructura Maven base creada en `yourApp/`.
- [x] Java 21 configurado en `pom.xml`.
- [x] Dependencias añadidas: Spring Boot Web y Spring Boot Test.
- [x] Clase principal y test de carga de contexto creados.
- [x] Sin lógica de negocio implementada.

## T1 - Preparar estructura base
- Crear paquetes para controlador, servicio, cliente, config y modelo.
- Definir DTOs según contratos existentes.

## T2 - Endpoint de entrada
- Implementar `GET /product/{productId}/similar` en puerto `5000`.
- Validar path param y respuesta JSON.

## T3 - Cliente externo
- Implementar cliente para:
  - `GET /product/{productId}/similarids`
  - `GET /product/{id}`
- Centralizar manejo de errores HTTP.

## T4 - Agregación concurrente
- Implementar orquestación con `CompletableFuture`.
- Configurar `Executor` dedicado para llamadas remotas.
- Preservar orden original de `similarids`.

## T5 - Resiliencia y respuesta parcial
- Configurar timeout por detalle individual.
- Omitir items con `404`, `500` o timeout.
- Definir comportamiento cuando falla `similarids`.

## T6 - Observabilidad mínima
- Añadir logs de tiempos y errores por dependencia.
- Exponer métricas básicas (éxitos/fallos/timeout) si aplica.

## T7 - Pruebas
- Unit tests de servicio de agregación (orden y filtrado).
- Tests de integración del endpoint contra mocks.
- Verificar comportamiento parcial y latencia aceptable.

## T8 - Ajuste final
- Calibrar timeout y tamaño de pool.
- Revisar cumplimiento con contratos y restricciones de la prueba.
