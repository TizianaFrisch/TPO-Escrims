package com.uade.TrabajoPracticoProcesoDesarrollo.console;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Confirmacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ConfirmacionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests de flujo de consola para ConsoleMenuRunner.
 * Simula: registrar, iniciar sesión, crear scrim, confirmar y finalizar, y salir.
 * Verifica que el flujo no se rompa y que los mensajes clave estén presentes.
 */
public class ConsoleMenuRunnerTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private ScrimService scrimService;
    private JuegoRepository juegoRepository;
    private ScrimRepository scrimRepository;
    private ConsoleEventCollector eventCollector;

    private PrintStream originalOut;
    private java.io.InputStream originalIn;

    @BeforeEach
    void setUp(){
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        scrimService = mock(ScrimService.class);
        juegoRepository = mock(JuegoRepository.class);
        scrimRepository = mock(ScrimRepository.class);
        eventCollector = mock(ConsoleEventCollector.class);

        originalOut = System.out;
        originalIn = System.in;
    }

    @AfterEach
    void tearDown(){
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void flujoCompleto_registro_login_crear_confirmar_finalizar_salir() throws Exception {
        // Datos base
        String email = "test@example.com";
        String pass = "secret";
        String region = "LATAM";

        // Usuario a devolver en login
        Usuario user = new Usuario();
        user.setId(10L);
        user.setEmail(email);
        user.setUsername("test");
        user.setNombre("test");
        user.setRegion(region);
        user.setPasswordHash("HASH_" + pass);

        // Stubs de repos y encoder para registro+login
    java.util.concurrent.atomic.AtomicInteger findByEmailCalls = new java.util.concurrent.atomic.AtomicInteger(0);
    when(usuarioRepository.findByEmail(email)).thenAnswer(inv ->
        findByEmailCalls.getAndIncrement() == 0 ? Optional.empty() : Optional.of(user)
    );
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "HASH_" + inv.getArgument(0));
        when(passwordEncoder.matches(eq(pass), eq("HASH_" + pass))).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            if (u.getId() == null) u.setId(10L);
            return u;
        });

        // Juegos disponibles
        Juego valorant = new Juego();
        valorant.setId(1L);
        valorant.setNombre("Valorant");
        when(juegoRepository.findAll()).thenReturn(List.of(valorant));

        // Crear scrim -> devolver entidad con id=100
        Scrim creado = new Scrim();
        creado.setId(100L);
        creado.setEstado(ScrimEstado.BUSCANDO);
        creado.setJuego(valorant);
        when(scrimService.crearScrim(any(CrearScrimRequest.class))).thenReturn(creado);
        when(scrimRepository.save(any(Scrim.class))).thenAnswer(inv -> inv.getArgument(0));

        // Listar mis scrims (el creado como creador)
        Scrim mio = new Scrim();
        mio.setId(100L);
        mio.setEstado(ScrimEstado.BUSCANDO);
        mio.setJuego(valorant);
        mio.setCreador(user);
    when(scrimService.listar()).thenReturn(List.of(mio));
    when(scrimService.listarPostulaciones(anyLong())).thenReturn(java.util.List.of());

        // Confirmar y finalizar OK
        when(scrimService.confirmar(eq(100L), any(ConfirmacionRequest.class))).thenAnswer(inv -> {
            Confirmacion c = new Confirmacion();
            c.setScrim(mio);
            c.setUsuario(user);
            c.setConfirmado(true);
            return c;
        });
    doNothing().when(scrimService).finalizar(100L);

        // Event collector vacío para este flujo (no abrimos Notificaciones)
        when(eventCollector.getForUserEmail(anyString())).thenReturn(List.of());

        // Script de entradas: registrar(1) -> datos -> ENTER -> login(2) -> datos -> crear(1) -> juego 1 -> datos -> ENTER
        // -> mis scrims(3) -> confirmar(1) id 100 -> ENTER -> mis scrims(3) -> finalizar(2) id 100 -> ENTER -> cerrar sesión(5) s -> salir(3) -> ENTER
        String input = String.join("\n", new String[]{
                "1",                // Registrarse
                email,               // email
                pass,                // contraseña
                region,              // región
                "",                 // ENTER (pause)
                "2",                // Iniciar sesión
                email,
                pass,
                "1",                // Crear scrim
                "1",                // Juego id
                "5v5",              // formato
                "0",                // rango min
                "1000",             // rango max
                "80",               // latencia max
                "2025-11-01",      // fecha (sin hora)
                "",                 // ENTER (pause)
                "3",                // Mis scrims
                "1",                // Confirmar
                "100",              // scrim id
                "",                 // ENTER (pause)
                "3",                // Mis scrims
                "2",                // Finalizar
                "100",              // scrim id
                "",                 // ENTER (pause)
                "5",                // Cerrar sesión
                "s",                // Confirmar cierre
                "3",                // Salir
                ""                  // ENTER (pause final)
        }) + "\n";

        // Redirigir IO ANTES de instanciar runner (Scanner se crea en el constructor)
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setIn(in);
        System.setOut(new PrintStream(out));

        // Ejecutar runner via reflection to avoid depending on a specific constructor signature
        Object runner = Class.forName("com.uade.TrabajoPracticoProcesoDesarrollo.console.ConsoleMenuRunner")
                .getDeclaredConstructor(
                        UsuarioRepository.class,
                        PasswordEncoder.class,
                        ScrimService.class,
                        JuegoRepository.class,
                        ScrimRepository.class,
                        ConsoleEventCollector.class
                )
                .newInstance(
                        usuarioRepository,
                        passwordEncoder,
                        scrimService,
                        juegoRepository,
                        scrimRepository,
                        eventCollector
                );
        runner.getClass().getMethod("run").invoke(runner);

    String console = out.toString(StandardCharsets.UTF_8);
    // Dump para inspección local si falla
    try {
        java.nio.file.Files.writeString(
            java.nio.file.Paths.get("target", "console-test-out.txt"),
            console,
            java.nio.charset.StandardCharsets.UTF_8
        );
    } catch (Exception ignore) {}

        // Validaciones mínimas de flujo y mensajes clave
        assertTrue(console.contains("Registro de usuario"), "Debe mostrar Registro de usuario");
        assertTrue(console.contains("Registro exitoso"), "Debe informar registro exitoso");
    // Evitar problemas de acentos en codificación; buscamos prefijo estable
    assertTrue(console.contains("Inicio de"), "Debe mostrar Inicio de sesión");
        assertTrue(console.contains("Crear scrim"), "Debe mostrar Crear scrim");
        assertTrue(console.contains("Scrim creado. Estado inicial"), "Debe informar scrim creado");
        assertTrue(console.contains("Mis scrims"), "Debe mostrar Mis scrims");
        assertTrue(console.contains("Confirmado."), "Debe informar confirmación");
        assertTrue(console.contains("Scrim finalizado."), "Debe informar finalización");
        assertTrue(console.contains("Saliendo"), "Debe despedirse al salir");

        // Interacciones clave
        verify(scrimService, atLeastOnce()).crearScrim(any(CrearScrimRequest.class));
        verify(scrimService, times(1)).confirmar(eq(100L), any(ConfirmacionRequest.class));
        verify(scrimService, times(1)).finalizar(100L);
        verify(scrimRepository, atLeastOnce()).save(any(Scrim.class));
    }
}
