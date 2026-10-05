import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** R4: cada transferencia exitosa genera [AUDITORIA] y [ANTIFRAUDE]; una rechazada, ninguno. */
public class AntifraudeTest {
    private final PrintStream consolaOriginal = System.out;
    private final ByteArrayOutputStream consola = new ByteArrayOutputStream();
    private TransaccionService servicio;

    @Before
    public void armar() {
        System.setOut(new PrintStream(consola));
        servicio = new TransaccionService(new ValidadorMonto(),
            new CatalogoComisiones().registrar("MISMO_BANCO", new SinComision()),
            new RepositorioEnMemoria(), new ComprobanteEspia(),
            List.of(new Auditoria(), new SistemaAntifraude()));
    }

    @After
    public void restaurar() { System.setOut(consolaOriginal); }

    private long lineasQueEmpiezanCon(String prefijo) {
        return consola.toString().lines().filter(l -> l.startsWith(prefijo)).count();
    }

    @Test
    public void exitosaGeneraAuditoriaYAntifraude() {
        servicio.transferir(new CuentaAhorros("O", "Ana", 100_000),
                            new CuentaAhorros("D", "Luis", 0), 10_000, "MISMO_BANCO");
        assertEquals(1, lineasQueEmpiezanCon("[AUDITORIA]"));
        assertEquals(1, lineasQueEmpiezanCon("[ANTIFRAUDE]"));
    }

    @Test
    public void rechazadaNoGeneraNinguno() {
        assertThrows(IllegalStateException.class,
            () -> servicio.transferir(new CuentaAhorros("O", "Ana", 100),
                                      new CuentaAhorros("D", "Luis", 0), 10_000, "MISMO_BANCO"));
        assertEquals(0, lineasQueEmpiezanCon("[AUDITORIA]"));
        assertEquals(0, lineasQueEmpiezanCon("[ANTIFRAUDE]"));
    }
}
