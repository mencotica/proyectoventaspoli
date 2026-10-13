import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/** Procesa las ventas y genera los reportes en modo automatico, menu de consola o interfaz grafica. */
public class Main {

    private static final String ARCHIVO_REPORTE_VENDEDORES = "reporte_vendedores.csv";
    private static final String ARCHIVO_REPORTE_PRODUCTOS = "reporte_productos.csv";
    private static final String ARCHIVO_SERIALIZADO = "reportes.ser";
    private static final String LISTA_ARCHIVOS_REPORTE =
            ARCHIVO_REPORTE_VENDEDORES + ", " + ARCHIVO_REPORTE_PRODUCTOS + ", " + ARCHIVO_SERIALIZADO;
    private static final String BOM = String.valueOf((char) 0xFEFF);

    private static Reportes reportes = new Reportes(
            new ArrayList<ResultadoVendedor>(), new ArrayList<ResultadoProducto>());

    /** Accion que escribe su resultado en una salida y puede fallar con un error de E/S. */
    interface Accion {

        /** Ejecuta la accion escribiendo en la salida indicada. */
        void ejecutar(PrintStream salida) throws IOException;
    }

    /** Dinero total recaudado por un vendedor. */
    private static class ResultadoVendedor implements Serializable {

        private static final long serialVersionUID = 1L;

        final String nombreCompleto;
        final long dinero;

        /** Crea el resultado de un vendedor. */
        ResultadoVendedor(String nombreCompleto, long dinero) {
            this.nombreCompleto = nombreCompleto;
            this.dinero = dinero;
        }

        /** Devuelve la linea "Nombre Apellido;dinero". */
        String aLinea() {
            return nombreCompleto + GenerateInfoFiles.DELIMITADOR + dinero;
        }
    }

    /** Unidades vendidas de un producto junto con su precio. */
    private static class ResultadoProducto implements Serializable {

        private static final long serialVersionUID = 1L;

        final String nombre;
        final long precio;
        final int cantidadVendida;

        /** Crea el resultado de un producto. */
        ResultadoProducto(String nombre, long precio, int cantidadVendida) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidadVendida = cantidadVendida;
        }

