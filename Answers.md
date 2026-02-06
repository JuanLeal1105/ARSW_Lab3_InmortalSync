# Laboratorio 3 - Inmortasl Sync

**Elaborado por**

Juan Carlos Leal Cruz

## Parte 1. Wait/notify: Productor/Consumidor

### 1. Ejecución y monitoreo de CPU (Alto Consumo)

**Pregunta:**  
¿Por qué el consumo es alto? ¿Qué clase lo causa?

**Respuesta:**  
El consumo de CPU es extremadamente alto (cercano al 100% en los núcleos asignados) debido a que se está utilizando una estrategia de **Espera Activa (Busy Waiting)**.

En lugar de que los hilos se "duerman" o bloqueen cuando no pueden realizar una acción (como leer de una cola vacía o escribir en una llena), se mantienen en un bucle infinito (`while(true)`) preguntando constantemente si la condición ha cambiado. Esto mantiene al procesador ejecutando instrucciones inútilmente sin liberar recursos.

- **Clase causante:** `edu.eci.arsw.pc.BusySpinQueue`
- **Métodos responsables:** `put(T item)` y `take()`.  
  Ambos contienen bucles infinitos que solo ceden el paso momentáneamente con `Thread.onSpinWait()`, lo cual no es suficiente para liberar la carga del sistema operativo.

### 2. 
