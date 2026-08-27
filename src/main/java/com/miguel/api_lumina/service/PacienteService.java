package com.miguel.api_lumina.service;

import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public PacienteEntity registrarPaciente(PacienteEntity paciente) {
        return pacienteRepository.save(paciente);
    }

    public Optional<PacienteEntity> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id);
    }

    @Transactional
    public void darDeBajaPaciente(Long id) {
        pacienteRepository.findById(id).ifPresent(pacienteRepository::delete);
    }
}
