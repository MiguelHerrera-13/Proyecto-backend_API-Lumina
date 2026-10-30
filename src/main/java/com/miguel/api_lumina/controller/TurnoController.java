package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@Tag(name = "Turnos", description = "Gestión de turnos de atención asignados a cuidadores y pacientes. Requiere token JWT.")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @Operation(summary = "Crear un turno",
            description = "Registra un nuevo turno asignando un cuidador a un paciente en un horario determinado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: cuidador o paciente inexistente, o superposición de horarios"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido")
    })
    @PostMapping
    public ResponseEntity<?> crearTurno(@RequestBody TurnoRequestDTO dto) {
        try {
            TurnoResponseDTO response = turnoService.crearTurno(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Obtener un turno por ID",
            description = "Busca y devuelve un turno activo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTurnoPorId(@PathVariable Long id) {
        try {
            TurnoResponseDTO response = turnoService.obtenerTurnoPorId(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Obtener turnos de un cuidador",
            description = "Lista todos los turnos activos asignados al cuidador con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de turnos obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: se requiere token JWT válido")
    })
    @GetMapping("/cuidador/{idCuidador}")
    public ResponseEntity<List<TurnoResponseDTO>> obtenerTurnosPorCuidador(@PathVariable Long idCuidador) {
        List<TurnoResponseDTO> turnos = turnoService.obtenerTurnosPorCuidador(idCuidador);
        return ResponseEntity.ok(turnos);
    }
}
