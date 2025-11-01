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
    void register_login_verify_happy_path() throws Exception {
        long before = usuarioRepository.count();

    RegisterRequest req = new RegisterRequest();
    req.email = "it_user_" + System.currentTimeMillis() + "@example.com";
        req.password = "Secret123!";
        req.region = "LATAM";

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

        var loginRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").exists())
            .andReturn();

        // Extract user id from login response
        var loginNode = objectMapper.readTree(loginRes.getResponse().getContentAsString());
        long userId = loginNode.get("id").asLong();

        // Verify
        mockMvc.perform(post("/api/auth/verify/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value((int) userId))
            .andExpect(jsonPath("$.verificacionEstado").value("VERIFICADO"));
    }
}
