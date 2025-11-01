package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ModeracionReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScrimService scrimService;

    @MockBean
    private ModeracionReporteService moderacionReporteService;

    @Test
    @WithMockUser(roles = {"MODERADOR"})
    void moderatorCanCancelScrim() throws Exception {
        mockMvc.perform(post("/api/scrims/1/cancelar"))
                .andExpect(status().isNoContent());

    verify(scrimService, times(1)).cancelar(eq(1L), isNull());
    }

    @Test
    @WithMockUser(roles = {"USUARIO"})
    void regularUserCannotCancelScrim() throws Exception {
        mockMvc.perform(post("/api/scrims/1/cancelar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"MODERADOR"})
    void moderatorCanResolveReport() throws Exception {
        mockMvc.perform(post("/api/mod/reportes/1/resolver?resolucion=test&estado=APROBADO"))
                .andExpect(status().isOk());

        verify(moderacionReporteService, times(1)).resolverReporte(1L, com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte.APROBADO, "test");
    }
}
