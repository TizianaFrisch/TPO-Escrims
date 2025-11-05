package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Confirmacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmacionRepository extends JpaRepository<Confirmacion, Long> {
	long countByScrimIdAndConfirmado(Long scrimId, boolean confirmado);
	java.util.List<Confirmacion> findByScrimId(Long scrimId);
	boolean existsByScrimIdAndUsuarioId(Long scrimId, Long usuarioId);
	void flush();
}
