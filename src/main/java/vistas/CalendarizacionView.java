package vistas;

import controladores.CalendarizacionController;
import modelo.Categoria;
import modelo.Recurso;
import modelo.Reserva;
import servicios.PDFService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarizacionView extends JPanel {

    private final CalendarizacionController controller;
    private final PDFService pdfService;

    private JComboBox<Categoria> cbCategoria;
    private JTextField txtFecha;
    private JButton btnBuscar;
    private JButton btnPdf;
    private JTable tablaMatriz;
    private DefaultTableModel modeloTabla;

    private static final int HORA_INICIO = 7;
    private static final int HORA_FIN = 18; // exclusivo

    public CalendarizacionView() {
        controller = new CalendarizacionController();
        pdfService = new PDFService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearPanelFiltros(), BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel();
        tablaMatriz = new JTable(modeloTabla);
        add(new JScrollPane(tablaMatriz), BorderLayout.CENTER);

        cargarCategorias();
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));

        panel.add(new JLabel("Categoría:"));
        cbCategoria = new JComboBox<>();
        cbCategoria.setPreferredSize(new Dimension(180, 25));
        panel.add(cbCategoria);

        panel.add(new JLabel("Fecha (yyyy-MM-dd):"));
        txtFecha = new JTextField("2026-09-05", 10);
        panel.add(txtFecha);

        btnBuscar = new JButton("Buscar");
        panel.add(btnBuscar);
        btnBuscar.addActionListener(e -> generarMatriz());

        btnPdf = new JButton("Generar PDF");
        panel.add(btnPdf);
        btnPdf.addActionListener(e -> generarPdf());

        return panel;
    }

    private void cargarCategorias() {
        try {
            List<Categoria> categorias = controller.obtenerCategorias();
            for (Categoria c : categorias) {
                cbCategoria.addItem(c);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + ex.getMessage());
        }
    }

    private void generarMatriz() {
        Categoria categoria = (Categoria) cbCategoria.getSelectedItem();
        if (categoria == null) {
            JOptionPane.showMessageDialog(this, "No hay categoría seleccionada.");
            return;
        }

        try {
            Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(txtFecha.getText().trim());
            List<Recurso> recursos = controller.obtenerRecursosPorCategoria(categoria);
            List<Reserva> reservasDelDia = controller.obtenerReservasDelDia(fecha);

            // Columnas: "Hora" + un recurso por columna
            String[] columnas = new String[recursos.size() + 1];
            columnas[0] = "Hora";
            for (int i = 0; i < recursos.size(); i++) {
                columnas[i + 1] = recursos.get(i).getDescripcion();
            }

            Object[][] datos = new Object[HORA_FIN - HORA_INICIO][recursos.size() + 1];
            for (int fila = 0; fila < datos.length; fila++) {
                int hora = HORA_INICIO + fila;
                datos[fila][0] = String.format("%02d:00", hora);

                for (int col = 0; col < recursos.size(); col++) {
                    Recurso recurso = recursos.get(col);
                    Reserva reserva = controller.buscarReservaEnCelda(reservasDelDia, recurso, hora);
                    if (reserva != null) {
                        String nombreFuncionario = reserva.getFuncionario() != null
                                ? reserva.getFuncionario().getNombre() : "?";
                        datos[fila][col + 1] = reserva.getActividad() + " (" + nombreFuncionario + ")";
                    } else {
                        datos[fila][col + 1] = "";
                    }
                }
            }

            modeloTabla.setDataVector(datos, columnas);

        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use yyyy-MM-dd.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void generarPdf() {
        if (modeloTabla.getColumnCount() == 0 || modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Primero presione 'Buscar' para cargar la matriz.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numColumnas = modeloTabla.getColumnCount();
            String[] columnas = new String[numColumnas];
            for (int c = 0; c < numColumnas; c++) {
                columnas[c] = modeloTabla.getColumnName(c);
            }

            List<Object[]> filas = new ArrayList<>();
            for (int f = 0; f < modeloTabla.getRowCount(); f++) {
                Object[] fila = new Object[numColumnas];
                for (int c = 0; c < numColumnas; c++) {
                    Object valor = modeloTabla.getValueAt(f, c);
                    fila[c] = valor == null ? "" : valor.toString();
                }
                filas.add(fila);
            }

            Categoria categoria = (Categoria) cbCategoria.getSelectedItem();
            String titulo = "Calendarizacion de recursos - " + txtFecha.getText().trim()
                    + (categoria != null ? " - " + categoria.getDescripcion() : "");

            String ruta = "data/reporte_calendarizacion_" + System.currentTimeMillis() + ".pdf";
            pdfService.generarReporte(titulo, columnas, filas, ruta);

            JOptionPane.showMessageDialog(this, "Reporte generado en: " + ruta,
                    "PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Para probar esta vista sola, sin pasar por LoginView
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Prueba Calendarización");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 500);
            frame.add(new CalendarizacionView());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
