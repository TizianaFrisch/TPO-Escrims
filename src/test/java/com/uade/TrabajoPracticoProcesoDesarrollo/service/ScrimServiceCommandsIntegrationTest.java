package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScrimServiceCommandsIntegrationTest {
    @Autowired
    ApplicationContext ctx;

    @Test
    void commandsAreRegistered(){
        var beans = ctx.getBeansOfType(com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.ScrimCommand.class);
        assertTrue(beans.values().stream().anyMatch(b -> b.getClass().getSimpleName().contains("AsignarRol")), "AsignarRol not registered");
        assertTrue(beans.values().stream().anyMatch(b -> b.getClass().getSimpleName().contains("InvitarJugador")), "InvitarJugador not registered");
        assertTrue(beans.values().stream().anyMatch(b -> b.getClass().getSimpleName().contains("SwapJugadores")), "SwapJugadores not registered");
    }
}
