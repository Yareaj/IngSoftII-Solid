import java.util.List;

/**
 * Coordina una transferencia: le pide a cada pieza que haga su parte, en orden.
 * No crea ninguna de sus dependencias; se las entregan por el constructor.
 */
public class TransaccionService {
    private final ValidadorMonto validador;
    private final CatalogoComisiones comisiones;
    private final RepositorioTransacciones repositorio;
    private final Comprobante comprobante;
    private final List<ObservadorTransaccion> observadores;

    public TransaccionService(ValidadorMonto validador,
                              CatalogoComisiones comisiones,
                              RepositorioTransacciones repositorio,
                              Comprobante comprobante,
                              List<ObservadorTransaccion> observadores) {
        this.validador = validador;
        this.comisiones = comisiones;
        this.repositorio = repositorio;
        this.comprobante = comprobante;
        this.observadores = List.copyOf(observadores);
    }

    public void transferir(CuentaTransaccional origen, Cuenta destino, double monto, String tipo) {
        validador.validar(monto);
        double comision = comisiones.calcular(tipo, monto);

        origen.retirar(monto + comision);
        destino.depositar(monto);

        Transaccion t = new Transaccion(tipo, origen.getNumero(), destino.getNumero(),
                                        origen.getTitular(), monto, comision);
        repositorio.guardar(t);
        comprobante.emitir(t);
        observadores.forEach(o -> o.transaccionRealizada(t));
    }
}
