import java.time.LocalDateTime;

/** Deja el registro de auditoría de cada transacción. */
public class Auditoria {
    public void registrar(Transaccion t) {
        System.out.println("[AUDITORIA] " + LocalDateTime.now() + " " + t.tipo()
            + " " + t.origen() + " -> " + t.destino() + " $" + t.monto());
    }
}
