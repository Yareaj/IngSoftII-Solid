# Diagrama de clases — código original (bloque 1)

Las clases con borde rojo y las relaciones marcadas con ⚠ son las que consideramos problemáticas.

```mermaid
classDiagram
    direction LR

    class Cuenta {
        #String numero
        #String titular
        #double saldo
        +depositar(double)
        +retirar(double)
    }
    class CuentaAhorros
    class CDT {
        -LocalDate vencimiento
        +retirar(double) lanza UnsupportedOperationException
    }
    class TransaccionService {
        -OracleRepositorio repositorio
        -SmsGateway sms
        +transferir(Cuenta, Cuenta, double, String tipo)
    }
    class OracleRepositorio {
        +guardarTransaccion(...)
    }
    class SmsGateway {
        +enviar(String, String)
    }
    class CobroCuotaManejo {
        +cobrarMensual(List~Cuenta~)
    }
    class ProductoBancario {
        <<interface>>
        +depositar(double)
        +retirar(double)
        +calcularIntereses() double
        +pagarCuota(double)
        +generarExtracto() String
    }
    class TarjetaCredito {
        +depositar(double) vacío
    }
    class CreditoVivienda {
        +depositar(double) vacío
        +retirar(double) vacío
    }
    class Main

    Cuenta <|-- CuentaAhorros
    Cuenta <|-- CDT : ⚠ LSP — no puede retirar
    TransaccionService ..> OracleRepositorio : ⚠ new (DIP)
    TransaccionService ..> SmsGateway : ⚠ new (DIP)
    TransaccionService ..> Cuenta
    CobroCuotaManejo ..> Cuenta : ⚠ recibe CDT y explota
    ProductoBancario <|.. TarjetaCredito : ⚠ ISP
    ProductoBancario <|.. CreditoVivienda : ⚠ ISP
    Main ..> TransaccionService : new
    Main ..> CobroCuotaManejo : new
    Main ..> CuentaAhorros : new
    Main ..> CDT : new
    Main ..> TarjetaCredito : new
    Main ..> CreditoVivienda : new

    classDef problema stroke:#d00,stroke-width:3px,color:#d00
    class TransaccionService:::problema
    class CDT:::problema
    class ProductoBancario:::problema
    class CreditoVivienda:::problema
    class TarjetaCredito:::problema
```

Notas:
- `TransaccionService` está en rojo porque concentra siete responsabilidades (SRP) y tiene el
  `switch` por tipo (OCP), además de crear sus dependencias (DIP).
- La herencia `Cuenta <|-- CDT` es la que rompe LSP.
- `Main` crea todo con `new`, pero eso no lo marcamos: que el programa principal arme el sistema
  está bien, lo malo es que `TransaccionService` también lo haga.
