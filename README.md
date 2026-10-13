# Proyecto de Ventas

Trabajo de Conceptos Fundamentales de Programación del Poli. Es un programa en Java 8 que genera archivos con datos de ventas inventados y después los lee para sacar dos reportes: cuánto dinero recaudó cada vendedor y qué productos se vendieron más.

## Integrantes

- Santiago González Agudelo
- Danys Gutiérrez Franco
- Iveth Gutiérrez Franco
- Daniela Natalia Ríos Manco

## Qué hace

1. `GenerateInfoFiles` crea los archivos de entrada:
   - `productos.txt`: `ID;Nombre;Precio`
   - `vendedores.txt`: `TipoDoc;NumeroDoc;Nombres;Apellidos`
   - `ventas_<Nombre>_<Apellido>_<ID>.txt`, uno por vendedor. La primera línea es `TipoDoc;NumeroDoc` y las demás `IDProducto;Cantidad;`
2. `Main` lee todos esos archivos, junta cada venta con el precio del producto y calcula el dinero por vendedor y las unidades vendidas por producto. Luego escribe los reportes en CSV y los guarda también serializados.

`Main` tiene tres modos:

| Comando | Modo |
|---|---|
| `java -cp bin Main` | Automático: procesa, escribe los reportes y dice si salió bien o mal. No pide nada. |
| `java -cp bin Main menu` | Menú en consola. |
| `java -cp bin Main gui` | Ventana gráfica. |

Opciones del menú:

1. Generar Archivos
2. Importar / Ver Base: muestra los productos, vendedores y archivos de ventas encontrados.
3. Importar / Ver Salida: calcula y muestra los reportes.
4. Descargar Salida: escribe los CSV y `reportes.ser`.
5. Salir
6. Ver último reporte guardado (serializado): lee `reportes.ser` y lo muestra.

La ventana tiene los botones "Generar archivos", "Ver base", "Ver salida", "Descargar CSV" y "Ver serializado", que hacen lo mismo que las opciones 1, 2, 3, 4 y 6. El resultado sale en el área de texto y los errores en un cuadro de diálogo, sin cerrar la ventana.

## Estructura

```
proyecto_de_ventas/
├── src/
│   ├── GenerateInfoFiles.java   genera los archivos de entrada (tiene main)
│   ├── Main.java                procesa y genera los reportes (tiene main)
│   └── VentanaVentas.java       ventana Swing, se abre con "Main gui"
├── pruebas/                     archivos con errores a propósito (ver README_PRUEBAS.md)
├── productos.txt, vendedores.txt, ventas_*.txt   datos generados
├── reporte_vendedores.csv, reporte_productos.csv, reportes.ser   salida
├── conslusion.txt
├── .project, .classpath         proyecto de Eclipse
└── README.md
```

Solo `GenerateInfoFiles` y `Main` tienen método `main`.

## Cómo compilar y ejecutar

Hace falta un JDK 8 o superior. Desde la raíz del proyecto:

```
javac --release 8 -d bin src/*.java
java -cp bin GenerateInfoFiles
java -cp bin Main
java -cp bin Main menu
java -cp bin Main gui
```

Con JDK 8 exacto, `--release` no existe; se usa `javac -d bin src/*.java`.

En Eclipse: File > Import > General > Existing Projects into Workspace y elegir la carpeta del proyecto. Ya trae `.project` y `.classpath` (fuentes en `src`, salida en `bin`, JRE JavaSE-1.8). Para correr, clic derecho en `GenerateInfoFiles.java` o `Main.java` > Run As > Java Application. Para los modos `menu` o `gui`, el argumento va en Run Configurations > Arguments. Eclipse corre el programa desde la raíz, así que los archivos se leen y se escriben ahí.

## Archivos que genera

| Archivo | Contenido |
|---|---|
| `reporte_vendedores.csv` | `Nombre Apellido;dinero`, de mayor a menor dinero |
| `reporte_productos.csv` | `Nombre;precio`, de mayor a menor cantidad vendida |
| `reportes.ser` | Las dos listas de resultados guardadas con `ObjectOutputStream` |

Si hay empate, se ordena por nombre para que el resultado siempre salga igual.

## Extras

**a) Varios archivos por vendedor.** Las ventas se suman por número de documento, no por archivo, así que un vendedor puede tener varios archivos de ventas. En `pruebas/` está Ana Lopez con `ventas_Ana_Lopez_10000001.txt` y `ventas_Ana_Lopez_10000001_2.txt`; su total (1.400.000) es la suma de los dos.

**b) Archivos serializados.** `ResultadoVendedor` y `ResultadoProducto` implementan `Serializable`. Después de generar los reportes (modo automático, opción 4 o botón "Descargar CSV") las dos listas se guardan en `reportes.ser`. La opción 6 o el botón "Ver serializado" lo leen con `ObjectInputStream`. Si el archivo no existe o está dañado, el programa avisa y sigue funcionando:

- No existe: `Error: No existe reportes.ser. Primero descargue la salida (opcion 4 o boton Descargar CSV).`
- Dañado: `Error: El archivo reportes.ser esta danado o no se puede leer.`

**c) Detección de datos erróneos.** Cuando un archivo de ventas trae algo malo, el programa muestra un aviso, salta esa línea o archivo y sigue con lo demás. Pruebas con los archivos de `pruebas/` (se corrió `java -cp ../bin Main` dentro de esa carpeta):

| Caso | Archivo | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| Producto inexistente | `ventas_producto_inexistente.txt` | Avisa y omite los productos 99 y 100 | `Aviso: producto inexistente (99) en ventas_producto_inexistente.txt, se omite.`<br>`Aviso: producto inexistente (100) en ventas_producto_inexistente.txt, se omite.` |
| Cantidad negativa o cero | `ventas_cantidad_negativa.txt` | Avisa y omite las cantidades -5 y 0 | `Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('1;-5;'), se omite.`<br>`Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('2;0;'), se omite.` |
| Línea mal formada | `ventas_linea_mal_formada.txt` | Avisa y omite `abc;xyz;` y `1;dos;` | `Aviso: linea mal formada en ventas_linea_mal_formada.txt ('abc;xyz;'), se omite.`<br>`Aviso: linea mal formada en ventas_linea_mal_formada.txt ('1;dos;'), se omite.` |
| Vendedor no registrado | `ventas_vendedor_no_registrado.txt` | Avisa y omite el archivo completo | `Aviso: vendedor no registrado (99999999) en ventas_vendedor_no_registrado.txt, se omite.` |
| Archivo vacío | `ventas_archivo_vacio.txt` | Avisa y omite el archivo | `Aviso: archivo ventas_archivo_vacio.txt esta vacio, se omite.` |
| Dos archivos del mismo vendedor | `ventas_Ana_Lopez_10000001.txt` y `..._2.txt` | Ana Lopez suma 1.400.000 | `Ana Lopez;1400000` en `reporte_vendedores.csv` |

Después de los avisos, el programa terminó con `>>> PROCESO FINALIZADO CON EXITO <<<` y escribió los tres archivos de salida. Si faltan `productos.txt` o `vendedores.txt`, el modo automático muestra `>>> ERROR: No se encontro el archivo productos.txt. Genere primero los archivos. <<<`.

## Ojo

La opción 1 del menú vuelve a crear `vendedores.txt` con vendedores nuevos, pero no borra los `ventas_*.txt` anteriores. Esos archivos viejos salen como "vendedor no registrado" hasta que se borren. Lo dejamos así a propósito para no borrar archivos del usuario.
