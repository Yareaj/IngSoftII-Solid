/** Dónde se guardan las transacciones. A TransaccionService no le importa cuál base de datos es. */
public interface RepositorioTransacciones {
    void guardar(Transaccion t);
}