        /** Devuelve la linea "Nombre;precio". */
        String aLinea() {
            return nombre + GenerateInfoFiles.DELIMITADOR + precio;
        }
    }

    /** Par de listas de reportes: vendedores y productos. */
    private static class Reportes {

        final List<ResultadoVendedor> vendedores;
        final List<ResultadoProducto> productos;

        /** Crea el par de reportes. */
        Reportes(List<ResultadoVendedor> vendedores, List<ResultadoProducto> productos) {
            this.vendedores = vendedores;
            this.productos = productos;
        }

        /** Indica si ambos reportes estan vacios. */
        boolean estaVacio() {
            return vendedores.isEmpty() && productos.isEmpty();
        }
    }

    /** Datos cargados de productos.txt y vendedores.txt. */
    private static class Catalogo {

        final Map<Long, Long> preciosProductos = new HashMap<Long, Long>();
        final Map<Long, String> nombresProductos = new HashMap<Long, String>();
        final Map<Long, String> nombresVendedores = new HashMap<Long, String>();
    }

    /** Totales acumulados al recorrer los archivos de ventas. */
    private static class Totales {

        final Map<Long, Long> dineroPorVendedor = new HashMap<Long, Long>();
        final Map<Long, Integer> cantidadPorProducto = new HashMap<Long, Integer>();
    }

    /** Elige el modo segun el primer argumento: sin argumentos, "menu" o "gui". */
    public static void main(String[] args) {
        String modo = args.length > 0 ? args[0] : "";
        if (modo.equalsIgnoreCase("menu")) {
            ejecutarModoMenu();
        } else if (modo.equalsIgnoreCase("gui")) {
            SwingUtilities.invokeLater(() -> new VentanaVentas().setVisible(true));
        } else {
            ejecutarModoAutomatico();
        }
    }

    /** Genera los reportes sin pedir datos al usuario. */
    private static void ejecutarModoAutomatico() {
        ejecutar(salida -> {
            descargarReportes(salida);
            salida.println("Reportes generados con exito: " + LISTA_ARCHIVOS_REPORTE);
        }, "Error al generar los reportes: ");
    }

    /** Muestra el menu de consola hasta que el usuario elige Salir. */
    private static void ejecutarModoMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            continuar = ejecutarOpcion(leerOpcion(scanner));
        }
        scanner.close();
    }

    /** Ejecuta una opcion del menu y devuelve false si el usuario eligio Salir. */
    private static boolean ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1:
                ejecutar(Main::generarArchivos, "Error al generar los archivos: ");
                break;
            case 2:
                ejecutar(Main::verBase, "Error al cargar los archivos base: ");
                break;
            case 3:
                ejecutar(Main::verSalida, "Error al procesar las ventas: ");
                break;
            case 4:
                ejecutar(Main::descargarYConfirmar, "Error al descargar la salida: ");
                break;
            case 5:
                System.out.println("Saliendo del programa...");
                return false;
            case 6:
                ejecutar(Main::verUltimoReporteSerializado, "Error al leer el reporte guardado: ");
                break;
            default:
                System.out.println("Opcion invalida. Intente de nuevo.");
        }
        return true;
    }

    /** Ejecuta una accion en consola y muestra el mensaje de error si falla. */
    private static void ejecutar(Accion accion, String prefijoError) {
        try {
            accion.ejecutar(System.out);
        } catch (IOException e) {
            System.out.println(prefijoError + e.getMessage());
        }
    }

    /** Imprime el menu principal. */
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

    /** Lee la opcion del usuario, o devuelve -1 si no es un numero. */
    private static int leerOpcion(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Genera los archivos de prueba con GenerateInfoFiles. */
    static void generarArchivos(PrintStream salida) throws IOException {
        GenerateInfoFiles.createProductsFile(GenerateInfoFiles.TOTAL_PRODUCTOS);
        GenerateInfoFiles.createSalesManInfoFile(GenerateInfoFiles.TOTAL_VENDEDORES);
        GenerateInfoFiles.generarArchivosVentasParaTodosLosVendedores(GenerateInfoFiles.MAXIMO_VENTAS_POR_VENDEDOR);
        salida.println("Archivos generados exitosamente.");
    }

    /** Muestra el resumen de productos, vendedores y archivos de ventas disponibles. */
    static void verBase(PrintStream salida) throws IOException {
        Catalogo catalogo = cargarCatalogo(salida);

        salida.println();
        salida.println("=== Base cargada ===");
        salida.println("Productos         : " + catalogo.preciosProductos.size());
        salida.println("Vendedores        : " + catalogo.nombresVendedores.size());
        salida.println("Archivos de ventas: " + listarArchivosVentas().length);

        salida.println();
        salida.println("--- Catalogo de productos ---");
        for (Map.Entry<Long, String> producto : catalogo.nombresProductos.entrySet()) {
            salida.println("  ID " + producto.getKey() + ": " + producto.getValue()
                    + " | Precio: $" + catalogo.preciosProductos.get(producto.getKey()));
        }

        salida.println();
        salida.println("--- Lista de vendedores ---");
        for (Map.Entry<Long, String> vendedor : catalogo.nombresVendedores.entrySet()) {
            salida.println("  Doc " + vendedor.getKey() + ": " + vendedor.getValue());
        }
    }

    /** Procesa las ventas y muestra los reportes sin escribir archivos. */
    static void verSalida(PrintStream salida) throws IOException {
        procesarVentas(salida);
        imprimirReportes(salida, reportes);
    }

    /** Muestra los reportes guardados en reportes.ser. */
    static void verUltimoReporteSerializado(PrintStream salida) throws IOException {
        Reportes guardados = leerReportesSerializados();
        salida.println();
        salida.println("=== Ultimo reporte guardado (serializado) ===");
        imprimirReportes(salida, guardados);
    }

    /** Procesa las ventas si aun no hay reportes y los guarda en los CSV y en reportes.ser. */
    static void descargarReportes(PrintStream salida) throws IOException {
        if (reportes.estaVacio()) {
            procesarVentas(salida);
        }
        descargarSalida();
    }

    /** Descarga los reportes y confirma los archivos creados. */
    static void descargarYConfirmar(PrintStream salida) throws IOException {
        descargarReportes(salida);
        salida.println("Archivos descargados: " + LISTA_ARCHIVOS_REPORTE);
    }

    /** Lee los catalogos y todos los archivos ventas_*.txt y arma los reportes ordenados. */
    static void procesarVentas(PrintStream salida) throws IOException {
        Catalogo catalogo = cargarCatalogo(salida);
        Totales totales = new Totales();
        for (Long documento : catalogo.nombresVendedores.keySet()) {
            totales.dineroPorVendedor.put(documento, 0L);
        }

        File[] archivosVentas = listarArchivosVentas();
        if (archivosVentas.length == 0) {
            salida.println("Aviso: no se encontraron archivos de ventas en el directorio actual.");
        } else {
            salida.println("Procesando " + archivosVentas.length + " archivos de ventas...");
            for (File archivo : archivosVentas) {
                procesarArchivoVentas(archivo, catalogo, totales, salida);
            }
        }

        reportes = new Reportes(armarReporteVendedores(catalogo, totales), armarReporteProductos(catalogo, totales));
    }

    /** Escribe los dos CSV y guarda los reportes en reportes.ser. */
    static void descargarSalida() throws IOException {
        List<String> lineasVendedores = new ArrayList<String>();
        for (ResultadoVendedor vendedor : reportes.vendedores) {
            lineasVendedores.add(vendedor.aLinea());
        }
        GenerateInfoFiles.escribirLineas(new File(ARCHIVO_REPORTE_VENDEDORES), lineasVendedores);

        List<String> lineasProductos = new ArrayList<String>();
        for (ResultadoProducto producto : reportes.productos) {
            lineasProductos.add(producto.aLinea());
        }
        GenerateInfoFiles.escribirLineas(new File(ARCHIVO_REPORTE_PRODUCTOS), lineasProductos);

        guardarReportesSerializados();
    }

    /** Arma el reporte de vendedores ordenado de mayor a menor dinero. */
    private static List<ResultadoVendedor> armarReporteVendedores(Catalogo catalogo, Totales totales) {
        List<ResultadoVendedor> vendedores = new ArrayList<ResultadoVendedor>();
        for (Map.Entry<Long, Long> total : totales.dineroPorVendedor.entrySet()) {
            vendedores.add(new ResultadoVendedor(catalogo.nombresVendedores.get(total.getKey()), total.getValue()));
        }
        vendedores.sort((a, b) -> Long.compare(b.dinero, a.dinero));
        return vendedores;
    }

    /** Arma el reporte de productos ordenado de mayor a menor cantidad vendida. */
    private static List<ResultadoProducto> armarReporteProductos(Catalogo catalogo, Totales totales) {
        List<ResultadoProducto> productos = new ArrayList<ResultadoProducto>();
        for (Map.Entry<Long, Long> precio : catalogo.preciosProductos.entrySet()) {
            long idProducto = precio.getKey();
            productos.add(new ResultadoProducto(catalogo.nombresProductos.get(idProducto), precio.getValue(),
                    totales.cantidadPorProducto.getOrDefault(idProducto, 0)));
        }
        productos.sort((a, b) -> Integer.compare(b.cantidadVendida, a.cantidadVendida));
        return productos;
    }

    /** Imprime los dos reportes con sus encabezados. */
    private static void imprimirReportes(PrintStream salida, Reportes aImprimir) {
        salida.println();
        salida.println("=== Reporte de Vendedores (mayor a menor dinero recaudado) ===");
        for (ResultadoVendedor vendedor : aImprimir.vendedores) {
            salida.println(vendedor.aLinea());
        }

        salida.println();
        salida.println("=== Reporte de Productos (mayor a menor cantidad vendida) ===");
        for (ResultadoProducto producto : aImprimir.productos) {
            salida.println(producto.aLinea() + " (unidades vendidas: " + producto.cantidadVendida + ")");
        }
    }

    /** Guarda las dos listas de reportes, una tras otra, en reportes.ser. */
    private static void guardarReportesSerializados() throws IOException {
        try (ObjectOutputStream salida = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(ARCHIVO_SERIALIZADO)))) {
            salida.writeObject(reportes.vendedores);
            salida.writeObject(reportes.productos);
        }
    }

    /** Lee las dos listas de reportes guardadas en reportes.ser. */
    @SuppressWarnings("unchecked")
    private static Reportes leerReportesSerializados() throws IOException {
        File archivo = new File(ARCHIVO_SERIALIZADO);
        if (!archivo.exists()) {
            throw new IOException("No existe el archivo " + ARCHIVO_SERIALIZADO + ". Descargue los reportes primero.");
        }
        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(archivo)))) {
            List<ResultadoVendedor> vendedores = (List<ResultadoVendedor>) entrada.readObject();
            List<ResultadoProducto> productos = (List<ResultadoProducto>) entrada.readObject();
            return new Reportes(vendedores, productos);
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new IOException("El archivo " + ARCHIVO_SERIALIZADO + " esta danado o no es valido.");
        }
    }

    /** Devuelve los archivos ventas_*.txt del directorio actual. */
    private static File[] listarArchivosVentas() {
        File[] archivos = new File(".").listFiles(
                (carpeta, nombre) -> nombre.startsWith(GenerateInfoFiles.PREFIJO_VENTAS) && nombre.endsWith(".txt"));
        return archivos != null ? archivos : new File[0];
    }

    /** Carga productos.txt y vendedores.txt. */
    private static Catalogo cargarCatalogo(PrintStream salida) throws IOException {
        Catalogo catalogo = new Catalogo();
        for (String linea : leerLineasBase(GenerateInfoFiles.ARCHIVO_PRODUCTOS)) {
            agregarProducto(catalogo, linea, salida);
        }
        for (String linea : leerLineasBase(GenerateInfoFiles.ARCHIVO_VENDEDORES)) {
            agregarVendedor(catalogo, linea, salida);
        }
        return catalogo;
    }

    /** Lee las lineas no vacias de un archivo base, sin BOM ni espacios en los extremos. */
    private static List<String> leerLineasBase(String nombreArchivo) throws IOException {
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) {
            throw new IOException("No se encontro el archivo " + nombreArchivo);
        }
        List<String> lineas = new ArrayList<String>();
        for (String linea : GenerateInfoFiles.leerLineas(archivo)) {
            String limpia = quitarBom(linea).trim();
            if (!limpia.isEmpty()) {
                lineas.add(limpia);
            }
        }
        return lineas;
    }

    /** Agrega al catalogo un producto con formato IDProducto;Nombre;Precio. */
    private static void agregarProducto(Catalogo catalogo, String linea, PrintStream salida) {
        String[] partes = linea.split(GenerateInfoFiles.DELIMITADOR);
        if (partes.length < 3) {
            return;
        }
        try {
            long id = Long.parseLong(partes[0].trim());
            long precio = Long.parseLong(partes[2].trim());
            catalogo.preciosProductos.put(id, precio);
            catalogo.nombresProductos.put(id, partes[1].trim());
        } catch (NumberFormatException e) {
            avisarLineaInvalida(salida, GenerateInfoFiles.ARCHIVO_PRODUCTOS, linea);
        }
    }

    /** Agrega al catalogo un vendedor con formato TipoDocumento;NumeroDocumento;Nombres;Apellidos. */
    private static void agregarVendedor(Catalogo catalogo, String linea, PrintStream salida) {
        String[] partes = linea.split(GenerateInfoFiles.DELIMITADOR);
        if (partes.length < 4) {
            return;
        }
        try {
            long documento = Long.parseLong(partes[1].trim());
            catalogo.nombresVendedores.put(documento, partes[2].trim() + " " + partes[3].trim());
        } catch (NumberFormatException e) {
            avisarLineaInvalida(salida, GenerateInfoFiles.ARCHIVO_VENDEDORES, linea);
        }
    }

    /** Avisa que una linea de un archivo base no es valida. */
    private static void avisarLineaInvalida(PrintStream salida, String nombreArchivo, String linea) {
        salida.println("Aviso: linea invalida en " + nombreArchivo + " ('" + linea + "'), se omite.");
    }

    /** Lee un archivo de ventas y acumula sus ventas en los totales. */
    private static void procesarArchivoVentas(File archivo, Catalogo catalogo, Totales totales, PrintStream salida) {
        List<String> lineas;
        try {
            lineas = GenerateInfoFiles.leerLineas(archivo);
        } catch (IOException e) {
            salida.println("Error al leer " + archivo.getName() + ": " + e.getMessage());
            return;
        }
        acumularVentas(archivo.getName(), lineas, catalogo, totales, salida);
    }

    /** Valida la cabecera de un archivo de ventas y acumula cada una de sus ventas. */
    private static void acumularVentas(String nombreArchivo, List<String> lineas, Catalogo catalogo,
                                       Totales totales, PrintStream salida) {
        if (lineas.isEmpty()) {
            salida.println("Aviso: archivo " + nombreArchivo + " esta vacio, se omite.");
            return;
        }
        Long documento = leerDocumentoCabecera(nombreArchivo, lineas.get(0), salida);
        if (documento == null) {
            return;
        }
        if (!catalogo.nombresVendedores.containsKey(documento)) {
            salida.println("Aviso: vendedor no registrado (" + documento + ") en " + nombreArchivo + ", se omite.");
            return;
        }
        for (String linea : lineas.subList(1, lineas.size())) {
            String venta = linea.trim();
            if (!venta.isEmpty()) {
                registrarVenta(nombreArchivo, venta, documento, catalogo, totales, salida);
            }
        }
    }

    /** Obtiene el documento de la cabecera TipoDocumento;NumeroDocumento, o null si no es valida. */
    private static Long leerDocumentoCabecera(String nombreArchivo, String cabecera, PrintStream salida) {
        String[] partes = quitarBom(cabecera).trim().split(GenerateInfoFiles.DELIMITADOR);
        if (partes.length < 2) {
            salida.println("Aviso: encabezado invalido en " + nombreArchivo + ", se omite.");
            return null;
        }
        try {
            return Long.parseLong(partes[1].trim());
        } catch (NumberFormatException e) {
            salida.println("Aviso: documento invalido en " + nombreArchivo + ", se omite.");
            return null;
        }
    }

    /** Suma una venta IDProducto;Cantidad; al vendedor y al producto, o avisa si no es valida. */
    private static void registrarVenta(String nombreArchivo, String linea, long documento,
                                       Catalogo catalogo, Totales totales, PrintStream salida) {
        try {
            String[] partes = linea.split(GenerateInfoFiles.DELIMITADOR);
            long idProducto = Long.parseLong(partes[0].trim());
            int cantidad = Integer.parseInt(partes[1].trim());

            if (cantidad <= 0) {
                salida.println("Aviso: cantidad invalida en " + nombreArchivo + " ('" + linea + "'), se omite.");
                return;
            }
            Long precio = catalogo.preciosProductos.get(idProducto);
            if (precio == null) {
                salida.println("Aviso: producto inexistente (" + idProducto + ") en " + nombreArchivo + ", se omite.");
                return;
            }

            totales.dineroPorVendedor.merge(documento, precio * cantidad, Long::sum);
            totales.cantidadPorProducto.merge(idProducto, cantidad, Integer::sum);
        } catch (Exception e) {
            salida.println("Aviso: linea mal formada en " + nombreArchivo + " ('" + linea + "'), se omite.");
        }
    }

    /** Elimina el caracter BOM que algunos editores agregan al inicio del archivo. */
    private static String quitarBom(String linea) {
        return linea.replace(BOM, "");
    }
}
