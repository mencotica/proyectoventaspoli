import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/**
 * Procesa los archivos de ventas y genera los reportes de vendedores y productos.
 * <p>
 * Modos de ejecucion: sin argumentos (automatico), {@code menu} (consola) y {@code gui} (ventana).
 * </p>
 *
 * @author Santiago Gonzalez Agudelo, Danys Gutierrez Franco, Iveth Gutierrez Franco, Daniela Natalia Rios Manco
 * @version 3.0
 */
public class Main {

    /** Dinero total recaudado por un vendedor. */
    private static class ResultadoVendedor implements Serializable {

        /** Version de la clase para la serializacion. */
        private static final long serialVersionUID = 1L;

        /** Nombres y apellidos del vendedor. */
        final String nombreCompleto;

        /** Dinero recaudado por el vendedor. */
        final double dinero;

        /**
         * Crea el resultado de un vendedor.
         *
         * @param nombreCompleto nombres y apellidos del vendedor
         * @param dinero         dinero recaudado
         */
        ResultadoVendedor(String nombreCompleto, double dinero) {
            this.nombreCompleto = nombreCompleto;
            this.dinero = dinero;
        }
    }

    /** Precio y unidades vendidas de un producto. */
    private static class ResultadoProducto implements Serializable {

        /** Version de la clase para la serializacion. */
        private static final long serialVersionUID = 1L;

        /** Nombre del producto. */
        final String nombre;

        /** Precio por unidad del producto. */
        final long precio;

        /** Unidades vendidas entre todos los vendedores. */
        final int cantidadVendida;

