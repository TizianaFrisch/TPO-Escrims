package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.HistorialUsuario;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialUsuarioRepository extends JpaRepository<HistorialUsuario, Long> {
    
    List<HistorialUsuario> findByUsuario(Usuario usuario);
    
    List<HistorialUsuario> findByTipoEvento(String tipoEvento);
    
    @Query("SELECT h FROM HistorialUsuario h WHERE h.usuario = :usuario ORDER BY h.fechaEvento DESC")
    List<HistorialUsuario> findByUsuarioOrderByFechaEventoDesc(@Param("usuario") Usuario usuario);
    
    @Query("SELECT h FROM HistorialUsuario h WHERE h.usuario = :usuario AND h.fechaEvento BETWEEN :inicio AND :fin")
    List<HistorialUsuario> findByUsuarioAndFechaBetween(@Param("usuario") Usuario usuario,
                                                       @Param("inicio") LocalDateTime inicio,
                                                       @Param("fin") LocalDateTime fin);
    
    @Query("SELECT COUNT(h) FROM HistorialUsuario h WHERE h.usuario = :usuario AND h.tipoEvento = :tipoEvento")
    Long countEventosPorUsuarioYTipo(@Param("usuario") Usuario usuario, @Param("tipoEvento") String tipoEvento);
}