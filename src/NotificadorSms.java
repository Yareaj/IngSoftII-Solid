/** Arma el mensaje para el cliente y lo envía por SMS. */
public class NotificadorSms implements ObservadorTransaccion {
    private final SmsGateway sms;

    public NotificadorSms(SmsGateway sms) { this.sms = sms; }

    @Override
    public void transaccionRealizada(Transaccion t) {
        sms.enviar(t.titularOrigen(),
            "Transferiste $" + t.monto() + " a la cuenta " + t.destino());
    }
}
