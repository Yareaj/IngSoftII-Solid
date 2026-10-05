import java.util.ArrayList;
import java.util.List;

/** Doble de prueba: anota las notificaciones en vez de enviar SMS. */
public class ObservadorEspia implements ObservadorTransaccion {
    public final List<Transaccion> recibidas = new ArrayList<>();

    @Override
    public void transaccionRealizada(Transaccion t) { recibidas.add(t); }
}
