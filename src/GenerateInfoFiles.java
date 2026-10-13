import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Genera los archivos planos de prueba (productos, vendedores y ventas) con datos pseudoaleatorios. */
public class GenerateInfoFiles {

    public static final String ARCHIVO_PRODUCTOS = "productos.txt";
    public static final String ARCHIVO_VENDEDORES = "vendedores.txt";
    public static final String PREFIJO_VENTAS = "ventas_";
    public static final String DELIMITADOR = ";";

    static final int TOTAL_PRODUCTOS = 12;
    static final int TOTAL_VENDEDORES = 6;
    static final int MAXIMO_VENTAS_POR_VENDEDOR = 10;

    private static final String TIPO_DOCUMENTO = "CC";
    private static final int PRECIO_MINIMO = 15000;
    private static final int PRECIO_MAXIMO = 4500000;
    private static final int DOCUMENTO_MINIMO = 10000000;
    private static final int DOCUMENTO_MAXIMO = 99999999;
    private static final int PRODUCTOS_POR_DEFECTO = 10;
    private static final int CANTIDAD_MAXIMA_POR_VENTA = 15;

    private static final String[] NOMBRES_PRODUCTOS = {
            "Teclado mecanico", "Mouse inalambrico", "Monitor 24 pulgadas", "Audifonos bluetooth",
            "Impresora multifuncional", "Silla ergonomica", "Escritorio elevable", "Memoria USB 64GB",
            "Webcam Full HD", "Cargador portatil", "Disco Solido SSD 1TB", "Base refrigerante",
            "Microfono USB", "Cable HDMI 4K", "Altavoces de escritorio"
    };

    private static final String[] PRIMEROS_NOMBRES = {
            "Santiago", "Valentina", "Andres", "Camila", "Sebastian",
            "Daniela", "Juan", "Laura", "Mateo", "Isabella",
            "Carlos", "Mariana", "Alejandro", "Sofia", "Diego"
    };

    private static final String[] APELLIDOS = {
            "Gonzalez", "Rodriguez", "Martinez", "Gomez", "Ramirez",
            "Lopez", "Torres", "Vargas", "Castro", "Moreno",
            "Herrera", "Perez", "Sanchez", "Navarro", "Morales"
    };

    /** Genera todos los archivos de entrada sin pedir datos al usuario. */
    public static void main(String[] args) {
        System.out.println("=== INICIANDO GENERACION DE ARCHIVOS DE PRUEBA ===");
        try {
            createProductsFile(TOTAL_PRODUCTOS);
            System.out.println("[OK] Archivo '" + ARCHIVO_PRODUCTOS + "' generado exitosamente (" + TOTAL_PRODUCTOS + " productos).");

            createSalesManInfoFile(TOTAL_VENDEDORES);
            System.out.println("[OK] Archivo '" + ARCHIVO_VENDEDORES + "' generado exitosamente (" + TOTAL_VENDEDORES + " vendedores).");

            generarArchivosVentasParaTodosLosVendedores(MAXIMO_VENTAS_POR_VENDEDOR);
            System.out.println("[OK] Archivos de ventas individuales por vendedor generados exitosamente.");

            System.out.println("\n>>> PROCESO FINALIZADO CON EXITO: Todos los archivos de entrada fueron creados correctamente. <<<");
        } catch (Exception e) {
            System.err.println("\n>>> ERROR EN LA GENERACION DE ARCHIVOS: " + e.getMessage() + " <<<");
        }
    }

    /**
     * Crea productos.txt con el formato IDProducto;NombreProducto;PrecioPorUnidad.
     *
     * @param productsCount cantidad de productos a generar
     * @throws IOException si falla la escritura del archivo
     */
    public static void createProductsFile(int productsCount) throws IOException {
        List<String> lineas = new ArrayList<String>();
        for (int i = 0; i < productsCount; i++) {
            String nombre = NOMBRES_PRODUCTOS[i % NOMBRES_PRODUCTOS.length];
            int precio = ThreadLocalRandom.current().nextInt(PRECIO_MINIMO, PRECIO_MAXIMO);
            lineas.add((i + 1) + DELIMITADOR + nombre + DELIMITADOR + precio);
        }
        escribirLineas(new File(ARCHIVO_PRODUCTOS), lineas);
    }

