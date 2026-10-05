/** Producto en el que el cliente le debe plata al banco y la paga por cuotas. */
public interface ProductoCredito {
    double calcularIntereses();
    void pagarCuota(double monto);
}
