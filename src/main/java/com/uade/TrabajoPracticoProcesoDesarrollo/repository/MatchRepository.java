package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Match;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByScrim(Scrim scrim);

    List<Match> findByEstadoAndFechaInicioBefore(Match.EstadoMatch estado, LocalDateTime before);
}
