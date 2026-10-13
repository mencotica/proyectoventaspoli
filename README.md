# Proyecto de Ventas

Trabajo final de Conceptos Fundamentales de Programación, Politécnico.
Programa en Java que genera datos de ventas de prueba y los procesa para producir reportes: cuánto recaudó cada vendedor y qué productos se vendieron más.

## Integrantes

- Santiago González Agudelo
- Danys Gutiérrez Franco
- Iveth Gutiérrez Franco
- Daniela Natalia Ríos Manco

## Qué hace el programa

Lee `productos.txt`, `vendedores.txt` y todos los `ventas_*.txt` del directorio, cruza cada venta con el precio de su producto y genera:

- `reporte_vendedores.csv`: nombre y apellido del vendedor, y el dinero recaudado, de mayor a menor.
- `reporte_productos.csv`: nombre del producto y precio, de mayor a menor cantidad de unidades vendidas.
- `reportes.ser`: las dos listas guardadas en formato binario, para poder volver a verlas sin reprocesar los archivos.

Se puede usar desde tres modos: automático, menú de consola o ventana gráfica.

## Estructura de archivos

```
proyectoventaspoli/
├── src/
│   ├── GenerateInfoFiles.java   Genera los datos de prueba (productos, vendedores y ventas).
│   ├── Main.java                Lee los datos, calcula los reportes y ofrece los modos de ejecución.
│   └── VentanaVentas.java       Ventana gráfica (Swing). No tiene método main.
├── pruebas/                     Archivos de ventas con errores a propósito (Extra c).
├── productos.txt                Catálogo de productos generado.
├── vendedores.txt               Lista de vendedores generada.
├── ventas_*.txt                 Ventas de cada vendedor.
├── reporte_vendedores.csv       Reporte de dinero recaudado por vendedor.
├── reporte_productos.csv        Reporte de productos vendidos.
├── reportes.ser                 Reportes serializados.
├── proyecto_de_ventas.iml       Módulo de IntelliJ IDEA (src como carpeta de fuentes, Java 8).
├── .project                     Proyecto de Eclipse.
├── .classpath                   Configuración de Eclipse (src, bin y JRE 1.8).
├── conslusion.txt               Conclusiones del equipo.
└── README.md
```

`GenerateInfoFiles` (o la opción 1 del menú) crea `productos.txt`, `vendedores.txt` y los `ventas_*.txt`. `Main` lee esos archivos y crea `reporte_vendedores.csv`, `reporte_productos.csv` y `reportes.ser`. Todos quedan en la raíz del proyecto.

Solo dos clases tienen método `main`: `GenerateInfoFiles` y `Main`.

## Cómo compilar y ejecutar

Todos los comandos se ejecutan desde la raíz del proyecto. El código es compatible con Java 8.

Compilar (con JDK 9 o superior; con JDK 8 se usa `javac -d bin src/*.java`):

```
javac --release 8 -d bin src/*.java
```

Generar los datos de prueba (opcional si ya existen los archivos):

```
java -cp bin GenerateInfoFiles
```

Modos de ejecución:

| Comando | Qué hace |
|---------|----------|
| `java -cp bin Main` | **Modo automático**: genera los reportes y `reportes.ser` sin preguntar nada. |
| `java -cp bin Main menu` | **Menú de consola** con seis opciones. |
| `java -cp bin Main gui` | **Ventana gráfica** "Ventas y Vendedores". |

Opciones del menú:

1. Generar archivos de prueba.
2. Importar / ver la base (productos y vendedores).
3. Importar / ver la salida (reportes en pantalla, sin guardar).
4. Descargar la salida (guarda los CSV y `reportes.ser`).
5. Salir.
6. Ver el último reporte guardado (lee `reportes.ser`).

Botones de la ventana: **Generar archivos**, **Ver base**, **Ver salida**, **Descargar CSV** y **Ver serializado**. El resultado aparece en el área de texto y los errores en un cuadro de diálogo.

### Desde IntelliJ IDEA

1. **File → Open** y seleccionar la carpeta del proyecto (la que contiene `src/`). El módulo `proyecto_de_ventas.iml` ya marca `src/` como carpeta de fuentes y fija el nivel de lenguaje en Java 8.
2. Si IntelliJ pide un JDK, elegirlo en **File → Project Structure → Project → SDK**.
3. Para el modo automático: clic derecho sobre `Main.java` → **Run 'Main.main()'**.
4. Para el menú o la ventana: **Run → Edit Configurations**, escribir `menu` o `gui` en **Program arguments** y dejar como **Working directory** la carpeta del proyecto, que es donde se leen y escriben los `.txt`, `.csv` y `reportes.ser`.

