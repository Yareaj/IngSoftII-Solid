/**
 * Lo que tienen en común todas las cuentas del banco: número, titular, saldo
 * y que pueden recibir dinero. No todas permiten retirar (ver CuentaTransaccional).
 */
public abstract class Cuenta implements ConExtracto {
    protected final String numero;
    protected final String titular;
    protected double saldo;

    protected Cuenta(String numero, String titular, double saldoInicial) {
        this.numero = numero;
        this.titular = titular;
        this.saldo = saldoInicial;
    }

    public String getNumero() { return numero; }
    public String getTitular() { return titular; }
    public double getSaldo() { return saldo; }

    public void depositar(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("Monto inválido");
        saldo += monto;
    }

    @Override
    public String generarExtracto() { return "Cuenta " + numero + " - saldo: $" + saldo; }

    /** Descuenta del saldo si alcanza. Solo para uso de las subclases. */
    protected void debitar(double monto) {
        if (monto > saldo) throw new IllegalStateException("Saldo insuficiente");
        saldo -= monto;
    }
}
