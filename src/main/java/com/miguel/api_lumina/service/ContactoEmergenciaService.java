package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ContactoEmergenciaRequestDTO;
import com.miguel.api_lumina.dto.ContactoEmergenciaResponseDTO;
import com.miguel.api_lumina.entity.ContactoEmergenciaEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.repository.ContactoEmergenciaRepository;
import com.miguel.api_lumina.repository.PacienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactoEmergenciaService {

    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final PacienteRepository pacienteRepository;

    public ContactoEmergenciaService(ContactoEmergenciaRepository contactoEmergenciaRepository,
                                     PacienteRepository pacienteRepository) {
        this.contactoEmergenciaRepository = contactoEmergenciaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public ContactoEmergenciaResponseDTO registrarContacto(Long idPaciente, ContactoEmergenciaRequestDTO dto) {
        PacienteEntity paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente));

        if (contactoEmergenciaRepository.existsByPacienteIdPacienteAndTelefono(idPaciente, dto.getTelefono().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un contacto registrado con el teléfono " + dto.getTelefono() + " para este paciente");
        }

        long totalExistentes = contactoEmergenciaRepository.countByPacienteIdPaciente(idPaciente);
        boolean debeSerPrincipal;

        if (totalExistentes == 0) {
            // Asegurar que al menos un contacto sea marcado como principal
            debeSerPrincipal = true;
        } else if (Boolean.TRUE.equals(dto.getEsPrincipal())) {
            // Máximo un contacto marcado como esPrincipal = true
            contactoEmergenciaRepository.resetAllPrincipalesByPacienteId(idPaciente);
            debeSerPrincipal = true;
        } else {
            boolean existePrincipal = contactoEmergenciaRepository.findFirstByPacienteIdPacienteAndEsPrincipalTrue(idPaciente).isPresent();
            debeSerPrincipal = !existePrincipal;
        }

        ContactoEmergenciaEntity entity = new ContactoEmergenciaEntity(
                paciente,
                dto.getNombre().trim(),
                dto.getRelacion().trim(),
                dto.getTelefono().trim(),
                debeSerPrincipal
        );

        ContactoEmergenciaEntity guardado = contactoEmergenciaRepository.save(entity);
        return mapToResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ContactoEmergenciaResponseDTO> obtenerContactosPorPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        return contactoEmergenciaRepository.findByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(idPaciente)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactoEmergenciaResponseDTO obtenerContactoPorId(Long idPaciente, Long idContacto) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        ContactoEmergenciaEntity entity = contactoEmergenciaRepository
                .findByIdContactoEmergenciaAndPacienteIdPaciente(idContacto, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado con ID: " + idContacto));

        return mapToResponseDTO(entity);
    }

    @Transactional
    public ContactoEmergenciaResponseDTO actualizarContacto(Long idPaciente, Long idContacto, ContactoEmergenciaRequestDTO dto) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        ContactoEmergenciaEntity entity = contactoEmergenciaRepository
                .findByIdContactoEmergenciaAndPacienteIdPaciente(idContacto, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado con ID: " + idContacto));

        if (!entity.getTelefono().equals(dto.getTelefono().trim()) &&
                contactoEmergenciaRepository.existsByPacienteIdPacienteAndTelefono(idPaciente, dto.getTelefono().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro contacto con el teléfono " + dto.getTelefono() + " para este paciente");
        }

        entity.setNombre(dto.getNombre().trim());
        entity.setRelacion(dto.getRelacion().trim());
        entity.setTelefono(dto.getTelefono().trim());

        if (Boolean.TRUE.equals(dto.getEsPrincipal())) {
            contactoEmergenciaRepository.resetAllPrincipalesByPacienteId(idPaciente);
            entity.setEsPrincipal(true);
        } else if (Boolean.FALSE.equals(dto.getEsPrincipal()) && Boolean.TRUE.equals(entity.getEsPrincipal())) {
            long total = contactoEmergenciaRepository.countByPacienteIdPaciente(idPaciente);
            if (total <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede desmarcar el único contacto de emergencia. Debe haber al menos un contacto principal.");
            }
            entity.setEsPrincipal(false);
            contactoEmergenciaRepository.save(entity);

            contactoEmergenciaRepository.findFirstByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(idPaciente)
                    .ifPresent(otro -> {
                        if (!otro.getIdContactoEmergencia().equals(idContacto)) {
                            otro.setEsPrincipal(true);
                            contactoEmergenciaRepository.save(otro);
                        }
                    });
        }

        ContactoEmergenciaEntity actualizado = contactoEmergenciaRepository.save(entity);
        return mapToResponseDTO(actualizado);
    }

    @Transactional
    public ContactoEmergenciaResponseDTO marcarComoPrincipal(Long idPaciente, Long idContacto) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        ContactoEmergenciaEntity entity = contactoEmergenciaRepository
                .findByIdContactoEmergenciaAndPacienteIdPaciente(idContacto, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado con ID: " + idContacto));

        contactoEmergenciaRepository.resetAllPrincipalesByPacienteId(idPaciente);
        entity.setEsPrincipal(true);
        return mapToResponseDTO(contactoEmergenciaRepository.save(entity));
    }

    @Transactional
    public void eliminarContacto(Long idPaciente, Long idContacto) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        ContactoEmergenciaEntity entity = contactoEmergenciaRepository
                .findByIdContactoEmergenciaAndPacienteIdPaciente(idContacto, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado con ID: " + idContacto));

        boolean eraPrincipal = Boolean.TRUE.equals(entity.getEsPrincipal());
        contactoEmergenciaRepository.delete(entity);

        if (eraPrincipal) {
            contactoEmergenciaRepository.findFirstByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(idPaciente)
                    .ifPresent(otro -> {
                        otro.setEsPrincipal(true);
                        contactoEmergenciaRepository.save(otro);
                    });
        }
    }

    private ContactoEmergenciaResponseDTO mapToResponseDTO(ContactoEmergenciaEntity entity) {
        return new ContactoEmergenciaResponseDTO(
                entity.getIdContactoEmergencia(),
                entity.getPaciente() != null ? entity.getPaciente().getIdPaciente() : null,
                entity.getNombre(),
                entity.getRelacion(),
                entity.getTelefono(),
                entity.getEsPrincipal(),
                entity.getActivo()
        );
    }
}
