# Módulo de Medicamentos - API REST (Lúmina)

Documentación del módulo de medicamentos asociados a tratamientos clínicos, implementado según la especificación académica de la Issue B-09.

---

## 1. Modelo de Datos y Atributos

La entidad `MedicamentoEntity` contempla de forma estricta los tres atributos funcionales acordados, vinculándose a un tratamiento mediante relación Muchos a Uno (`@ManyToOne`):

* **`idMedicamento`** (`Long`): Identificador único autoincremental de la entidad.
* **`idTratamiento`** (`Long`): Referencia al tratamiento clínico padre (`TratamientoEntity`).
* **`nombre`** (`String`): Nombre comercial o genérico del medicamento (obligatorio, no vacío).
* **`dosis`** (`String`): Dosis prescrita, ej.: `"500 mg"` (obligatorio, no vacío).
* **`frecuencia`** (`String`): Frecuencia de administración, ej.: `"Cada 8 horas"` (obligatorio, no vacío).
* **`activo`** (`Boolean`): Indicador de estado para baja lógica (por defecto `true`).

---

## 2. Decisiones de Diseño y Baja Lógica

1. **Baja lógica del medicamento:**
   * Utiliza las anotaciones de Hibernate `@SQLDelete(sql = "UPDATE medicamento SET activo = false WHERE id_medicamento=?")` y `@SQLRestriction("activo=true")`.
   * Al dar de baja un medicamento (`DELETE /api/medicamentos/{id}`), el registro se actualiza a `activo = false` en la base de datos sin destrucción física, preservando la trazabilidad médica y auditoría histórica. Las consultas subsiguientes retornan automáticamente `404 Not Found`.

2. **Comportamiento ante la baja del Tratamiento:**
   * No se configuran eliminaciones físicas en cascada (`CascadeType.REMOVE` / `orphanRemoval`).
   * Los registros de medicamentos asociados al tratamiento permanecen íntegros en base de datos.
   * Al consultar los medicamentos por tratamiento (`GET /api/medicamentos/tratamiento/{idTratamiento}`), el servicio comprueba primero la vigencia del tratamiento; como el tratamiento dado de baja queda excluido por su propio `@SQLRestriction("activo=true")`, la API responde coherentemente `404 Not Found ("El tratamiento con ID ... no existe")`.

---

## 3. Endpoints Disponibles

### 3.1. Crear Medicamento
* **Método:** `POST`
* **Ruta:** `/api/medicamentos`
* **Headers:** `Content-Type: application/json`
* **Cuerpo de la Solicitud (Request Body):**
```json
{
  "idTratamiento": 1,
  "nombre": "Amoxicilina",
  "dosis": "500 mg",
  "frecuencia": "Cada 8 horas"
}
```
* **Respuesta Exitosa (`201 Created`):**
```json
{
  "idMedicamento": 1,
  "idTratamiento": 1,
  "nombre": "Amoxicilina",
  "dosis": "500 mg",
  "frecuencia": "Cada 8 horas",
  "activo": true
}
```
* **Errores comunes:**
  * `400 Bad Request`: Datos obligatorios vacíos o nulos.
  * `404 Not Found`: Tratamiento con `idTratamiento` no existe o está inactivo.

---

### 3.2. Consultar Medicamento por ID
* **Método:** `GET`
* **Ruta:** `/api/medicamentos/{id}`
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "idMedicamento": 1,
  "idTratamiento": 1,
  "nombre": "Amoxicilina",
  "dosis": "500 mg",
  "frecuencia": "Cada 8 horas",
  "activo": true
}
```
* **Error:** `404 Not Found` si el medicamento no existe o fue dado de baja.

---

### 3.3. Listar Medicamentos por Tratamiento
* **Método:** `GET`
* **Ruta:** `/api/medicamentos/tratamiento/{idTratamiento}`
* **Respuesta Exitosa (`200 OK`):**
```json
[
  {
    "idMedicamento": 1,
    "idTratamiento": 1,
    "nombre": "Amoxicilina",
    "dosis": "500 mg",
    "frecuencia": "Cada 8 horas",
    "activo": true
  },
  {
    "idMedicamento": 2,
    "idTratamiento": 1,
    "nombre": "Ibuprofeno",
    "dosis": "400 mg",
    "frecuencia": "Cada 12 horas",
    "activo": true
  }
]
```
* **Error:** `404 Not Found` si el tratamiento indicado no existe o está inactivo.

---

### 3.4. Actualizar Medicamento
* **Método:** `PUT`
* **Ruta:** `/api/medicamentos/{id}`
* **Cuerpo de la Solicitud (Request Body):**
```json
{
  "idTratamiento": 1,
  "nombre": "Amoxicilina",
  "dosis": "875 mg",
  "frecuencia": "Cada 12 horas"
}
```
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "idMedicamento": 1,
  "idTratamiento": 1,
  "nombre": "Amoxicilina",
  "dosis": "875 mg",
  "frecuencia": "Cada 12 horas",
  "activo": true
}
```

---

### 3.5. Dar de Baja Medicamento (Baja Lógica)
* **Método:** `DELETE`
* **Ruta:** `/api/medicamentos/{id}`
* **Respuesta Exitosa:** `204 No Content`
* **Error:** `404 Not Found` si el medicamento no existe o ya estaba dado de baja.
