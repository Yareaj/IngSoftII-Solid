import java.time.LocalDate;

/**
 * Certificado de Depósito a Término. Es una Cuenta (tiene saldo y titular),
 * pero NO una CuentaTransaccional: no se puede usar como origen de
 * transferencias ni se le cobra cuota de manejo, y el compilador lo impide.
 *
 * Al vencimiento el cliente puede redimirlo. Esa es una operación propia del
 * CDT, con su propia regla, no un "retirar" heredado que a veces explota.
 */
public class CDT extends Cuenta {
    private final LocalDate vencimiento;

    public CDT(String numero, String titular, double monto, LocalDate vencimiento) {
        super(numero, titular, monto);
        this.vencimiento = vencimiento;
    }

    public LocalDate getVencimiento() { return vencimiento; }

    public boolean estaVencido() {
        return !LocalDate.now().isBefore(vencimiento);
    }

    public void redimir(double monto) {
        if (!estaVencido()) {
            throw new IllegalStateException("Un CDT no permite retiros antes del vencimiento");
        }
        debitar(monto);
    }
}
