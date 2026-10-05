/** Entrega al cliente el comprobante de una transacción. */
public interface Comprobante {
    void emitir(Transaccion t);
}
