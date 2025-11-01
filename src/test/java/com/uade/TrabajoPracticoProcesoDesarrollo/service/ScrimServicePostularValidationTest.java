package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ScrimServicePostularValidationTest {

    @Autowired
    ScrimService scrimService;

    @Autowired
    JuegoRepository juegoRepo;

    @Autowired
    UsuarioRepository usuarioRepo;

    @Autowired
    ScrimRepository scrimRepo;

    private Juego createJuego() {
        Juego j = new Juego();
        j.setNombre("TestGame");
        return juegoRepo.save(j);
    }

    private Usuario createUsuario(Integer mmr, Integer latencia) {
        Usuario u = new Usuario();
        u.setUsername(java.util.UUID.randomUUID().toString());
        u.setNombre("Test");
        u.setEmail(u.getUsername() + "@example.com");
        u.setPasswordHash("x");
        u.setMmr(mmr);
        u.setLatencia(latencia);
        return usuarioRepo.save(u);
    }

    private Scrim createScrim(Juego juego, Integer rangoMin, Integer rangoMax, Integer latenciaMax, Integer cupos) {
        Scrim s = new Scrim();
        s.setJuego(juego);
        s.setRegion("LATAM");
        s.setFormato("1v1");
        s.setRangoMin(rangoMin);
        s.setRangoMax(rangoMax);
        s.setLatenciaMax(latenciaMax);
        s.setCuposTotal(cupos);
        s.setFechaHora(LocalDateTime.now().plusDays(1));
        s.setDuracionMinutos(60);
        s.setEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado.BUSCANDO);
        return scrimRepo.save(s);
    }

    @Test
    void rechazaPostulacionFueraDeRango() {
        Juego juego = createJuego();
        Scrim s = createScrim(juego, 1000, 2000, 100, 2);
        Usuario u = createUsuario(900, 50); // mmr por debajo

        PostulacionRequest req = new PostulacionRequest();
        req.usuarioId = u.getId();

        assertThrows(BusinessException.class, () -> scrimService.postular(s.getId(), req));
    }

    @Test
    void rechazaPostulacionPorAltaLatencia() {
        Juego juego = createJuego();
        Scrim s = createScrim(juego, 1000, 2000, 50, 2);
        Usuario u = createUsuario(1500, 120); // latencia mayor

        PostulacionRequest req = new PostulacionRequest();
        req.usuarioId = u.getId();

        assertThrows(BusinessException.class, () -> scrimService.postular(s.getId(), req));
    }

    @Test
    void aceptaPostulacionDentroDeUmbralesYRespetaCupos() {
        Juego juego = createJuego();
        Scrim s = createScrim(juego, 1000, 2000, 200, 1);
        Usuario u1 = createUsuario(1500, 50);
        Usuario u2 = createUsuario(1600, 40);

        PostulacionRequest r1 = new PostulacionRequest(); r1.usuarioId = u1.getId();
        var p1 = scrimService.postular(s.getId(), r1);
        assertThat(p1.getEstado()).isEqualTo(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado.ACEPTADA);

        PostulacionRequest r2 = new PostulacionRequest(); r2.usuarioId = u2.getId();
        var p2 = scrimService.postular(s.getId(), r2);
        // cupo=1 so second should be pendiente
        assertThat(p2.getEstado()).isEqualTo(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado.PENDIENTE);
    }
}
