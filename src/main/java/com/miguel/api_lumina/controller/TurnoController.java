package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteRequestDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteResponseDTO;
import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping
    public ResponseEntity<?> crearTurno(@Valid @RequestBody TurnoRequestDTO dto) {
        try {
            TurnoResponseDTO response = turnoService.crearTurno(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Page<TurnoResponseDTO>> buscarTurnos(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long cuidadorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(page = 0, size = 10, sort = "fechaHoraInicio", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        LocalDateTime fDesde = (fechaDesde != null) ? fechaDesde : desde;
        LocalDateTime fHasta = (fechaHasta != null) ? fechaHasta : hasta;

        Page<TurnoResponseDTO> turnos = turnoService.buscarTurnos(
                pacienteId, profesionalId, medicoId, cuidadorId, estado, fDesde, fHasta, pageable
        );
        return ResponseEntity.ok(turnos);
    }

    @PostMapping("/recurrentes")
    public ResponseEntity<?> generarTurnosRecurrentes(@Valid @RequestBody TurnoRecurrenteRequestDTO dto) {
        try {
            TurnoRecurrenteResponseDTO response = turnoService.generarTurnosRecurrentes(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/recurrencia/{idRecurrencia}")
    public ResponseEntity<List<TurnoResponseDTO>> obtenerTurnosPorRecurrencia(@PathVariable String idRecurrencia) {
        List<TurnoResponseDTO> turnos = turnoService.obtenerTurnosPorRecurrencia(idRecurrencia);
        return ResponseEntity.ok(turnos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTurnoPorId(@PathVariable Long id) {
        try {
            TurnoResponseDTO response = turnoService.obtenerTurnoPorId(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cuidador/{idCuidador}")
    public ResponseEntity<List<TurnoResponseDTO>> obtenerTurnosPorCuidador(@PathVariable Long idCuidador) {
        List<TurnoResponseDTO> turnos = turnoService.obtenerTurnosPorCuidador(idCuidador);
        return ResponseEntity.ok(turnos);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarTurno(
            @PathVariable Long id,
            @Valid @RequestBody CancelarTurnoDTO dto
    ) {
        try {
            TurnoResponseDTO response = turnoService.cancelarTurno(id, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/{id}/reprogramar")
    public ResponseEntity<?> reprogramarTurno(
            @PathVariable Long id,
            @Valid @RequestBody ReprogramarTurnoDTO dto
    ) {
        try {
            TurnoResponseDTO response = turnoService.reprogramarTurno(id, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
}
