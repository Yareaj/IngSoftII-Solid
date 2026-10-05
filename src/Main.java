import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Cuenta ana = new CuentaAhorros("001-1", "Ana", 2_000_000);
        Cuenta luis = new CuentaAhorros("001-2", "Luis", 500_000);
        Cuenta cdtAna = new CDT("CDT-9", "Ana", 10_000_000, LocalDate.now().plusMonths(6));

        CatalogoComisiones comisiones = new CatalogoComisiones()
            .registrar("MISMO_BANCO", new SinComision())
            .registrar("OTRO_BANCO", new ComisionFija(7_500))
            .registrar("INTERNACIONAL", new ComisionPorcentual(0.03, 25_000));

        TransaccionService servicio = new TransaccionService(comisiones);
        servicio.transferir(ana, luis, 150_000, "OTRO_BANCO");

        new CobroCuotaManejo().cobrarMensual(List.of(ana, luis));

        List<ProductoBancario> productos =
            List.of(new TarjetaCredito(3_000_000), new CreditoVivienda(120_000_000));
        for (ProductoBancario p : productos) System.out.println(p.generarExtracto());
    }
}
