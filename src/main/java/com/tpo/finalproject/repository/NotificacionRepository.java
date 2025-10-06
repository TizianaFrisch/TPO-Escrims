package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Notificacion;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByUsuario(Usuario usuario);
    
    List<Notificacion> findByUsuarioAndLeidaFalse(Usuario usuario);
    
    List<Notificacion> findByTipo(Notificacion.TipoNotificacion tipo);
    
    @Query("SELECT n FROM Notificacion n WHERE n.usuario = :usuario ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findByUsuarioOrderByFechaCreacionDesc(@Param("usuario") Usuario usuario);
    
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuario = :usuario AND n.leida = false")
    Long countNotificacionesNoLeidasPorUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT n FROM Notificacion n WHERE n.usuario = :usuario AND n.leida = false ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNotificacionesNoLeidasPorUsuario(@Param("usuario") Usuario usuario);
}