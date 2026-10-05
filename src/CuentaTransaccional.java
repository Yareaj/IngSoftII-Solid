/**
 * Cuenta de la que el cliente puede sacar dinero en cualquier momento:
 * sirve como origen de transferencias y se le cobra la cuota de manejo.
 *
 * Contrato de retirar(monto):
 *  - si la operación se permite, el saldo baja exactamente en "monto";
 *  - si no se permite (saldo insuficiente u otra regla del producto), lanza
 *    IllegalStateException y el saldo NO cambia.
 * Quien use una CuentaTransaccional debe estar preparado para ese rechazo,
 * pero nunca para un "esta cuenta no sabe retirar".
 */
public abstract class CuentaTransaccional extends Cuenta {

    protected CuentaTransaccional(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    public void retirar(double monto) {
        debitar(monto);
    }

    /**
     * Cargo que cobra el banco (p. ej. la cuota de manejo). No es un retiro
     * del cliente, así que no le aplican las reglas de retiro de cada producto
     * (como el límite diario de la cuenta infantil); solo necesita saldo.
     * Es final para que ninguna subclase lo pueda volver a restringir.
     */
    public final void cobrarCargo(double monto) {
        debitar(monto);
    }
}
