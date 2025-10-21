package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
	List<Notificacion> findByDestinatarioOrderByIdDesc(String destinatario);
	long countByDestinatarioAndLeidaIsFalse(String destinatario);
	List<Notificacion> findByDestinatarioAndLeidaIsFalse(String destinatario);
}
