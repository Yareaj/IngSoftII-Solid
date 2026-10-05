import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Cuenta para menores de edad (R2). Recibe depósitos sin límite, pero lo que
 * se retira en un mismo día no puede superar $200.000.
 *
 * Cumple el contrato de CuentaTransaccional.retirar: si el retiro supera el
 * límite del día se rechaza con IllegalStateException y el saldo no cambia.
 */
public class CuentaInfantil extends CuentaTransaccional {
    private static final double LIMITE_DIARIO = 200_000;

    private final Supplier<LocalDate> hoy;
    private LocalDate diaActual;
    private double retiradoHoy;

    public CuentaInfantil(String numero, String titular, double saldoInicial) {
        this(numero, titular, saldoInicial, LocalDate::now);
    }

    /** El reloj se puede cambiar para las pruebas. */
    public CuentaInfantil(String numero, String titular, double saldoInicial,
                          Supplier<LocalDate> hoy) {
        super(numero, titular, saldoInicial);
        this.hoy = hoy;
        this.diaActual = hoy.get();
    }

    public double getRetiradoHoy() {
        reiniciarSiCambioElDia();
        return retiradoHoy;
    }

    @Override
    public void retirar(double monto) {
        reiniciarSiCambioElDia();
        if (retiradoHoy + monto > LIMITE_DIARIO) {
            throw new IllegalStateException("Supera el límite diario de retiros de la cuenta infantil");
        }
        super.retirar(monto);      // si no hay saldo, lanza y no sumamos nada
        retiradoHoy += monto;
    }

    private void reiniciarSiCambioElDia() {
        LocalDate fecha = hoy.get();
        if (!fecha.equals(diaActual)) {
            diaActual = fecha;
            retiradoHoy = 0;
        }
    }
}
