# Matriz de permisos por rol y endpoint

## Objetivo

Este documento releva los endpoints implementados actualmente en el backend de Lúmina y en las últimas ramas de trabajo terminadas por el equipo, y propone una política de acceso para los roles `ADMIN`, `CUIDADOR` y `MEDICO`.

La matriz constituye una definición funcional de autorización. No implementa controles de acceso ni modifica el comportamiento de los endpoints.

Al momento de esta actualización, algunas funcionalidades relevadas todavía pueden encontrarse en ramas de trabajo o ramas `feature` pendientes de integración definitiva en `main`.

## Leyenda

- ✅ Permitido
- ❌ Denegado
- ⚠️ Pendiente de definición
- **propio**: el recurso corresponde al usuario autenticado.
- **relacionado**: el recurso se encuentra vinculado funcionalmente con el usuario autenticado.

Cuando un permiso indica que un recurso debe ser "propio" o estar "relacionado" con el usuario, el acceso debería limitarse utilizando la identidad obtenida mediante autenticación y no únicamente un identificador enviado por el cliente.

Salvo las reglas indicadas explícitamente como confirmadas, los permisos de esta matriz constituyen una propuesta inicial para revisión del equipo. Los símbolos ✅ y ❌ representan la política propuesta y no deben interpretarse como requisitos definitivamente aprobados hasta su validación.

---

## Endpoints relevados

### Alertas

- `GET /api/alertas/medico/{id}`  
  Consulta las alertas asociadas a un médico.

  La versión implementada en B-10 permite filtrar por estado de lectura mediante el parámetro opcional `leida=true|false`. Si el parámetro se omite, se conserva el comportamiento de consultar alertas no leídas.

- `PATCH /api/alertas/medico/{idMedico}/{idAlerta}/leida`  
  Marca como leída una alerta perteneciente al médico indicado.

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

  En la implementación de B-08 este endpoint permanece accesible sin JWT. La política sobre qué roles puede solicitar una cuenta creada mediante este mecanismo todavía requiere definición.

- `GET /api/usuarios/{id}`  
  Consulta los datos de un usuario.

- `DELETE /api/usuarios/{id}`  
  Realiza la baja lógica de un usuario.

### Autenticación

- `POST /api/auth/login`  
  Valida correo y contraseña y, cuando las credenciales son correctas, devuelve un token JWT con información sobre la identidad y el rol del usuario.

  Este endpoint es público porque se utiliza para obtener la autenticación inicial.

### Medicamentos

- `POST /api/medicamentos`  
  Crea un medicamento asociado a un tratamiento.

- `GET /api/medicamentos/{id}`  
  Consulta un medicamento por su identificador.

- `GET /api/medicamentos/tratamiento/{idTratamiento}`  
  Consulta los medicamentos asociados a un tratamiento.

- `PUT /api/medicamentos/{id}`  
  Actualiza los datos de un medicamento.

- `DELETE /api/medicamentos/{id}`  
  Realiza la baja lógica de un medicamento.

---

## Matriz propuesta de permisos

