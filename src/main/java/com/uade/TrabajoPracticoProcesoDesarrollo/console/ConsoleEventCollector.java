package com.uade.TrabajoPracticoProcesoDesarrollo.console;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimCreatedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimStateChanged;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.DomainEventBus;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.PostulacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.events.ScrimCoincidenteEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Collector de eventos de dominio para el perfil "console".
 * Suscribe al DomainEventBus y almacena mensajes legibles por usuario (email).
 * Mantiene también un feed global para eventos que no puedan asignarse a un usuario específico.
 */
@Component
@Profile("console")
public class ConsoleEventCollector {

    private final DomainEventBus bus;
    private final ScrimRepository scrimRepository;
    private final PostulacionRepository postulacionRepository;

    private final ConfirmacionRepository confirmacionRepository;
    private final UsuarioRepository usuarioRepository;

    private final Map<String, List<String>> perUser = new HashMap<>();
    private final List<String> global = new ArrayList<>();

    public ConsoleEventCollector(DomainEventBus bus,
                                 ScrimRepository scrimRepository,
                                 PostulacionRepository postulacionRepository,
                                 ConfirmacionRepository confirmacionRepository,
                                 UsuarioRepository usuarioRepository) {
        this.bus = bus;
        this.scrimRepository = scrimRepository;
        this.postulacionRepository = postulacionRepository;
        this.confirmacionRepository = confirmacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostConstruct
    public void subscribe() {
        bus.subscribe(this::onEvent);
    }

    public List<String> getForUserEmail(String email){
        List<String> out = new ArrayList<>();
        synchronized (perUser) {
            out.addAll(perUser.getOrDefault(email, Collections.emptyList()));
        }
        synchronized (global) {
            out.addAll(global);
        }
        return out;
    }

    private void onEvent(DomainEvent e){
        try {
            if (e instanceof ScrimCreatedEvent sce) {
                handleScrimCreated(sce);
            } else if (e instanceof ScrimStateChanged ssc) {
                handleStateChanged(ssc);
            } else if (e instanceof ScrimCoincidenteEvent sce) {
                handleScrimCoincidente(sce);
            }
        } catch (Exception ignore) {
            // En consola no interrumpimos el flujo si un mensaje de evento falla
        }
    }

    private void handleScrimCreated(ScrimCreatedEvent ev){
        Optional<Scrim> sOpt = scrimRepository.findById(ev.scrimId());
        String msg;
        if (sOpt.isPresent()) {
            Scrim s = sOpt.get();
            String juego = s.getJuego() != null ? s.getJuego().getNombre() : "";
            String cupos = s.getCuposTotal() != null ? " | Cupos=" + s.getCuposTotal() : "";
            msg = "Scrim creado ID=" + s.getId() + " | Juego=" + juego + " | Región=" + nullSafe(s.getRegion()) + cupos;
            if (s.getCreador() != null && s.getCreador().getEmail() != null) {
                addForUser(s.getCreador().getEmail(), msg);
                return;
            }
        } else {
            msg = "Scrim creado ID=" + ev.scrimId();
        }
        addGlobal(msg);
    }

    private void handleStateChanged(ScrimStateChanged ev){
    Optional<Scrim> sOpt = scrimRepository.findById(ev.scrimId());
    String nuevo = ev.nuevoEstado() != null ? ev.nuevoEstado() : "";
    String msg;
        // Mensajes específicos y minimalistas por estado
        if (nuevo.equalsIgnoreCase(ScrimEstado.LOBBY_ARMADO.name())) {
            long aceptadas = safeCountAceptadas(ev.scrimId());
            long pendientes = safeCountPendientes(ev.scrimId());
            String suffix = sOpt.isPresent() ? buildSuffix(sOpt.get()) : "";
            msg = "Scrim " + ev.scrimId() + " lobby armado. Aceptadas=" + aceptadas + ", Pendientes=" + pendientes + "." + suffix;
        } else if (nuevo.equalsIgnoreCase(ScrimEstado.CONFIRMADO.name())) {
            long conf = safeCountConfirmadas(ev.scrimId());
            String suffix = sOpt.isPresent() ? buildSuffix(sOpt.get()) : "";
            msg = "Scrim " + ev.scrimId() + " confirmaciones completas=" + conf + "." + suffix;
        } else if (nuevo.equalsIgnoreCase(ScrimEstado.EN_JUEGO.name())) {
            String suffix = sOpt.isPresent() ? buildSuffix(sOpt.get()) : "";
            msg = "Scrim " + ev.scrimId() + " en juego." + suffix;
        } else if (nuevo.equalsIgnoreCase(ScrimEstado.FINALIZADO.name())) {
            String suffix = sOpt.isPresent() ? buildSuffix(sOpt.get()) : "";
            msg = "Scrim " + ev.scrimId() + " finalizado." + suffix;
        } else {
            msg = "Scrim " + ev.scrimId() + " estado " + ev.anteriorEstado() + " -> " + ev.nuevoEstado();
        }

        if (sOpt.isEmpty()) { addGlobal(msg); return; }
        Scrim s = sOpt.get();
        // Notificar al creador si existe
        if (s.getCreador() != null && s.getCreador().getEmail() != null) {
            addForUser(s.getCreador().getEmail(), msg);
        }
        // Notificar a postulantes del scrim
        try {
            var postus = postulacionRepository.findByScrimId(ev.scrimId());
            for (var p : postus) {
                if (p.getUsuario() != null && p.getUsuario().getEmail() != null) {
                    addForUser(p.getUsuario().getEmail(), msg);
                }
            }
        } catch (Exception ignore) { }
        // También agregar al feed global
        addGlobal(msg);
    }

    private void handleScrimCoincidente(ScrimCoincidenteEvent ev){
        Optional<Scrim> sOpt = scrimRepository.findById(ev.scrimId());
        String msg;
        if (sOpt.isPresent()) {
            Scrim s = sOpt.get();
            String juego = s.getJuego() != null ? s.getJuego().getNombre() : "";
            String region = nullSafe(s.getRegion());
            msg = "¡Nuevo scrim que coincide con tu búsqueda favorita! Scrim #" + s.getId() +
                  " | Juego=" + juego + " | Región=" + region;
        } else {
            msg = "¡Nuevo scrim que coincide con tu búsqueda favorita! Scrim #" + ev.scrimId();
        }
        // Buscar el email del usuario correctamente
        usuarioRepository.findById(ev.usuarioId())
            .map(u -> u.getEmail())
            .ifPresentOrElse(
                email -> addForUser(email, msg),
                () -> addGlobal(msg)
            );
    }

    private long safeCountAceptadas(Long scrimId){
        try { return postulacionRepository.countByScrimIdAndEstado(scrimId, PostulacionEstado.ACEPTADA); }
        catch (Exception e){ return 0; }
    }
    private long safeCountPendientes(Long scrimId){
        try { return postulacionRepository.countByScrimIdAndEstado(scrimId, PostulacionEstado.PENDIENTE); }
        catch (Exception e){ return 0; }
    }
    private long safeCountConfirmadas(Long scrimId){
        try { return confirmacionRepository.countByScrimIdAndConfirmado(scrimId, true); }
        catch (Exception e){ return 0; }
    }

    private void addForUser(String email, String msg){
        synchronized (perUser) {
            perUser.computeIfAbsent(email, k -> new ArrayList<>()).add(msg);
        }
    }

    private void addGlobal(String msg){
        synchronized (global) { global.add(msg); }
    }

    private String nullSafe(Object v){ return v == null ? "" : String.valueOf(v); }

    private String buildSuffix(Scrim s){
        String juego = s.getJuego() != null ? s.getJuego().getNombre() : "";
        String region = nullSafe(s.getRegion());
        String cupos = s.getCuposTotal() != null ? " | Cupos=" + s.getCuposTotal() : "";
        return " | Juego=" + juego + " | Región=" + region + cupos;
    }
}
