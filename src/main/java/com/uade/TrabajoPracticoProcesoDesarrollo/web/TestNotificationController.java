package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-notif")
public class TestNotificationController {
    private final NotificacionService service;
    public TestNotificationController(NotificacionService service){ this.service = service; }

    public static class SendRequest { public Long usuarioId; public String titulo; public String mensaje; }

    @PostMapping("/send-to-user")
    public ResponseEntity<Void> sendToUser(@RequestBody SendRequest req){
        service.crearYEnviarATodosCanales(req.usuarioId, req.titulo != null ? req.titulo : "Test", req.mensaje != null ? req.mensaje : "Hola!", TipoNotificacion.EN_JUEGO);
        return ResponseEntity.ok().build();
    }
}
