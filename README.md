# Proyecto de Ventas

Este es el trabajo de Conceptos Fundamentales de Programación del Poli. Es un programa en Java que arma unos archivos con datos de ventas inventados y después los lee para sacar dos reportes: cuánta plata hizo cada vendedor y qué productos se vendieron más.

## Integrantes
- Santiago González Agudelo
- Danys Gutiérrez Franco
- Iveth Gutiérrez Franco
- Daniela Natalia Ríos Manco

## De qué va
Todo se maneja desde un menú en la consola:

1. Generar Archivos: crea los archivos de prueba (productos, vendedores y las ventas de cada uno).
2. Importar / Ver Base: lee esos archivos y te dice cuántos productos y vendedores hay, y muestra un pedacito para revisar que quedó bien cargado.
3. Importar / Ver Salida: junta cada venta con el precio del producto, saca cuánto recaudó cada vendedor y cuántas unidades se vendieron de cada producto, y lo muestra ordenado de mayor a menor.
4. Descargar Salida: guarda esos dos reportes en CSV para abrirlos en Excel.
5. Salir.

Si algún archivo trae algo raro (un producto que no existe o una cantidad en negativo) el programa lo salta y sigue, no se cae.

## Para correrlo
Toca tener Java instalado.
- En Eclipse: abrir el proyecto y darle run a la clase Main.
- En terminal: entrar a la carpeta del código, `javac *.java` y luego `java Main`. Primero la opción 1 para generar los archivos y después las demás.

Son dos clases con main: GenerateInfoFiles (la que genera los archivos) y Main (el menú con todo lo demás).

## Ojo
Esta es la entrega 2, todavía es una versión preliminar. Lo que falta lo dejamos anotado en el documento de faltantes.
