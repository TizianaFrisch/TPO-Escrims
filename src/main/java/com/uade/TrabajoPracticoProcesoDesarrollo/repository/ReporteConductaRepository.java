package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteConductaRepository extends JpaRepository<ReporteConducta, Long> {
    List<ReporteConducta> findByEstado(EstadoReporte estado);
    List<ReporteConducta> findByReportante(Usuario reportante);
}
