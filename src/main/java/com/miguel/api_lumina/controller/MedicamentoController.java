package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.MedicamentoRequestDTO;
import com.miguel.api_lumina.dto.MedicamentoResponseDTO;
import com.miguel.api_lumina.service.MedicamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    public MedicamentoController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> crearMedicamento(@Valid @RequestBody MedicamentoRequestDTO requestDTO) {
        MedicamentoResponseDTO creado = medicamentoService.crearMedicamento(requestDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> obtenerMedicamentoPorId(@PathVariable("id") Long id) {
        return medicamentoService.obtenerMedicamentoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tratamiento/{idTratamiento}")
    public ResponseEntity<List<MedicamentoResponseDTO>> obtenerMedicamentosPorTratamiento(@PathVariable("idTratamiento") Long idTratamiento) {
        List<MedicamentoResponseDTO> medicamentos = medicamentoService.obtenerMedicamentosPorTratamiento(idTratamiento);
        return ResponseEntity.ok(medicamentos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> actualizarMedicamento(@PathVariable("id") Long id,
                                                                        @Valid @RequestBody MedicamentoRequestDTO requestDTO) {
        MedicamentoResponseDTO actualizado = medicamentoService.actualizarMedicamento(id, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBajaMedicamento(@PathVariable("id") Long id) {
        medicamentoService.darDeBajaMedicamento(id);
        return ResponseEntity.noContent().build();
    }
}
