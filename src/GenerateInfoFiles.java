import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Clase encargada de generar los archivos planos de prueba con datos pseudoaleatorios
 * para el sistema de ventas.
 * <p>
 * Requisitos de la Entrega 1 (Semana 3):
 * - createProductsFile: genera "productos.txt" con formato ID;Nombre;Precio.
 * - createSalesManInfoFile: genera "vendedores.txt" con formato TipoDoc;NumDoc;Nombres;Apellidos.
 * - createSalesMenFile: genera archivos de ventas individuales por cada vendedor.
 * - Metodo main independiente que ejecuta la generacion completa sin solicitar datos al usuario.
 * </p>
 *
 * @author Proyecto de Ventas
 * @version 1.0
 */
public class GenerateInfoFiles {

    /** Nombre del archivo que almacena el catalogo de productos disponibles. */
    public static final String ARCHIVO_PRODUCTOS = "productos.txt";

    /** Nombre del archivo que almacena la informacion general de los vendedores. */
    public static final String ARCHIVO_VENDEDORES = "vendedores.txt";

    /** Prefijo para nombrar los archivos individuales de ventas de cada vendedor. */
    public static final String PREFIJO_VENTAS = "ventas_";

    /** Delimitador estandar utilizado en los archivos de texto plano. */
    public static final String DELIMITADOR = ";";

    /** Catalogo de nombres de productos de tecnologia y oficina para pruebas. */
    private static final String[] NOMBRES_PRODUCTOS = {
            "Teclado mecanico", "Mouse inalambrico", "Monitor 24 pulgadas", "Audifonos bluetooth",
            "Impresora multifuncional", "Silla ergonomica", "Escritorio elevable", "Memoria USB 64GB",
            "Webcam Full HD", "Cargador portatil", "Disco Solido SSD 1TB", "Base refrigerante",
            "Microfono USB", "Cable HDMI 4K", "Altavoces de escritorio"
    };

    /** Lista de nombres comunes para la generacion pseudoaleatoria de vendedores. */
    private static final String[] PRIMEROS_NOMBRES = {
            "Santiago", "Valentina", "Andres", "Camila", "Sebastian",
            "Daniela", "Juan", "Laura", "Mateo", "Isabella",
            "Carlos", "Mariana", "Alejandro", "Sofia", "Diego"
    };

    /** Lista de apellidos comunes para la generacion pseudoaleatoria de vendedores. */
    private static final String[] APELLIDOS = {
            "Gonzalez", "Rodriguez", "Martinez", "Gomez", "Ramirez",
            "Lopez", "Torres", "Vargas", "Castro", "Moreno",
            "Herrera", "Perez", "Sanchez", "Navarro", "Morales"
    };

