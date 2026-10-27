# Flujo de Recuperación y Reseteo de Contraseña por Token - API REST (Lúmina)

Documentación técnica correspondiente a la **Issue B-16 (#27)**: Implementación del flujo seguro de recuperación y restablecimiento de contraseña mediante token temporal de un solo uso.

---

## 1. Historia de Usuario y Objetivos

> **Como** usuario que olvidó su contraseña,  
> **quiero** solicitar un enlace de restablecimiento con token temporal,  
> **para** recuperar el acceso a mi cuenta de manera segura.

### Criterios de Aceptación:
1. **Anti-enumeración de usuarios:** No revelar si un email existe o no en la base de datos al solicitar la recuperación.
2. **Token de un solo uso y expiración estricta:** El token sólo puede utilizarse una única vez y tiene un período de validez temporal corto (15 a 30 minutos).

---

## 2. Modelo de Datos: `PasswordResetTokenEntity`

La entidad `PasswordResetTokenEntity` (`password_reset_token`) gestiona el ciclo de vida del token de recuperación:

* **`idToken`** (`Long`): Identificador único autoincremental (`id_token`).
* **`token`** (`String`): Cadena aleatoria segura y única generada mediante UUID (`token`).
* **`usuario`** (`UsuarioEntity`): Referencia Many-to-One al usuario solicitante (`id_usuario`).
* **`fechaExpiracion`** (`LocalDateTime`): Fecha y hora límite de validez del token (por defecto: 15 minutos).
* **`usado`** (`Boolean`): Indicador de un solo uso. Inicialmente `false`, se establece en `true` una vez consumido o invalidado por una nueva solicitud.
* **`fechaCreacion`** (`LocalDateTime`): Fecha y hora en la que se generó el token.

### Métodos Auxiliares del Modelo:
* `isExpirado()`: Comprueba si `LocalDateTime.now().isAfter(fechaExpiracion)`.
* `isValido()`: Comprueba que `!usado && !isExpirado()`.

---

## 3. Decisiones de Seguridad

1. **Protección Anti-Enumeración (OWASP):**
   * En `POST /api/auth/forgot-password`, la API siempre responde con código HTTP `200 OK` y el mismo mensaje genérico neutral, sin importar si el email ingresado existe en la base de datos, si es inexistente o si la cuenta se encuentra inactiva.
   * Esto previene que atacantes deduzcan qué cuentas existen en el sistema.

2. **Políticas de Expiración e Invalidación:**
   * La duración por defecto es de 15 minutos (configurable a través de `auth.reset-token.expiration-minutes`).
   * Si el usuario genera una nueva solicitud antes de consumir la anterior, los tokens previos sin usar para ese usuario son invalidados (`usado = true`).

3. **Almacenamiento Seguro (BCrypt):**
   * Al resetear la contraseña, se cifra inmediatamente mediante `BCryptPasswordEncoder` antes de persistirla en `UsuarioEntity.passwordHash`.

4. **Acceso Público en Filtro de Seguridad:**
   * Los endpoints `/api/auth/forgot-password` y `/api/auth/reset-password` están configurados bajo `/api/auth/**` como `permitAll()` en `SecurityConfig`, permitiendo el acceso sin requerir un Bearer token.

---

## 4. Endpoints Disponibles

### 4.1. Solicitar Recuperación de Contraseña
* **Método:** `POST`
* **Ruta:** `/api/auth/forgot-password`
* **Headers:** `Content-Type: application/json`
* **Cuerpo de la Solicitud (Request Body):**
```json
{
  "email": "medico@lumina.com"
}
```
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "mensaje": "Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña."
}
```
* **Errores:**
  * `400 Bad Request`: Formato de correo electrónico inválido o campo vacío (`email` obligatorio).

---

### 4.2. Restablecer Contraseña
* **Método:** `POST`
* **Ruta:** `/api/auth/reset-password`
* **Headers:** `Content-Type: application/json`
* **Cuerpo de la Solicitud (Request Body):**
```json
{
  "token": "4f9d854e-399a-4c28-98bb-df0886a877aa",
  "nuevaPassword": "MiNuevaPasswordSegura2026!"
}
```
*(Nota: El payload también admite `"password"` o `"newPassword"` de forma compatible).*

* **Respuesta Exitosa (`200 OK`):**
```json
{
  "mensaje": "Contraseña restablecida exitosamente"
}
```
* **Errores Comunes:**
  * `400 Bad Request`:
    * `"El token de recuperación es inválido o no existe"` (Token desconocido).
    * `"El token de recuperación ya ha sido utilizado"` (Violación de token de un solo uso).
    * `"El token de recuperación ha expirado"` (Token fuera de su ventana de validez).
    * `"La cuenta de usuario no está activa"` (Usuario inactivo).
    * Campos vacíos o no proporcionados (`token` o `nuevaPassword` en blanco).
