package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AlertaResponseDTO;
import com.miguel.api_lumina.entity.AlertaEntity;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.AlertaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public AlertaEntity crearAlerta(ReporteDiarioEntity reporte,
                                    UsuarioEntity medico,
                                    String mensajeAlerta) {

        AlertaEntity alerta = new AlertaEntity();

        alerta.setReporte(reporte);
        alerta.setMedico(medico);
        alerta.setMensajeAlerta(mensajeAlerta);
        alerta.setLeida(false);

        return alertaRepository.save(alerta);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> obtenerAlertasPorMedicoYEstado(Long idMedico, Boolean leida) {
        boolean estadoLeida = (leida != null) ? leida : false;
        return alertaRepository
                .findByMedico_IdUsuarioAndLeida(idMedico, estadoLeida)
                .stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> obtenerAlertasNoLeidasPorMedico(Long idMedico) {
        return obtenerAlertasPorMedicoYEstado(idMedico, false);
    }

    @Transactional
    public AlertaResponseDTO marcarAlertaComoLeida(Long idMedico, Long idAlerta) {
        AlertaEntity alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));

        if (alerta.getMedico() == null || !idMedico.equals(alerta.getMedico().getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada");
        }

        if (!Boolean.TRUE.equals(alerta.getLeida())) {
            alerta.setLeida(true);
            alerta = alertaRepository.save(alerta);
        }

        return mapearAResponseDTO(alerta);
    }

    private AlertaResponseDTO mapearAResponseDTO(AlertaEntity alerta) {

        AlertaResponseDTO dto = new AlertaResponseDTO();

        dto.setId(alerta.getIdAlerta());
        dto.setMensajeAlerta(alerta.getMensajeAlerta());
        dto.setLeida(alerta.getLeida());
        dto.setUrgente(true);

        if (alerta.getReporte() != null) {

            dto.setIdReporte(alerta.getReporte().getIdReporte());
            dto.setFechaHora(alerta.getReporte().getFechaRegistro());

            if (alerta.getReporte().getTurno() != null
                    && alerta.getReporte().getTurno().getPaciente() != null) {

                dto.setNombrePaciente(
                        alerta.getReporte()
                                .getTurno()
                                .getPaciente()
                                .getNombreCompleto()
                );
            }
        }

        if (alerta.getMedico() != null) {
            dto.setIdMedico(alerta.getMedico().getIdUsuario());
        }

        return dto;
    }
}