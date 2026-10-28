package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.SignosVitalesRequestDTO;
import com.miguel.api_lumina.dto.SignosVitalesResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.SignosVitalesEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.SignosVitalesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignosVitalesService {

    private final SignosVitalesRepository signosVitalesRepository;
    private final PacienteRepository pacienteRepository;

    public SignosVitalesService(SignosVitalesRepository signosVitalesRepository,
                                PacienteRepository pacienteRepository) {
        this.signosVitalesRepository = signosVitalesRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public SignosVitalesResponseDTO registrarSignosVitales(Long idPaciente, SignosVitalesRequestDTO requestDTO, String usuarioAutenticado) {
        PacienteEntity paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente));

        validarPresionArterial(requestDTO.getPresionArterial());

        String autorFinal = requestDTO.getAutor();
        if (autorFinal == null || autorFinal.trim().isEmpty()) {
            autorFinal = (usuarioAutenticado != null && !usuarioAutenticado.trim().isEmpty())
                    ? usuarioAutenticado
                    : "Personal de salud";
        }

        SignosVitalesEntity entity = new SignosVitalesEntity();
        entity.setPaciente(paciente);
        entity.setTemperatura(requestDTO.getTemperatura());
        entity.setPresionArterial(requestDTO.getPresionArterial());
        entity.setFrecuenciaCardiaca(requestDTO.getFrecuenciaCardiaca());
        entity.setGlucemia(requestDTO.getGlucemia());
        entity.setAutor(autorFinal);
        entity.setFechaHora(LocalDateTime.now());
        entity.setObservaciones(requestDTO.getObservaciones());

        SignosVitalesEntity guardado = signosVitalesRepository.save(entity);
        return mapToResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<SignosVitalesResponseDTO> obtenerHistorialSignosVitales(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        List<SignosVitalesEntity> registros = signosVitalesRepository.findByPacienteIdPacienteOrderByFechaHoraDesc(idPaciente);

        return registros.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private void validarPresionArterial(String presion) {
        String[] partes = presion.split("/");
        if (partes.length == 2) {
            try {
                int sistolica = Integer.parseInt(partes[0].trim());
                int diastolica = Integer.parseInt(partes[1].trim());

                if (sistolica <= diastolica) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La presión sistólica (" + sistolica + ") debe ser mayor a la diastólica (" + diastolica + ")");
                }
                if (sistolica < 60 || sistolica > 260) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La presión sistólica debe encontrarse en un rango biológico razonable (60 - 260 mmHg)");
                }
                if (diastolica < 30 || diastolica > 160) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La presión diastólica debe encontrarse en un rango biológico razonable (30 - 160 mmHg)");
                }
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valores de presión arterial no numéricos");
            }
        }
    }

    private SignosVitalesResponseDTO mapToResponseDTO(SignosVitalesEntity entity) {
        return new SignosVitalesResponseDTO(
                entity.getIdSignoVital(),
                entity.getPaciente() != null ? entity.getPaciente().getIdPaciente() : null,
                entity.getTemperatura(),
                entity.getPresionArterial(),
                entity.getFrecuenciaCardiaca(),
                entity.getGlucemia(),
                entity.getAutor(),
                entity.getFechaHora(),
                entity.getObservaciones()
        );
    }
}
