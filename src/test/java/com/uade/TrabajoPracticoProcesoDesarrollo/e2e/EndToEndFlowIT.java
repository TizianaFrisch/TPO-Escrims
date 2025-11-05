package com.uade.TrabajoPracticoProcesoDesarrollo.e2e;

import com.uade.TrabajoPracticoProcesoDesarrollo.TrabajoPracticoProcesoDesarrolloApplication;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ConfirmacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootTest(classes = TrabajoPracticoProcesoDesarrolloApplication.class)
@ActiveProfiles("local")
@Transactional
public class EndToEndFlowIT {

    @Autowired private ScrimService scrimService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JuegoRepository juegoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void fullFlow_matchmaking_states_confirmations() {
        // Arrange: juego base
        Juego juego = juegoRepository.findAll().stream().findFirst().orElseGet(() -> {
            Juego j = new Juego();
            j.setNombre("Valorant");
            return juegoRepository.save(j);
        });

        // Usuarios candidatos (LATAM, buen ping, MMR cercanos)
        Usuario u1 = new Usuario();
        u1.setUsername("e2e_u1");
        u1.setNombre("E2E One");
        u1.setEmail("e2e_u1@local");
        u1.setPasswordHash(passwordEncoder.encode("123"));
        u1.setRegion("LATAM");
        u1.setLatencia(40);
        u1.setMmr(1200);
        u1.setVerificacionEstado(VerificacionEstado.VERIFICADO);
        u1 = usuarioRepository.save(u1);

        Usuario u2 = new Usuario();
        u2.setUsername("e2e_u2");
        u2.setNombre("E2E Two");
        u2.setEmail("e2e_u2@local");
        u2.setPasswordHash(passwordEncoder.encode("123"));
        u2.setRegion("LATAM");
        u2.setLatencia(45);
        u2.setMmr(1250);
        u2.setVerificacionEstado(VerificacionEstado.VERIFICADO);
        u2 = usuarioRepository.save(u2);

        // Crear scrim 1v1/5v5 (el sistema toma cupo minimo=2 si no se informa)
        CrearScrimRequest req = new CrearScrimRequest();
        req.creadorId = u1.getId();
        req.juegoId = juego.getId();
        req.region = "LATAM";
        req.formato = "1v1"; // para rapidez del test
        req.rangoMin = 1000; req.rangoMax = 2000;
        req.latenciaMax = 80;
        req.fechaHora = LocalDateTime.now();
        Scrim scrim = scrimService.crearScrim(req);

        // Postulaciones
        PostulacionRequest p1 = new PostulacionRequest();
        p1.usuarioId = u1.getId();
        p1.rolDeseado = com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol.FLEX;
        scrimService.postular(scrim.getId(), p1);

        PostulacionRequest p2 = new PostulacionRequest();
        p2.usuarioId = u2.getId();
        p2.rolDeseado = com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol.FLEX;
        scrimService.postular(scrim.getId(), p2);

        // Matchmaking (MMR) – no debe fallar y prepara el lobby
        scrimService.runMatchmaking(scrim.getId(), "mmr");

        // Confirmaciones (servicio promueve a aceptada si hiciera falta)
        ConfirmacionRequest c1 = new ConfirmacionRequest(); c1.usuarioId = u1.getId(); c1.confirmado = true;
        ConfirmacionRequest c2 = new ConfirmacionRequest(); c2.usuarioId = u2.getId(); c2.confirmado = true;
        scrimService.confirmar(scrim.getId(), c1);
        scrimService.confirmar(scrim.getId(), c2);

        // Estado actual tras confirmaciones (puede quedar en CONFIRMADO o pasar a EN_JUEGO si la fecha es inmediata)
        scrim = scrimService.obtener(scrim.getId());
        if (scrim.getEstado() != ScrimEstado.EN_JUEGO) {
            // Iniciar explícitamente si aún no está en juego
            scrim = scrimService.iniciar(scrim.getId());
        }
        Assertions.assertEquals(ScrimEstado.EN_JUEGO, scrim.getEstado(), "Debe estar en EN_JUEGO luego de iniciar");
        scrimService.finalizar(scrim.getId());
        Scrim finalizado = scrimService.obtener(scrim.getId());
        Assertions.assertEquals(ScrimEstado.FINALIZADO, finalizado.getEstado(), "Debe finalizar en FINALIZADO");
    }
}
