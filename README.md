# Laboratorio L2 — SOLID: el backend de Banco Andino

Ingeniería de Software II · Universidad Nacional de Colombia · 2026-2

**Lenguaje elegido:** Java 21 (el mismo del código base, para no introducir ruido al traducir).
**Pruebas:** JUnit 4.13.2 (los `.jar` están en `lib/`, así el repo compila sin Maven ni Gradle).

## Cómo ejecutar

```bash
./run.sh            # compila y ejecuta Main
./check.sh          # prueba de caracterización: compara contra salida_original.txt
./test.sh           # pruebas unitarias (desde el bloque 3)
```

## Bloque 0 — Arranque

Copiamos el código base sin cambiar nada (solo cambiamos las comillas tipográficas `’` del
`INSERT` de `OracleRepositorio` por comillas simples normales, porque así venían en el PDF).
La salida quedó guardada en `salida_original.txt`.

Lo primero que notamos leyendo el flujo de una transferencia: `transferir` hace **todo** —
valida, calcula comisión, mueve plata, guarda, imprime, manda SMS y audita— en un solo
método. Y el CDT de Ana se crea en `Main` pero nunca se usa (sospechoso…).

Como el PDF sugiere `diff`, escribimos `check.sh`, que hace el diff pero reemplaza la
fecha/hora de la auditoría por `<FECHA>`, para que el diff solo falle si cambió algo de verdad.

## Bloque 1 — Diagnóstico

### 1.1 Tabla de hallazgos

| # | Clase / método | Letra | Evidencia en el código | Consecuencia para el banco o el cliente |
|---|---|---|---|---|
| 1 | `TransaccionService.transferir` | **S** | Un solo método de 36 líneas con 7 bloques comentados: validación, comisión, movimiento de dinero, persistencia, comprobante, SMS y auditoría. | Si legal pide cambiar el texto del comprobante, o marketing el texto del SMS, hay que tocar el mismo método que mueve la plata. Un error de tipeo ahí puede cobrar mal una transferencia o dejar de guardarla. Además varias personas no pueden trabajar a la vez en esa clase sin pisarse. |
| 2 | `CobroCuotaManejo.cobrarMensual` | **S** | Cobra la cuota y además decide cómo se informa el cobro (`System.out.println`). | Si el reporte del cobro tiene que ir a un archivo o a otra parte hay que tocar la lógica del cobro. |
| 3 | `TransaccionService.transferir` (`switch (tipo)`) | **O** | Cada tipo de transferencia es un `case` con un `String`. | Agregar un tipo (p. ej. transferencias por llave) obliga a abrir y editar la clase más delicada del sistema y volver a probar todas las transferencias que ya funcionaban. Si alguien escribe `"OTRO BANCO"` en vez de `"OTRO_BANCO"` el error solo aparece en ejecución. |
| 4 | `TransaccionService.transferir` (notificación y auditoría fijas) | **O** | El SMS y la auditoría están escritos "a mano" dentro del método. | Si llega un canal nuevo (push, correo) o un sistema nuevo que necesite enterarse de cada transacción, hay que editar el método otra vez. |
| 5 | `CDT extends Cuenta`, `CDT.retirar` | **L** | `CDT` hereda `retirar` pero lo sobrescribe para lanzar `UnsupportedOperationException` antes del vencimiento. Un `CDT` no se puede usar en todos los lugares donde se espera una `Cuenta`. | Cualquier código que reciba una `Cuenta` (cobro de cuota, transferencias) puede explotar en ejecución. Ver experimento 1: el proceso nocturno de cobro se cae a la mitad. |
| 6 | `TransaccionService.transferir` con `CDT` como origen | **L** | El tipo `Cuenta` del parámetro deja pasar un `CDT`. | Una transferencia desde un CDT compila perfecto y solo falla en producción, frente al cliente. |
| 7 | `TarjetaCredito.depositar`, `CreditoVivienda.depositar`, `CreditoVivienda.retirar` | **L** | Métodos vacíos con comentario `// no aplica`: no hacen nada **y no avisan**. | Si un desarrollador (o un cajero de la app) llama `depositar` sobre el crédito de vivienda creyendo que está abonando, el dinero "se consigna" y desaparece sin error. Es peor que lanzar excepción porque nadie se entera. |
| 8 | `ProductoBancario` | **I** | Interfaz con 5 métodos que mezcla cosas de cuentas (depositar/retirar) con cosas de crédito (intereses, pagar cuota) y de reporte (extracto). | Tarjeta y crédito están obligados a implementar operaciones que no tienen. El generador de extractos solo necesita `generarExtracto()`, pero depende de toda la interfaz: si cambia la firma de `pagarCuota`, se recompila el generador de extractos sin razón. |
| 9 | `TransaccionService` (`new OracleRepositorio()`, `new SmsGateway()`) | **D** | La lógica de negocio crea directamente las clases concretas de infraestructura. | No se puede cambiar Oracle por otra base de datos (o por un doble de prueba) sin editar la clase. Toda prueba de `transferir` escribe en la base de producción y le manda un SMS real a un cliente (experimento 2). |
| 10 | `CobroCuotaManejo` → `Cuenta` concreta | **D** | Depende de la clase concreta `Cuenta` con todo su comportamiento, cuando solo necesita "algo a lo que se le pueda cobrar". | Por eso termina aceptando un CDT (está muy relacionado con el hallazgo 5). |

