# Guía de Pruebas Manuales en Postman - Logout y Revocación de Tokens (B-15)

Esta guía detalla el **paso a paso manual** para validar en **Postman** el funcionamiento del endpoint de logout, la invalidación en base de datos del Refresh Token y la lista negra en memoria del JWT (**Issue B-15 / #26**).

---

## Preparación Inicial

1. **Iniciar el servidor backend:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. **URL Base:** `http://localhost:8080`

---

## Flujo Paso a Paso de Pruebas Manuales

### Paso 1: Iniciar Sesión (Login)
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/login`
* **Headers:** `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "email": "medico@lumina.com",
  "password": "PasswordSegura123"
}
```
* **Respuesta esperada (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "email": "medico@lumina.com",
  "rol": "MEDICO",
  "refreshToken": "4e1a6c77-990a-48d1-81fe-d3f382a829f0"
}
```
> **Acción:** Copia el `token` (JWT) y el `refreshToken`.

---

### Paso 2: Verificar Acceso a Endpoint Protegido con el JWT
* **Método:** `GET`
* **URL:** `http://localhost:8080/api/usuarios/1`
* **Headers:**
  * `Authorization: Bearer <PEGA_AQUI_EL_JWT_DEL_PASO_1>`
* **Respuesta esperada:** `200 OK`.

---

### Paso 3: Ejecutar Logout (`POST /api/auth/logout`)
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/logout`
* **Headers:**
  * `Authorization: Bearer <PEGA_AQUI_EL_JWT_DEL_PASO_1>`
  * `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "refreshToken": "4e1a6c77-990a-48d1-81fe-d3f382a829f0"
}
```
* **Respuesta esperada (`200 OK`):**
```json
{
  "mensaje": "Sesión cerrada exitosamente"
}
```

---

### Paso 4: Validar Rechazo al Intentar Reutilizar el Refresh Token
* **Método:** `POST`
* **URL:** `http://localhost:8080/api/auth/refresh`
* **Headers:** `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "refreshToken": "4e1a6c77-990a-48d1-81fe-d3f382a829f0"
}
```
* **Respuesta esperada (`401 Unauthorized`):**
```json
{
  "error": "El refresh token ya ha sido utilizado o revocado"
}
```

---

### Paso 5: Validar Rechazo del Access Token en Blacklist
* **Método:** `GET`
* **URL:** `http://localhost:8080/api/usuarios/1`
* **Headers:**
  * `Authorization: Bearer <PEGA_AQUI_EL_JWT_INVALIDADO_EN_EL_PASO_3>`
* **Respuesta esperada (`401 Unauthorized`):**
```json
{
  "error": "Acceso no autorizado: se requiere autenticación válida"
}
```
> **Resultado exitoso:** El access token fue retenido en la lista negra en memoria y ya no permite acceder al sistema tras el logout.
