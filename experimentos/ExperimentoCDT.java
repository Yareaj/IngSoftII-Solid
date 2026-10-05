import java.time.LocalDate;
import java.util.List;

// Experimento 1 del bloque 1: ¿qué pasa si el cobro de cuota incluye un CDT?
public class ExperimentoCDT {
    public static void main(String[] args) {
        Cuenta ana = new CuentaAhorros("001-1", "Ana", 2_000_000);
        Cuenta luis = new CuentaAhorros("001-2", "Luis", 500_000);
        Cuenta cdtAna = new CDT("CDT-9", "Ana", 10_000_000, LocalDate.now().plusMonths(6));
        Cuenta pedro = new CuentaAhorros("001-3", "Pedro", 800_000);

        new CobroCuotaManejo().cobrarMensual(List.of(ana, cdtAna, luis, pedro));
    }
}
