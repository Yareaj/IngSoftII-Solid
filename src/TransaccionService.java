/**
 * Coordina una transferencia: le pide a cada pieza que haga su parte, en orden.
 */
public class TransaccionService {
    private final ValidadorMonto validador = new ValidadorMonto();
    private final CatalogoComisiones comisiones;
    private final OracleRepositorio repositorio = new OracleRepositorio();
    private final ImpresoraComprobante comprobante = new ImpresoraComprobante();
    private final NotificadorSms notificador = new NotificadorSms();
    private final Auditoria auditoria = new Auditoria();

    public TransaccionService(CatalogoComisiones comisiones) {
        this.comisiones = comisiones;
    }

    public void transferir(CuentaTransaccional origen, Cuenta destino, double monto, String tipo) {
        validador.validar(monto);
        double comision = comisiones.calcular(tipo, monto);

        origen.retirar(monto + comision);
        destino.depositar(monto);

        Transaccion t = new Transaccion(tipo, origen.getNumero(), destino.getNumero(),
                                        origen.getTitular(), monto, comision);
        repositorio.guardarTransaccion(t.origen(), t.destino(), t.monto(), t.comision());
        comprobante.imprimir(t);
        notificador.notificar(t);
        auditoria.registrar(t);
    }
}
