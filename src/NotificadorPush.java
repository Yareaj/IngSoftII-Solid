/** R3: avisa al cliente con una notificación push en la app. */
public class NotificadorPush implements ObservadorTransaccion {
    @Override
    public void transaccionRealizada(Transaccion t) {
        System.out.println("[PUSH] Para " + t.titularOrigen() + ": Transferiste $"
            + t.monto() + " a la cuenta " + t.destino());
    }
}
