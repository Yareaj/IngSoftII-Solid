import java.util.ArrayList;
import java.util.List;

/** Doble de prueba: anota los comprobantes en vez de imprimirlos. */
public class ComprobanteEspia implements Comprobante {
    public final List<Transaccion> emitidos = new ArrayList<>();

    @Override
    public void emitir(Transaccion t) { emitidos.add(t); }
}
