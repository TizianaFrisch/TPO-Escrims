package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;
import java.util.List;

public interface ScrimRepository extends JpaRepository<Scrim, Long>, JpaSpecificationExecutor<Scrim> {
	java.util.List<Scrim> findByEstado(ScrimEstado estado);

    List<Scrim> findByEstadoInAndFechaCreacionBefore(List<ScrimEstado> estados, LocalDateTime before);
}
