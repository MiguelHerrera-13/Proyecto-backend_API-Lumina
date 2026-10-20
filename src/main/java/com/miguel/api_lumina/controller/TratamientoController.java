package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.TratamientoRequestDTO;
import com.miguel.api_lumina.dto.TratamientoResponseDTO;
import com.miguel.api_lumina.service.TratamientoService;
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
@RequestMapping("/api/tratamientos")
@Tag(name = "Tratamientos", description = "Gestión de tratamientos y prescripciones médicas asociadas a los pacientes. Requiere token JWT.")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @Operation(summary = "Registrar un tratamiento",
            description = "Crea un nuevo tratamiento o prescripción médica vinculado a un paciente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tratamiento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o paciente inexistente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido")
    })
    @PostMapping
    public ResponseEntity<TratamientoResponseDTO> registrarTratamiento(@Valid @RequestBody TratamientoRequestDTO requestDTO) {
        TratamientoResponseDTO nuevoTratamiento = tratamientoService.registrarTratamiento(requestDTO);
        return new ResponseEntity<>(nuevoTratamiento, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener tratamientos de un paciente",
            description = "Lista todos los tratamientos activos del paciente con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tratamientos obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido")
    })
    @GetMapping("/paciente/{id}")
    public ResponseEntity<List<TratamientoResponseDTO>> obtenerTratamientosPorPaciente(@PathVariable("id") Long id) {
        List<TratamientoResponseDTO> tratamientos = tratamientoService.obtenerTratamientosPorPaciente(id);
        return ResponseEntity.ok(tratamientos);
    }

    @Operation(summary = "Obtener un tratamiento por ID",
            description = "Busca y devuelve un tratamiento activo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tratamiento encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Tratamiento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TratamientoResponseDTO> obtenerTratamientoPorId(@PathVariable("id") Long id) {
        return tratamientoService.obtenerTratamientoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Dar de baja un tratamiento",
            description = "Realiza un borrado lógico del tratamiento. El registro permanece en la base de datos con activo=false.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tratamiento dado de baja correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Tratamiento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTratamiento(@PathVariable("id") Long id) {
        tratamientoService.darDeBajaTratamiento(id);
        return ResponseEntity.noContent().build();
    }
}
