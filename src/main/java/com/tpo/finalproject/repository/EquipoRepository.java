package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Equipo;
import com.tpo.finalproject.domain.entities.Scrim;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    
    List<Equipo> findByScrim(Scrim scrim);
    
    List<Equipo> findByCapitan(Usuario capitan);
    
    @Query("SELECT e FROM Equipo e WHERE e.scrim = :scrim AND e.lado = :lado")
    List<Equipo> findByScrimAndLado(@Param("scrim") Scrim scrim, @Param("lado") String lado);
    
    @Query("SELECT COUNT(e) FROM Equipo e WHERE e.scrim = :scrim")
    Long countEquiposPorScrim(@Param("scrim") Scrim scrim);
    
    @Query("SELECT e FROM Equipo e JOIN e.miembros m WHERE m.usuario = :usuario")
    List<Equipo> findEquiposDelUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT e FROM Equipo e WHERE SIZE(e.miembros) = 5")
    List<Equipo> findEquiposCompletos();
    
    @Query("SELECT e FROM Equipo e WHERE SIZE(e.miembros) < 5")
    List<Equipo> findEquiposIncompletos();
}