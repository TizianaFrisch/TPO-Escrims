package com.uade.TrabajoPracticoProcesoDesarrollo.domain;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands.AsignarRolCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AsignarRolCommandTest {
    @Test
    void executeReturnsMap(){
        var cmd = new AsignarRolCommand();
    Object res = cmd.execute(1L, 2L, "{\"rol\":\"MID\"}");
        assertNotNull(res);
        assertTrue(res instanceof java.util.Map);
    @SuppressWarnings("unchecked")
    java.util.Map<String,Object> m = (java.util.Map<String,Object>) res;
    assertEquals(1L, m.get("scrimId"));
    assertEquals(2L, m.get("actorId"));
    }
}