        /**
         * Crea el resultado de un producto.
         *
         * @param nombre          nombre del producto
         * @param precio          precio por unidad
         * @param cantidadVendida unidades vendidas
         */
        ResultadoProducto(String nombre, long precio, int cantidadVendida) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidadVendida = cantidadVendida;
        }
    }

    /** Accion que escribe su resultado en un destino de salida; la usan el menu y la ventana. */
    interface Accion {

        /**
         * Ejecuta la accion.
         *
         * @param salida destino donde se escriben los mensajes
         * @throws IOException si falla la lectura o escritura de archivos
         */
        void ejecutar(PrintStream salida) throws IOException;
    }

    /** Archivo CSV con el dinero recaudado por cada vendedor. */
    static final String ARCHIVO_REPORTE_VENDEDORES = "reporte_vendedores.csv";

    /** Archivo CSV con los productos ordenados por cantidad vendida. */
    static final String ARCHIVO_REPORTE_PRODUCTOS = "reporte_productos.csv";

    /** Archivo donde se guardan serializadas las dos listas de resultados. */
    static final String ARCHIVO_SERIALIZADO = "reportes.ser";

    /** Opcion del menu que termina el programa. */
    private static final int OPCION_SALIR = 5;

    /** Ultimo reporte de vendedores calculado, de mayor a menor dinero. */
    private static List<ResultadoVendedor> reporteVendedores = new ArrayList<>();

    /** Ultimo reporte de productos calculado, de mayor a menor cantidad vendida. */
    private static List<ResultadoProducto> reporteProductos = new ArrayList<>();

    /**
     * Punto de entrada: elige el modo segun el primer argumento.
     *
     * @param args vacio para modo automatico, {@code menu} o {@code gui}
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            ejecutarModoAutomatico();
        } else if ("menu".equalsIgnoreCase(args[0])) {
            ejecutarMenu();
        } else if ("gui".equalsIgnoreCase(args[0])) {
            SwingUtilities.invokeLater(() -> new VentanaVentas().setVisible(true));
        } else {
            System.out.println("Argumento no reconocido: " + args[0]);
            System.out.println("Uso: java -cp bin Main [menu | gui]");
        }
    }

    /** Procesa los archivos, escribe los CSV y el serializado sin pedir datos al usuario. */
    private static void ejecutarModoAutomatico() {
        System.out.println("=== PROCESANDO ARCHIVOS DE VENTAS ===");
        try {
            descargarSalida(System.out);
            System.out.println();
            System.out.println(">>> PROCESO FINALIZADO CON EXITO <<<");
        } catch (IOException e) {
            System.err.println(">>> ERROR: " + e.getMessage() + " <<<");
        }
    }

    /** Muestra el menu de consola hasta que el usuario elija salir. */
    private static void ejecutarMenu() {
        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            do {
                mostrarMenu();
                opcion = leerOpcion(scanner);
                switch (opcion) {
                    case 1:
                        ejecutarEnConsola(Main::generarArchivos);
                        break;
                    case 2:
                        ejecutarEnConsola(Main::verBase);
                        break;
                    case 3:
                        ejecutarEnConsola(Main::verSalida);
                        break;
                    case 4:
                        ejecutarEnConsola(Main::descargarSalida);
                        break;
                    case 6:
                        ejecutarEnConsola(Main::verReporteSerializado);
                        break;
                    case OPCION_SALIR:
                        System.out.println("Saliendo del programa...");
                        break;
                    default:
                        System.out.println("Opcion invalida. Intente de nuevo.");
                }
            } while (opcion != OPCION_SALIR);
        }
    }

    /** Imprime las opciones del menu principal. */
    private static void mostrarMenu() {
        System.out.println();
        System.out.println("========= MENU PRINCIPAL =========");
        System.out.println("[1] Generar Archivos");
        System.out.println("[2] Importar / Ver Base");
        System.out.println("[3] Importar / Ver Salida");
        System.out.println("[4] Descargar Salida");
        System.out.println("[5] Salir");
        System.out.println("[6] Ver ultimo reporte guardado (serializado)");
        System.out.print("Seleccione una opcion: ");
    }

    /**
     * Lee la opcion escrita por el usuario.
     *
     * @param scanner lector de la consola
     * @return la opcion, -1 si no es un numero, o la opcion de salir si ya no hay entrada
     */
    private static int leerOpcion(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            System.out.println();
            return OPCION_SALIR;
        }
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Ejecuta una accion en la consola y muestra el error sin cerrar el menu.
     *
     * @param accion accion a ejecutar
     */
    private static void ejecutarEnConsola(Accion accion) {
        try {
            accion.ejecutar(System.out);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Genera productos, vendedores y un archivo de ventas por vendedor.
     *
     * @param salida destino de los mensajes
     * @throws IOException si no se pueden escribir los archivos
     */
    static void generarArchivos(PrintStream salida) throws IOException {
        try {
            GenerateInfoFiles.createProductsFile(12);
            GenerateInfoFiles.createSalesManInfoFile(10);
            GenerateInfoFiles.generarArchivosVentasParaTodosLosVendedores(10);
        } catch (IOException e) {
            throw new IOException("No se pudieron generar los archivos: " + e.getMessage(), e);
        }
        salida.println("Archivos generados exitosamente.");
    }

    /**
     * Muestra los productos, vendedores y archivos de ventas que hay en la carpeta.
     *
     * @param salida destino de los mensajes
     * @throws IOException si faltan productos.txt o vendedores.txt
     */
    static void verBase(PrintStream salida) throws IOException {
        Map<Long, Long> preciosProductos = new LinkedHashMap<>();
        Map<Long, String> nombresProductos = new LinkedHashMap<>();
        Map<Long, String> nombresVendedores = new LinkedHashMap<>();
        cargarProductos(preciosProductos, nombresProductos, salida);
        cargarVendedores(nombresVendedores, salida);
        File[] archivosVentas = buscarArchivosVentas();

        salida.println();
        salida.println("=== Productos (" + preciosProductos.size() + ") ===");
        for (Map.Entry<Long, Long> producto : preciosProductos.entrySet()) {
            salida.println(producto.getKey() + " - " + nombresProductos.get(producto.getKey()) + " - $" + producto.getValue());
        }

        salida.println();
        salida.println("=== Vendedores (" + nombresVendedores.size() + ") ===");
        for (Map.Entry<Long, String> vendedor : nombresVendedores.entrySet()) {
            salida.println(vendedor.getKey() + " - " + vendedor.getValue());
        }

        salida.println();
        salida.println("=== Archivos de ventas (" + archivosVentas.length + ") ===");
        for (File archivo : archivosVentas) {
            salida.println(archivo.getName());
        }
    }

    /**
     * Calcula los reportes y los muestra.
     *
     * @param salida destino de los mensajes
     * @throws IOException si faltan los archivos de entrada
     */
    static void verSalida(PrintStream salida) throws IOException {
        calcularReportes(salida);
        mostrarReportes(reporteVendedores, reporteProductos, salida);
    }

    /**
     * Calcula los reportes, escribe los dos CSV y guarda las listas en reportes.ser.
     *
     * @param salida destino de los mensajes
     * @throws IOException si faltan los archivos de entrada o no se pueden escribir los reportes
     */
    static void descargarSalida(PrintStream salida) throws IOException {
        calcularReportes(salida);

        List<String> lineasVendedores = new ArrayList<>();
        for (ResultadoVendedor resultado : reporteVendedores) {
            lineasVendedores.add(String.format(Locale.US, "%s;%.0f", resultado.nombreCompleto, resultado.dinero));
        }
        List<String> lineasProductos = new ArrayList<>();
        for (ResultadoProducto resultado : reporteProductos) {
            lineasProductos.add(resultado.nombre + ";" + resultado.precio);
        }

        escribirLineas(ARCHIVO_REPORTE_VENDEDORES, lineasVendedores);
        escribirLineas(ARCHIVO_REPORTE_PRODUCTOS, lineasProductos);
        guardarReportesSerializados();

        salida.println();
        salida.println("Reportes guardados exitosamente:");
        salida.println("- " + ARCHIVO_REPORTE_VENDEDORES + " (" + reporteVendedores.size() + " vendedores)");
        salida.println("- " + ARCHIVO_REPORTE_PRODUCTOS + " (" + reporteProductos.size() + " productos)");
        salida.println("- " + ARCHIVO_SERIALIZADO);
    }

    /**
     * Lee reportes.ser y muestra su contenido.
     *
     * @param salida destino de los mensajes
     * @throws IOException si el archivo no existe o esta danado
     */
    static void verReporteSerializado(PrintStream salida) throws IOException {
        File archivo = new File(ARCHIVO_SERIALIZADO);
        if (!archivo.exists()) {
            throw new IOException("No existe " + ARCHIVO_SERIALIZADO + ". Primero descargue la salida (opcion 4 o boton Descargar CSV).");
        }

        List<ResultadoVendedor> vendedores;
        List<ResultadoProducto> productos;
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            vendedores = leerLista(entrada, ResultadoVendedor.class);
            productos = leerLista(entrada, ResultadoProducto.class);
        } catch (ClassNotFoundException | ClassCastException | IOException e) {
            throw new IOException("El archivo " + ARCHIVO_SERIALIZADO + " esta danado o no se puede leer.", e);
        }

        salida.println();
        salida.println("Contenido de " + ARCHIVO_SERIALIZADO + ":");
        mostrarReportes(vendedores, productos, salida);
    }

    /**
     * Lee una lista serializada y comprueba el tipo de cada elemento.
     *
     * @param entrada flujo de lectura de objetos
     * @param tipo    clase esperada de los elementos
     * @param <T>     tipo de los elementos
     * @return la lista leida
     * @throws IOException            si el contenido no es una lista
     * @throws ClassNotFoundException si el archivo tiene una clase desconocida
     */
    private static <T> List<T> leerLista(ObjectInputStream entrada, Class<T> tipo) throws IOException, ClassNotFoundException {
        Object objeto = entrada.readObject();
        if (!(objeto instanceof List)) {
            throw new InvalidObjectException("Se esperaba una lista");
        }
        List<T> lista = new ArrayList<>();
        for (Object elemento : (List<?>) objeto) {
            lista.add(tipo.cast(elemento));
        }
        return lista;
    }

    /**
     * Guarda las dos listas de resultados en reportes.ser.
     *
     * @throws IOException si no se puede escribir el archivo
     */
    private static void guardarReportesSerializados() throws IOException {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO_SERIALIZADO))) {
            salida.writeObject(reporteVendedores);
            salida.writeObject(reporteProductos);
        }
    }

    /**
     * Escribe cada linea en el archivo indicado.
     *
     * @param nombreArchivo archivo de destino
     * @param lineas        lineas a escribir
     * @throws IOException si no se puede escribir el archivo
     */
    private static void escribirLineas(String nombreArchivo, List<String> lineas) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(nombreArchivo))) {
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
        }
    }

    /**
     * Imprime los reportes de vendedores y productos.
     *
     * @param vendedores resultados de vendedores
     * @param productos  resultados de productos
     * @param salida     destino de los mensajes
     */
    private static void mostrarReportes(List<ResultadoVendedor> vendedores, List<ResultadoProducto> productos, PrintStream salida) {
        salida.println();
        salida.println("=== Reporte de Vendedores (mayor a menor dinero recaudado) ===");
        for (ResultadoVendedor resultado : vendedores) {
            salida.printf(Locale.US, "%s;%.0f%n", resultado.nombreCompleto, resultado.dinero);
        }

        salida.println();
        salida.println("=== Reporte de Productos (mayor a menor cantidad vendida) ===");
        for (ResultadoProducto resultado : productos) {
            salida.println(resultado.nombre + ";" + resultado.precio + " (unidades vendidas: " + resultado.cantidadVendida + ")");
        }
    }

    /**
     * Lee todos los archivos y llena los reportes de vendedores y productos.
     *
     * @param salida destino de los avisos por datos erroneos
     * @throws IOException si faltan productos.txt, vendedores.txt o archivos de ventas
     */
    private static void calcularReportes(PrintStream salida) throws IOException {
        Map<Long, Long> preciosProductos = new HashMap<>();
        Map<Long, String> nombresProductos = new HashMap<>();
        Map<Long, String> nombresVendedores = new HashMap<>();
        Map<Long, Double> dineroPorVendedor = new HashMap<>();
        Map<Long, Integer> cantidadPorProducto = new HashMap<>();

        cargarProductos(preciosProductos, nombresProductos, salida);
        cargarVendedores(nombresVendedores, salida);
        for (Long numeroDocumento : nombresVendedores.keySet()) {
            dineroPorVendedor.put(numeroDocumento, 0.0);
        }

        File[] archivosVentas = buscarArchivosVentas();
        if (archivosVentas.length == 0) {
            throw new IOException("No se encontraron archivos de ventas (" + GenerateInfoFiles.PREFIJO_VENTAS + "*.txt).");
        }
        for (File archivo : archivosVentas) {
            procesarArchivoVentas(archivo, preciosProductos, nombresVendedores, cantidadPorProducto, dineroPorVendedor, salida);
        }

        reporteVendedores = new ArrayList<>();
        for (Map.Entry<Long, Double> entrada : dineroPorVendedor.entrySet()) {
            reporteVendedores.add(new ResultadoVendedor(nombresVendedores.get(entrada.getKey()), entrada.getValue()));
        }
        reporteVendedores.sort((a, b) -> b.dinero != a.dinero
                ? Double.compare(b.dinero, a.dinero)
                : a.nombreCompleto.compareTo(b.nombreCompleto));

        reporteProductos = new ArrayList<>();
        for (Map.Entry<Long, Long> entrada : preciosProductos.entrySet()) {
            int cantidadVendida = cantidadPorProducto.getOrDefault(entrada.getKey(), 0);
            reporteProductos.add(new ResultadoProducto(nombresProductos.get(entrada.getKey()), entrada.getValue(), cantidadVendida));
        }
        reporteProductos.sort((a, b) -> b.cantidadVendida != a.cantidadVendida
                ? Integer.compare(b.cantidadVendida, a.cantidadVendida)
                : a.nombre.compareTo(b.nombre));
    }

    /**
     * Busca los archivos de ventas de la carpeta actual.
     *
     * @return archivos ventas_*.txt ordenados por nombre
     */
    private static File[] buscarArchivosVentas() {
        File[] archivos = new File(".").listFiles((carpeta, nombre) ->
                nombre.startsWith(GenerateInfoFiles.PREFIJO_VENTAS) && nombre.endsWith(".txt"));
        if (archivos == null) {
            return new File[0];
        }
        Arrays.sort(archivos);
        return archivos;
    }

    /**
     * Abre un archivo de entrada obligatorio.
     *
     * @param nombreArchivo nombre del archivo
     * @return lector del archivo
     * @throws IOException si el archivo no existe
     */
    private static BufferedReader abrirArchivoBase(String nombreArchivo) throws IOException {
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) {
            throw new IOException("No se encontro el archivo " + nombreArchivo + ". Genere primero los archivos.");
        }
        return new BufferedReader(new FileReader(archivo));
    }

    /**
     * Quita espacios y la marca BOM que dejan algunos editores al inicio de la linea.
     *
     * @param linea linea leida
     * @return linea limpia
     */
    private static String limpiarLinea(String linea) {
        return linea.replace("﻿", "").trim();
    }

    /**
     * Carga productos.txt en los mapas de precios y nombres.
     *
     * @param preciosProductos precio por ID de producto
     * @param nombresProductos nombre por ID de producto
     * @param salida           destino de los avisos
     * @throws IOException si el archivo no existe o no se puede leer
     */
    private static void cargarProductos(Map<Long, Long> preciosProductos, Map<Long, String> nombresProductos,
                                        PrintStream salida) throws IOException {
        try (BufferedReader lector = abrirArchivoBase(GenerateInfoFiles.ARCHIVO_PRODUCTOS)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = limpiarLinea(linea);
                if (linea.isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(GenerateInfoFiles.DELIMITADOR);
                try {
                    long id = Long.parseLong(partes[0].trim());
                    long precio = Long.parseLong(partes[2].trim());
                    preciosProductos.put(id, precio);
                    nombresProductos.put(id, partes[1].trim());
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    salida.println("Aviso: linea invalida en " + GenerateInfoFiles.ARCHIVO_PRODUCTOS + " ('" + linea + "'), se omite.");
                }
            }
        }
    }

    /**
     * Carga vendedores.txt en el mapa de nombres.
     *
     * @param nombresVendedores nombre completo por numero de documento
     * @param salida            destino de los avisos
     * @throws IOException si el archivo no existe o no se puede leer
     */
    private static void cargarVendedores(Map<Long, String> nombresVendedores, PrintStream salida) throws IOException {
        try (BufferedReader lector = abrirArchivoBase(GenerateInfoFiles.ARCHIVO_VENDEDORES)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = limpiarLinea(linea);
                if (linea.isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(GenerateInfoFiles.DELIMITADOR);
                try {
                    long numeroDocumento = Long.parseLong(partes[1].trim());
                    nombresVendedores.put(numeroDocumento, partes[2].trim() + " " + partes[3].trim());
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    salida.println("Aviso: linea invalida en " + GenerateInfoFiles.ARCHIVO_VENDEDORES + " ('" + linea + "'), se omite.");
                }
            }
        }
    }

    /**
     * Suma las ventas de un archivo y avisa de los datos erroneos que encuentre.
     *
     * @param archivo             archivo de ventas
     * @param preciosProductos    precio por ID de producto
     * @param nombresVendedores   nombre por numero de documento
     * @param cantidadPorProducto unidades acumuladas por producto
     * @param dineroPorVendedor   dinero acumulado por vendedor
     * @param salida              destino de los avisos
     */
    private static void procesarArchivoVentas(File archivo, Map<Long, Long> preciosProductos,
                                              Map<Long, String> nombresVendedores,
                                              Map<Long, Integer> cantidadPorProducto,
                                              Map<Long, Double> dineroPorVendedor, PrintStream salida) {
        String nombreArchivo = archivo.getName();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String primeraLinea = lector.readLine();
            if (primeraLinea == null) {
                salida.println("Aviso: archivo " + nombreArchivo + " esta vacio, se omite.");
                return;
            }

            String[] cabecera = limpiarLinea(primeraLinea).split(GenerateInfoFiles.DELIMITADOR);
            long numeroDocumento;
            try {
                numeroDocumento = Long.parseLong(cabecera[1].trim());
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                salida.println("Aviso: encabezado invalido en " + nombreArchivo + ", se omite.");
                return;
            }

            if (!nombresVendedores.containsKey(numeroDocumento)) {
                salida.println("Aviso: vendedor no registrado (" + numeroDocumento + ") en " + nombreArchivo + ", se omite.");
                return;
            }

            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = limpiarLinea(linea);
                if (linea.isEmpty()) {
                    continue;
                }

                long idProducto;
                int cantidad;
                try {
                    String[] partes = linea.split(GenerateInfoFiles.DELIMITADOR);
                    idProducto = Long.parseLong(partes[0].trim());
                    cantidad = Integer.parseInt(partes[1].trim());
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    salida.println("Aviso: linea mal formada en " + nombreArchivo + " ('" + linea + "'), se omite.");
                    continue;
                }

                if (cantidad <= 0) {
                    salida.println("Aviso: cantidad invalida en " + nombreArchivo + " ('" + linea + "'), se omite.");
                    continue;
                }

                Long precio = preciosProductos.get(idProducto);
                if (precio == null) {
                    salida.println("Aviso: producto inexistente (" + idProducto + ") en " + nombreArchivo + ", se omite.");
                    continue;
                }

                dineroPorVendedor.merge(numeroDocumento, (double) precio * cantidad, Double::sum);
                cantidadPorProducto.merge(idProducto, cantidad, Integer::sum);
            }
        } catch (IOException e) {
            salida.println("Error al leer " + nombreArchivo + ": " + e.getMessage());
        }
    }
}
