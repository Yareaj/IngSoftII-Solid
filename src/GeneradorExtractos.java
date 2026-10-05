import java.util.List;

/** Imprime el extracto de cualquier producto: cuentas, tarjetas o créditos. */
public class GeneradorExtractos {
    public void imprimir(List<? extends ConExtracto> productos) {
        for (ConExtracto p : productos) {
            System.out.println(p.generarExtracto());
        }
    }
}
