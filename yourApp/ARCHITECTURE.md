# ARCHITECTURE

## Contexto
Servicio backend que expone `GET /product/{productId}/similar` y compone datos desde mocks externos.

## Flujo principal
1. API recibe `productId`.
2. Servicio consulta `GET http://localhost:3001/product/{productId}/similarids`.
3. Para cada ID similar, lanza consulta concurrente a `GET http://localhost:3001/product/{id}`.
4. Agrega solo respuestas válidas (`200`) en el mismo orden de `similarids`.
5. Devuelve lista de `ProductDetail`.

## Componentes lógicos
- `Controller`: expone endpoint y valida entrada.
- `SimilarProductsService`: orquesta flujo y concurrencia.
- `ExternalProductClient`: encapsula llamadas HTTP a mocks.
- `ExecutorConfig`: configura thread pool dedicado.
- `ErrorHandling`: transforma errores técnicos en comportamiento esperado (respuesta parcial).

## Reglas de resiliencia
- Timeout por llamada individual a detalle.
- Si una llamada de detalle falla (`404`, `500`, timeout), se omite ese item.
- Si falla la llamada de `similarids`, se responde error de upstream (sin inventar datos).
- Nunca alterar el orden relativo de IDs exitosos.

## Contratos y compatibilidad
- Entrada: `GET /product/{productId}/similar`.
- Salida: `200` con array de `ProductDetail` (posiblemente parcial).
- Puerto del servicio: `5000`.
- Dependencia externa: `http://localhost:3001`.

## Observabilidad mínima
- Logs de inicio/fin por request y tiempos de agregación.
- Contadores de éxito, timeout y error por llamada externa.
