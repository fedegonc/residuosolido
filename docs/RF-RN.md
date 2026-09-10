**Nota para la defensa de tesis:** estos puntos no son omisiones — son decisiones de alcance conscientes, justificadas porque exceden lo que una herramienta de software puede o debe resolver.

---

## 6. Criterio de alcance y backlog pendiente

**Criterio para decidir si algo nuevo entra al alcance** (chequear en este orden):
1. ¿Está en el oficio o surge de una necesidad real confirmada por el stakeholder (organización/usuario)?
2. ¿Es responsabilidad de un sistema de software, o es logística/inversión física/proceso humano?
3. ¿Se puede resolver con un campo o servicio simple, o requiere una entidad/módulo nuevo?

Si 1 es sí, 2 es "sí es del software" y 3 es "simple" → entra al backlog. Si no, se documenta como limitación consciente (sección 5).

**Backlog pendiente (no implementado):**
- 🟡 **Métricas privadas por organización + descarga PDF.** Nueva ruta protegida `/acopio/metricas` con `@PreAuthorize("hasRole('ORGANIZATION')")` y endpoint `GET /acopio/metricas/pdf` (sugerido: OpenPDF o iText community). La ruta pública `/metricas` (totales agregados, sin datos personales) es una decisión consciente de diseño, no un bug — está explícitamente en `permitAll()` en `SecurityConfig`.
- 🟡 **Consistencia de nombres** (baja prioridad): revisar que los nombres de métodos de `RequestQueryService`/`RequestOrgService`/`RequestMetricsService`/`CityOrgService` reflejen consistentemente su sub-dominio.
