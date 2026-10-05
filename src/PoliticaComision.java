/** Regla para calcular la comisión de un tipo de transacción. */
public interface PoliticaComision {
    double calcular(double monto);
}
