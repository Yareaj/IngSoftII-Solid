public class ComisionFija implements PoliticaComision {
    private final double valor;

    public ComisionFija(double valor) { this.valor = valor; }

    @Override
    public double calcular(double monto) { return valor; }
}
