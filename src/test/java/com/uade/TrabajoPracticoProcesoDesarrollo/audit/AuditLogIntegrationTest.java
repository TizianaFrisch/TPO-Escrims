package com.uade.TrabajoPracticoProcesoDesarrollo.audit;

import com.uade.TrabajoPracticoProcesoDesarrollo.repository.LogAuditoriaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuditLogIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JuegoRepository juegoRepo;
    @Autowired LogAuditoriaRepository auditRepo;

    @Test
    @WithMockUser(username = "auditUser")
    public void creatingScrimProducesAuditRecord() throws Exception {
        // Create a minimal Juego
        var juego = new Juego();
        juego.setNombre("TestGameForAudit");
        juego = juegoRepo.save(juego);

        String payload = "{\n" +
                "  \"juegoId\": " + juego.getId() + ",\n" +
                "  \"region\": \"LATAM\",\n" +
                "  \"formato\": \"5v5\",\n" +
                "  \"rangoMin\": 0,\n" +
                "  \"rangoMax\": 9999,\n" +
                "  \"latenciaMax\": 80,\n" +
                "  \"fechaHora\": \"2025-10-18T21:00:00\",\n" +
                "  \"cuposTotal\": 2,\n" +
                "  \"duracionMinutos\": 60\n" +
                "}";

        mvc.perform(post("/api/scrims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().is2xxSuccessful());

        // Assert an audit log exists for entidad 'Scrim' and usuario 'auditUser'
        var logs = auditRepo.findByEntidadOrderByTimestampDesc("Scrim");
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getUsuario()).isEqualTo("auditUser");
    }
}
