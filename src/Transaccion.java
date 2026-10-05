/**
 * Datos de una transacción ya realizada. Es lo que se guarda, se imprime,
 * se notifica y se audita, para no tener que pasar seis parámetros sueltos.
 */
public record Transaccion(String tipo,
                          String origen,
                          String destino,
                          String titularOrigen,
                          double monto,
                          double comision) {
}
