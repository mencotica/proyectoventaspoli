import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Ventana Swing que ejecuta las acciones de Main y muestra su resultado. */
public final class VentanaVentas extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String CODIFICACION = "UTF-8";
    private static final int ANCHO = 800;
    private static final int ALTO = 550;

    private final JTextArea areaResultados = new JTextArea();

    /** Construye la ventana con sus botones y el area de resultados. */
    public VentanaVentas() {
        super("Ventas y Vendedores");
        areaResultados.setEditable(false);
        areaResultados.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 0, 8));
        panelBotones.add(crearBoton("Generar archivos", Main::generarArchivos));
        panelBotones.add(crearBoton("Ver base", Main::verBase));
        panelBotones.add(crearBoton("Ver salida", Main::verSalida));
        panelBotones.add(crearBoton("Descargar CSV", Main::descargarYConfirmar));
        panelBotones.add(crearBoton("Ver serializado", Main::verUltimoReporteSerializado));

        JPanel contenedorBotones = new JPanel(new BorderLayout());
        contenedorBotones.add(panelBotones, BorderLayout.NORTH);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        raiz.add(contenedorBotones, BorderLayout.WEST);
        raiz.add(new JScrollPane(areaResultados), BorderLayout.CENTER);

        setContentPane(raiz);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(ANCHO, ALTO);
        setLocationRelativeTo(null);
    }

    /** Crea un boton que ejecuta la accion indicada al presionarlo. */
    private JButton crearBoton(String texto, Main.Accion accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(evento -> ejecutarAccion(accion));
        return boton;
    }

    /** Ejecuta la accion, muestra su texto en el area y los errores en un dialogo. */
    private void ejecutarAccion(Main.Accion accion) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            PrintStream salida = new PrintStream(buffer, true, CODIFICACION);
            try {
                accion.ejecutar(salida);
            } finally {
                salida.flush();
                areaResultados.setText(buffer.toString(CODIFICACION));
                areaResultados.setCaretPosition(0);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
