/** Imprime el comprobante que ve el cliente. */
public class ImpresoraComprobante {
    public void imprimir(Transaccion t) {
        System.out.println("===== BANCO ANDINO - COMPROBANTE =====");
        System.out.println("Origen: " + t.origen());
        System.out.println("Destino: " + t.destino());
        System.out.println("Monto: $" + t.monto());
        System.out.println("Comisión: $" + t.comision());
        System.out.println("======================================");
    }
}
