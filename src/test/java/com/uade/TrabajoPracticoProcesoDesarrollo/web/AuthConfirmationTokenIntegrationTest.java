package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ConfirmationToken;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmationTokenRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ConfirmationService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthConfirmationTokenIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ConfirmationTokenRepository tokenRepository;

    @Autowired
    ConfirmationService confirmationService;

    @BeforeEach
    void clean() {
        // Clean tokens first to avoid FK issues if any user cleanup happens elsewhere
        try { tokenRepository.deleteAll(); } catch (Exception ignore) {}
    }

    @Test
    @SuppressWarnings("null")
    void confirm_token_success_then_login_ok() throws Exception {
        String email = "it_conf_" + System.currentTimeMillis() + "@example.com";
        String username = "it_conf_" + System.currentTimeMillis();

        RegisterRequest req = new RegisterRequest();
        req.email = email;
        req.password = "Secret123!";
        req.region = "LATAM";
        req.username = username;

        // Register -> creates a token and leaves user in PENDIENTE
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Usuario u = usuarioRepository.findByEmail(email).orElseThrow();
        // Locate the token for this user
        ConfirmationToken t = tokenRepository.findAll().stream()
                .filter(tok -> tok.getUsuario() != null && u.getId().equals(tok.getUsuario().getId()))
                .findFirst()
                .orElseThrow();

        // Confirm by token
        mockMvc.perform(get("/api/auth/confirm").param("token", t.getToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(u.getId().intValue()))
            .andExpect(jsonPath("$.verificacionEstado").value("VERIFICADO"));

        // Login should now work
        var loginJson = "{\"email\":\"" + email + "\",\"password\":\"Secret123!\"}";
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.verificacionEstado").value("VERIFICADO"));
    }

    @Test
    @SuppressWarnings("null")
    void confirm_token_expired_returns_410() throws Exception {
        String email = "it_conf_exp_" + System.currentTimeMillis() + "@example.com";
        String username = "it_conf_exp_" + System.currentTimeMillis();

        RegisterRequest req = new RegisterRequest();
        req.email = email;
        req.password = "Secret123!";
        req.region = "LATAM";
        req.username = username;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Usuario u = usuarioRepository.findByEmail(email).orElseThrow();
        ConfirmationToken t = tokenRepository.findAll().stream()
                .filter(tok -> tok.getUsuario() != null && u.getId().equals(tok.getUsuario().getId()))
                .findFirst()
                .orElseThrow();

        // Force expiration
        t.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(t);

        mockMvc.perform(get("/api/auth/confirm").param("token", t.getToken()))
            .andExpect(status().isGone());

        // Still forbidden to login
        var loginJson = "{\"email\":\"" + email + "\",\"password\":\"Secret123!\"}";
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
            .andExpect(status().isForbidden());
    }

    @Test
    @SuppressWarnings("null")
    void confirm_token_reuse_returns_410() throws Exception {
        String email = "it_conf_reuse_" + System.currentTimeMillis() + "@example.com";
        String username = "it_conf_reuse_" + System.currentTimeMillis();

        RegisterRequest req = new RegisterRequest();
        req.email = email;
        req.password = "Secret123!";
        req.region = "LATAM";
        req.username = username;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Usuario u = usuarioRepository.findByEmail(email).orElseThrow();
        ConfirmationToken t = tokenRepository.findAll().stream()
                .filter(tok -> tok.getUsuario() != null && u.getId().equals(tok.getUsuario().getId()))
                .findFirst()
                .orElseThrow();

        // First confirmation works
        mockMvc.perform(get("/api/auth/confirm").param("token", t.getToken()))
            .andExpect(status().isOk());

        // Reuse should return 410 Gone
        mockMvc.perform(get("/api/auth/confirm").param("token", t.getToken()))
            .andExpect(status().isGone());
    }
}
