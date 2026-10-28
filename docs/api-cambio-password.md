# Cambio de Contraseña de Usuario Autenticado - API REST (Lúmina)

Documentación técnica correspondiente a la **Issue B-17**: Implementación del endpoint seguro para actualización periódica de contraseña por parte de usuarios autenticados.

---

## 1. Historia de Usuario y Objetivos

> **Como** usuario logueado,  
> **quiero** cambiar mi contraseña actual,  
> **para** mantener la seguridad de mi cuenta periódicamente.

### Criterios de Aceptación:
1. **Validación de contraseña actual:** Si la contraseña actual no coincide, responde **`400 Bad Request`**.
2. **Políticas de complejidad:** La nueva contraseña debe tener un mínimo de 8 caracteres, al menos un número y al menos una letra mayúscula (expresión regular `^(?=.*[A-Z])(?=.*\d).{8,}$`).
3. **Persistencia segura:** La contraseña actualizada se almacena con hash seguro **BCrypt** (`$2a$`).
4. **Coincidencia de confirmación:** La nueva contraseña y su confirmación deben coincidir exactamente, caso contrario responde **`400 Bad Request`**.

---

## 2. Especificación del Endpoint

### 2.1. Actualizar Contraseña (`PUT /api/auth/change-password`)

* **Método:** `PUT`
* **Ruta:** `/api/auth/change-password`
* **Headers:**
  * `Authorization: Bearer <access_token_jwt>` *(Obligatorio)*
  * `Content-Type: application/json`
* **Cuerpo de Solicitud (Request Body):**
```json
{
  "passwordActual": "ClaveActual123!",
  "nuevaPassword": "NuevaClaveSegura2026!",
  "confirmarPassword": "NuevaClaveSegura2026!"
}
```

* **Respuesta Exitosa (`200 OK`):**
```json
{
  "mensaje": "Contraseña actualizada exitosamente"
}
```

---

## 3. Manejo de Errores

| Código HTTP | Causa | Respuesta |
| :--- | :--- | :--- |
| **`400 Bad Request`** | La contraseña actual no coincide | `{"error": "La contraseña actual no coincide"}` |
| **`400 Bad Request`** | La confirmación no coincide | `{"error": "La confirmación de la contraseña no coincide"}` |
| **`400 Bad Request`** | No cumple requisitos de complejidad | `{"error": "La nueva contraseña debe tener al menos 8 caracteres, incluir al menos una letra mayúscula y un número"}` |
| **`400 Bad Request`** | Campos vacíos o nulos | Mensaje de validación de Bean Validation (`@NotBlank`) |
| **`401 Unauthorized`**| Solicitud sin token o token inválido | `{"error": "Acceso no autorizado: se requiere autenticación válida"}` |
