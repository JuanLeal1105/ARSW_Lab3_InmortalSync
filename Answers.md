# Laboratorio 3 - Inmortasl Sync

**Elaborado por**

Juan Carlos Leal Cruz

## Parte 1. Wait/notify: Productor/Consumidor

### 1. Ejecución y monitoreo de CPU (Alto Consumo)

**Pregunta:**  
¿Por qué el consumo es alto? ¿Qué clase lo causa?

**Respuesta:**  
El consumo alto de CPU ocurre al momento de ejecutar el programa en modo `spin`. Lo anterior se debe a que se está implemantado una espera activa (busy - waiting) al momento de sincronizar.
Esto significa que los hilos quedan en un bucle infinito verificando si pueden acceder o no a la cola, de tal manera que el procesador se mantiene ocupado ejectuando instrucciones sin ninguna utilidad.
- Clase causante: `BusySpinQueue`
- Métodos causantes:
  - `put ()`
  - `take ()`
  
  Ambos métodos tienen bucles infinitos (`while(true)`) que solo ceden el paso de forma momentanea con `Thread.onSpinWait()`, lo cual no es suficiente para poder liberar de forma correcta la carga del sistema operativo.

### 2. Implementación Eficiente. Consumidor Rápido y Productor Lento
Para una correcta implementación lo que se debe de hacer es usar el modo `monitor` el cual hace empleo de la clase `BoundedBuffer` que reemplaza la espera activa por un mecanismo que se basa en monitores.

**Comportamiento:**  
Dado que el consumidor es más rápido, la cola estará frecuentemente vacía. En `BoundedBuffer`, el método `take()` evalúa `while (q.isEmpty()) { this.wait(); }`, de tal forma que al ejecutar `wait(), el hilo del consumidor libera el bloqueo y el sistema operativo lo pasa a estado WAITING, reduciendo el consumo de CPU a niveles bajos mientras se espera que el productor inserte un nuevo elemento.

### 3. Implementación Eficiente. Consumidor Lento y Productor Rápido
Para este punto se vuelve a hacer uso del modo `monitor` para así implementar la clase `BoundedBuffer`, ya que mediante el método `put` se garantiza que se respete el límite del stock de la cola.

**Comportamiento:**  
Se incluye la condición de guardia `while (q.size() == capacity) { this.wait(); }`, de tal forma que se bloquea al productor cuando la cola alcanza su capacidad máxima definida.
Lo anterior implica el que productor rápido entra en estado de espera tan pronto se llena el buffer, deteniendo su ejecución y el consumo de CPU hasta que el consumidor lento libere espacio y notifique el cambio (`notifyAll()`).

