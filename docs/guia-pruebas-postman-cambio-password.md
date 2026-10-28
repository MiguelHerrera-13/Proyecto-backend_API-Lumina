# Guía de Pruebas Manuales en Postman - Cambio de Contraseña (B-17)

Esta guía detalla el protocolo con **5 pruebas manuales en Postman** para validar la implementación del endpoint `PUT /api/auth/change-password` (**Issue B-17**).

---

## Preparación Inicial

1. **Iniciar el servidor backend:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. **Obtener Token de Acceso (Login previo):**
   * **POST** `http://localhost:8080/api/auth/login`
   * Body:
     ```json
     {
       "email": "medico@lumina.com",
       "password": "PasswordSegura123"
     }
     ```
   * Copia el `"token"` devuelto para usarlo como Bearer Token en los siguientes casos.

---

## Casos de Prueba Manuales

### Caso 1: Cambio Exitoso de Contraseña (Criterio de Aceptación)
* **Objetivo:** Actualizar la contraseña cumpliendo todas las políticas y validar el login posterior.
* **Método:** `PUT`
* **URL:** `http://localhost:8080/api/auth/change-password`
* **Headers / Authorization:**
  * **Authorization:** `Bearer <pega_aqui_el_jwt>`
  * `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "passwordActual": "PasswordSegura123",
  "nuevaPassword": "MiNuevaPassword2026!",
  "confirmarPassword": "MiNuevaPassword2026!"
}
```
* **Respuesta Esperada (`200 OK`):**
```json
{
  "mensaje": "Contraseña actualizada exitosamente"
}
```
> **Verificación adicional:**
> Intenta hacer login con la clave vieja `PasswordSegura123` $\rightarrow$ devuelve `401 Unauthorized`.  
> Haz login con `MiNuevaPassword2026!` $\rightarrow$ devuelve `200 OK`.

---

### Caso 2: Error por Contraseña Actual Incorrecta (Criterio 1)
* **Objetivo:** Validar que la API rechace la solicitud si la contraseña actual no coincide.
* **Método:** `PUT`
* **URL:** `http://localhost:8080/api/auth/change-password`
* **Headers / Authorization:**
  * **Authorization:** `Bearer <pega_aqui_el_jwt>`
  * `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "passwordActual": "ClaveEquivocada999!",
  "nuevaPassword": "OtraClaveSegura2026!",
  "confirmarPassword": "OtraClaveSegura2026!"
}
```
* **Respuesta Esperada (`400 Bad Request`):**
```json
{
  "error": "La contraseña actual no coincide"
}
```

---

### Caso 3: Error por Confirmación No Coincidente
* **Objetivo:** Validar que la nueva contraseña y su confirmación sean exactamente iguales.
* **Método:** `PUT`
* **URL:** `http://localhost:8080/api/auth/change-password`
* **Headers / Authorization:**
  * **Authorization:** `Bearer <pega_aqui_el_jwt>`
  * `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "passwordActual": "MiNuevaPassword2026!",
  "nuevaPassword": "TerceraClaveSegura123!",
  "confirmarPassword": "PasswordDiferente456!"
}
```
* **Respuesta Esperada (`400 Bad Request`):**
```json
{
  "error": "La confirmación de la contraseña no coincide"
}
```

---

### Caso 4: Error por Incumplimiento de Política de Complejidad
* **Objetivo:** Validar el rechazo si la contraseña tiene menos de 8 caracteres o carece de mayúsculas/números.
* **Método:** `PUT`
* **URL:** `http://localhost:8080/api/auth/change-password`
* **Headers / Authorization:**
  * **Authorization:** `Bearer <pega_aqui_el_jwt>`
  * `Content-Type: application/json`
* **Body (raw JSON):** *(Contraseña sin mayúsculas ni números y menor a 8 caracteres)*
```json
{
  "passwordActual": "MiNuevaPassword2026!",
  "nuevaPassword": "simple",
  "confirmarPassword": "simple"
}
```
* **Respuesta Esperada (`400 Bad Request`):**
```json
{
  "nuevaPassword": "La nueva contraseña debe tener al menos 8 caracteres, incluir al menos una letra mayúscula y un número"
}
```

---

### Caso 5: Error por Intento de Cambio sin Autenticación
* **Objetivo:** Validar que un usuario anónimo o sin token no pueda invocar el endpoint.
* **Método:** `PUT`
* **URL:** `http://localhost:8080/api/auth/change-password`
* **Headers:**
  * *(Sin cabecera `Authorization`)*
  * `Content-Type: application/json`
* **Body (raw JSON):**
```json
{
  "passwordActual": "Cualquiera123!",
  "nuevaPassword": "ValidaPassword123!",
  "confirmarPassword": "ValidaPassword123!"
}
```
* **Respuesta Esperada (`401 Unauthorized`):**
```json
{
  "error": "Acceso no autorizado: se requiere autenticación válida"
}
```