    /**
     * Crea vendedores.txt con el formato TipoDocumento;NumeroDocumento;Nombres;Apellidos.
     *
     * @param salesmanCount cantidad de vendedores a generar
     * @throws IOException si falla la escritura del archivo
     */
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        Random random = new Random();
        List<String> lineas = new ArrayList<String>();
        for (int i = 0; i < salesmanCount; i++) {
            long numeroDocumento = DOCUMENTO_MINIMO + (long) (random.nextDouble() * (DOCUMENTO_MAXIMO - DOCUMENTO_MINIMO));
            String nombre = PRIMEROS_NOMBRES[random.nextInt(PRIMEROS_NOMBRES.length)];
            String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];
            lineas.add(TIPO_DOCUMENTO + DELIMITADOR + numeroDocumento + DELIMITADOR + nombre + DELIMITADOR + apellido);
        }
        escribirLineas(new File(ARCHIVO_VENDEDORES), lineas);
    }

    /**
     * Crea el archivo de ventas de un vendedor: cabecera TipoDocumento;NumeroDocumento y lineas IDProducto;Cantidad;.
     *
     * @param randomSalesCount cantidad de ventas a generar
     * @param name             nombre del vendedor usado en el nombre del archivo
     * @param id               numero de documento del vendedor
     * @throws IOException si falla la escritura del archivo
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        String nombreArchivo = PREFIJO_VENTAS + name.trim().replaceAll("\\s+", "_") + "_" + id + ".txt";
        int productosDisponibles = contarProductosDisponibles();
        if (productosDisponibles <= 0) {
            productosDisponibles = PRODUCTOS_POR_DEFECTO;
        }

        Random random = new Random();
        List<String> lineas = new ArrayList<String>();
        lineas.add(TIPO_DOCUMENTO + DELIMITADOR + id);
        for (int i = 0; i < randomSalesCount; i++) {
            int idProducto = 1 + random.nextInt(productosDisponibles);
            int cantidad = 1 + random.nextInt(CANTIDAD_MAXIMA_POR_VENTA);
            lineas.add(idProducto + DELIMITADOR + cantidad + DELIMITADOR);
        }
        escribirLineas(new File(nombreArchivo), lineas);
    }

    /**
     * Crea un archivo de ventas por cada vendedor de vendedores.txt; uno de cada tres recibe un segundo archivo "_2".
     *
     * @param maxVentasPorVendedor maximo de ventas por archivo
     * @throws IOException si no existe vendedores.txt o falla la escritura de algun archivo
     */
    public static void generarArchivosVentasParaTodosLosVendedores(int maxVentasPorVendedor) throws IOException {
        File archivoVendedores = new File(ARCHIVO_VENDEDORES);
        if (!archivoVendedores.exists()) {
            throw new IOException("El archivo " + ARCHIVO_VENDEDORES + " no existe. Generelo primero.");
        }

        Random random = new Random();
        int indiceVendedor = 0;
        for (String linea : leerLineas(archivoVendedores)) {
            String[] partes = linea.trim().split(DELIMITADOR);
            if (partes.length < 4) {
                continue;
            }
            long documento = Long.parseLong(partes[1].trim());
            String nombreCompleto = partes[2].trim() + "_" + partes[3].trim();

            createSalesMenFile(1 + random.nextInt(maxVentasPorVendedor), nombreCompleto, documento);
            if (indiceVendedor % 3 == 0) {
                createSalesMenFile(1 + random.nextInt(maxVentasPorVendedor), nombreCompleto + "_2", documento);
            }
            indiceVendedor++;
        }
    }

    /** Lee todas las lineas de un archivo de texto. */
    static List<String> leerLineas(File archivo) throws IOException {
        List<String> lineas = new ArrayList<String>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }

    /** Escribe las lineas en el archivo, una por renglon, sobrescribiendolo. */
    static void escribirLineas(File archivo, List<String> lineas) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
        }
    }

    /** Cuenta las lineas de productos.txt, o devuelve 0 si no se puede leer. */
    private static int contarProductosDisponibles() {
        try {
            return leerLineas(new File(ARCHIVO_PRODUCTOS)).size();
        } catch (IOException e) {
            return 0;
        }
    }
}
