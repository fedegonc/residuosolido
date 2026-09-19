# Limitaciones y Fuera de Alcance — Eco Solicitud

Lo que el MVP reconoce como limitación consciente o quedó fuera de alcance, con su justificación. Extraído de `MEJORAS.md`.

## Limitaciones reconocidas (fuera del alcance del MVP)

### Arquitectura de cuenta/organización

- Una cuenta `User` con rol `ORGANIZATION` representa simultáneamente
  identidad de acceso y participante de negocio.
- **No soporta:** múltiples operadores por cooperativa, cambio de
  responsable sin cambiar la cuenta, ni una misma persona como ciudadano y
  operador de cooperativa.
- **Para soportarlo** haría falta separar `Cuenta`, `Organización` y su
  relación de pertenencia. No se hace en este MVP.

### Verificación de legitimidad

- El registro público permite autodeclararse organización. No verifica que
  la cooperativa exista ni que quien se registra la represente.
- **Para producción** se necesitaría validación humana o un entorno
  controlado.

### Trazabilidad del residuo

- `COMPLETED` significa "la organización declaró completada la solicitud".
  No verifica retiro físico, cantidad recibida, conformidad del ciudadano
  ni destino final.
- Las métricas cuentan solicitudes completadas, no kilos reciclados ni
  impacto ambiental medido.

### Asignación de recolector

- `InformalCollector` es una agenda interna de la organización. `Request`
  no tiene relación con el recolector responsable.
- No se puede responder "qué recolector atendió esta solicitud".

### Concurrencia

- MongoDB standalone no soporta transacciones multi-documento. Las
  operaciones que involucran varios documentos (crear solicitud + subir
  imagen) no son atómicas a nivel de base de datos.
- `@Version` protege las transiciones de estado y el borrado, pero no hay
  rollback automático si una operación parcial falla después de persistir.

### Historial de transiciones

- Se conserva el estado actual, no una historia de transiciones con fecha,
  responsable y motivo.


## Fuera de alcance — GPS y geolocalización interactiva

La sección 1.4 de la tesis establece explícitamente que "No hay mapas ni
geolocalización interactiva", fundamentado en el Oficio 044/2023. Esta
decisión se mantiene para el MVP.

Si después de la defensa se decide ampliar el alcance, la implementación
sería:

1. **Modelo:** agregar campo opcional `location: {lat: Double, lng: Double}`
   a `Request`. No obligatorio, no rompe RF-3.
2. **Frontend:** botón "Usar mi ubicación GPS" (Geolocation API del
   navegador) que muestra un mapa Leaflet con marcador arrastrable dentro
   del área de cobertura Rivera–Sant'Ana do Livramento.
3. **Validación server-side:** el punto lat/lng, si viene, debe caer dentro
   de un radio o polígono que cubra la zona fronteriza. Rechazar o ignorar
   si cae fuera.
4. **Documentación:** actualizar la sección 1.4 de la tesis y este archivo
   explicando el cambio de alcance y su justificación técnica.

Esta mejora queda registrada como posible evolución, no como deuda técnica.

---