**Otras cosas que vimos y que no son exactamente de SOLID** (las anotamos, pero no las
corregimos, porque la regla 3 dice que el comportamiento no puede cambiar):

- `Cuenta.retirar` no valida que el monto sea positivo: `retirar(-100_000)` **aumenta** el saldo.
- El mensaje dice "Supera el tope diario" pero el código compara contra el monto de **una**
  transferencia, no contra lo acumulado en el día. O el mensaje está mal o el control está mal.
- Se usa `double` para dinero (por eso el extracto imprime `$1.2E8`). En un banco real
  debería ser `BigDecimal` o centavos en `long`.
- `TarjetaCredito.pagarCuota` permite pagar más de la deuda y dejarla negativa.
- Si `destino.depositar` fallara después de `origen.retirar`, la plata ya salió del origen y no
  hay rollback. No es un problema de SOLID, es de transaccionalidad.

### 1.2 Experimentos

**Experimento 1 — el CDT.** No quisimos editar `Main` y arriesgarnos a dejar el cambio en el
commit, así que lo hicimos en `experimentos/ExperimentoCDT.java`: cobramos la cuota a
`[ana, cdtAna, luis, pedro]`. Salida (`experimentos/salida_experimento_cdt.txt`):

```
Cuota de manejo cobrada a 001-1
Exception in thread "main" java.lang.UnsupportedOperationException: Un CDT no permite retiros antes del vencimiento
	at CDT.retirar(CDT.java:14)
	at CobroCuotaManejo.cobrarMensual(CobroCuotaManejo.java:8)
```

A Ana se le cobra, el programa explota en el CDT y **a Luis y a Pedro nunca se les cobra**.
En producción, con un millón de cuentas y el CDT en la posición 500 000: las primeras 499 999
cuentas quedan cobradas, las otras 500 000 no, y el proceso termina con error. A la mañana
siguiente alguien tiene que averiguar hasta dónde llegó, y si simplemente lo vuelve a correr
**les cobra doble a las primeras 499 999** (no hay forma de saber cuáles ya se cobraron, porque
el cobro ni siquiera se guarda). Además el banco deja de recibir la mitad del ingreso por
cuotas ese mes.

**Experimento 2 — la prueba imposible.** Escribimos `experimentos/PruebaImposibleTest.java`
(JUnit). La prueba técnicamente "pasa", pero mirando la salida
(`experimentos/salida_prueba_imposible.txt`):

```
[ORACLE] Conectando a jdbc:oracle:thin:@prod-db:1521/BANCO...
[ORACLE] INSERT INTO transacciones VALUES ('T-1', 'T-2', 100000.0, 7500.0)
...
[SMS] Para Prueba: Transferiste $100000.0 a la cuenta T-2
```

