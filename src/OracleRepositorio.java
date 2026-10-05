public class OracleRepositorio implements RepositorioTransacciones {
    @Override
    public void guardar(Transaccion t) {
        System.out.println("[ORACLE] Conectando a jdbc:oracle:thin:@prod-db:1521/BANCO...");
        System.out.println("[ORACLE] INSERT INTO transacciones VALUES ('"
            + t.origen() + "', '" + t.destino() + "', " + t.monto() + ", " + t.comision() + ")");
    }
}
