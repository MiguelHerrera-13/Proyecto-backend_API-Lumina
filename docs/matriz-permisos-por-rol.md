# Matriz de permisos por rol y endpoint

## Objetivo

Este documento releva los endpoints actualmente existentes en el backend de Lúmina y propone una política de acceso para los roles `ADMIN`, `CUIDADOR` y `MEDICO`.

Esta matriz es una definición funcional y no implementa autenticación, JWT ni controles de autorización.

## Leyenda

- ✅ Permitido
- ❌ Denegado
- ⚠️ Pendiente de definición

Cuando un permiso indica que el recurso debe estar "relacionado" con el usuario, significa que el acceso debería limitarse a pacientes, turnos, reportes o tratamientos vinculados con el usuario autenticado.

La forma técnica de comprobar esa relación queda fuera del alcance de esta issue y deberá resolverse posteriormente mediante autenticación y autorización.

---

## Endpoints relevados

### Alertas

- `GET /api/alertas/medico/{id}`  
  Consulta las alertas clínicas no leídas asociadas a un médico.

### Pacientes

- `POST /api/pacientes`  
  Registra un nuevo paciente.

- `GET /api/pacientes/{id}`  
  Obtiene los datos de un paciente.

- `GET /api/pacientes/{id}/historial`  
  Obtiene el historial clínico resumido del paciente.

- `DELETE /api/pacientes/{id}`  
  Realiza la baja lógica de un paciente.

### Reportes diarios

- `POST /api/reportes-diarios`  
  Registra un reporte diario asociado a un turno.

- `GET /api/reportes-diarios/turno/{idTurno}`  
  Consulta los reportes asociados a un turno.

- `GET /api/reportes-diarios/{id}`  
  Obtiene un reporte diario por su identificador.

### Tratamientos

- `POST /api/tratamientos`  
  Registra un tratamiento asociado a un paciente y un médico.

- `GET /api/tratamientos/paciente/{id}`  
  Consulta los tratamientos de un paciente.

- `GET /api/tratamientos/{id}`  
  Consulta un tratamiento por su identificador.

- `DELETE /api/tratamientos/{id}`  
  Realiza la baja lógica de un tratamiento.

### Turnos

- `POST /api/turnos`  
  Registra un turno entre paciente, cuidador y médico.

- `GET /api/turnos/{id}`  
  Consulta un turno por su identificador.

- `GET /api/turnos/cuidador/{idCuidador}`  
  Consulta los turnos asociados a un cuidador.

### Usuarios

- `POST /api/usuarios`  
  Registra un nuevo usuario.

- `GET /api/usuarios/{id}`  
  Consulta los datos de un usuario.

- `DELETE /api/usuarios/{id}`  
  Realiza la baja lógica de un usuario.

---

## Matriz propuesta de permisos

| Endpoint | ADMIN | CUIDADOR | MEDICO | Justificación |
|---|---|---|---|---|
| `GET /api/alertas/medico/{id}` | ✅ | ❌ | ✅ propio | Las alertas clínicas están destinadas al médico responsable |
| `POST /api/pacientes` | ✅ | ✅ | ❌ | Se propone que administración y cuidadores puedan registrar pacientes |
| `GET /api/pacientes/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Cuidador y médico requieren consultar pacientes vinculados |
| `GET /api/pacientes/{id}/historial` | ✅ | ✅ relacionado | ✅ relacionado | El historial puede ser necesario para la atención y cuidado del paciente |
| `DELETE /api/pacientes/{id}` | ✅ | ❌ | ❌ | Se propone como una operación administrativa |
| `POST /api/reportes-diarios` | ⚠️ | ✅ relacionado | ❌ | El reporte diario está asociado al seguimiento realizado mediante un turno |
| `GET /api/reportes-diarios/turno/{idTurno}` | ✅ | ✅ relacionado | ✅ relacionado | Los participantes relacionados con el turno pueden requerir su consulta |
| `GET /api/reportes-diarios/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Se propone la misma restricción que para la consulta por turno |
| `POST /api/tratamientos` | ⚠️ | ❌ | ✅ | La creación de tratamientos corresponde funcionalmente al médico |
| `GET /api/tratamientos/paciente/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Médico y cuidador pueden necesitar consultar las indicaciones del paciente |
| `GET /api/tratamientos/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Se limita a tratamientos de pacientes relacionados |
| `DELETE /api/tratamientos/{id}` | ⚠️ | ❌ | ✅ propio | Se propone que el médico pueda dar de baja tratamientos que le correspondan |
| `POST /api/turnos` | ✅ | ✅ | ⚠️ | Falta confirmar si los médicos también pueden crear turnos |
| `GET /api/turnos/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Cuidador y médico deberían consultar únicamente turnos relacionados |
| `GET /api/turnos/cuidador/{idCuidador}` | ✅ | ✅ propio | ❌ | El endpoint representa específicamente la agenda de un cuidador |
| `POST /api/usuarios` | ⚠️ | ⚠️ | ⚠️ | Debe definirse si existe autorregistro o si las cuentas son creadas por administración |
| `GET /api/usuarios/{id}` | ✅ | ✅ propio | ✅ propio | Cada usuario debería consultar sus propios datos; ADMIN podría administrar usuarios |
| `DELETE /api/usuarios/{id}` | ✅ | ❌ | ❌ | Se propone como una operación administrativa |

---

## Regla confirmada para la creación de turnos

Para crear un turno:

- El usuario asignado como cuidador debe tener rol `CUIDADOR`.
- El usuario asignado como médico debe tener rol `MEDICO`.

Actualmente el servicio de turnos valida el rol del médico, pero la validación equivalente para el cuidador todavía no está implementada.

Esta issue únicamente documenta la regla. Su implementación corresponde a una tarea posterior de autorización y reglas de acceso.

---

## Decisiones pendientes de confirmación

Las siguientes decisiones no se presentan como requisitos definitivos:

1. Si un `ADMIN` puede registrar reportes diarios.
2. Si un `ADMIN` puede crear o dar de baja tratamientos.
3. Si un usuario con rol `MEDICO` puede crear turnos.
4. Cómo funciona `POST /api/usuarios`: autorregistro, creación exclusiva por `ADMIN` u otro mecanismo.

Estas decisiones deben ser confirmadas por el responsable del proyecto antes de implementar la autorización definitiva.

---

## Consideraciones futuras

Actualmente la API no implementa autenticación JWT, por lo que no puede verificar que el usuario que realiza una petición sea realmente el usuario identificado en una ruta.

Por ejemplo, un parámetro `{idMedico}` no demuestra por sí solo que quien realiza la petición sea ese médico.

La autenticación se implementará en B-08 y la aplicación efectiva de esta matriz de autorización se realizará posteriormente en B-12.

También deberán resolverse técnicamente las restricciones de acceso a recursos "relacionados", utilizando la identidad obtenida del usuario autenticado en lugar de confiar únicamente en identificadores enviados por el cliente.