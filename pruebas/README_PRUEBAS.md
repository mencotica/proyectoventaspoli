# Pruebas con datos erróneos

Esta carpeta trae su propio `productos.txt` y `vendedores.txt`, con datos fijos, para que los resultados siempre den lo mismo. Los archivos de error usan el vendedor 10000002 (Luis Perez), que sí está registrado; así el programa llega a revisar cada línea.

| Archivo | Qué tiene |
|---|---|
| `ventas_producto_inexistente.txt` | Productos 99 y 100, que no existen |
| `ventas_cantidad_negativa.txt` | Cantidades -5 y 0 |
| `ventas_linea_mal_formada.txt` | Líneas `abc;xyz;` y `1;dos;` |
| `ventas_vendedor_no_registrado.txt` | Documento 99999999, que no está en vendedores.txt |
| `ventas_archivo_vacio.txt` | Nada |
| `ventas_Ana_Lopez_10000001.txt` y `ventas_Ana_Lopez_10000001_2.txt` | Dos archivos válidos del mismo vendedor (extra a) |

## Cómo correrlas

Desde la raíz del proyecto, ya compilado:

```
cd pruebas
java -cp ../bin Main
```

También se pueden copiar **todos** los archivos de esta carpeta a la raíz (incluidos `productos.txt` y `vendedores.txt`) y correr `java -cp bin Main`. Ojo: eso reemplaza los archivos base de la raíz. Si solo se copian los `ventas_*.txt`, casi todo sale como "vendedor no registrado", porque los vendedores de la raíz se generan al azar.

## Resultado esperado

- Se muestra un aviso por cada dato malo y el programa sigue.
- `reporte_vendedores.csv`: `Ana Lopez;1400000` (suma de sus dos archivos) y `Luis Perez;0`.
- `reporte_productos.csv`: Mouse inalambrico (5 unidades), Monitor 24 pulgadas (2) y Teclado mecanico (2).
