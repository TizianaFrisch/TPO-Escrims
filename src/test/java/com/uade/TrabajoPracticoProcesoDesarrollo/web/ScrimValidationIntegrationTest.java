package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class ScrimValidationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarioRepo;

    private long crearScrimBasico(int rangoMin, int rangoMax, int latenciaMax) throws Exception {
        var crear = new java.util.HashMap<String,Object>();
        crear.put("juegoId", 1);
        crear.put("region", "LATAM");
        crear.put("formato", "1v1");
        crear.put("cuposTotal", 2);
        crear.put("rangoMin", rangoMin);
        crear.put("rangoMax", rangoMax);
        crear.put("latenciaMax", latenciaMax);
        crear.put("fechaHora", LocalDateTime.now().plusMinutes(10).toString());
        crear.put("duracionMinutos", 30);

        String resp = mvc.perform(post("/api/scrims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(crear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(resp);
        if (!m.find()) throw new IllegalStateException("No id");
        return Long.parseLong(m.group(1));
    }

    private void setUser(int id, Integer mmr, Integer latencia){
        Usuario u = usuarioRepo.findById((long) id).orElseThrow();
        if (mmr != null) u.setMmr(mmr);
        if (latencia != null) u.setLatencia(latencia);
        usuarioRepo.save(u);
    }

    @BeforeEach
    void ensureUserKnownValues(){
        // Make sure user 1 exists with deterministic base values
        try { setUser(1, 1000, 50); } catch (Exception ignore) {}
    }

    @Test
    void rechaza_postulacion_por_mmr_fuera_de_rango() throws Exception {
        long scrimId = crearScrimBasico(200, 800, 150);
        // Usuario 1 con mmr alto (1000) debe ser rechazado por rango
        setUser(1, 1000, 50);

        var post = new java.util.HashMap<String,Object>();
        post.put("usuarioId", 1);
        post.put("rolDeseado", "MID");

        mvc.perform(post("/api/scrims/{id}/postulaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(post)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value((org.hamcrest.Matcher<? super String>) org.hamcrest.Matchers.containsString("MMR")));
    }

    @Test
    void rechaza_postulacion_por_latencia_superior_al_maximo() throws Exception {
        long scrimId = crearScrimBasico(100, 1200, 60);
        // Usuario 1 con latencia 120 debe ser rechazado por latencia
        setUser(1, 500, 120);

        var post = new java.util.HashMap<String,Object>();
        post.put("usuarioId", 1);
        post.put("rolDeseado", "MID");

        mvc.perform(post("/api/scrims/{id}/postulaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(post)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value((org.hamcrest.Matcher<? super String>) org.hamcrest.Matchers.containsString("Latencia")));
    }
}
