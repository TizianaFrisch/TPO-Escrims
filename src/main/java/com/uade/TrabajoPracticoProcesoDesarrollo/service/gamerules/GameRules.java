package com.uade.TrabajoPracticoProcesoDesarrollo.service.gamerules;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;

import java.util.List;

/**
 * Per-game hook interface. Implementations can enforce custom validations
 * or transformations for specific games (roles/formats/etc.).
 */
public interface GameRules {
    /** Return the game name this rules implementation applies to (case-insensitive match on Juego.nombre) */
    String supports();

    default void validateCreate(CrearScrimRequest req, Juego juego) {
        // noop by default
    }

    default void validatePostular(PostulacionRequest req, Scrim scrim, Usuario usuario) {
        // noop by default
    }

    default void validateFormTeam(Scrim scrim, List<Usuario> candidatos) {
        // noop by default
    }
}
