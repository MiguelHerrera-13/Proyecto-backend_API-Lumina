package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.MedicamentoRequestDTO;
import com.miguel.api_lumina.dto.MedicamentoResponseDTO;
import com.miguel.api_lumina.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos", description = "Gestión de medicamentos vinculados a tratamientos médicos. Requiere token JWT.")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    public MedicamentoController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    @Operation(summary = "Crear un medicamento",
            description = "Registra un nuevo medicamento asociado a un tratamiento médico existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Medicamento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Tratamiento no encontrado")
    })
    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> crearMedicamento(@Valid @RequestBody MedicamentoRequestDTO requestDTO) {
        MedicamentoResponseDTO creado = medicamentoService.crearMedicamento(requestDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener medicamento por ID",
            description = "Busca y devuelve un medicamento activo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> obtenerMedicamentoPorId(@PathVariable("id") Long id) {
        return medicamentoService.obtenerMedicamentoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener medicamentos por tratamiento",
            description = "Lista todos los medicamentos activos asociados a un tratamiento médico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de medicamentos obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Tratamiento no encontrado")
    })
    @GetMapping("/tratamiento/{idTratamiento}")
    public ResponseEntity<List<MedicamentoResponseDTO>> obtenerMedicamentosPorTratamiento(@PathVariable("idTratamiento") Long idTratamiento) {
        List<MedicamentoResponseDTO> medicamentos = medicamentoService.obtenerMedicamentosPorTratamiento(idTratamiento);
        return ResponseEntity.ok(medicamentos);
    }

    @Operation(summary = "Actualizar medicamento",
            description = "Modifica los datos de un medicamento existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Medicamento o tratamiento no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> actualizarMedicamento(@PathVariable("id") Long id,
                                                                        @Valid @RequestBody MedicamentoRequestDTO requestDTO) {
        MedicamentoResponseDTO actualizado = medicamentoService.actualizarMedicamento(id, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Dar de baja un medicamento",
            description = "Realiza la baja lógica de un medicamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Medicamento dado de baja correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBajaMedicamento(@PathVariable("id") Long id) {
        medicamentoService.darDeBajaMedicamento(id);
        return ResponseEntity.noContent().build();
    }
}
