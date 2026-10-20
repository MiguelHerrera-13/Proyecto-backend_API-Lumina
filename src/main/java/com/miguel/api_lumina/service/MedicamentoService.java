package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.MedicamentoRequestDTO;
import com.miguel.api_lumina.dto.MedicamentoResponseDTO;
import com.miguel.api_lumina.entity.MedicamentoEntity;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.repository.MedicamentoRepository;
import com.miguel.api_lumina.repository.TratamientoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final TratamientoRepository tratamientoRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository, TratamientoRepository tratamientoRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.tratamientoRepository = tratamientoRepository;
    }

    @Transactional
    public MedicamentoResponseDTO crearMedicamento(MedicamentoRequestDTO requestDTO) {
        TratamientoEntity tratamiento = tratamientoRepository.findById(requestDTO.getIdTratamiento())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El tratamiento con ID " + requestDTO.getIdTratamiento() + " no existe"));

        MedicamentoEntity entity = new MedicamentoEntity();
        entity.setTratamiento(tratamiento);
        entity.setNombre(requestDTO.getNombre());
        entity.setDosis(requestDTO.getDosis());
        entity.setFrecuencia(requestDTO.getFrecuencia());
        entity.setActivo(true);

        MedicamentoEntity guardado = medicamentoRepository.save(entity);
        return mapearAResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public Optional<MedicamentoResponseDTO> obtenerMedicamentoPorId(Long id) {
        return medicamentoRepository.findById(id).map(this::mapearAResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponseDTO> obtenerMedicamentosPorTratamiento(Long idTratamiento) {
        if (!tratamientoRepository.existsById(idTratamiento)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El tratamiento con ID " + idTratamiento + " no existe");
        }

        return medicamentoRepository.findByTratamiento_IdTratamiento(idTratamiento).stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicamentoResponseDTO actualizarMedicamento(Long id, MedicamentoRequestDTO requestDTO) {
        MedicamentoEntity entity = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El medicamento con ID " + id + " no existe"));

        if (!entity.getTratamiento().getIdTratamiento().equals(requestDTO.getIdTratamiento())) {
            TratamientoEntity nuevoTratamiento = tratamientoRepository.findById(requestDTO.getIdTratamiento())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "El tratamiento con ID " + requestDTO.getIdTratamiento() + " no existe"));
            entity.setTratamiento(nuevoTratamiento);
        }

        entity.setNombre(requestDTO.getNombre());
        entity.setDosis(requestDTO.getDosis());
        entity.setFrecuencia(requestDTO.getFrecuencia());

        MedicamentoEntity actualizado = medicamentoRepository.save(entity);
        return mapearAResponseDTO(actualizado);
    }

    @Transactional
    public void darDeBajaMedicamento(Long id) {
        MedicamentoEntity entity = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El medicamento con ID " + id + " no existe"));
        medicamentoRepository.delete(entity);
    }

    private MedicamentoResponseDTO mapearAResponseDTO(MedicamentoEntity entity) {
        MedicamentoResponseDTO dto = new MedicamentoResponseDTO();
        dto.setIdMedicamento(entity.getIdMedicamento());
        if (entity.getTratamiento() != null) {
            dto.setIdTratamiento(entity.getTratamiento().getIdTratamiento());
        }
        dto.setNombre(entity.getNombre());
        dto.setDosis(entity.getDosis());
        dto.setFrecuencia(entity.getFrecuencia());
        dto.setActivo(entity.getActivo());
        return dto;
    }
}
