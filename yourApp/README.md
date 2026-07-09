# yourApp

## Resumen

Este proyecto contiene una aplicación Spring Boot para el ejercicio técnico de backend.

Funcionalidad objetivo de la aplicación:
- GET /product/{productId}/similar

El contrato público está definido por similarProducts.yaml.

## Estado actual

La base del proyecto está creada y la configuración inicial está aplicada.

En este punto, el endpoint objetivo todavía no se documenta como implementado.

## Entorno de ejecución

- La aplicación está configurada para ejecutarse en el puerto 5000.
- La integración downstream consume los mocks proporcionados en http://localhost:3001.
- Docker Compose del repositorio levanta mocks, k6, InfluxDB y Grafana, pero no levanta la aplicación Spring Boot.

## Verificacion manual con mocks proporcionados

Objetivo:

Verificar manualmente el endpoint publico contra `simulado`, validando orden, omision de fallos individuales y respuesta parcial.

Comandos:

1. Levantar mocks e infraestructura de observabilidad (desde la raiz del repositorio):
	- `docker-compose up -d simulado influxdb grafana`
2. Verificar que el mock responde:
	- `curl http://localhost:3001/product/1/similarids`
3. Arrancar la aplicacion (desde `yourApp`):
	- `mvn spring-boot:run`
4. Probar endpoints publicos:
	- `curl http://localhost:5000/product/1/similar`
	- `curl http://localhost:5000/product/2/similar`
	- `curl http://localhost:5000/product/3/similar`
	- `curl http://localhost:5000/product/4/similar`
	- `curl http://localhost:5000/product/5/similar`

Resultados esperados con timeouts iniciales (`read-timeout=1500ms`):

- product 1 -> productos 2, 3, 4.
- product 2 -> productos 3, 100 aproximadamente (1000 suele omitirse por timeout).
- product 3 -> producto 100 aproximadamente (1000 y 10000 suelen omitirse por timeout).
- product 4 -> productos 1, 2 omitiendo 5 por 404.
- product 5 -> productos 1, 2 omitiendo 6 por 500.

Nota:

Los resultados parciales dependen de la estrategia de timeout configurada. Si cambian `connect-timeout` o `read-timeout`, puede variar que productos lentos se incluyan u omitan.

## Estilo de API y contrato

- La API pública es REST síncrona sobre HTTP.
- La especificación del contrato es OpenAPI.
- No se usa AsyncAPI para este caso.
- Se sigue enfoque API First / contract-first para la API pública.
- `similarProducts.yaml` se usa como fuente de verdad para la firma del endpoint público.
- Se usará OpenAPI Generator para generar interfaces del servidor (preferiblemente `interfaceOnly`), no una aplicación completa.
- El controller implementará la interfaz generada para mantener alineación contrato-código.

## Arquitectura objetivo

La arquitectura prevista para la solución es simple y por capas:
- Controller
- Service
- HTTP Client
- Configuration
- Error Handling

Para la integración downstream, el cliente hacia los mocks (`existingApis.yaml`) no se generará con OpenAPI Generator.

Ese cliente se implementará manualmente con `RestClient` para mantener control explícito de timeouts, errores y normalización de similar IDs.

## Concurrencia

La concurrencia se aplicará de forma interna para optimizar la obtención de detalles de productos.

Esta decisión no cambia el contrato público ni el estilo síncrono de la API.

## Decisión de modelo y mapping

El modelo interno será `Product`.

La API pública usa los modelos generados desde OpenAPI (`similarProducts.yaml`).

El mapping de `Product` hacia el `ProductDetail` generado para la API se realizará con MapStruct.

MapStruct se elige porque genera código en compilación, evita reflection en runtime y aporta type-safety.

No se usa MapStruct para ocultar complejidad inexistente en downstream.

En el estado actual, el mock downstream y el contrato público comparten la misma forma:
- id
- name
- price
- availability

Por este motivo, el cliente downstream puede deserializar directamente al modelo interno `Product`.

Si en el futuro el contrato downstream fuera distinto (por ejemplo, integración con un DataService HOST/COBOL), se crearían DTOs downstream específicos y mappers adicionales para desacoplar modelos.
