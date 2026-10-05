import java.util.ArrayList;
import java.util.List;

/** Doble de prueba: guarda las transacciones en una lista en vez de Oracle. */
public class RepositorioEnMemoria implements RepositorioTransacciones {
    public final List<Transaccion> guardadas = new ArrayList<>();

    @Override
    public void guardar(Transaccion t) { guardadas.add(t); }
}
