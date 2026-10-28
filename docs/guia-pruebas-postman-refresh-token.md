# Guía de Pruebas Manuales en Postman - Módulo de Refresh Token (B-14)

Esta guía detalla el **paso a paso manual** para validar en **Postman** el funcionamiento del mecanismo de refresco de tokens JWT, rotación automática, expiración y mitigación de ataques de repetición (**Issue B-14 / #25**).

---

## Preparación Inicial

1. **Iniciar el servidor backend:**
   Ejecuta en tu terminal PowerShell desde la raíz del proyecto:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *El servidor quedará disponible en `http://localhost:8080`.*

2. **Configuración general en Postman:**
   * Todas las peticiones deben incluir la cabecera:
     * `Content-Type: application/json`

---

## Flujo Paso a Paso de Pruebas Manuales

### Paso 1: Crear o verificar Usuario de Prueba
Si no cuentas con un usuario registrado, créalo enviando:
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/usuarios`
* **Body (raw JSON):**
```json
{
  "nombre": "Dra. Martinez",
  "email": "dra.martinez@lumina.com",
  "password": "ClaveSegura123!",
  "rol": "MEDICO"
}
```
* **Respuesta esperada:** `200 OK` (o `201 Created`).

---

### Paso 2: Iniciar Sesión y Obtener Refresh Token Inicial
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/login`
* **Body (raw JSON):**
```json
{
  "email": "dra.martinez@lumina.com",
  "password": "ClaveSegura123!"
}
```
* **Respuesta esperada (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkcmEubWFydGluZXpAbHVtaW5hLmNvbSIsImlkVXN1YXJpbyI6MSwicm9sIjoiTUVESUNPIn0...",
  "tipo": "Bearer",
  "email": "dra.martinez@lumina.com",
  "rol": "MEDICO",
  "refreshToken": "7c2d9e11-4a1b-4f92-91f3-c5b8d27a1900"
}
```
> **Verificación:** Copia el valor de `"refreshToken"` (llamémoslo **Token 1**) y el `"token"` JWT.

---

### Paso 3: Renovar Access Token con Refresh Token Válido (Criterio 1: Nuevo JWT y Nuevo Refresh Token)
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/refresh`
* **Body (raw JSON):** *(Pega el Token 1 copiado en el Paso 2)*
```json
{
  "refreshToken": "7c2d9e11-4a1b-4f92-91f3-c5b8d27a1900"
}
```
* **Respuesta esperada (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.NUEVO_JWT_ACCESS_TOKEN...",
  "tipo": "Bearer",
  "email": "dra.martinez@lumina.com",
  "rol": "MEDICO",
  "refreshToken": "8f3b4c22-991c-4311-a54b-d7e9f1a23456"
}
```
> **Verificaciones:**
> 1. Se emitió un **nuevo JWT** en el campo `token`.
> 2. Se emitió un **nuevo refresh token** rotado (llamémoslo **Token 2**).
> 3. El **Token 2** es diferente al **Token 1** (Rotación confirmada).

---

### Paso 4: Probar Mitigación de Ataques de Repetición (Replay Attack)
Intenta reenviar **exactamente la misma petición del Paso 3** utilizando nuevamente el **Token 1** (que ya fue consumido):
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/refresh`
* **Body (raw JSON):**
```json
{
  "refreshToken": "7c2d9e11-4a1b-4f92-91f3-c5b8d27a1900"
}
```
* **Respuesta esperada (`401 Unauthorized`):**
```json
{
  "error": "El refresh token ya ha sido utilizado o revocado"
}
```
> **Criterio cumplido:** El token anterior quedó revocado en la base de datos tras su primer uso, impidiendo su reutilización fraudulenta.

---

### Paso 5: Renovar Sesión con el Nuevo Refresh Token Rotado (Token 2)
Envía ahora el **Token 2** obtenido en el Paso 3:
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/refresh`
* **Body (raw JSON):**
```json
{
  "refreshToken": "8f3b4c22-991c-4311-a54b-d7e9f1a23456"
}
```
* **Respuesta esperada (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.OTRO_NUEVO_JWT...",
  "tipo": "Bearer",
  "email": "dra.martinez@lumina.com",
  "rol": "MEDICO",
  "refreshToken": "3a11bb99-7722-4a0b-bc11-e123456789ab"
}
```
> **Verificación:** El ciclo de renovación continua funciona sin interrupciones para el usuario legítimo, rotando a **Token 3**.

---

### Paso 6: Probar Refresh Token Inexistente (Criterio 2: 401 Unauthorized)
Envía un token ficticio o aleatorio que no esté registrado en la base de datos:
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/refresh`
* **Body (raw JSON):**
```json
{
  "refreshToken": "token-falso-que-no-existe-en-bd-99999"
}
```
* **Respuesta esperada (`401 Unauthorized`):**
```json
{
  "error": "Refresh token inexistente o inválido"
}
```

---

### Paso 7: Probar Validación de Campo Vacío
Envía la solicitud sin token o con cadena vacía:
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/refresh`
* **Body (raw JSON):**
```json
{
  "refreshToken": ""
}
```
* **Respuesta esperada (`400 Bad Request`):**
```json
{
  "refreshToken": "El refresh token es obligatorio"
}
```

---

### Paso 8: Comprobar Acceso a Endpoint Protegido con el JWT Renovado
Para verificar que el nuevo JWT emitido por `/api/auth/refresh` tiene permisos y autenticación vigentes:
* **Método:** `GET`
* **URL:** `http://localhost:8080/api/usuarios/1`
* **Headers:**
  * `Authorization: Bearer <PEGA_AQUI_EL_NUEVO_JWT_DEL_PASO_5>`
* **Respuesta esperada (`200 OK`):**
```json
{
  "idUsuario": 1,
  "nombre": "Dra. Martinez",
  "email": "dra.martinez@lumina.com",
  "rol": "MEDICO",
  "activo": true
}
```
