import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TransaccionServiceTest {
    private RepositorioEnMemoria repositorio;
    private ComprobanteEspia comprobante;
    private ObservadorEspia notificaciones;
    private TransaccionService servicio;

    private CuentaAhorros origen;
    private CuentaAhorros destino;

    @Before
    public void armar() {
        repositorio = new RepositorioEnMemoria();
        comprobante = new ComprobanteEspia();
        notificaciones = new ObservadorEspia();

        CatalogoComisiones comisiones = new CatalogoComisiones()
            .registrar("MISMO_BANCO", new SinComision())
            .registrar("OTRO_BANCO", new ComisionFija(7_500))
            .registrar("INTERNACIONAL", new ComisionPorcentual(0.03, 25_000));

        servicio = new TransaccionService(new ValidadorMonto(), comisiones,
                                          repositorio, comprobante, List.of(notificaciones));

        origen = new CuentaAhorros("O-1", "Ana", 1_000_000);
        destino = new CuentaAhorros("D-1", "Luis", 200_000);
    }

    // 1
    @Test
    public void mismoBancoNoCobraComisionYMueveExactamenteElMonto() {
        servicio.transferir(origen, destino, 300_000, "MISMO_BANCO");

        assertEquals(700_000, origen.getSaldo(), 0.001);
        assertEquals(500_000, destino.getSaldo(), 0.001);
        assertEquals(0, repositorio.guardadas.get(0).comision(), 0.001);
    }

    // 2
    @Test
    public void otroBancoCobra7500YDescuentaMontoMasComision() {
        servicio.transferir(origen, destino, 100_000, "OTRO_BANCO");

        assertEquals(7_500, repositorio.guardadas.get(0).comision(), 0.001);
        assertEquals(1_000_000 - 100_000 - 7_500, origen.getSaldo(), 0.001);
        assertEquals(300_000, destino.getSaldo(), 0.001); // el destino recibe solo el monto
    }

    // 3
    @Test
    public void saldoInsuficienteSeRechazaYNoSeGuardaNiSeNotifica() {
        assertThrows(IllegalStateException.class,
            () -> servicio.transferir(origen, destino, 995_000, "OTRO_BANCO"));

        assertTrue(repositorio.guardadas.isEmpty());
        assertTrue(comprobante.emitidos.isEmpty());
        assertTrue(notificaciones.recibidas.isEmpty());
        assertEquals(1_000_000, origen.getSaldo(), 0.001);
        assertEquals(200_000, destino.getSaldo(), 0.001);
    }

    // 4
    @Test
    public void cadaTransferenciaExitosaSeGuardaUnaVezYNotificaUnaVez() {
        servicio.transferir(origen, destino, 10_000, "MISMO_BANCO");
        assertEquals(1, repositorio.guardadas.size());
        assertEquals(1, notificaciones.recibidas.size());

        servicio.transferir(origen, destino, 20_000, "OTRO_BANCO");
        assertEquals(2, repositorio.guardadas.size());
        assertEquals(2, notificaciones.recibidas.size());
        // y la que se notifica es la misma que se guardó
        assertEquals(repositorio.guardadas.get(1), notificaciones.recibidas.get(1));
    }

    // 5
    @Test
    public void tipoDesconocidoSeRechazaYElSaldoNoCambia() {
        assertThrows(IllegalArgumentException.class,
            () -> servicio.transferir(origen, destino, 50_000, "CRIPTO"));

        assertEquals(1_000_000, origen.getSaldo(), 0.001);
        assertTrue(repositorio.guardadas.isEmpty());
        assertTrue(notificaciones.recibidas.isEmpty());
    }
}