### Desde Eclipse

1. Importar el proyecto con **File → Import → General → Existing Projects into Workspace** y seleccionar la carpeta del proyecto.
2. Para el modo automático: clic derecho sobre `Main.java` → **Run As → Java Application**.
3. Para el menú o la ventana: **Run Configurations → Arguments** y escribir `menu` o `gui`.

## Extras implementados

### Extra a: varios archivos por vendedor

`GenerateInfoFiles.generarArchivosVentasParaTodosLosVendedores()` crea un segundo archivo de ventas (con sufijo `_2`) para uno de cada tres vendedores. El reporte suma automáticamente todos los `ventas_*.txt` del mismo número de documento.

### Extra b: archivos serializados

`Main` guarda las listas de reportes en `reportes.ser` con `ObjectOutputStream` cada vez que se descargan los reportes (modo automático, opción 4 del menú o botón "Descargar CSV"). La opción 6 del menú y el botón "Ver serializado" leen ese archivo con `ObjectInputStream`. Si el archivo no existe o está dañado, el programa muestra un mensaje y sigue funcionando.

### Extra c: detección de datos erróneos

La carpeta `pruebas/` contiene archivos de ventas con errores intencionales. Para probarlos, copiar los archivos `.txt` de `pruebas/` a la raíz del proyecto y ejecutar `java -cp bin Main`.

| Caso | Archivo | Resultado esperado | Resultado obtenido |
|------|---------|--------------------|--------------------|
| 1 | `ventas_producto_inexistente.txt` | `Aviso: producto inexistente (99) en ventas_producto_inexistente.txt, se omite.` | `Aviso: producto inexistente (99) en ventas_producto_inexistente.txt, se omite.` y `Aviso: producto inexistente (100) en ventas_producto_inexistente.txt, se omite.` |
| 2 | `ventas_cantidad_negativa.txt` | `Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('1;-5;'), se omite.` | `Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('1;-5;'), se omite.` y `Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('2;0;'), se omite.` |
| 3 | `ventas_linea_mal_formada.txt` | `Aviso: linea mal formada en ventas_linea_mal_formada.txt ('abc;xyz;'), se omite.` | `Aviso: linea mal formada en ventas_linea_mal_formada.txt ('abc;xyz;'), se omite.`, `Aviso: linea mal formada en ventas_linea_mal_formada.txt ('1;dos;'), se omite.` y `Aviso: linea mal formada en ventas_linea_mal_formada.txt (';5;'), se omite.` |
| 4 | `ventas_vendedor_no_registrado.txt` | `Aviso: vendedor no registrado (99999999) en ventas_vendedor_no_registrado.txt, se omite.` | `Aviso: vendedor no registrado (99999999) en ventas_vendedor_no_registrado.txt, se omite.` |
| 5 | `ventas_archivo_vacio.txt` | `Aviso: archivo ventas_archivo_vacio.txt esta vacio, se omite.` | `Aviso: archivo ventas_archivo_vacio.txt esta vacio, se omite.` |

Salida completa de la consola con los archivos de `pruebas/` copiados a la raíz:

```
Procesando 13 archivos de ventas...
Aviso: archivo ventas_archivo_vacio.txt esta vacio, se omite.
Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('1;-5;'), se omite.
Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('2;0;'), se omite.
Aviso: linea mal formada en ventas_linea_mal_formada.txt ('abc;xyz;'), se omite.
Aviso: linea mal formada en ventas_linea_mal_formada.txt ('1;dos;'), se omite.
Aviso: linea mal formada en ventas_linea_mal_formada.txt (';5;'), se omite.
Aviso: producto inexistente (99) en ventas_producto_inexistente.txt, se omite.
Aviso: producto inexistente (100) en ventas_producto_inexistente.txt, se omite.
Aviso: vendedor no registrado (99999999) en ventas_vendedor_no_registrado.txt, se omite.
Reportes generados con exito: reporte_vendedores.csv, reporte_productos.csv, reportes.ser
```

En todos los casos el programa muestra el aviso, omite solo la línea o el archivo afectado y termina con normalidad.

## Git

Se suben `.project`, `.classpath` y `proyecto_de_ventas.iml`. El `.gitignore` excluye `bin/`, `out/`, `.idea/`, `.run/` y `.settings`.
