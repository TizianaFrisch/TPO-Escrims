package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.HistorialUsuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialUsuarioRepository extends JpaRepository<HistorialUsuario, Long> {
    List<HistorialUsuario> findByUsuario(Usuario usuario);
}
