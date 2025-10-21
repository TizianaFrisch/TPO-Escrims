// model/LogAuditoria.java
@Entity
public class LogAuditoria {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    String entidad; Long entidadId; String accion; String usuario;
    LocalDateTime timestamp = LocalDateTime.now();
    @Lob String detallesJson;
}



// Ejemplos de uso
audit.log("Scrim", scrimId, "cambio_estado", currentUser, Map.of("from", prev, "to", next));
        audit.log("ReporteConducta", rep.getId(), "resolucion", modUser, Map.of("resultado", "no_show"));
