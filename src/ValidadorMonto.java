/** Reglas de validación del monto de una transacción. */
public class ValidadorMonto {
    private static final double TOPE = 5_000_000;

    public void validar(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("Monto inválido");
        if (monto > TOPE) throw new IllegalArgumentException("Supera el tope diario");
    }
}
