# Mecanismo de Revocación de Tokens y Endpoint de Logout - API REST (Lúmina)

Documentación técnica correspondiente a la **Issue B-15 (#26)**: Implementación de revocación de credenciales activas, invalidación en base de datos de Refresh Tokens y lista negra en memoria para tokens JWT.

---

## 1. Historia de Usuario y Objetivos

> **Como** usuario autenticado,  
> **quiero** cerrar sesión explícitamente en el sistema,  
> **para** invalidar mis credenciales activas y prevenir usos no autorizados en dispositivos compartidos.

### Criterios de Aceptación:
1. **POST `/api/auth/logout`** devuelve `200 OK` (o `204 No Content`).
2. Tras el logout, el **refresh token queda revocado** y no puede volver a ser utilizado para renovar sesión.
3. El **access token (JWT)** se agrega a una **lista negra (blacklist) en memoria** hasta su expiración natural, impidiendo su uso en endpoints protegidos.

---

## 2. Decisiones de Diseño y Seguridad

1. **Lista Negra en Memoria (`TokenBlacklistService`):**
   * Almacenamiento concurrente de alta velocidad (`ConcurrentHashMap<String, Instant>`).
   * Registra el JWT junto a su timestamp de expiración natural (`Claims.getExpiration`).
   * El filtro `JwtAuthenticationFilter` consulta la lista negra antes de autenticar al usuario. Si el token está en lista negra, se omite la autenticación y la cadena de Spring Security devuelve `401 Unauthorized`.
   * **Limpieza automática:** Cuando un token en la lista negra sobrepasa su fecha de expiración natural, se purga de la memoria para evitar memory leaks.

2. **Revocación de Refresh Token en Base de Datos:**
   * El token de refresco asociado se marca con `revocado = true` en la tabla `refresh_token`.
   * Cualquier solicitud posterior a `POST /api/auth/refresh` con ese token será rechazada con `401 Unauthorized` (`"El refresh token ya ha sido utilizado o revocado"`).
   * Si el usuario se desconecta con su Access Token, todos sus Refresh Tokens activos son revocados preventivamente.

---

## 3. Especificación de Endpoints

### 3.1. Cerrar Sesión (`POST /api/auth/logout`)

* **Método:** `POST`
* **Ruta:** `/api/auth/logout`
* **Headers:**
  * `Authorization: Bearer <access_token>` *(Opcional / Recomendado)*
  * `Content-Type: application/json`
* **Cuerpo de Solicitud (Opcional):**
```json
{
  "refreshToken": "7c2d9e11-4a1b-4f92-91f3-c5b8d27a1900"
}
```
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "mensaje": "Sesión cerrada exitosamente"
}
```

---

## 4. Efectos Posteriores al Logout

1. **Intento de renovación con el Refresh Token revocado (`POST /api/auth/refresh`):**
   * **Código HTTP:** `401 Unauthorized`
   * **Respuesta:**
     ```json
     {
       "error": "El refresh token ya ha sido utilizado o revocado"
     }
     ```

2. **Intento de acceso a endpoint protegido con el Access Token JWT revocado:**
   * **Código HTTP:** `401 Unauthorized`
   * **Respuesta:**
     ```json
     {
       "error": "Acceso no autorizado: se requiere autenticación válida"
     }
     ```
