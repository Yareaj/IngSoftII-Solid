/** Un porcentaje del monto más un valor fijo (p. ej. transferencias internacionales). */
public class ComisionPorcentual implements PoliticaComision {
    private final double porcentaje;
    private final double fijo;

    public ComisionPorcentual(double porcentaje, double fijo) {
        this.porcentaje = porcentaje;
        this.fijo = fijo;
    }

    @Override
    public double calcular(double monto) { return monto * porcentaje + fijo; }
}
