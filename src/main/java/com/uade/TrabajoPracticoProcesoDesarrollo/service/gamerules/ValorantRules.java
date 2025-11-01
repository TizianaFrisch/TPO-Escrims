package com.uade.TrabajoPracticoProcesoDesarrollo.service.gamerules;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValorantRules implements GameRules {
    @Override
    public String supports() { return "valorant"; }

    @Override
    public void validateCreate(CrearScrimRequest req, Juego juego) {
        // Valorant typical format is 5v5; enforce cupos to be multiple of 5 when formato declares "5v5"
        if (req.formato != null && req.formato.toLowerCase().contains("5v5")) {
            if (req.cuposTotal == null || req.cuposTotal % 5 != 0) {
                throw new BusinessException("Valorant: para formato 5v5 el cupo total debe ser múltiplo de 5");
            }
        }
    }

    @Override
    public void validatePostular(PostulacionRequest req, Scrim scrim, Usuario usuario) {
        // Require explicit role selection for Valorant (e.g., Duelist, Controller, Initiator, Sentinel)
        if (req.rolDeseado == null || req.rolDeseado.toString().isBlank()) {
            throw new BusinessException("Valorant: es obligatorio seleccionar un rol deseado (p. ej. Duelist, Controller)");
        }
    }

    @Override
    public void validateFormTeam(Scrim scrim, List<Usuario> candidatos) {
        // Small example: prefer players with mmr set for Valorant
        long missing = candidatos.stream().filter(u -> u.getMmr() == null).count();
        if (missing > candidatos.size() / 2) {
            throw new BusinessException("Valorant: demasiados candidatos sin MMR, no se puede formar equipos confiables");
        }
    }
}
