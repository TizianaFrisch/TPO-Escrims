package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Notificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionRepository repo;
    private final NotificacionService service;

    public NotificacionController(NotificacionRepository repo, NotificacionService service){
        this.repo = repo;
        this.service = service;
    }

    // Existing simple endpoints
    @GetMapping
    public List<Notificacion> listar(@RequestParam String usuarioId){
        return repo.findByDestinatarioOrderByIdDesc(usuarioId);
    }

    @GetMapping("/unread-count")
    public long unread(@RequestParam String usuarioId){
        return repo.countByDestinatarioAndLeidaIsFalse(usuarioId);
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<Void> leer(@PathVariable Long id){
        service.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    // New richer endpoints
    @GetMapping("/usuario/{usuario}")
    public List<Notificacion> listarPorUsuario(@PathVariable("usuario") String usuarioIdOrEmail){
        return service.listarPorUsuario(usuarioIdOrEmail);
    }

    @GetMapping("/usuario/{usuario}/no-leidas")
    public List<Notificacion> noLeidas(@PathVariable("usuario") String usuarioIdOrEmail){
        return service.noLeidas(usuarioIdOrEmail);
    }

    @GetMapping("/usuario/{usuario}/count-no-leidas")
    public long contarNoLeidas(@PathVariable("usuario") String usuarioIdOrEmail){
        return service.contarNoLeidas(usuarioIdOrEmail);
    }

    @PutMapping("/usuario/{usuario}/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable("usuario") String usuarioIdOrEmail){
        service.marcarTodasComoLeidas(usuarioIdOrEmail);
        return ResponseEntity.noContent().build();
    }
}