O sea, **no cumplimos la condición**: insertamos una transacción falsa en la base de
producción y mandamos un SMS. No lo logramos porque `TransaccionService` crea
`OracleRepositorio` y `SmsGateway` con `new` dentro de la clase, así que no hay por dónde
meterle una versión falsa. Tampoco hay un método que devuelva la comisión: la tuvimos que
deducir del saldo final, o sea que la prueba depende de que el movimiento de dinero también
funcione (no prueba una sola cosa).

(Pensamos en heredar de `TransaccionService` para sobrescribir… pero los campos son
`private final` y se inicializan en la declaración, así que tampoco.)

### 1.3 Medición "antes"

| Métrica | Antes |
|---|---|
| Líneas del método `transferir` | 36 (desde la firma hasta la llave de cierre) |
| Razones distintas por las que `TransaccionService` podría cambiar | 7 (reglas de validación/tope, tarifas de comisión, forma de mover el dinero, base de datos, formato del comprobante, canal de notificación, auditoría) |
| Clases concretas que `TransaccionService` crea con `new` | 2 (`OracleRepositorio`, `SmsGateway`) |
| Métodos vacíos o que lanzan excepción por "no aplica" | 4 (`TarjetaCredito.depositar`, `CreditoVivienda.depositar`, `CreditoVivienda.retirar`, `CDT.retirar`) |
| ¿Se puede probar `transferir` sin Oracle ni SMS? | No |

Sobre `CDT.retirar`: dudamos si contarlo porque sí retira después del vencimiento. Lo contamos
porque durante toda la vida útil del CDT lanza "no aplica", que es justo el problema.

### 1.4 Diagrama de clases del código original

Ver [`docs/uml-antes.md`](docs/uml-antes.md).

## Bloque 2 — Refactorización

En cada punto de control corrimos `./check.sh` (diff contra `salida_original.txt` ignorando la
fecha de auditoría) antes de hacer el commit. Los cinco dieron `OK: el comportamiento no cambió`.

### Punto de control S

Partimos `transferir` en una clase por responsabilidad:

| Responsabilidad original (comentario del código) | Ahora vive en |
|---|---|
| 1. Validación | `ValidadorMonto` |
| 2. Cálculo de la comisión | `CalculadoraComision` (el `switch` se mudó aquí tal cual; lo arreglamos en O) |
| 3. Movimiento del dinero | se queda en `TransaccionService` (es justo lo que coordina) |
| 4. Persistencia | `OracleRepositorio` (ya existía) |
| 5. Comprobante | `ImpresoraComprobante` |
| 6. Notificación | `NotificadorSms` (arma el texto) + `SmsGateway` (lo envía) |
| 7. Auditoría | `Auditoria` |

También creamos el record `Transaccion` para no pasar `origen, destino, monto, comision, tipo,
titular` sueltos a todas las clases.

A propósito **todavía** dejamos los `new` dentro de `TransaccionService`: queríamos un commit
que solo separara responsabilidades, para que el diff se pudiera leer. Eso es D.

**Pregunta de control.** *¿Qué hace `TransaccionService`, en una frase?* "Coordina los pasos de
una transferencia." Al principio escribimos "valida, cobra la comisión, mueve el dinero **y**
avisa…" y nos dimos cuenta de que estábamos describiendo lo que hacen las otras clases; lo
que hace ella es el orden. Nos quedó la duda de si "mover el dinero" (`origen.retirar` +
`destino.depositar`) es otra responsabilidad: decidimos que no, porque son dos líneas que *son*
la transferencia, y sacarlas a otra clase dejaría a `TransaccionService` vacía.

*Si legal pide cambiar el formato del comprobante, ¿qué archivo tocan?* Solo
`ImpresoraComprobante.java`.

### Punto de control O

Reemplazamos el `switch` por la interfaz `PoliticaComision` con tres implementaciones
(`SinComision`, `ComisionFija`, `ComisionPorcentual`) y un `CatalogoComisiones` que relaciona el
nombre del tipo (`"OTRO_BANCO"`) con su política. El catálogo se arma en `Main` y se le pasa a
`TransaccionService` por el constructor.

