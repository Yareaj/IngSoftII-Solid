import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import org.junit.Test;

/** R2: cuenta infantil. */
public class CuentaInfantilTest {
    private LocalDate fecha = LocalDate.of(2026, 10, 5);

    private CuentaInfantil nueva(double saldo) {
        return new CuentaInfantil("INF-1", "Sofía", saldo, () -> fecha);
    }

    @Test
    public void criterioDeAceptacion_retiro60milDespuesDe150milSeRechaza() {
        CuentaInfantil cuenta = nueva(1_000_000);
        cuenta.retirar(150_000);

        assertThrows(IllegalStateException.class, () -> cuenta.retirar(60_000));
        assertEquals(850_000, cuenta.getSaldo(), 0.001);
    }

    @Test
    public void depositosSinLimite() {
        CuentaInfantil cuenta = nueva(0);
        cuenta.depositar(5_000_000);
        assertEquals(5_000_000, cuenta.getSaldo(), 0.001);
    }

    @Test
    public void alDiaSiguienteSeReiniciaElLimite() {
        CuentaInfantil cuenta = nueva(1_000_000);
        cuenta.retirar(200_000);
        fecha = fecha.plusDays(1);
        cuenta.retirar(200_000);
        assertEquals(600_000, cuenta.getSaldo(), 0.001);
    }

    @Test
    public void retiroRechazadoPorSaldoNoCuentaParaElLimite() {
        CuentaInfantil cuenta = nueva(50_000);
        assertThrows(IllegalStateException.class, () -> cuenta.retirar(100_000));
        assertEquals(0, cuenta.getRetiradoHoy(), 0.001);
    }

    @Test
    public void sirveComoOrigenDeTransferencias() {
        CuentaInfantil origen = nueva(500_000);
        CuentaAhorros destino = new CuentaAhorros("D-1", "Mamá", 0);
        TransaccionService servicio = new TransaccionService(new ValidadorMonto(),
            new CatalogoComisiones().registrar("MISMO_BANCO", new SinComision()),
            new RepositorioEnMemoria(), new ComprobanteEspia(), List.of());

        servicio.transferir(origen, destino, 100_000, "MISMO_BANCO");

        assertEquals(400_000, origen.getSaldo(), 0.001);
    }

    @Test
    public void transferenciaQueSuperaElLimiteSeRechaza() {
        CuentaInfantil origen = nueva(500_000);
        CuentaAhorros destino = new CuentaAhorros("D-1", "Mamá", 0);
        TransaccionService servicio = new TransaccionService(new ValidadorMonto(),
            new CatalogoComisiones().registrar("MISMO_BANCO", new SinComision()),
            new RepositorioEnMemoria(), new ComprobanteEspia(), List.of());

        assertThrows(IllegalStateException.class,
            () -> servicio.transferir(origen, destino, 250_000, "MISMO_BANCO"));
        assertEquals(500_000, origen.getSaldo(), 0.001);
    }

    @Test
    public void seLeCobraLaCuotaDeManejo() {
        CuentaInfantil cuenta = nueva(100_000);
        new CobroCuotaManejo().cobrarMensual(List.of(cuenta));
        assertEquals(100_000 - 12_900, cuenta.getSaldo(), 0.001);
    }

    @Test
    public void laCuotaSeCobraAunqueElNinoYaHayaRetiradoElMaximoDelDia() {
        CuentaInfantil cuenta = nueva(1_000_000);
        cuenta.retirar(200_000);   // el niño ya usó todo su límite hoy
        new CobroCuotaManejo().cobrarMensual(List.of(cuenta));   // esa noche corre el cobro
        assertEquals(1_000_000 - 200_000 - 12_900, cuenta.getSaldo(), 0.001);
    }
}
