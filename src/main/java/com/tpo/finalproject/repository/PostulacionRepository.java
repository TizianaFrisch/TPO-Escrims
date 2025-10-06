package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Postulacion;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.domain.entities.Scrim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    
    List<Postulacion> findByUsuario(Usuario usuario);
    
    List<Postulacion> findByScrim(Scrim scrim);
    
    List<Postulacion> findByEstado(Postulacion.EstadoPostulacion estado);
    
    @Query("SELECT p FROM Postulacion p WHERE p.scrim = :scrim AND p.estado = :estado")
    List<Postulacion> findByScrimAndEstado(@Param("scrim") Scrim scrim, 
                                          @Param("estado") Postulacion.EstadoPostulacion estado);
    
    @Query("SELECT p FROM Postulacion p WHERE p.usuario = :usuario AND p.estado = :estado")
    List<Postulacion> findByUsuarioAndEstado(@Param("usuario") Usuario usuario, 
                                            @Param("estado") Postulacion.EstadoPostulacion estado);
    
    Optional<Postulacion> findByUsuarioAndScrim(Usuario usuario, Scrim scrim);
    
    @Query("SELECT COUNT(p) FROM Postulacion p WHERE p.scrim = :scrim AND p.estado = 'ACEPTADA'")
    Long countPostulacionesAceptadasPorScrim(@Param("scrim") Scrim scrim);
    
    @Query("SELECT p FROM Postulacion p WHERE p.scrim.creador = :creador AND p.estado = 'PENDIENTE'")
    List<Postulacion> findPostulacionesPendientesPorCreador(@Param("creador") Usuario creador);
    
    boolean existsByUsuarioAndScrim(Usuario usuario, Scrim scrim);
}