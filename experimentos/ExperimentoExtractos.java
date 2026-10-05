import java.time.LocalDate;
import java.util.List;

// Pregunta de control I: un solo generador para cuentas, CDT, tarjetas y créditos.
public class ExperimentoExtractos {
    public static void main(String[] args) {
        List<ConExtracto> todos = List.of(
            new CuentaAhorros("001-1", "Ana", 2_000_000),
            new CDT("CDT-9", "Ana", 10_000_000, LocalDate.now().plusMonths(6)),
            new TarjetaCredito(3_000_000),
            new CreditoVivienda(120_000_000));
        new GeneradorExtractos().imprimir(todos);
    }
}
