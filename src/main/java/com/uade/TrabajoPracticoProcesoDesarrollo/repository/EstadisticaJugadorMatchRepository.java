package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.EstadisticaJugadorMatch;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Match;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadisticaJugadorMatchRepository extends JpaRepository<EstadisticaJugadorMatch, Long> {
    List<EstadisticaJugadorMatch> findByMatch(Match match);
    List<EstadisticaJugadorMatch> findByUsuario(Usuario usuario);
}
