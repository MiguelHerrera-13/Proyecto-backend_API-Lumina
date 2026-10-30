package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.PacienteRequestDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.miguel.api_lumina.dto.HistorialDTO;

import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Gestión de pacientes registrados en el sistema Lumina. Requiere token JWT.")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @Operation(summary = "Obtener historial clínico de un paciente",
            description = "Devuelve los turnos y reportes diarios asociados al paciente indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @GetMapping("/{id}/historial")
    public ResponseEntity<HistorialDTO> obtenerHistorial(@PathVariable Long id) {
        HistorialDTO historial = pacienteService.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }

    @Operation(summary = "Registrar un nuevo paciente",
            description = "Crea un paciente con los datos clínicos básicos. Devuelve el paciente creado con su ID generado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido")
    })
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteRequestDTO requestDTO) {
        // Mapeo manual RequestDTO -> Entity
        PacienteEntity entity = new PacienteEntity();
        entity.setNombreCompleto(requestDTO.getNombreCompleto());
        entity.setFechaNacimiento(requestDTO.getFechaNacimiento());
        entity.setDiagnosticoFase(requestDTO.getDiagnosticoFase());

        PacienteEntity pacienteGuardado = pacienteService.registrarPaciente(entity);

        // Mapeo manual Entity -> ResponseDTO
        PacienteResponseDTO responseDTO = new PacienteResponseDTO(
                pacienteGuardado.getIdPaciente(),
                pacienteGuardado.getNombreCompleto(),
                pacienteGuardado.getFechaNacimiento(),
                pacienteGuardado.getDiagnosticoFase(),
                pacienteGuardado.getActivo()
        );

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener un paciente por ID",
            description = "Busca un paciente activo por su ID. Retorna 404 si no existe o fue dado de baja.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPaciente(@PathVariable Long id) {
        Optional<PacienteEntity> pacienteOpt = pacienteService.obtenerPacientePorId(id);

        if (pacienteOpt.isPresent()) {
            PacienteEntity entity = pacienteOpt.get();
            // Mapeo manual Entity -> ResponseDTO
            PacienteResponseDTO responseDTO = new PacienteResponseDTO(
                    entity.getIdPaciente(),
                    entity.getNombreCompleto(),
                    entity.getFechaNacimiento(),
                    entity.getDiagnosticoFase(),
                    entity.getActivo()
            );
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Dar de baja un paciente",
            description = "Realiza un borrado lógico del paciente. El registro permanece en la base de datos con activo=false.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paciente dado de baja correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.darDeBajaPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