Primero pensamos en hacer una clase por tipo (`ComisionOtroBanco`, `ComisionInternacional`…),
pero vimos que `OTRO_BANCO` es simplemente "una tarifa fija de 7.500", así que preferimos
clases por **forma de cobrar** con el valor como parámetro. Así, si el banco sube la tarifa,
se cambia un número en `Main`, no una clase.

Algo que no esperábamos: para que agregar un tipo nuevo solo toque `Main`, el catálogo tenía
que venir de afuera. O sea que en este punto ya tuvimos que inyectar una dependencia por
constructor (adelantándonos un poco a D). Las demás las dejamos con `new` hasta D.

Mantuvimos el tipo como `String` (y no un `enum`) porque un `enum` volvería a obligar a editar
un archivo existente cada vez que llega un tipo. El costo es que un tipo mal escrito solo se
detecta en ejecución ("Tipo de transferencia desconocido"), igual que antes.

**Pregunta de control.** *Si mañana llega un tipo nuevo, ¿qué archivos existentes hay que
modificar?*

- Si su comisión es de una forma que ya existe (gratis, fija o porcentaje + fijo): **solo
  `Main.java`** (una línea `.registrar(...)`). Cero archivos nuevos.
- Si es una forma nueva de calcular (p. ej. por rangos de monto): un archivo nuevo que
  implemente `PoliticaComision` y una línea en `Main.java`. Ningún otro existente.

### Punto de control L

El problema era que `CDT extends Cuenta` heredaba una promesa ("me puedes retirar") que no
puede cumplir. Lo resolvimos separando dos ideas que estaban mezcladas en `Cuenta`:

- `Cuenta` (ahora abstracta): número, titular, saldo y `depositar`. Todo producto de depósito
  lo tiene, incluido el CDT.
- `CuentaTransaccional extends Cuenta`: agrega `retirar`. Es lo que piden
  `TransaccionService` (como origen) y `CobroCuotaManejo`.
- `CuentaAhorros extends CuentaTransaccional`.
- `CDT extends Cuenta` (no transaccional). Para no perder lo que el CDT sí permitía —sacar
  la plata al vencimiento— le dimos una operación propia, `redimir`, que solo existe en `CDT`.

En `CuentaTransaccional` dejamos escrito el **contrato** de `retirar` en el Javadoc: puede
rechazar la operación con `IllegalStateException` sin cambiar el saldo (p. ej. saldo
insuficiente), pero nunca decir "no sé retirar". Lo escribimos porque nos dimos cuenta de que
LSP se trata de no romper lo que el cliente espera, y si eso no está escrito en ningún lado
cada quien entiende lo que quiere.

Repetimos el experimento 1 con el código nuevo
(`experimentos/salida_experimento_cdt_despues_L.txt`):

```
experimentos/ExperimentoCDT.java:12: error: incompatible types: inference variable E has incompatible bounds
        new CobroCuotaManejo().cobrarMensual(List.of(ana, cdtAna, luis, pedro));
    upper bounds: CuentaTransaccional,Object
    lower bounds: Cuenta
```

(Por eso `experimentos/` ya no compila con el código nuevo: lo dejamos así a propósito como
evidencia. `run.sh` y las pruebas no lo incluyen.)

**Pregunta de control.**

*¿Se detecta al compilar o al ejecutar?* Al **compilar**. Es mejor porque el error lo ve el
desarrollador en su computador, no el proceso nocturno con un millón de cuentas reales. Un
error de compilación no se puede "olvidar probar"; un error en ejecución solo aparece si alguna
prueba (o un cliente) pasa justo por ese camino.

*¿Por qué no basta un try/catch que ignore los CDT?*
1. Arregla el síntoma en un solo lugar: `TransaccionService` (y cualquier clase futura que
   reciba una `Cuenta`) seguiría pudiendo explotar con un CDT. Cada cliente nuevo tendría que
   acordarse de poner su propio try/catch.
