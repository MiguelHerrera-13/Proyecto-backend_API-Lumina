package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.UsuarioRequestDTO;
import com.miguel.api_lumina.dto.UsuarioResponseDTO;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.exception.EmailYaRegistradoException;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailYaRegistradoException("El email ya se encuentra registrado");
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setNombre(request.getNombre());
        entity.setEmail(request.getEmail());
        // TODO: Hashear contraseña usando PasswordEncoder antes de producción
        entity.setPasswordHash(request.getPassword());
        entity.setRol(request.getRol());
        entity.setActivo(true);

        UsuarioEntity saved = usuarioRepository.save(entity);
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
