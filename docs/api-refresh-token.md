# Mecanismo de Refresco de Tokens JWT (Refresh Token) - API REST (Lúmina)

Documentación técnica correspondiente a la **Issue B-14 (#25)**: Implementación de la renovación de tokens JWT mediante Refresh Token con rotación automática y mitigación de ataques de repetición.

---

## 1. Historia de Usuario y Objetivos

> **Como** usuario autenticado en la plataforma,  
> **quiero** renovar mi token de acceso mediante un refresh token sin tener que volver a ingresar mis credenciales,  
> **para** mantener mi sesión activa de forma segura.

### Criterios de Aceptación:
1. **POST `/api/auth/refresh`** devuelve un nuevo access token JWT y un nuevo refresh token.
2. Refresh token expirado o inexistente devuelve **`401 Unauthorized`**.
3. Pruebas unitarias y de integración para **rotación y expiración**.

---

## 2. Modelo de Datos: `RefreshTokenEntity`

La entidad `RefreshTokenEntity` (`refresh_token`) administra el ciclo de vida del refresh token:

* **`idRefreshToken`** (`Long`): Identificador único autoincremental (`id_refresh_token`).
* **`token`** (`String`): Token único de alta entropía generado como UUID v4 (`token`, longitud 128).
* **`usuario`** (`UsuarioEntity`): Referencia Many-to-One al usuario titular (`id_usuario`).
* **`fechaExpiracion`** (`LocalDateTime`): Fecha y hora límite de validez (por defecto: 7 días, configurable vía `jwt.refresh-expiration-days`).
* **`revocado`** (`Boolean`): Indicador de revocación (`revocado`). Si el token es utilizado, se marca como `true` de inmediato para evitar ataques de repetición (Replay Attacks).
* **`fechaCreacion`** (`LocalDateTime`): Timestamp de generación del token.

### Métodos Auxiliares del Modelo:
* `isExpirado()`: Comprueba si `LocalDateTime.now().isAfter(fechaExpiracion)`.
* `isRevocado()`: Retorna `Boolean.TRUE.equals(revocado)`.
* `isValido()`: Comprueba `!isRevocado() && !isExpirado()`.

---

## 3. Políticas de Seguridad y Rotación de Tokens (RFC 6749 / OWASP)

1. **Rotación de Refresh Tokens (Token Rotation):**
   * Cada vez que un refresh token se consume exitosamente mediante `POST /api/auth/refresh`, dicho token se invalida irrevocablemente (`revocado = true`).
   * En la misma transacción se genera un **nuevo Refresh Token** y se emite un **nuevo JWT Access Token**.
   * Esto previene que si un refresh token es interceptado, pueda ser reutilizado indefinidamente.

2. **Detección y Mitigación de Ataques de Repetición (Replay Attacks):**
   * Si un cliente intenta enviar un refresh token que ya fue marcado como `revocado`, la API rechaza la solicitud de inmediato con código **`401 Unauthorized`** (`"El refresh token ya ha sido utilizado o revocado"`).

3. **Expiración Estricta:**
   * Si la fecha actual supera `fechaExpiracion`, el token se revoca y se rechaza con **`401 Unauthorized`** (`"El refresh token ha expirado"`).

4. **Acceso Público en Filtro de Seguridad:**
   * El endpoint `/api/auth/refresh` forma parte de `/api/auth/**`, estando configurado como `permitAll()` en Spring Security para permitir la renovación sin requerir un access token válido previo.

---

## 4. Especificación de Endpoints

### 4.1. Iniciar Sesión (Emisión Inicial)
* **Método:** `POST`
* **Ruta:** `/api/auth/login`
* **Headers:** `Content-Type: application/json`
* **Cuerpo de Solicitud:**
```json
{
  "email": "medico@lumina.com",
  "password": "PasswordSegura123"
}
```
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "email": "medico@lumina.com",
  "rol": "MEDICO",
  "refreshToken": "7d9c612f-8703-4e8c-8f24-2cbb8c983a12"
}
```

---

### 4.2. Renovar Token de Acceso (`POST /api/auth/refresh`)
* **Método:** `POST`
* **Ruta:** `/api/auth/refresh`
* **Headers:** `Content-Type: application/json`
* **Cuerpo de Solicitud:**
```json
{
  "refreshToken": "7d9c612f-8703-4e8c-8f24-2cbb8c983a12"
}
```
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.nuevo_access_token...",
  "tipo": "Bearer",
  "email": "medico@lumina.com",
  "rol": "MEDICO",
  "refreshToken": "e3a479b1-591e-450b-80ac-fc2174d89610"
}
```
*(Nótese que se devuelve un nuevo JWT y un nuevo Refresh Token rotado).*

* **Errores Comunes:**
  * `401 Unauthorized`:
    * `"Refresh token inexistente o inválido"` (El token no existe en la base de datos).
    * `"El refresh token ha expirado"` (El token sobrepasó su período de validez).
    * `"El refresh token ya ha sido utilizado o revocado"` (Intento de repetición con token anterior).
    * `"La cuenta de usuario asociada no se encuentra activa"` (Usuario inactivo).
  * `400 Bad Request`:
    * `"El refresh token es obligatorio"` (Campo ausente o en blanco).