2. Atrapar `UnsupportedOperationException` (o peor, `Exception`) también se tragaría errores
   reales, y la cuenta quedaría sin cobrar sin que nadie se entere.
3. El diseño seguiría mintiendo: el tipo dice que un CDT es una `Cuenta` que se puede retirar,
   y no lo es. El try/catch es aceptar que el tipo miente y vivir con eso.

Duda que nos quedó: `CDT.redimir` también lanza excepción si no está vencido. ¿No es lo mismo
de antes? Concluimos que no: `redimir` es del CDT, nadie lo llama "creyendo que tiene una
cuenta cualquiera", y su regla (la fecha) es parte de su contrato desde el principio, igual que
"saldo insuficiente" es parte del contrato de `retirar`.

### Punto de control I

`ProductoBancario` obligaba a todos a implementar cinco métodos. Lo partimos según **quién usa
qué**:

| Interfaz | Métodos | La usa |
|---|---|---|
| `ConExtracto` | `generarExtracto()` | `GeneradorExtractos` |
| `ProductoCredito` | `calcularIntereses()`, `pagarCuota()` | (procesos de crédito; hoy nadie la llama, pero agrupa lo que tienen en común tarjeta y crédito) |
| — (`depositar`/`retirar`) | ya estaban en `Cuenta` / `CuentaTransaccional` | `TransaccionService`, `CobroCuotaManejo` |

- `TarjetaCredito` y `CreditoVivienda` implementan `ProductoCredito` y `ConExtracto`. Se
  borraron los tres métodos vacíos.
- El "retirar" de la tarjeta en realidad era un **avance**, así que lo renombramos
  `realizarAvance` y quedó como método propio de la tarjeta (no le inventamos interfaz porque
  solo la tarjeta lo tiene).
- `Cuenta` implementa `ConExtracto` (así lo tienen ahorros, CDT y las cuentas futuras).
- Se borró `ProductoBancario`.

Dudamos de si `ProductoCredito` era una abstracción innecesaria, porque nadie la usa todavía.
La dejamos porque reemplaza la parte de `ProductoBancario` que sí era común a tarjeta y crédito,
pero si en la revisión cruzada nos dicen que sobra, lo aceptamos.

**Pregunta de control.** *¿Un mismo generador sirve para cuentas, tarjetas y créditos?* Sí.
`experimentos/ExperimentoExtractos.java` le pasa una cuenta de ahorros, un CDT, una tarjeta y
un crédito al mismo `GeneradorExtractos`:

```
Cuenta 001-1 - saldo: $2000000.0
Cuenta CDT-9 - saldo: $1.0E7
Tarjeta - deuda: $0.0
Crédito vivienda - pendiente: $1.2E8
```

Necesitó solo `ConExtracto`. No necesita conocer los demás métodos porque lo único que hace
con cada producto es pedirle su texto; si dependiera de `depositar` o `pagarCuota`, no podría
recibir a la vez cosas que tienen unos pero no otros. (Nota: `Main` sigue imprimiendo solo
tarjeta y crédito, como el original, para que la salida no cambie.)

### Punto de control D

Creamos tres abstracciones y `TransaccionService` las recibe por constructor:

| Abstracción | Implementación en producción |
|---|---|
| `RepositorioTransacciones` | `OracleRepositorio` |
| `Comprobante` | `ImpresoraComprobante` |
| `ObservadorTransaccion` (lista) | `NotificadorSms` (que a su vez recibe un `SmsGateway`), `Auditoria` |

Además recibe `ValidadorMonto` y `CatalogoComisiones` (de O). Todo se arma en `Main`.

¿Por qué una **lista** de observadores en vez de un `Notificador` y una `Auditoria` por
separado? Por el hallazgo 4 del diagnóstico: notificar y auditar son "cosas que tienen que
enterarse de que la transferencia salió bien" y cambian por razones que no tienen nada que ver
con transferir. Con la lista, si aparece otra cosa que tenga que enterarse, no hay que tocar
`TransaccionService`. (No sabíamos qué iba a pedir el negocio; fue una apuesta según lo que
vimos en el diagnóstico.)

