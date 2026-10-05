import java.time.LocalDateTime;

/** Deja el registro de auditoría de cada transacción. */
public class Auditoria implements ObservadorTransaccion {
    @Override
    public void transaccionRealizada(Transaccion t) {
        System.out.println("[AUDITORIA] " + LocalDateTime.now() + " " + t.tipo()
            + " " + t.origen() + " -> " + t.destino() + " $" + t.monto());
    }
}
