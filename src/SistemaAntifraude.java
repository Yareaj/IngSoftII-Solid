/** R4: envía cada transacción exitosa al sistema antifraude del banco (regulatorio). */
public class SistemaAntifraude implements ObservadorTransaccion {
    @Override
    public void transaccionRealizada(Transaccion t) {
        System.out.println("[ANTIFRAUDE] Analizando " + t.tipo() + " " + t.origen()
            + " -> " + t.destino() + " $" + t.monto() + " (comisión $" + t.comision() + ")");
    }
}
