package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.IncidenteRequestDTO;
import com.miguel.api_lumina.dto.IncidenteResponseDTO;
import com.miguel.api_lumina.entity.IncidenteReporteEntity;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.repository.IncidenteReporteRepository;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidenteService {

    private static final List<String> SEVERIDADES_VALIDAS = Arrays.asList("LEVE", "MODERADA", "ALTA");

    private final IncidenteReporteRepository incidenteReporteRepository;
    private final ReporteDiarioRepository reporteDiarioRepository;
    private final AlertaService alertaService;

    public IncidenteService(IncidenteReporteRepository incidenteReporteRepository,
                            ReporteDiarioRepository reporteDiarioRepository,
                            AlertaService alertaService) {
        this.incidenteReporteRepository = incidenteReporteRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.alertaService = alertaService;
    }

    @Transactional
    public IncidenteResponseDTO registrarIncidente(Long idReporte, IncidenteRequestDTO dto) {
        ReporteDiarioEntity reporte = reporteDiarioRepository.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El reporte diario con ID " + idReporte + " no existe"
                ));

        String severidad = dto.getSeveridad() != null ? dto.getSeveridad().trim().toUpperCase() : "";
        if (!SEVERIDADES_VALIDAS.contains(severidad)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Severidad inválida: " + dto.getSeveridad() + ". Valores permitidos: LEVE, MODERADA, ALTA"
            );
        }

        IncidenteReporteEntity incidente = new IncidenteReporteEntity();
        incidente.setReporte(reporte);
        incidente.setTipoIncidente(dto.getTipoIncidente().trim());
        incidente.setDescripcion(dto.getDescripcion().trim());
        incidente.setSeveridad(severidad);
        incidente.setFechaHora(dto.getFechaHora() != null ? dto.getFechaHora() : LocalDateTime.now());
        incidente.setActivo(true);

        IncidenteReporteEntity guardado = incidenteReporteRepository.save(incidente);

        boolean alertaGenerada = false;
        if ("ALTA".equalsIgnoreCase(severidad)) {
            if (reporte.getTurno() == null || reporte.getTurno().getMedico() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El turno asociado al reporte no tiene un médico asignado para emitir la alerta"
                );
            }

            String mensajeAlerta = "Incidente severo detectado: "
                    + guardado.getTipoIncidente() + " - "
                    + guardado.getDescripcion();

            alertaService.crearAlerta(
                    reporte,
                    reporte.getTurno().getMedico(),
                    mensajeAlerta
            );
            alertaGenerada = true;
        }

        return mapearADTO(guardado, alertaGenerada);
    }

    @Transactional(readOnly = true)
    public List<IncidenteResponseDTO> obtenerIncidentesPorReporte(Long idReporte) {
        if (!reporteDiarioRepository.existsById(idReporte)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El reporte diario con ID " + idReporte + " no existe"
            );
        }

        return incidenteReporteRepository.findByReporte_IdReporte(idReporte)
                .stream()
                .map(inc -> mapearADTO(inc, "ALTA".equalsIgnoreCase(inc.getSeveridad())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidenteResponseDTO obtenerIncidentePorId(Long idIncidente) {
        IncidenteReporteEntity incidente = incidenteReporteRepository.findById(idIncidente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Incidente no encontrado con ID " + idIncidente
                ));

        return mapearADTO(incidente, "ALTA".equalsIgnoreCase(incidente.getSeveridad()));
    }

    private IncidenteResponseDTO mapearADTO(IncidenteReporteEntity entity, Boolean alertaGenerada) {
        Long idReporte = entity.getReporte() != null ? entity.getReporte().getIdReporte() : null;
        return new IncidenteResponseDTO(
                entity.getIdIncidente(),
                idReporte,
                entity.getTipoIncidente(),
                entity.getDescripcion(),
                entity.getSeveridad(),
                entity.getFechaHora(),
                alertaGenerada,
                entity.getActivo()
        );
    }
}