`ValidadorMonto` lo inyectamos como clase concreta, sin interfaz: es lógica pura, no se conecta
a nada externo y no le vimos sentido a una interfaz con una sola implementación posible.

**Pregunta de control.**

*¿Cuántas clases concretas conoce ahora `TransaccionService`?* Crea con `new` **cero**
dependencias (el único `new` que queda es `new Transaccion(...)`, que es un dato, no una
dependencia). Conoce por nombre: `ValidadorMonto`, `CatalogoComisiones` y el record
`Transaccion` (concretos, pero sin infraestructura), y las abstracciones `Cuenta`,
`CuentaTransaccional`, `RepositorioTransacciones`, `Comprobante` y `ObservadorTransaccion`.
No conoce `OracleRepositorio`, `SmsGateway` ni `System.out`.

*¿Quién decide si se usa Oracle o si se notifica por SMS?* `Main` (la raíz de composición).

*¿Ya es posible la prueba del experimento 2?* Sí: basta con pasarle un repositorio en memoria y
un observador que anote en vez de enviar. Es justo lo que hacemos en el bloque 3.

## Bloque 3 — Pruebas unitarias

Framework: JUnit 4.13.2. Ejecutar con `./test.sh`.

Dobles de prueba (en `test/`), todos escritos a mano, sin Mockito:

- `RepositorioEnMemoria`: guarda las transacciones en una lista en vez de Oracle.
- `ComprobanteEspia`: anota los comprobantes en vez de imprimirlos.
- `ObservadorEspia`: anota las notificaciones en vez de mandar SMS.

| # | Prueba (`TransaccionServiceTest`) | Qué verifica |
|---|---|---|
| 1 | `mismoBancoNoCobraComisionYMueveExactamenteElMonto` | comisión 0; origen −300.000, destino +300.000 |
| 2 | `otroBancoCobra7500YDescuentaMontoMasComision` | comisión 7.500; origen −107.500; destino solo +100.000 |
| 3 | `saldoInsuficienteSeRechazaYNoSeGuardaNiSeNotifica` | lanza excepción; nada guardado, ni comprobante, ni notificación; saldos intactos |
| 4 | `cadaTransferenciaExitosaSeGuardaUnaVezYNotificaUnaVez` | 1 guardado y 1 notificación por transferencia (probado con dos seguidas) |
| 5 | `tipoDesconocidoSeRechazaYElSaldoNoCambia` | `"CRIPTO"` → excepción; saldo del origen intacto; nada guardado |

Salida:

```
JUnit version 4.13.2
.....
Time: 0.038

OK (5 tests)
```

Ni una línea de `[ORACLE]` ni de `[SMS]`.

Para estar seguros de que las pruebas sirven (y no pasan "porque sí"), rompimos el código a
propósito: movimos `repositorio.guardar(t)` antes de `origen.retirar(...)`. La prueba 3 falló
(`Tests run: 5, Failures: 1`), porque con saldo insuficiente la transacción quedaba guardada
aunque se rechazara. Volvimos a dejar el código como estaba.

**Pregunta de control.**

*¿Cuánto tardan?* 38 ms las cinco pruebas según JUnit (unos 1,6 s el script completo, pero casi
todo es compilar y arrancar la JVM).

*¿Cuántas líneas de `TransaccionService` tuvieron que cambiar para poder probarla?* En el
bloque 3, **ninguna**: las pruebas se escribieron sobre el código de `control-D` sin tocarlo.
El cambio que lo hizo posible fue el del punto D (pasar de crear las dependencias con `new` a
recibirlas en el constructor). Comparando `bloque-0-codigo-base` contra `control-D`, la clase
se reescribió casi entera, pero el método `transferir` quedó en 13 líneas.

*¿Qué habría pasado en el bloque 1?* Lo mismo que en el experimento 2: cada prueba habría
insertado en la base de producción y mandado SMS reales (cinco pruebas = varias transacciones
falsas en Oracle y SMS a clientes cada vez que alguien corre las pruebas). Además las pruebas
3 y 4 ni siquiera se pueden escribir: no hay forma de preguntar "¿se guardó?" o "¿cuántas
notificaciones salieron?" sin leer la consola o la base de datos real.

