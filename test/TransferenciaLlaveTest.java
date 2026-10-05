import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

/** R1: una transferencia por LLAVE de $50.000 descuenta exactamente $50.000. */
public class TransferenciaLlaveTest {
    @Test
    public void transferenciaPorLlaveNoTieneComision() {
        CatalogoComisiones comisiones = new CatalogoComisiones()
            .registrar("LLAVE", new SinComision());
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        TransaccionService servicio = new TransaccionService(new ValidadorMonto(), comisiones,
            repositorio, new ComprobanteEspia(), List.of(new ObservadorEspia()));

        CuentaAhorros origen = new CuentaAhorros("O-1", "Ana", 1_000_000);
        CuentaAhorros destino = new CuentaAhorros("D-1", "Luis", 0);

        servicio.transferir(origen, destino, 50_000, "LLAVE");

        assertEquals(950_000, origen.getSaldo(), 0.001);
        assertEquals(50_000, destino.getSaldo(), 0.001);
        assertEquals(0, repositorio.guardadas.get(0).comision(), 0.001);
    }
}
