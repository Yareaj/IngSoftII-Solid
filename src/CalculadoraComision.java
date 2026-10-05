/** Calcula la comisión según el tipo de transferencia. */
public class CalculadoraComision {
    public double calcular(String tipo, double monto) {
        return switch (tipo) {
            case "MISMO_BANCO" -> 0;
            case "OTRO_BANCO" -> 7_500;
            case "INTERNACIONAL" -> monto * 0.03 + 25_000;
            default -> throw new IllegalArgumentException("Tipo de transferencia desconocido");
        };
    }
}
