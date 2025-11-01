package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ReporteConductaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeracionService {
    // Penaliza al usuario por abandono/no-show
    public void aplicarStrike(Usuario usuario) {
        int strikes = usuario.getStrikes() != null ? usuario.getStrikes() : 0;
        strikes++;
        usuario.setStrikes(strikes);
        if (strikes >= 3) {
            usuario.setCooldownHasta(java.time.LocalDateTime.now().plusDays(3)); // Ejemplo: 3 días de cooldown
        }
        // Guardar usuario actualizado en el repositorio
        // usuarioRepository.save(usuario); // Descomentar si tienes acceso al repo
    }

    // Verifica si el usuario tiene cooldown activo
    public boolean tieneCooldown(Usuario usuario) {
        return usuario.getCooldownHasta() != null && usuario.getCooldownHasta().isAfter(java.time.LocalDateTime.now());
    }
    private final ReporteConductaRepository reporteRepo;
    private final UsuarioRepository usuarioRepo;

    public ModeracionService(ReporteConductaRepository reporteRepo, UsuarioRepository usuarioRepo) {
        this.reporteRepo = reporteRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // Chain of Responsibility lite
    interface Handler { boolean handle(ReporteConducta r); }

    class BotHandler implements Handler {
        @Override public boolean handle(ReporteConducta r){
            String desc = (r.getMotivo() != null ? r.getMotivo().toString().toLowerCase() : "");
            if (desc.contains("hack") || desc.contains("cheat") || desc.contains("afk")){
                r.setEstado(EstadoReporte.APROBADO); r.setSancion("Advertencia automática");
                reporteRepo.save(r); return true;
            }
            return false;
        }
    }

    class ModeradorHandler implements Handler {
        @Override public boolean handle(ReporteConducta r){
            // fallback: leave as pendiente
            r.setEstado(EstadoReporte.PENDIENTE);
            reporteRepo.save(r); return true;
        }
    }

    private Handler chain(){
        Handler bot = new BotHandler();
        Handler mod = new ModeradorHandler();
        return r -> bot.handle(r) || mod.handle(r);
    }

    @Transactional
    public ReporteConducta crear(Long scrimId, Long reportadoId, String motivo){
        var r = new ReporteConducta();
        // scrim linked by id in controller; keep minimal here
        var reportado = usuarioRepo.findById(reportadoId).orElseThrow();
        r.setReportado(reportado);
        // Convertir String motivo a enum MotivoReporte si corresponde
        try {
            var motivoEnum = com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.MotivoReporte.valueOf(motivo.toUpperCase());
            r.setMotivo(motivoEnum);
        } catch (Exception e) {
            // Si no es un valor válido, guardar el texto original si el campo lo permite
            if (r.getClass().getDeclaredFields() != null) {
                // Si el campo motivo es String, usar el texto
                try {
                    java.lang.reflect.Field f = r.getClass().getDeclaredField("motivo");
                    if (f.getType().equals(String.class)) {
                        f.setAccessible(true);
                        f.set(r, motivo);
                    } else {
                        r.setMotivo(null);
                    }
                } catch (Exception ex) {
                    r.setMotivo(null);
                }
            } else {
                r.setMotivo(null);
            }
        }
        var saved = reporteRepo.save(r);
        chain().handle(saved);
        return saved;
    }

    @Transactional
    public ReporteConducta resolver(Long reporteId, EstadoReporte estado, String sancion){
        var r = reporteRepo.findById(reporteId).orElseThrow();
        if (estado != null) r.setEstado(estado);
        if (sancion != null) r.setSancion(sancion);
        return reporteRepo.save(r);
    }

    @Transactional(readOnly = true)
    public List<ReporteConducta> pendientes(){
        return reporteRepo.findByEstado(EstadoReporte.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<ReporteConducta> listarPorEstado(EstadoReporte estado){
        return estado != null ? reporteRepo.findByEstado(estado) : reporteRepo.findAll();
    }
}