| Endpoint | ADMIN | CUIDADOR | MEDICO | Justificación |
|---|---|---|---|---|
| `GET /api/alertas/medico/{id}?leida={estado}` | ✅ | ❌ | ✅ propio | Las alertas están destinadas al médico responsable. B-10 permite consultar leídas y no leídas |
| `PATCH /api/alertas/medico/{idMedico}/{idAlerta}/leida` | ⚠️ | ❌ | ✅ propio | B-10 permite al médico marcar como leídas las alertas que le pertenecen; falta confirmar si ADMIN también puede hacerlo |
| `POST /api/pacientes` | ✅ | ✅ | ❌ | Se propone que administración y cuidadores puedan registrar pacientes |
| `GET /api/pacientes/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Cuidador y médico requieren consultar pacientes vinculados con su actividad |
| `GET /api/pacientes/{id}/historial` | ✅ | ✅ relacionado | ✅ relacionado | El historial puede ser necesario para la atención y seguimiento del paciente |
| `DELETE /api/pacientes/{id}` | ✅ | ❌ | ❌ | Se propone como una operación administrativa |
| `POST /api/reportes-diarios` | ⚠️ | ✅ relacionado | ❌ | El reporte diario está asociado al seguimiento realizado durante un turno; falta confirmar si ADMIN también puede registrarlo |
| `GET /api/reportes-diarios/turno/{idTurno}` | ✅ | ✅ relacionado | ✅ relacionado | Los participantes relacionados con el turno pueden requerir consultar sus reportes |
| `GET /api/reportes-diarios/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Se propone aplicar la misma restricción de relación que en la consulta por turno |
| `POST /api/tratamientos` | ⚠️ | ❌ | ✅ | La creación de tratamientos corresponde funcionalmente al médico; falta confirmar facultades de ADMIN |
| `GET /api/tratamientos/paciente/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Médico y cuidador pueden necesitar consultar las indicaciones correspondientes al paciente |
| `GET /api/tratamientos/{id}` | ✅ | ✅ relacionado | ✅ relacionado | El acceso debería limitarse a tratamientos vinculados con pacientes relacionados |
| `DELETE /api/tratamientos/{id}` | ⚠️ | ❌ | ✅ propio | Se propone que el médico pueda dar de baja tratamientos que le correspondan; falta confirmar facultades de ADMIN |
| `POST /api/turnos` | ✅ | ✅ | ⚠️ | Falta confirmar si un médico puede crear turnos |
| `GET /api/turnos/{id}` | ✅ | ✅ relacionado | ✅ relacionado | Cuidador y médico deberían consultar únicamente turnos relacionados con ellos |
| `GET /api/turnos/cuidador/{idCuidador}` | ✅ | ✅ propio | ❌ | El endpoint representa específicamente la agenda de un cuidador |
| `POST /api/usuarios` | ⚠️ público | ⚠️ público | ⚠️ público | B-08 permite actualmente acceder al registro sin JWT, pero todavía debe definirse qué roles pueden solicitarse mediante autorregistro |
| `GET /api/usuarios/{id}` | ✅ | ✅ propio | ✅ propio | Cada usuario debería consultar sus propios datos; ADMIN podría administrar usuarios |
| `DELETE /api/usuarios/{id}` | ✅ | ❌ | ❌ | Se propone como una operación administrativa |
| `POST /api/auth/login` | ✅ público | ✅ público | ✅ público | El inicio de sesión debe ser accesible antes de disponer de un JWT; la autorización por rol no aplica todavía en esta operación |
| `POST /api/medicamentos` | ⚠️ | ❌ | ✅ relacionado | Los medicamentos se crean asociados a tratamientos y la funcionalidad fue planteada para el médico |
| `GET /api/medicamentos/{id}` | ✅ | ✅ relacionado | ✅ relacionado | La medicación puede ser necesaria para el seguimiento del paciente, restringiendo el acceso a recursos relacionados |
| `GET /api/medicamentos/tratamiento/{idTratamiento}` | ✅ | ✅ relacionado | ✅ relacionado | Permite consultar la medicación correspondiente a un tratamiento relacionado |
| `PUT /api/medicamentos/{id}` | ⚠️ | ❌ | ✅ relacionado | La modificación de una prescripción debería corresponder al médico; falta confirmar facultades de ADMIN |
| `DELETE /api/medicamentos/{id}` | ⚠️ | ❌ | ✅ relacionado | La baja de una medicación debería corresponder al médico; falta confirmar facultades de ADMIN |

---

## Regla confirmada para la creación de turnos

Para crear un turno:

- El usuario asignado como cuidador debe tener rol `CUIDADOR`.
- El usuario asignado como médico debe tener rol `MEDICO`.

La regla establece que no debe ser posible asociar como cuidador a un usuario perteneciente a otro rol ni asociar como médico a un usuario que no tenga rol `MEDICO`.

En la implementación relevada originalmente, el servicio de turnos valida el rol del médico, mientras que la validación equivalente para el cuidador todavía debe quedar garantizada.

Esta issue documenta la regla funcional. La aplicación de los controles de autorización corresponde a B-12.

---

## Decisiones pendientes de confirmación

Las siguientes decisiones no se presentan como requisitos definitivos:

1. Si un `ADMIN` puede registrar reportes diarios.
2. Si un `ADMIN` puede crear o dar de baja tratamientos.
3. Si un usuario con rol `MEDICO` puede crear turnos.
4. Cómo debe funcionar definitivamente `POST /api/usuarios`: autorregistro, creación exclusiva por `ADMIN` u otro mecanismo.
5. Qué roles puede solicitar una persona mediante `POST /api/usuarios` si se mantiene el registro público.
6. Si un `ADMIN` puede crear, modificar o dar de baja medicamentos.
7. Si un `ADMIN` puede marcar como leídas alertas pertenecientes a médicos.

Estas decisiones deben ser confirmadas por el responsable del proyecto antes de implementar la autorización definitiva en B-12.

Hasta que exista una decisión funcional, no deben inventarse reglas de autorización para resolver estos casos directamente en código.

---

## Autenticación y relación con B-08

La autenticación JWT fue implementada como parte de B-08 en la rama de trabajo correspondiente, aunque al momento de actualizar este documento todavía puede no encontrarse integrada definitivamente en `main`.

B-08 incorpora:

- inicio de sesión mediante `POST /api/auth/login`;
- generación de JWT;
- identificación del usuario mediante su correo;
- inclusión del identificador y rol del usuario en el token;
- almacenamiento de contraseñas utilizando BCrypt;
- protección general de endpoints que requieren autenticación;
- respuesta `401 Unauthorized` ante solicitudes sin autenticación válida.

La autenticación permite determinar quién realiza una solicitud.

Esta matriz define una capa diferente: la **autorización**, es decir, determinar qué puede hacer ese usuario una vez autenticado.

La aplicación detallada de estos permisos corresponde a B-12.

---

## Restricciones sobre recursos propios o relacionados

Un parámetro enviado en una URL no demuestra por sí solo la identidad del usuario.

Por ejemplo:

`GET /api/alertas/medico/{idMedico}`

no debería considerar autorizado al solicitante únicamente porque conozca o envíe el identificador de un médico.

De forma similar, un cuidador no debería obtener acceso a cualquier paciente, turno, reporte o tratamiento simplemente modificando un identificador en la solicitud.

B-12 deberá utilizar la identidad obtenida del usuario autenticado para comprobar las relaciones correspondientes y aplicar las reglas definidas en esta matriz.

El servidor tampoco debe confiar en un rol enviado libremente por el cliente para decidir permisos. El rol utilizado para autorización debe provenir de la identidad autenticada y de la información confiable mantenida por el backend.

---

## Estado del relevamiento

La matriz contempla 25 operaciones REST considerando las funcionalidades implementadas hasta la actualización de este documento:

- Alertas y estado de lectura.
- Pacientes e historial clínico.
- Reportes diarios.
- Tratamientos.
- Turnos.
- Usuarios.
- Autenticación JWT.
- Medicamentos asociados a tratamientos.

B-11 corresponde a documentación del modelo funcional y no incorpora endpoints adicionales.

B-13 incorpora documentación mediante OpenAPI/Swagger y utiliza el mecanismo JWT definido en B-08, pero no agrega operaciones funcionales nuevas que requieran una regla adicional en esta matriz.

Si antes de implementar B-12 aparecen nuevos endpoints, deberán relevarse y contar con una decisión de permisos antes de aplicarles controles de autorización.