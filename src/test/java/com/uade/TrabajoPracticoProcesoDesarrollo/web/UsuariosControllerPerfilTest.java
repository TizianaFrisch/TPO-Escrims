package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.UsuarioService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PerfilUsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class UsuariosControllerPerfilTest {
    private MockMvc mockMvc;
    private UsuarioService usuarioService;
    private UsuarioRepository usuarioRepository;
    private JuegoRepository juegoRepository;
    private UsuariosController usuariosController;

    private Usuario usuario;
    private Juego juego;

    @BeforeEach
    void setup() {
        usuarioService = Mockito.mock(UsuarioService.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        juegoRepository = Mockito.mock(JuegoRepository.class);
        usuariosController = new UsuariosController(usuarioRepository, null, null, usuarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(usuariosController).build();

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        usuario.setRegion("EUW");
        usuario.setRolesPreferidos(List.of("Support"));
        usuario.setDisponibilidadHoraria("Lunes a Viernes 18-22");
        juego = new Juego();
        juego.setId(10L);
        juego.setNombre("Valorant");
        usuario.setJuegoPrincipal(juego);
    }

    @Test
    void editarPerfilUsuario_OK() throws Exception {
        PerfilUsuarioDTO dto = new PerfilUsuarioDTO();
        dto.setRegion("LAN");
        dto.setRolesPreferidos(List.of("Duelist", "Controller"));
        dto.setDisponibilidadHoraria("Sábados 10-14");
        dto.setJuegoPrincipalId(10L);
        Mockito.when(usuarioService.actualizarPerfil(eq(1L), any(PerfilUsuarioDTO.class))).thenReturn(Optional.of(usuario));
        String json = "{\"region\":\"LAN\",\"rolesPreferidos\":[\"Duelist\",\"Controller\"],\"disponibilidadHoraria\":\"Sábados 10-14\",\"juegoPrincipalId\":10}";
        mockMvc.perform(put("/api/usuarios/1/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("EUW"))
                .andExpect(jsonPath("$.juegoPrincipal.id").value(10));
    }
}
