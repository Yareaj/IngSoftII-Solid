import java.util.HashMap;
import java.util.Map;

/**
 * Relaciona cada tipo de transferencia con su política de comisión.
 * Los tipos se registran desde afuera (Main), así que agregar uno nuevo
 * no obliga a editar esta clase ni TransaccionService.
 */
public class CatalogoComisiones {
    private final Map<String, PoliticaComision> politicas = new HashMap<>();

    public CatalogoComisiones registrar(String tipo, PoliticaComision politica) {
        politicas.put(tipo, politica);
        return this;
    }

    public double calcular(String tipo, double monto) {
        PoliticaComision politica = politicas.get(tipo);
        if (politica == null) {
            throw new IllegalArgumentException("Tipo de transferencia desconocido");
        }
        return politica.calcular(monto);
    }
}
