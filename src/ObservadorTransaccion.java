/**
 * Algo que tiene que enterarse cada vez que una transacción termina bien
 * (notificaciones al cliente, auditoría, ...). Se llaman en el orden en que
 * se registran y solo si la transacción fue exitosa.
 */
public interface ObservadorTransaccion {
    void transaccionRealizada(Transaccion t);
}
