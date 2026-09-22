package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.service.TurnoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping
    public ResponseEntity<?> crearTurno(@RequestBody TurnoRequestDTO dto) {
        try {
            TurnoResponseDTO response = turnoService.crearTurno(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
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
}
