// service/AuditLogService.java
@Service
public class AuditLogService {
    private final ObjectMapper om = new ObjectMapper();
    private final LogAuditoriaRepository repo;
    public void log(String entidad, Long id, String accion, String usuario, Object detalles){
        var l = new LogAuditoria();
        l.setEntidad(entidad); l.setEntidadId(id); l.setAccion(accion); l.setUsuario(usuario);
        try { l.setDetallesJson(om.writeValueAsString(detalles)); } catch(Exception ignored){}
        repo.save(l);
    }
}