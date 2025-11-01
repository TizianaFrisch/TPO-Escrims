package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScrimEndpointsIntegrationTest {

        @Autowired MockMvc mvc;

        @Test
        @WithMockUser(username = "integration", roles = {"USUARIO"})
        void crear_postular_confirmar_y_command_flujo_basico() throws Exception {
        // 1) Crear scrim (cupos=1 para simplificar)
        var crear = new java.util.HashMap<String,Object>();
        crear.put("juegoId", 1);
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
        if (!m.find()) {
            throw new IllegalStateException("No se pudo extraer el id del scrim creado");
        }
        long scrimId = Long.parseLong(m.group(1));

        // 2) Postular usuario 1 -> debería quedar aceptada y como cupo=1, pasar a LOBBY_ARMADO
        var postular = new java.util.HashMap<String,Object>();
        postular.put("usuarioId", 1);
        postular.put("rolDeseado", "MID");
        mvc.perform(post("/api/scrims/{id}/postulaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(postular)))
                .andExpect(status().isOk());
        // Verificar estado LOBBY_ARMADO evitando parsear respuestas profundas con ciclos
        mvc.perform(get("/api/scrims/{id}", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LOBBY_ARMADO"));

        // 3) Confirmar usuario 1 -> como cupos=1, debe pasar a CONFIRMADO y arrancar (EN_JUEGO)
        var confirmar = new java.util.HashMap<String,Object>();
        confirmar.put("usuarioId", 1);
        confirmar.put("confirmado", true);
        mvc.perform(post("/api/scrims/{id}/confirmaciones", scrimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(confirmar)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/scrims/{id}", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));

        // 4) Ejecutar un comando (asignarRol), respuesta 200 con campos mínimos
        var cmd = new java.util.HashMap<String,Object>();
        cmd.put("actorId", 1);
        cmd.put("payload", "{\"rol\":\"MID\"}");
        mvc.perform(post("/api/scrims/{id}/acciones/{command}", scrimId, "asignarrol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.scrimId").value(scrimId))
                .andExpect(jsonPath("$.actorId").value(1));

        // 5) Finalizar scrim simple (sin estadísticas): debe quedar FINALIZADO
        mvc.perform(post("/api/scrims/{id}/finalizar", scrimId))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/scrims/{id}", scrimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }
}