    /**
     * Crea el archivo de informacion de productos ("productos.txt") con datos pseudoaleatorios.
     * Formato por linea: IDProducto;NombreProducto;PrecioPorUnidad
     *
     * @param productsCount Cantidad de productos a generar en el archivo.
     * @throws IOException Si ocurre un error durante la escritura del archivo.
     */
    public static void createProductsFile(int productsCount) throws IOException {
        int precioMinimo = 15000;
        int precioMaximo = 4500000;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_PRODUCTOS))) {
            for (int i = 0; i < productsCount; i++) {
                int id = i + 1;
                String nombre = NOMBRES_PRODUCTOS[i % NOMBRES_PRODUCTOS.length];
                int precio = ThreadLocalRandom.current().nextInt(precioMinimo, precioMaximo);

                bw.write(String.format("%d%s%s%s%d", id, DELIMITADOR, nombre, DELIMITADOR, precio));
                bw.newLine();
            }
        }
    }

    /**
     * Crea el archivo de informacion de vendedores ("vendedores.txt") con datos pseudoaleatorios coherentes.
     * Formato por linea: TipoDocumento;NumeroDocumento;Nombres;Apellidos
     *
     * @param salesmanCount Cantidad de vendedores a generar.
     * @throws IOException Si ocurre un error durante la creacion o escritura del archivo.
     */
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        String tipoDocumento = "CC";
        int docMin = 10000000;
        int docMax = 99999999;
        Random random = new Random();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_VENDEDORES))) {
            for (int i = 0; i < salesmanCount; i++) {
                long numeroDocumento = docMin + (long) (random.nextDouble() * (docMax - docMin));
                String nombre = PRIMEROS_NOMBRES[random.nextInt(PRIMEROS_NOMBRES.length)];
                String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];

                bw.write(String.format("%s%s%d%s%s%s%s", tipoDocumento, DELIMITADOR, numeroDocumento,
                        DELIMITADOR, nombre, DELIMITADOR, apellido));
                bw.newLine();
            }
        }
    }

    /**
     * Crea un archivo plano con las ventas pseudoaleatorias de un vendedor en particular.
     * <p>
     * Formato del archivo:
     * Primera linea: TipoDocumento;NumeroDocumento
     * Lineas siguientes (una venta por linea): IDProducto;CantidadVendida;
     * </p>
     *
     * @param randomSalesCount Cantidad de transacciones/ventas a generar en el archivo.
     * @param name             Nombre o identificador descriptivo del vendedor.
     * @param id               Numero de documento unico del vendedor.
     * @throws IOException Si ocurre un error al escribir el archivo en el disco.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        String nombreSanitizado = name.trim().replaceAll("\\s+", "_");
        String nombreArchivo = PREFIJO_VENTAS + nombreSanitizado + "_" + id + ".txt";

        // Determinamos la cantidad de productos disponibles leyendo el catalogo o usando un valor base
        int maxProductosDisponibles = contarProductosDisponibles();
        if (maxProductosDisponibles <= 0) {
            maxProductosDisponibles = 10;
        }

        Random random = new Random();
        String tipoDocumento = "CC";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            // Primera linea: TipoDocumento;NumeroDocumento
            bw.write(String.format("%s%s%d", tipoDocumento, DELIMITADOR, id));
            bw.newLine();

            // Lineas de ventas: IDProducto;Cantidad;
            for (int i = 0; i < randomSalesCount; i++) {
                int idProducto = 1 + random.nextInt(maxProductosDisponibles);
                int cantidadVendida = 1 + random.nextInt(15);

                bw.write(String.format("%d%s%d%s", idProducto, DELIMITADOR, cantidadVendida, DELIMITADOR));
                bw.newLine();
            }
        }
    }

    /**
     * Genera archivos de ventas individuales para cada uno de los vendedores listados en vendedores.txt.
     *
     * @param maxVentasPorVendedor Maximo numero de transacciones que puede tener un vendedor.
     * @throws IOException Si ocurre un fallo al leer vendedores.txt o al crear los archivos de ventas.
     */
    public static void generarArchivosVentasParaTodosLosVendedores(int maxVentasPorVendedor) throws IOException {
        File archivoVendedores = new File(ARCHIVO_VENDEDORES);
        if (!archivoVendedores.exists()) {
            throw new IOException("El archivo " + ARCHIVO_VENDEDORES + " no existe. Generelo primero.");
        }

        Random random = new Random();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoVendedores))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(DELIMITADOR);
                if (partes.length >= 4) {
                    long idVendedor = Long.parseLong(partes[1].trim());
                    String nombreCompleto = partes[2].trim() + "_" + partes[3].trim();
                    int ventasCount = 1 + random.nextInt(maxVentasPorVendedor);

                    createSalesMenFile(ventasCount, nombreCompleto, idVendedor);
                }
            }
        }
    }

    /**
     * Cuenta cuantos productos existen en el archivo de productos para no generar IDs inexistentes.
     *
     * @return Numero de productos en productos.txt, o 0 si el archivo no existe.
     */
    private static int contarProductosDisponibles() {
        File archivo = new File(ARCHIVO_PRODUCTOS);
        if (!archivo.exists()) {
            return 0;
        }

        int contador = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            while (br.readLine() != null) {
                contador++;
            }
        } catch (IOException e) {
            return 0;
        }
        return contador;
    }

    /**
     * Metodo principal de ejecucion de la clase GenerateInfoFiles.
     * Genera automaticamente los archivos de entrada (productos, vendedores y ventas de cada uno)
     * sin solicitar entradas por teclado al usuario, cumpliendo estrictamente con la Entrega 1.
     *
     * @param args Argumentos de linea de comandos (no requeridos).
     */
    public static void main(String[] args) {
        System.out.println("=== INICIANDO GENERACION DE ARCHIVOS DE PRUEBA (ENTREGA 1) ===");
        try {
            int totalProductos = 12;
            int totalVendedores = 6;
            int maxVentasPorVendedor = 10;

            // 1. Generar archivo de productos
            createProductsFile(totalProductos);
            System.out.println("[OK] Archivo '" + ARCHIVO_PRODUCTOS + "' generado exitosamente (" + totalProductos + " productos).");

            // 2. Generar archivo de vendedores
            createSalesManInfoFile(totalVendedores);
            System.out.println("[OK] Archivo '" + ARCHIVO_VENDEDORES + "' generado exitosamente (" + totalVendedores + " vendedores).");

            // 3. Generar archivos individuales de ventas por cada vendedor
            generarArchivosVentasParaTodosLosVendedores(maxVentasPorVendedor);
            System.out.println("[OK] Archivos de ventas individuales por vendedor generados exitosamente.");

            System.out.println("\n>>> PROCESO FINALIZADO CON EXITO: Todos los archivos de entrada fueron creados correctamente. <<<");
        } catch (Exception e) {
            System.err.println("\n>>> ERROR EN LA GENERACION DE ARCHIVOS: " + e.getMessage() + " <<<");
        }
    }

}
