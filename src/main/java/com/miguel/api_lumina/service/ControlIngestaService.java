package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ControlIngestaRequestDTO;
import com.miguel.api_lumina.dto.ControlIngestaResponseDTO;
import com.miguel.api_lumina.dto.ResumenIngestaResponseDTO;
import com.miguel.api_lumina.entity.ControlIngestaEntity;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.repository.ControlIngestaRepository;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ControlIngestaService {

    private final ControlIngestaRepository controlIngestaRepository;
    private final ReporteDiarioRepository reporteDiarioRepository;

    public ControlIngestaService(ControlIngestaRepository controlIngestaRepository,
                                 ReporteDiarioRepository reporteDiarioRepository) {
        this.controlIngestaRepository = controlIngestaRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
    }

    @Transactional
    public ControlIngestaResponseDTO registrarIngesta(Long idReporte, ControlIngestaRequestDTO dto) {
        ReporteDiarioEntity reporte = reporteDiarioRepository.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El reporte diario con ID " + idReporte + " no existe"
                ));

        if (dto.getPorcentajeConsumido() == null || dto.getPorcentajeConsumido() < 0 || dto.getPorcentajeConsumido() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El porcentaje consumido debe estar comprendido entre 0 y 100"
            );
        }

        if (dto.getAguaMl() == null || dto.getAguaMl() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de agua en mililitros no puede ser negativa"
            );
        }

        ControlIngestaEntity entity = new ControlIngestaEntity();
        entity.setReporte(reporte);
        entity.setTipoComida(dto.getTipoComida().trim());
        entity.setDescripcionComida(dto.getDescripcionComida() != null ? dto.getDescripcionComida().trim() : null);
        entity.setPorcentajeConsumido(dto.getPorcentajeConsumido());
        entity.setAguaMl(dto.getAguaMl());
        entity.setFechaHora(dto.getFechaHora() != null ? dto.getFechaHora() : LocalDateTime.now());
        entity.setObservaciones(dto.getObservaciones() != null ? dto.getObservaciones().trim() : null);
        entity.setActivo(true);

        ControlIngestaEntity guardado = controlIngestaRepository.save(entity);
        return mapearADTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ControlIngestaResponseDTO> obtenerIngestasPorReporte(Long idReporte) {
        if (!reporteDiarioRepository.existsById(idReporte)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El reporte diario con ID " + idReporte + " no existe"
            );
        }

        return controlIngestaRepository.findByReporte_IdReporte(idReporte)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumenIngestaResponseDTO obtenerResumenIngestaPorReporte(Long idReporte) {
        if (!reporteDiarioRepository.existsById(idReporte)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El reporte diario con ID " + idReporte + " no existe"
            );
        }

        List<ControlIngestaEntity> lista = controlIngestaRepository.findByReporte_IdReporte(idReporte);

        int totalAguaMl = lista.stream().mapToInt(ControlIngestaEntity::getAguaMl).sum();
        double promedioConsumo = lista.isEmpty() ? 0.0 :
                lista.stream().mapToInt(ControlIngestaEntity::getPorcentajeConsumido).average().orElse(0.0);
        double promedioRedondeado = Math.round(promedioConsumo * 100.0) / 100.0;

        List<ControlIngestaResponseDTO> dtos = lista.stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());

        return new ResumenIngestaResponseDTO(
                idReporte,
                totalAguaMl,
                lista.size(),
                promedioRedondeado,
                dtos
        );
    }

    @Transactional(readOnly = true)
    public ControlIngestaResponseDTO obtenerIngestaPorId(Long idControlIngesta) {
        ControlIngestaEntity entity = controlIngestaRepository.findById(idControlIngesta)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Control de ingesta no encontrado con ID " + idControlIngesta
                ));

        return mapearADTO(entity);
    }

    private ControlIngestaResponseDTO mapearADTO(ControlIngestaEntity entity) {
        Long idReporte = entity.getReporte() != null ? entity.getReporte().getIdReporte() : null;
        return new ControlIngestaResponseDTO(
                entity.getIdControlIngesta(),
                idReporte,
                entity.getTipoComida(),
                entity.getDescripcionComida(),
                entity.getPorcentajeConsumido(),
                entity.getAguaMl(),
                entity.getFechaHora(),
                entity.getObservaciones(),
                entity.getActivo()
        );
    }
}
