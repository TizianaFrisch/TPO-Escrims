package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PerfilUsuarioDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final JuegoRepository juegoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, JuegoRepository juegoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.juegoRepository = juegoRepository;
    }

    @Transactional
    public Optional<Usuario> actualizarPerfil(Long usuarioId, PerfilUsuarioDTO dto) {
        var usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) return Optional.empty();
        var usuario = usuarioOpt.get();
        if (dto.getRegion() != null) usuario.setRegion(dto.getRegion());
        if (dto.getJuegoPrincipalId() != null) {
            var juegoOpt = juegoRepository.findById(dto.getJuegoPrincipalId());
            juegoOpt.ifPresent(usuario::setJuegoPrincipal);
        }
        if (dto.getRolesPreferidos() != null) usuario.setRolesPreferidos(dto.getRolesPreferidos());
        if (dto.getDisponibilidadHoraria() != null) usuario.setDisponibilidadHoraria(dto.getDisponibilidadHoraria());
        usuarioRepository.save(usuario);
        return Optional.of(usuario);
    }
}
