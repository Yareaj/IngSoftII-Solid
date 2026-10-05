/** Arma el mensaje para el cliente y lo envía por SMS. */
public class NotificadorSms {
    private final SmsGateway sms = new SmsGateway();

    public void notificar(Transaccion t) {
        sms.enviar(t.titularOrigen(),
            "Transferiste $" + t.monto() + " a la cuenta " + t.destino());
    }
}
