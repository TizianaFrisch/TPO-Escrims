package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ReporteConductaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeracionService {
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
            String desc = (r.getMotivo() != null ? r.getMotivo().toLowerCase() : "");
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
        r.setReportado(reportado); r.setMotivo(motivo);
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
