package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Match;
import com.tpo.finalproject.domain.entities.Scrim;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    Optional<Match> findByScrim(Scrim scrim);
    
    List<Match> findByEstado(Match.EstadoMatch estado);
    
    @Query("SELECT m FROM Match m WHERE m.equipoGanador.capitan = :usuario OR m.equipoPerdedor.capitan = :usuario")
    List<Match> findMatchesDelUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT m FROM Match m JOIN m.equipoGanador.miembros mg JOIN m.equipoPerdedor.miembros mp " +
           "WHERE mg.usuario = :usuario OR mp.usuario = :usuario")
    List<Match> findMatchesConParticipacionDelUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT m FROM Match m WHERE m.fechaInicio BETWEEN :inicio AND :fin")
    List<Match> findMatchesEnRangoFecha(@Param("inicio") LocalDateTime inicio, 
                                       @Param("fin") LocalDateTime fin);
    
    @Query("SELECT m FROM Match m WHERE m.estado = 'FINALIZADO' ORDER BY m.fechaFin DESC")
    List<Match> findMatchesFinalizadosRecientes();
    
    @Query("SELECT m FROM Match m WHERE m.estado = 'EN_PROGRESO' AND m.fechaInicio < :limite")
    List<Match> findMatchesEnProgresoAntiguas(@Param("limite") LocalDateTime limite);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.equipoGanador = :equipo")
    Long countVictoriasPorEquipo(@Param("equipo") com.tpo.finalproject.domain.entities.Equipo equipo);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.equipoPerdedor = :equipo")
    Long countDerrotasPorEquipo(@Param("equipo") com.tpo.finalproject.domain.entities.Equipo equipo);
    
    @Query("SELECT AVG(m.duracionMinutos) FROM Match m WHERE m.estado = 'FINALIZADO'")
    Double findDuracionPromedioMatches();
}