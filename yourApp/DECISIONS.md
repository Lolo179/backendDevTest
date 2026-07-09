# DECISIONS

## D1 - Integración externa REST síncrona
- Decisión: consumir mocks vía HTTP REST síncrono.
- Motivo: los contratos y entorno de prueba ya están definidos así.
- Consecuencia: simplicidad operativa y menor complejidad de infraestructura.

## D2 - Concurrencia interna con CompletableFuture
- Decisión: resolver detalles en paralelo con `CompletableFuture`.
- Motivo: reducir latencia total cuando hay múltiples IDs similares.
- Consecuencia: mejor tiempo de respuesta promedio frente a ejecución secuencial.

## D3 - Executor dedicado
- Decisión: usar pool dedicado para IO remoto.
- Motivo: aislar carga externa y evitar bloquear recursos compartidos.
- Consecuencia: control explícito de capacidad y degradación.

## D4 - Timeouts por llamada individual
- Decisión: timeout por request a `GET /product/{id}`.
- Motivo: evitar esperas largas por dependencias lentas.
- Consecuencia: habilita respuesta parcial predecible.

## D5 - Respuesta parcial ante fallos individuales
- Decisión: omitir productos fallidos (`404`, `500`, timeout) y continuar.
- Motivo: priorizar disponibilidad y utilidad del endpoint agregado.
- Consecuencia: la longitud de respuesta puede ser menor que `similarids`.

## D6 - Preservar orden de similarids
- Decisión: mantener orden original en resultados exitosos.
- Motivo: requerimiento explícito del problema.
- Consecuencia: la agregación debe mapear por posición y filtrar sin reordenar.

## D7 - No AsyncAPI, no sobreingeniería
- Decisión: evitar mensajería/event-driven en esta solución.
- Motivo: alcance acotado, contrato REST claro y foco en entrega.
- Consecuencia: arquitectura directa, mantenible y suficiente para la prueba.

## Pendiente técnica
- Definir valores exactos de timeout y tamaño del thread pool según pruebas locales de latencia/carga.
