import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Ventana grafica del proyecto. Cada boton reutiliza los metodos de {@link Main}
 * y muestra lo que estos imprimen en el area de texto.
 *
 * @author Santiago Gonzalez Agudelo, Danys Gutierrez Franco, Iveth Gutierrez Franco, Daniela Natalia Rios Manco
 * @version 3.0
 */
public final class VentanaVentas extends JFrame {

    /** Version de la clase para la serializacion. */
    private static final long serialVersionUID = 1L;

    /** Codificacion usada para pasar la salida de los metodos al area de texto. */
    private static final String CODIFICACION = "UTF-8";

    /** Area donde se muestra el resultado de cada accion. */
    private final JTextArea areaResultado = new JTextArea();

    /** Construye la ventana con los botones a la izquierda y el area de texto a la derecha. */
    public VentanaVentas() {
        super("Ventas y Vendedores");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 0, 10));
        agregarBoton(panelBotones, "Generar archivos", Main::generarArchivos);
        agregarBoton(panelBotones, "Ver base", Main::verBase);
        agregarBoton(panelBotones, "Ver salida", Main::verSalida);
        agregarBoton(panelBotones, "Descargar CSV", Main::descargarSalida);
        agregarBoton(panelBotones, "Ver serializado", Main::verReporteSerializado);

        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        panelIzquierdo.add(panelBotones, BorderLayout.NORTH);

        areaResultado.setEditable(false);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaResultado.setText("Seleccione una accion en los botones de la izquierda.");
        JScrollPane panelTexto = new JScrollPane(areaResultado);
        panelTexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 5, 10, 10), panelTexto.getBorder()));

        add(panelIzquierdo, BorderLayout.WEST);
        add(panelTexto, BorderLayout.CENTER);
    }

    /**
     * Crea un boton que ejecuta la accion indicada.
     *
     * @param panel  panel donde se agrega el boton
     * @param texto  texto del boton
     * @param accion accion de {@link Main} que ejecuta el boton
     */
    private void agregarBoton(JPanel panel, String texto, Main.Accion accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(evento -> ejecutarAccion(texto, accion));
        panel.add(boton);
    }

    /**
     * Ejecuta la accion, muestra su salida y avisa de los errores con un dialogo.
     *
     * @param titulo nombre de la accion, usado en el dialogo de error
     * @param accion accion a ejecutar
     */
    private void ejecutarAccion(String titulo, Main.Accion accion) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        String error = null;
        try (PrintStream salida = new PrintStream(buffer, true, CODIFICACION)) {
            accion.ejecutar(salida);
        } catch (IOException e) {
            error = e.getMessage();
        }
        areaResultado.setText(leerBuffer(buffer).trim());
        areaResultado.setCaretPosition(0);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error en " + titulo, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Convierte el contenido del buffer en texto.
     *
     * @param buffer bytes escritos por la accion
     * @return el texto escrito
     */
    private static String leerBuffer(ByteArrayOutputStream buffer) {
        try {
            return buffer.toString(CODIFICACION);
        } catch (UnsupportedEncodingException e) {
            return buffer.toString();
        }
    }
}
