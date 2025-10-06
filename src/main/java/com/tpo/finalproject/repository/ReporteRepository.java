package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Reporte;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    
    List<Reporte> findByReportador(Usuario reportador);
    
    List<Reporte> findByReportado(Usuario reportado);
    
    List<Reporte> findByEstado(Reporte.EstadoReporte estado);
    
    List<Reporte> findByModerador(Usuario moderador);
    
    @Query("SELECT r FROM Reporte r WHERE r.estado = 'PENDIENTE' ORDER BY r.fechaReporte ASC")
    List<Reporte> findReportesPendientesOrdenadosPorFecha();
    
    @Query("SELECT COUNT(r) FROM Reporte r WHERE r.reportado = :usuario AND r.estado = 'RESUELTO'")
    Long countReportesResueltosContraUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT r FROM Reporte r WHERE r.estado IN ('PENDIENTE', 'EN_REVISION') ORDER BY r.fechaReporte ASC")
    List<Reporte> findReportesParaModeración();
}