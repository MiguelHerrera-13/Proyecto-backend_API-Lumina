package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.UsuarioRequestDTO;
import com.miguel.api_lumina.dto.UsuarioResponseDTO;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.exception.EmailYaRegistradoException;
import com.miguel.api_lumina.repository.UsuarioRepository;
import com.miguel.api_lumina.entity.TokenVerificacionEntity;
import com.miguel.api_lumina.repository.TokenVerificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerificacionRepository tokenVerificacionRepository;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this(usuarioRepository, passwordEncoder, null, null);
    }

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          TokenVerificacionRepository tokenVerificacionRepository,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenVerificacionRepository = tokenVerificacionRepository;
        this.emailService = emailService;
    }

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        Optional<UsuarioEntity> existenteOpt = usuarioRepository.findByEmail(email);

        UsuarioEntity saved;
        if (existenteOpt.isPresent()) {
            UsuarioEntity existente = existenteOpt.get();
            if (Boolean.TRUE.equals(existente.getActivo())) {
                throw new EmailYaRegistradoException("El correo ya se encuentra registrado y activo. Puedes iniciar sesión directamente.");
            }
            // Si la cuenta estaba registrada pero inactiva (ej. el token anterior expiró o no se pudo activar), actualizamos y reenviamos
            existente.setNombre(request.getNombre());
            existente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existente.setRol(request.getRol());
            saved = usuarioRepository.save(existente);
        } else {
            UsuarioEntity entity = new UsuarioEntity();
            entity.setNombre(request.getNombre());
            entity.setEmail(email);
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            entity.setRol(request.getRol());
            // La cuenta inicia inactiva hasta que confirme el correo de verificación
            entity.setActivo(false);
            saved = usuarioRepository.save(entity);
        }

        // Generar token de verificación único de 6 dígitos alfanuméricos en mayúsculas
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        if (tokenVerificacionRepository != null) {
            TokenVerificacionEntity tokenEntity = new TokenVerificacionEntity(
                    token,
                    saved,
                    LocalDateTime.now().plusHours(24)
            );
            tokenVerificacionRepository.save(tokenEntity);
        }

        if (emailService != null) {
            emailService.enviarCorreoVerificacion(saved, token);
        }

        return mapearADTO(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(this::mapearADTO);
    }

    @Transactional
    public void darDeBajaUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });
    }

    private UsuarioResponseDTO mapearADTO(UsuarioEntity entity) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(entity.getIdUsuario());
        dto.setNombre(entity.getNombre());
        dto.setEmail(entity.getEmail());
        dto.setRol(entity.getRol());
        dto.setActivo(entity.getActivo());
        return dto;
    }
}
