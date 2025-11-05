package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.LoginByEmailRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthEndpointsIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Test
    @SuppressWarnings("null")
    void register_login_verify_happy_path() throws Exception {
        long before = usuarioRepository.count();

        RegisterRequest req = new RegisterRequest();
        req.email = "it_user_" + System.currentTimeMillis() + "@example.com";
        req.password = "Secret123!";
        req.region = "LATAM";
        req.username = "it_user_" + System.currentTimeMillis();

        // Register
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn();

        assertThat(usuarioRepository.count()).isEqualTo(before + 1);

        // Login
        LoginByEmailRequest login = new LoginByEmailRequest();
        login.email = req.email;
        login.password = req.password;

        // Login debe fallar (403) hasta verificar
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isForbidden());

        // Para obtener el ID del usuario, leerlo desde el repositorio
        var user = usuarioRepository.findByEmail(req.email).orElseThrow();
        long userId = user.getId();

        // Verify
        mockMvc.perform(post("/api/auth/verify/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value((int) userId))
            .andExpect(jsonPath("$.verificacionEstado").value("VERIFICADO"));

        // Login ahora debe funcionar
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").exists())
            .andExpect(jsonPath("$.verificacionEstado").value("VERIFICADO"));
    }
}
