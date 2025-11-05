package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.TrabajoPracticoProcesoDesarrolloApplication;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.model.BusquedaFavorita;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.BusquedaFavoritaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.NotificacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest(classes = TrabajoPracticoProcesoDesarrolloApplication.class)
@ActiveProfiles("local")
@Transactional
public class ScrimCreatedSubscriberIntegrationTest {

    @Autowired private ScrimService scrimService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JuegoRepository juegoRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private BusquedaFavoritaRepository busquedaFavoritaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void creaScrim_notificaUsuariosConBusquedaFavoritaCoincidente() {
        // Juego base
        Juego juego = juegoRepository.findAll().stream().findFirst().orElseGet(() -> {
            Juego j = new Juego();
            j.setNombre("Valorant");
            return juegoRepository.save(j);
        });

        // Usuario interesado con búsqueda favorita activa
        Usuario interesado = new Usuario();
        interesado.setUsername("bf_user");
        interesado.setNombre("BF User");
        interesado.setEmail("bf_user@local");
        interesado.setPasswordHash(passwordEncoder.encode("123"));
        interesado.setRegion("LATAM");
        interesado.setNotifyPush(true);
        interesado = usuarioRepository.save(interesado);

        BusquedaFavorita bf = new BusquedaFavorita();
        bf.setUsuario(interesado);
        bf.setJuego("Valorant");
        bf.setRegion("LATAM");
        bf.setRangoMin(1000);
        bf.setRangoMax(2000);
        bf.setLatenciaMax(100);
        bf.setAlertasActivas(true);
        busquedaFavoritaRepository.save(bf);

        // Creador del scrim
        Usuario creador = new Usuario();
        creador.setUsername("creator");
        creador.setNombre("Creator");
        creador.setEmail("creator@local");
        creador.setPasswordHash(passwordEncoder.encode("123"));
        creador.setRegion("LATAM");
        creador = usuarioRepository.save(creador);

        // Crear scrim que coincide con la búsqueda favorita
        CrearScrimRequest req = new CrearScrimRequest();
        req.creadorId = creador.getId();
        req.juegoId = juego.getId();
        req.region = "LATAM";
        req.formato = "1v1";
        req.rangoMin = 1200; req.rangoMax = 1500;
        req.latenciaMax = 80;
        req.fechaHora = LocalDateTime.now();

    Scrim scrim = scrimService.crearScrim(req);

        // Verificación: debe existir al menos una notificación al usuario interesado
        var notifs = notificacionRepository.findByDestinatarioOrderByIdDesc(interesado.getEmail());
        boolean tieneNotifFavorita = notifs.stream().anyMatch(n -> {
            String p = n.getPayload();
            return p != null
                    && p.contains("Scrim favorito disponible")
                    && p.contains("coincide con tu búsqueda favorita")
                    && p.contains("Scrim #" + scrim.getId());
        });
        Assertions.assertTrue(tieneNotifFavorita, "El usuario con búsqueda favorita no recibió la notificación esperada");
    }
}
