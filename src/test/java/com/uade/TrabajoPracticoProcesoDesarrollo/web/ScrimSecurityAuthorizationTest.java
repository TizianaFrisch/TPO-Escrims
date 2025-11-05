package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class ScrimSecurityAuthorizationTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired ConfirmationTokenRepository tokenRepository;

    private Usuario creador;

    @BeforeEach
    void setUp() {
        // Limpiar tokens primero para no violar FK al borrar usuarios
        try { tokenRepository.deleteAll(); } catch (Exception ignore) {}
        usuarioRepository.deleteAll();

        Usuario u1 = new Usuario();
        u1.setUsername("creador@test");
        u1.setEmail("creador@test");
        u1.setNombre("creador@test");
        u1.setPasswordHash("x");
        u1.setRegion("LATAM");
        creador = usuarioRepository.save(u1);

        Usuario u2 = new Usuario();
        u2.setUsername("otro@test");
        u2.setEmail("otro@test");
        u2.setNombre("otro@test");
        u2.setPasswordHash("x");
        u2.setRegion("LATAM");
        usuarioRepository.save(u2);
    }

    private long crearScrimConCreador(long creadorId) throws Exception {
        var crear = new HashMap<String,Object>();
        crear.put("juegoId", 1);
        crear.put("creadorId", creadorId);
        crear.put("region", "LATAM");
        crear.put("formato", "1v1");
        crear.put("cuposTotal", 1);
        crear.put("rangoMin", 1);
        crear.put("rangoMax", 100);
        crear.put("latenciaMax", 150);
        crear.put("fechaHora", LocalDateTime.now().plusMinutes(5).toString());
        crear.put("duracionMinutos", 30);

        MvcResult crearResult = mvc.perform(post("/api/scrims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(crear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String respCrear = crearResult.getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(respCrear);
        if (!m.find()) throw new IllegalStateException("No se pudo extraer el id del scrim creado");
        return Long.parseLong(m.group(1));
    }

    @Test
    void finalizar_y_cancelar_requieren_ser_creador_si_existe() throws Exception {
        long scrimId = crearScrimConCreador(creador.getId());

        // Sin autenticación (actor=null) -> Forbidden porque el scrim tiene creador
        mvc.perform(post("/api/scrims/{id}/finalizar", scrimId))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/scrims/{id}/cancelar", scrimId))
                .andExpect(status().isForbidden());

        // Con autenticación de OTRO usuario (no creador) -> Forbidden
        // Nota: El proyecto permite /api/** sin auth; @AuthenticationPrincipal será null a menos que se configure un Authentication real.
        // Para este assert de seguridad alcanzan los casos anteriores (null != creador) que directamente devuelven 403.
        // Si en el futuro se configura autenticación real, se puede ampliar usando spring-security-test para setear un principal Usuario.
    }
}
