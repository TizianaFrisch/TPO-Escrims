package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Confirmacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Notificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
class NotificationsSchedulerIntegrationTest {

    @Autowired
    NotificationsScheduler scheduler;

    @Autowired
    ScrimRepository scrimRepo;

    @Autowired
    ConfirmacionRepository confirmacionRepo;

    @Autowired
    UsuarioRepository usuarioRepo;

    @Autowired
    JuegoRepository juegoRepo;

    @Autowired
    NotificacionRepository notificacionRepo;

    @Test
    @Transactional
    void job_creates_notifications_for_confirmed_users_within_window() {
        // arrange: reminderHours default is 4; create scrim at now + 4 hours + 10 minutes
        int reminderOffsetMinutes = 10;
        LocalDateTime fecha = LocalDateTime.now().plusHours(4).plusMinutes(reminderOffsetMinutes);

        Juego juego = juegoRepo.findAll().stream().findFirst().orElseGet(() -> {
            Juego j = new Juego();
            j.setNombre("TestGame-Reminder-" + System.nanoTime());
            return juegoRepo.save(j);
        });

    // prefer existing test user if present (avoids PK/unique collisions); otherwise create
    Usuario u = usuarioRepo.findByEmail("notif_integration@example.com").orElseGet(() ->
        usuarioRepo.findAll().stream().findFirst().orElseGet(() -> {
            Usuario nu = new Usuario();
            nu.setUsername("notif_integration");
            nu.setNombre("Notif User " + System.nanoTime());
            nu.setEmail("notif_integration@example.com");
            nu.setPasswordHash("x");
            nu.setNotifyPush(true);
            nu.setNotifyEmail(false);
            nu.setNotifyDiscord(false);
            return usuarioRepo.save(nu);
        })
    );
    // ensure push notifications enabled for this user
    u.setNotifyPush(true);
    usuarioRepo.save(u);

        Scrim s = new Scrim();
        s.setJuego(juego);
        s.setRegion("LATAM");
        s.setFormato("1v1");
        s.setCuposTotal(1);
        s.setFechaHora(fecha);
        scrimRepo.save(s);

        Confirmacion c = new Confirmacion();
        c.setScrim(s);
        c.setUsuario(u);
        c.setConfirmado(true);
        confirmacionRepo.save(c);

        // pre-check: no notifications yet for destinatario
        String destinatario = u.getEmail();
        List<Notificacion> before = notificacionRepo.findByDestinatarioOrderByIdDesc(destinatario);
        assertThat(before).isEmpty();

        // act
        scheduler.sendReminders();

        // assert: at least one notification created for destinatario
        List<Notificacion> after = notificacionRepo.findByDestinatarioOrderByIdDesc(destinatario);
        assertThat(after).isNotEmpty();
    }
}