## Bloque 4 — "Negocio pidió cambios"

Antes de cada requerimiento miramos el código de `bloque-0-codigo-base` y estimamos cuántos
archivos existentes habría que modificar allí. Contamos `Main.java` cuando hay que tocarlo, y no
contamos el README ni los archivos de prueba nuevos como "existentes modificados".

### R1 — Transferencias por llave

- **Estimado en el original:** 1 archivo (`TransaccionService.java`, agregar un `case "LLAVE" -> comision = 0;` al `switch`).
- **Real en el refactorizado:** 1 existente modificado (`Main.java`: una línea
  `.registrar("LLAVE", new SinComision())`), 0 archivos nuevos de producción, 1 archivo de
  prueba nuevo (`TransferenciaLlaveTest`).
- El número es el mismo (1), pero la diferencia es **cuál** archivo: en el original es la clase
  que mueve la plata; aquí es la línea de configuración. No se tocó `TransaccionService`.
- Criterio de aceptación: `TransferenciaLlaveTest` verifica que $50.000 por LLAVE descuentan
  exactamente $50.000. Ojo: la prueba arma su propio catálogo, así que no prueba que `Main` haya
  registrado `"LLAVE"`; eso solo se ve leyendo `Main`.

### R2 — Cuenta infantil

- **Estimado en el original:** 0 existentes (bastaba con `class CuentaInfantil extends Cuenta`
  sobrescribiendo `retirar`) + 1 nuevo. Honestamente, en el original este parecía fácil.
- **Real en el refactorizado:** 2 existentes modificados (`CuentaTransaccional.java`,
  `CobroCuotaManejo.java`), 1 nuevo de producción (`CuentaInfantil.java`) y 1 de prueba
  (`CuentaInfantilTest`, 8 pruebas).

Qué pasó: la primera versión (`CuentaInfantil extends CuentaTransaccional`, con el límite en
`retirar`) cumplía el criterio de aceptación, era origen de transferencias y se le cobraba la
cuota. Pero escribimos una prueba más, pensando en el experimento 1: *¿qué pasa si el niño ya
retiró sus $200.000 hoy y esa noche corre el cobro de cuota?*

```
1) laCuotaSeCobraAunqueElNinoYaHayaRetiradoElMaximoDelDia(CuentaInfantilTest)
java.lang.IllegalStateException: Supera el límite diario de retiros de la cuenta infantil
Tests run: 14,  Failures: 1
```

El proceso nocturno se volvía a caer, igual que con el CDT. Técnicamente `CuentaInfantil` no
viola LSP (su rechazo está dentro del contrato que escribimos en `CuentaTransaccional`), pero
`CobroCuotaManejo` estaba usando `retirar` —una operación pensada para el cliente, con reglas que
cada producto puede endurecer— para algo que no es un retiro del cliente.

Solución: agregamos `cobrarCargo(monto)` en `CuentaTransaccional`, `final` para que ninguna
subclase lo pueda restringir, y `CobroCuotaManejo` lo usa en vez de `retirar`. Para las cuentas
de ahorros el comportamiento es idéntico (`check.sh` sigue en OK).

Esto lo tomamos como una falla de nuestro diseño del punto L: separamos bien "puede retirar /
no puede retirar", pero no separamos "retiro del cliente" de "cargo del banco". Por eso hubo que
modificar dos archivos existentes.

Decisiones que tomamos sin que el requerimiento lo dijera (las anotamos para preguntarle al
"negocio"):
- La **comisión** de una transferencia sí cuenta para el límite diario (se retira
  `monto + comisión`), porque sale de la plata del niño por una acción suya.
- La **cuota de manejo** no cuenta para el límite.
- Sigue pendiente algo que ya pasaba en el original: si una cuenta **no tiene saldo** para la
  cuota, el cobro también se cae. No lo cambiamos porque cambiaría el comportamiento, pero es el
  mismo riesgo.
