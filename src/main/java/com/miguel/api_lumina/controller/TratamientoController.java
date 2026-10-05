package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.TratamientoRequestDTO;
import com.miguel.api_lumina.dto.TratamientoResponseDTO;
import com.miguel.api_lumina.service.TratamientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamientos")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @PostMapping
    public ResponseEntity<TratamientoResponseDTO> registrarTratamiento(@Valid @RequestBody TratamientoRequestDTO requestDTO) {
        TratamientoResponseDTO nuevoTratamiento = tratamientoService.registrarTratamiento(requestDTO);
        return new ResponseEntity<>(nuevoTratamiento, HttpStatus.CREATED);
    }

    @GetMapping("/paciente/{id}")
    public ResponseEntity<List<TratamientoResponseDTO>> obtenerTratamientosPorPaciente(@PathVariable("id") Long id) {
        List<TratamientoResponseDTO> tratamientos = tratamientoService.obtenerTratamientosPorPaciente(id);
        return ResponseEntity.ok(tratamientos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TratamientoResponseDTO> obtenerTratamientoPorId(@PathVariable("id") Long id) {
        return tratamientoService.obtenerTratamientoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTratamiento(@PathVariable("id") Long id) {
        tratamientoService.darDeBajaTratamiento(id);
        return ResponseEntity.noContent().build();
    }
}
