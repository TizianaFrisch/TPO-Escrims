package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.ratelimit.enabled=true",
        "app.ratelimit.windowSeconds=60",
        "app.ratelimit.maxPerIp=5",
        "app.ratelimit.maxPerUser=1"
})
public class SecurityRateLimitingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScrimService scrimService;

    @Test
    @WithMockUser(username = "u1", roles = {"USUARIO"})
    void postulations_are_rate_limited_by_user_and_ip() throws Exception {
    String payload = "{\"usuarioId\":1,\"rolDeseado\":\"MID\"}";

    // first request -> should be accepted (service is mocked)
    mockMvc.perform(post("/api/scrims/1/postulaciones")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().is2xxSuccessful());

    // second request (same user) -> should be 429 due to maxPerUser=1
    mockMvc.perform(post("/api/scrims/1/postulaciones")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isTooManyRequests());
    }
}
