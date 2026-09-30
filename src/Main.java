import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static class ResultadoVendedor {
        String nombreCompleto;
        double dinero;

        ResultadoVendedor(String nombreCompleto, double dinero) {
            this.nombreCompleto = nombreCompleto;
            this.dinero = dinero;
        }
    }

    private static class ResultadoProducto {
        String nombre;
        long precio;
        int cantidadVendida;

        ResultadoProducto(String nombre, long precio, int cantidadVendida) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidadVendida = cantidadVendida;
        }
    }

    private static List<ResultadoVendedor> reporteVendedores = new ArrayList<>();
    private static List<ResultadoProducto> reporteProductos = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            int opcion = leerOpcion(scanner);

            switch (opcion) {
                case 1:
                    generarArchivos();
                    break;
                case 2:
                    verBase();
                    break;
                case 3:
                    verSalida();
                    break;
                case 4:
                    descargarSalida();
                    break;
                case 5:
                    salir = true;
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opcion invalida. Intente de nuevo.");
            }
        }

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("========= MENU PRINCIPAL =========");
        System.out.println("[1] Generar Archivos");
        System.out.println("[2] Importar / Ver Base");
        System.out.println("[3] Importar / Ver Salida");
        System.out.println("[4] Descargar Salida");
        System.out.println("[5] Salir");
        System.out.print("Seleccione una opcion: ");
    }

    private static int leerOpcion(Scanner scanner) {
        String entrada = scanner.nextLine().trim();
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void generarArchivos() {
        try {
            GenerateInfoFiles.createProductsFile(12);
            GenerateInfoFiles.createSalesManInfoFile(10);
            GenerateInfoFiles.generarArchivosVentasParaTodosLosVendedores(10);
            System.out.println("Archivos generados exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al generar los archivos: " + e.getMessage());
        }
    }

    private static void verBase() {
    }

    private static void verSalida() {
        Map<Long, Long> preciosProductos = new HashMap<>();
        Map<Long, String> nombresProductos = new HashMap<>();
        Map<Long, String> nombresVendedores = new HashMap<>();
        Map<Long, Double> dineroPorVendedor = new HashMap<>();
        Map<Long, Integer> cantidadPorProducto = new HashMap<>();

        try {
            cargarProductos(preciosProductos, nombresProductos);
            cargarVendedores(nombresVendedores, dineroPorVendedor);
        } catch (IOException e) {
            System.out.println("Error al cargar los archivos base: " + e.getMessage());
            return;
        }

        File carpeta = new File(".");
        File[] archivosVentas = carpeta.listFiles((dir, nombre) -> nombre.startsWith("ventas_") && nombre.endsWith(".txt"));

        if (archivosVentas != null) {
            for (File archivo : archivosVentas) {
                procesarArchivoVentas(archivo, preciosProductos, nombresVendedores, cantidadPorProducto, dineroPorVendedor);
            }
        }

        reporteVendedores = new ArrayList<>();
        for (Map.Entry<Long, Double> entrada : dineroPorVendedor.entrySet()) {
            String nombreCompleto = nombresVendedores.get(entrada.getKey());
            if (nombreCompleto != null) {
                reporteVendedores.add(new ResultadoVendedor(nombreCompleto, entrada.getValue()));
            }
        }
        reporteVendedores.sort((a, b) -> Double.compare(b.dinero, a.dinero));

        reporteProductos = new ArrayList<>();
        for (Map.Entry<Long, Long> entrada : preciosProductos.entrySet()) {
            long idProducto = entrada.getKey();
            int cantidadVendida = cantidadPorProducto.getOrDefault(idProducto, 0);
            String nombre = nombresProductos.get(idProducto);
            reporteProductos.add(new ResultadoProducto(nombre, entrada.getValue(), cantidadVendida));
        }
        reporteProductos.sort((a, b) -> Integer.compare(b.cantidadVendida, a.cantidadVendida));

        System.out.println();
        System.out.println("=== Reporte de Vendedores (mayor a menor dinero recaudado) ===");
        for (ResultadoVendedor resultado : reporteVendedores) {
            System.out.printf(Locale.US, "%s;%.0f%n", resultado.nombreCompleto, resultado.dinero);
        }

        System.out.println();
        System.out.println("=== Reporte de Productos (mayor a menor cantidad vendida) ===");
        for (ResultadoProducto resultado : reporteProductos) {
            System.out.println(resultado.nombre + ";" + resultado.precio + " (unidades vendidas: " + resultado.cantidadVendida + ")");
        }
    }

    private static void descargarSalida() {
    }

    private static void cargarProductos(Map<Long, Long> preciosProductos, Map<Long, String> nombresProductos) throws IOException {
        File archivo = new File("productos.txt");
        if (!archivo.exists()) {
            throw new IOException("No se encontro el archivo productos.txt");
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.replace("﻿", "").trim();
                if (linea.isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";");
                if (partes.length < 3) {
                    continue;
                }

                try {
                    long id = Long.parseLong(partes[0].trim());
                    String nombre = partes[1].trim();
                    long precio = Long.parseLong(partes[2].trim());

                    preciosProductos.put(id, precio);
                    nombresProductos.put(id, nombre);
                } catch (NumberFormatException e) {
                    System.out.println("Aviso: linea invalida en productos.txt ('" + linea + "'), se omite.");
                }
            }
        }
    }

    private static void cargarVendedores(Map<Long, String> nombresVendedores, Map<Long, Double> dineroPorVendedor) throws IOException {
        File archivo = new File("vendedores.txt");
        if (!archivo.exists()) {
            throw new IOException("No se encontro el archivo vendedores.txt");
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.replace("﻿", "").trim();
                if (linea.isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";");
                if (partes.length < 4) {
                    continue;
                }

                try {
                    long numeroDocumento = Long.parseLong(partes[1].trim());
                    String nombreCompleto = partes[2].trim() + " " + partes[3].trim();

                    nombresVendedores.put(numeroDocumento, nombreCompleto);
                    dineroPorVendedor.put(numeroDocumento, 0.0);
                } catch (NumberFormatException e) {
                    System.out.println("Aviso: linea invalida en vendedores.txt ('" + linea + "'), se omite.");
                }
            }
        }
    }

    private static void procesarArchivoVentas(File archivo, Map<Long, Long> preciosProductos,
                                               Map<Long, String> nombresVendedores,
                                               Map<Long, Integer> cantidadPorProducto,
                                               Map<Long, Double> dineroPorVendedor) {
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String primeraLinea = lector.readLine();
            if (primeraLinea == null) {
                System.out.println("Aviso: archivo " + archivo.getName() + " esta vacio, se omite.");
                return;
            }

            primeraLinea = primeraLinea.replace("﻿", "").trim();
            String[] cabecera = primeraLinea.split(";");
            if (cabecera.length < 2) {
                System.out.println("Aviso: encabezado invalido en " + archivo.getName() + ", se omite.");
                return;
            }

            long numeroDocumento;
            try {
                numeroDocumento = Long.parseLong(cabecera[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Aviso: documento invalido en " + archivo.getName() + ", se omite.");
                return;
            }

            if (!nombresVendedores.containsKey(numeroDocumento)) {
                System.out.println("Aviso: vendedor no registrado (" + numeroDocumento + ") en " + archivo.getName() + ", se omite.");
                return;
            }

            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }

                try {
                    String[] partes = linea.split(";");
                    long idProducto = Long.parseLong(partes[0].trim());
                    int cantidad = Integer.parseInt(partes[1].trim());

                    if (cantidad <= 0) {
                        System.out.println("Aviso: cantidad invalida en " + archivo.getName() + " ('" + linea + "'), se omite.");
                        continue;
                    }

                    Long precio = preciosProductos.get(idProducto);
                    if (precio == null) {
                        System.out.println("Aviso: producto inexistente (" + idProducto + ") en " + archivo.getName() + ", se omite.");
                        continue;
                    }

                    double subtotal = precio * cantidad;
                    dineroPorVendedor.merge(numeroDocumento, subtotal, Double::sum);
                    cantidadPorProducto.merge(idProducto, cantidad, Integer::sum);
                } catch (Exception e) {
                    System.out.println("Aviso: linea mal formada en " + archivo.getName() + " ('" + linea + "'), se omite.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer " + archivo.getName() + ": " + e.getMessage());
        }
    }
}
