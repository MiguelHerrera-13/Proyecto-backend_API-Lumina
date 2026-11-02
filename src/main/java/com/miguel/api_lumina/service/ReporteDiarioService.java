package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ReporteDiarioRequestDTO;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ReporteDiarioService {

    private final ReporteDiarioRepository reporteDiarioRepository;
    private final TurnoRepository turnoRepository;
    private final AlertaService alertaService;

    public ReporteDiarioService(
            ReporteDiarioRepository reporteDiarioRepository,
            TurnoRepository turnoRepository,
            AlertaService alertaService) {

        this.reporteDiarioRepository = reporteDiarioRepository;
        this.turnoRepository = turnoRepository;
        this.alertaService = alertaService;
    }

    @Transactional
    public ReporteDiarioEntity registrarReporte(ReporteDiarioRequestDTO requestDTO) {

        TurnoEntity turno = turnoRepository.findById(requestDTO.getIdTurno())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El turno con ID " + requestDTO.getIdTurno() + " no existe"
                ));

        ReporteDiarioEntity entity = new ReporteDiarioEntity();
        entity.setTurno(turno);
        entity.setTemperatura(requestDTO.getTemperatura());
        entity.setPresionArterial(requestDTO.getPresionArterial());
        entity.setObservacion(requestDTO.getObservacion());

        ReporteDiarioEntity reporteGuardado = reporteDiarioRepository.save(entity);

        if (esReporteCritico(reporteGuardado)) {

            if (turno.getMedico() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El turno no tiene un médico asignado"
                );
            }

            String mensaje = generarMensajeAlerta(reporteGuardado);

            alertaService.crearAlerta(
                    reporteGuardado,
                    turno.getMedico(),
                    mensaje
            );
        }

        return reporteGuardado;
    }

    private boolean esReporteCritico(ReporteDiarioEntity reporte) {

        if (reporte.getTemperatura() != null) {
            if (reporte.getTemperatura() >= 38.0
                    || reporte.getTemperatura() <= 35.0) {
                return true;
            }
        }

        if (reporte.getPresionArterial() != null) {

            try {
                String[] partes = reporte.getPresionArterial().split("/");

                if (partes.length == 2) {
                    int sistolica = Integer.parseInt(partes[0].trim());
                    int diastolica = Integer.parseInt(partes[1].trim());

                    return sistolica >= 180 || diastolica >= 120;
                }

            } catch (NumberFormatException ignored) {
                // Si el formato no es válido, no lo consideramos crítico acá.
            }
        }

        return false;
    }

    private String generarMensajeAlerta(ReporteDiarioEntity reporte) {

        if (reporte.getTemperatura() != null
                && (reporte.getTemperatura() >= 38.0
                || reporte.getTemperatura() <= 35.0)) {

            return "Temperatura crítica detectada: "
                    + reporte.getTemperatura() + " °C";
        }

        return "Presión arterial crítica detectada: "
                + reporte.getPresionArterial();
    }

    public List<ReporteDiarioEntity> obtenerReportesPorTurno(Long idTurno) {
        // Validar que el turno exista (Punto 9)
        if (!turnoRepository.existsById(idTurno)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El turno con ID " + idTurno + " no existe");
        }
        return reporteDiarioRepository.findByTurno_IdTurno(idTurno);
    }

    public Optional<ReporteDiarioEntity> obtenerReportePorId(Long id) {
        return reporteDiarioRepository.findById(id);
    }
}
