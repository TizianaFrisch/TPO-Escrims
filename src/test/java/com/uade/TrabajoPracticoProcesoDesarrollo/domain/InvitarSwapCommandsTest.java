package com.uade.TrabajoPracticoProcesoDesarrollo.domain;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.InvitarJugadorCommand;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.SwapJugadoresCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InvitarSwapCommandsTest {
    @Test
    void invitarCommandExecutes(){
        var cmd = new InvitarJugadorCommand();
        var res = cmd.execute(10L, 5L, "{\"usuarioId\":3}");
        assertNotNull(res);
        assertTrue(res instanceof java.util.Map);
    }

    @Test
    void swapCommandExecutes(){
        var cmd = new SwapJugadoresCommand();
        var res = cmd.execute(11L, 6L, "{\"a\":1,\"b\":2}");
        assertNotNull(res);
    @SuppressWarnings("unchecked")
    java.util.Map<String,Object> map = (java.util.Map<String,Object>) res;
    assertEquals(Boolean.TRUE, map.get("swapped"));
    }
}
