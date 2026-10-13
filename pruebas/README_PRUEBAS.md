# Carpeta pruebas/ — Extra c: Detección de datos erróneos

Esta carpeta contiene archivos de ventas con errores intencionales para demostrar
que el programa los detecta, muestra un aviso claro en consola y continúa sin caerse.

Para ejecutar las pruebas: copiar los archivos `.txt` de esta carpeta a la raíz del
proyecto y correr `java Main` (o la opción [3] del menú).

## Tabla de pruebas

| # | Archivo | Error introducido | Aviso esperado en consola | ¿El programa sigue? |
|---|---------|-------------------|--------------------------|---------------------|
| 1 | `ventas_producto_inexistente.txt` | IDs de producto 99 y 100 no existen en `productos.txt` | `Aviso: producto inexistente (99) en ventas_producto_inexistente.txt, se omite.` | Sí |
| 2 | `ventas_cantidad_negativa.txt` | Cantidades -5 y 0 (no válidas) | `Aviso: cantidad invalida en ventas_cantidad_negativa.txt ('1;-5;'), se omite.` | Sí |
| 3 | `ventas_linea_mal_formada.txt` | Texto en lugar de números (`abc;xyz;`, `1;dos;`) | `Aviso: linea mal formada en ventas_linea_mal_formada.txt ('abc;xyz;'), se omite.` | Sí |
| 4 | `ventas_vendedor_no_registrado.txt` | Número de documento 99999999 no está en `vendedores.txt` | `Aviso: vendedor no registrado (99999999) en ventas_vendedor_no_registrado.txt, se omite.` | Sí |
| 5 | `ventas_archivo_vacio.txt` | Archivo completamente vacío | `Aviso: archivo ventas_archivo_vacio.txt esta vacio, se omite.` | Sí |

## Resultado obtenido

Salida real de `java -cp bin Main` con estos archivos copiados a la raíz del proyecto:

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
