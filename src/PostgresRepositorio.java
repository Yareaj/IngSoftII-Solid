/** R5: guarda las transacciones en PostgreSQL. OracleRepositorio se conserva para poder devolverse. */
public class PostgresRepositorio implements RepositorioTransacciones {
    @Override
    public void guardar(Transaccion t) {
        System.out.println("[POSTGRES] Conectando a jdbc:postgresql://prod-db:5432/banco...");
        System.out.println("[POSTGRES] INSERT INTO transacciones VALUES ('"
            + t.origen() + "', '" + t.destino() + "', " + t.monto() + ", " + t.comision() + ")");
    }
}
