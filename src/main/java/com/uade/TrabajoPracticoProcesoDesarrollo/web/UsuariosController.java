package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.HistorialUsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.NotifPrefsRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.UpdateUsuarioRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PerfilUsuarioDTO;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {
    private final UsuarioRepository usuarioRepository;
    private final ScrimRepository scrimRepository;
    private final HistorialUsuarioRepository historialRepository;
    private final UsuarioService usuarioService;

    public UsuariosController(UsuarioRepository usuarioRepository, ScrimRepository scrimRepository,
                              HistorialUsuarioRepository historialRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.scrimRepository = scrimRepository;
        this.historialRepository = historialRepository;
        this.usuarioService = usuarioService;
    }
    @PutMapping("/{id}/perfil")
    public Usuario editarPerfil(@PathVariable Long id, @RequestBody PerfilUsuarioDTO dto) {
        return usuarioService.actualizarPerfil(id, dto).orElseThrow();
    }

    @GetMapping
    public List<Usuario> listar(){
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}/historial")
    public List<java.util.Map<String, Object>> historial(@PathVariable Long id){
        var u = usuarioRepository.findById(id).orElseThrow();
        var list = historialRepository.findByUsuario(u);
        return list.stream().map(h -> {
            Long mid = h.getMatch() != null ? h.getMatch().getId() : null;
            return java.util.Map.of(
                    "match", java.util.Map.of("id", mid),
                    "resultado", h.getResultado().name(),
                    "mmrAntes", h.getMmrAntes(),
                    "mmrDespues", h.getMmrDespues(),
                    "fechaRegistro", h.getFechaRegistro()
            );
        }).toList();
    }

    @GetMapping("/{id}")
    public Usuario detalle(@PathVariable Long id){
        return usuarioRepository.findById(id).orElseThrow();
    }

    @GetMapping("/{id}/scrims-participando")
    public List<Scrim> scrimsParticipando(@PathVariable Long id){
    // Simple: scrims donde el usuario tiene postulacion aceptada.
        return scrimRepository.findAll().stream()
                .filter(s -> s.getPostulaciones().stream()
                        .anyMatch(p -> p.getUsuario().getId().equals(id) && p.getEstado() == PostulacionEstado.ACEPTADA))
                .toList();
    }

    @PatchMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody UpdateUsuarioRequest req){
        var u = usuarioRepository.findById(id).orElseThrow();
        if (req.region != null) u.setRegion(req.region);
        return usuarioRepository.save(u);
    }

    @GetMapping("/{id}/preferencias-notificacion")
    public NotifPrefsRequest getPrefs(@PathVariable Long id){
        var u = usuarioRepository.findById(id).orElseThrow();
        var r = new NotifPrefsRequest();
        r.notifyPush = u.getNotifyPush();
        r.notifyEmail = u.getNotifyEmail();
        r.notifyDiscord = u.getNotifyDiscord();
        return r;
    }

    @PutMapping("/{id}/preferencias-notificacion")
    public Usuario setPrefs(@PathVariable Long id, @RequestBody NotifPrefsRequest req){
        var u = usuarioRepository.findById(id).orElseThrow();
        if (req.notifyPush != null) u.setNotifyPush(req.notifyPush);
        if (req.notifyEmail != null) u.setNotifyEmail(req.notifyEmail);
        if (req.notifyDiscord != null) u.setNotifyDiscord(req.notifyDiscord);
        return usuarioRepository.save(u);
    }
}
