package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Notificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.CanalNotificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.NotifierFactory;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotifierFactory notifierFactory;
    private final com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionIntentRepository intentRepository;

    @Value("${app.notifications.push.enabled:true}")
    private boolean pushEnabled;
    @Value("${app.notifications.email.enabled:false}")
    private boolean emailEnabled;
    @Value("${app.notifications.discord.enabled:false}")
    private boolean discordEnabled;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioRepository usuarioRepository,
                               NotifierFactory notifierFactory,
                               com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionIntentRepository intentRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.notifierFactory = notifierFactory;
        this.intentRepository = intentRepository;
    }

    @Transactional
    public Notificacion crear(String destinatario, TipoNotificacion tipo, CanalNotificacion canal, String payload) {
        Notificacion n = new Notificacion();
        n.setDestinatario(destinatario);
        n.setTipo(tipo);
        n.setCanal(canal);
        n.setPayload(payload);
        return notificacionRepository.save(n);
    }

    @Transactional
    public void crearYEnviarATodosCanales(Long usuarioId, String titulo, String mensaje, TipoNotificacion tipo) {
        Usuario u = usuarioRepository.findById(usuarioId).orElseThrow();
        String destinatario = u.getEmail() != null ? u.getEmail() : (u.getUsername() != null ? u.getUsername() : String.valueOf(usuarioId));

        // PUSH
        if (pushEnabled && Boolean.TRUE.equals(u.getNotifyPush())) {
            var n = crear(destinatario, tipo, CanalNotificacion.PUSH, titulo + " | " + mensaje);
            boolean ok = notifierFactory.createPush().send(destinatario, titulo + ": " + mensaje);
            var intent = new com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.NotificacionIntent();
            intent.setNotificacionId(n.getId()); intent.setCanal(CanalNotificacion.PUSH);
            intent.setDestinatario(destinatario); intent.setPayload(titulo + " | " + mensaje);
            intent.setSuccess(ok);
            if (!ok) intent.setErrorMessage("Push provider returned failure");
            intentRepository.save(intent);
        }
        // EMAIL (stub through notifier factory)
        if (emailEnabled && Boolean.TRUE.equals(u.getNotifyEmail())) {
            var n = crear(destinatario, tipo, CanalNotificacion.EMAIL, titulo + " | " + mensaje);
            boolean ok = notifierFactory.createEmail().send(destinatario, titulo + ": " + mensaje);
            var intent = new com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.NotificacionIntent();
            intent.setNotificacionId(n.getId()); intent.setCanal(CanalNotificacion.EMAIL);
            intent.setDestinatario(destinatario); intent.setPayload(titulo + " | " + mensaje);
            intent.setSuccess(ok);
            if (!ok) intent.setErrorMessage("Email provider returned failure");
            intentRepository.save(intent);
        }
        // DISCORD (stub through notifier factory)
        if (discordEnabled && Boolean.TRUE.equals(u.getNotifyDiscord())) {
            var n = crear(destinatario, tipo, CanalNotificacion.DISCORD, titulo + " | " + mensaje);
            boolean ok = notifierFactory.createChat().send(destinatario, titulo + ": " + mensaje);
            var intent = new com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.NotificacionIntent();
            intent.setNotificacionId(n.getId()); intent.setCanal(CanalNotificacion.DISCORD);
            intent.setDestinatario(destinatario); intent.setPayload(titulo + " | " + mensaje);
            intent.setSuccess(ok);
            if (!ok) intent.setErrorMessage("Discord provider returned failure");
            intentRepository.save(intent);
        }
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarPorUsuario(String usuarioIdOrEmail) {
        return notificacionRepository.findByDestinatarioOrderByIdDesc(usuarioIdOrEmail);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> noLeidas(String usuarioIdOrEmail) {
        return notificacionRepository.findByDestinatarioAndLeidaIsFalse(usuarioIdOrEmail);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String usuarioIdOrEmail) {
        return notificacionRepository.countByDestinatarioAndLeidaIsFalse(usuarioIdOrEmail);
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        var n = notificacionRepository.findById(notificacionId).orElseThrow();
        n.setLeida(true);
        notificacionRepository.save(n);
    }

    @Transactional
    public void marcarTodasComoLeidas(String usuarioIdOrEmail) {
        var pendientes = notificacionRepository.findByDestinatarioAndLeidaIsFalse(usuarioIdOrEmail);
        for (var n : pendientes) {
            n.setLeida(true);
        }
        notificacionRepository.saveAll(pendientes);
    }
}
