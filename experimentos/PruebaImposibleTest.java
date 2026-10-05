import static org.junit.Assert.assertEquals;
import org.junit.Test;

// Experimento 2 del bloque 1: intentamos probar la comisión de OTRO_BANCO
// sin conectarnos a Oracle ni mandar SMS.
public class PruebaImposibleTest {
    @Test
    public void transferenciaOtroBancoCobra7500() {
        Cuenta origen = new CuentaAhorros("T-1", "Prueba", 1_000_000);
        Cuenta destino = new CuentaAhorros("T-2", "Prueba2", 0);

        // No hay forma de pasarle un repositorio o un SMS falso:
        // TransaccionService los crea con new por dentro.
        new TransaccionService().transferir(origen, destino, 100_000, "OTRO_BANCO");

        // La comisión no se devuelve; solo la podemos deducir del saldo.
        assertEquals(1_000_000 - 100_000 - 7_500, origen.getSaldo(), 0.001);
    }
}
