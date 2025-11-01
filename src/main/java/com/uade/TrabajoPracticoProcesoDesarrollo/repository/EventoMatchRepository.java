package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.EventoMatch;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoMatchRepository extends JpaRepository<EventoMatch, Long> {
    List<EventoMatch> findByMatch(Match match);
}
