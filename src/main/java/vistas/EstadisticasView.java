package vistas;

import controladores.EstadisticasController;
import servicios.PDFService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista de la funcionalidad 8: "Estadísticas". Muestra, lado a lado (tal
 * como en el ejemplo de pantalla del enunciado), dos secciones
 * independientes:
 *  - Recursos: cantidad de reservas por categoría de recurso en un período.
 *  - Actividades: cantidad de actividades programadas por semana en un período.
 *
 * El gráfico de barras se dibuja con Java2D (BarChartPanel interno) para no
 * agregar una dependencia externa de gráficos al proyecto.
 *
 * Se implementa como JPanel para poder incrustarse como pestaña tanto en la
 * vista de Funcionario como en la de Administrador (ambos roles pueden usar
 * esta funcionalidad según el enunciado).
 */
public class EstadisticasView extends JPanel {

    private final EstadisticasController controller;
    private final PDFService pdfService;

    private JSpinner spinnerDesdeRecursos;
    private JSpinner spinnerHastaRecursos;
    private DefaultTableModel modeloTablaRecursos;
    private BarChartPanel chartRecursos;
    private Map<String, Integer> datosRecursosActuales = new LinkedHashMap<>();

    private JSpinner spinnerDesdeActividades;
    private JSpinner spinnerHastaActividades;
    private DefaultTableModel modeloTablaActividades;
    private BarChartPanel chartActividades;
    private Map<String, Integer> datosActividadesActuales = new LinkedHashMap<>();

    public EstadisticasView() {
        this.controller = new EstadisticasController();
        this.pdfService = new PDFService();

        // Layout de dos columnas (Recursos | Actividades) visibles a la vez,
        // igual que en el ejemplo de pantalla del enunciado, en vez de pestañas.
        setLayout(new GridLayout(1, 2, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearPanelRecursos());
        add(crearPanelActividadesSemana());
    }

    // ---------------------------------------------------------------
    // RECURSOS
    // ---------------------------------------------------------------

    private JPanel crearPanelRecursos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Recursos"));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        Calendar haceUnMes = Calendar.getInstance();
        haceUnMes.add(Calendar.MONTH, -1);

        spinnerDesdeRecursos = crearSpinnerFecha(haceUnMes.getTime());
        spinnerHastaRecursos = crearSpinnerFecha(new Date());

        controles.add(new JLabel("Desde:"));
        controles.add(spinnerDesdeRecursos);
        controles.add(new JLabel("Hasta:"));
        controles.add(spinnerHastaRecursos);

        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> consultarRecursos());
        controles.add(btnConsultar);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> generarPdfRecursos());
        controles.add(btnPdf);

        panel.add(controles, BorderLayout.NORTH);

        modeloTablaRecursos = new DefaultTableModel(new String[]{"Categoría", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloTablaRecursos);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new Dimension(260, 300));

        chartRecursos = new BarChartPanel();
        chartRecursos.setBorder(BorderFactory.createTitledBorder("Reservas por categoría de recurso"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, chartRecursos);
        split.setResizeWeight(0.3);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void consultarRecursos() {
        Date desde = (Date) spinnerDesdeRecursos.getValue();
        Date hasta = (Date) spinnerHastaRecursos.getValue();
        if (desde.after(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a 'Hasta'.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            datosRecursosActuales = controller.estadisticasRecursos(desde, hasta);

            modeloTablaRecursos.setRowCount(0);
            for (Map.Entry<String, Integer> e : datosRecursosActuales.entrySet()) {
                modeloTablaRecursos.addRow(new Object[]{e.getKey(), e.getValue()});
            }
            chartRecursos.setDatos(datosRecursosActuales);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer las reservas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarPdfRecursos() {
        if (datosRecursosActuales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero realice una consulta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Object[]> filas = new ArrayList<>();
            for (Map.Entry<String, Integer> e : datosRecursosActuales.entrySet()) {
                filas.add(new Object[]{e.getKey(), e.getValue()});
            }
            String ruta = "data/reporte_estadisticas_recursos_" + System.currentTimeMillis() + ".pdf";
            pdfService.generarReporte("Estadisticas de recursos reservados",
                    new String[]{"Categoria", "Cantidad"}, filas, ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado en: " + ruta,
                    "PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------
    // ACTIVIDADES
    // ---------------------------------------------------------------

    private JPanel crearPanelActividadesSemana() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Actividades"));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        Calendar haceDosMeses = Calendar.getInstance();
        haceDosMeses.add(Calendar.MONTH, -2);

        spinnerDesdeActividades = crearSpinnerFecha(haceDosMeses.getTime());
        spinnerHastaActividades = crearSpinnerFecha(new Date());

        controles.add(new JLabel("Desde:"));
        controles.add(spinnerDesdeActividades);
        controles.add(new JLabel("Hasta:"));
        controles.add(spinnerHastaActividades);

        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> consultarActividades());
        controles.add(btnConsultar);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> generarPdfActividades());
        controles.add(btnPdf);

        panel.add(controles, BorderLayout.NORTH);

        modeloTablaActividades = new DefaultTableModel(new String[]{"Semana", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloTablaActividades);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new Dimension(260, 300));

        chartActividades = new BarChartPanel();
        chartActividades.setBorder(BorderFactory.createTitledBorder("Actividades programadas por semana"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, chartActividades);
        split.setResizeWeight(0.3);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void consultarActividades() {
        Date desde = (Date) spinnerDesdeActividades.getValue();
        Date hasta = (Date) spinnerHastaActividades.getValue();
        if (desde.after(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a 'Hasta'.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            datosActividadesActuales = controller.estadisticasActividades(desde, hasta);

            modeloTablaActividades.setRowCount(0);
            for (Map.Entry<String, Integer> e : datosActividadesActuales.entrySet()) {
                modeloTablaActividades.addRow(new Object[]{e.getKey(), e.getValue()});
            }
            chartActividades.setDatos(datosActividadesActuales);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer las reservas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarPdfActividades() {
        if (datosActividadesActuales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero realice una consulta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Object[]> filas = new ArrayList<>();
            for (Map.Entry<String, Integer> e : datosActividadesActuales.entrySet()) {
                filas.add(new Object[]{e.getKey(), e.getValue()});
            }
            String ruta = "data/reporte_estadisticas_actividades_" + System.currentTimeMillis() + ".pdf";
            pdfService.generarReporte("Estadisticas de actividades programadas",
                    new String[]{"Semana", "Cantidad"}, filas, ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado en: " + ruta,
                    "PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private JSpinner crearSpinnerFecha(Date valorInicial) {
        SpinnerDateModel modelo = new SpinnerDateModel();
        modelo.setValue(valorInicial);
        JSpinner spinner = new JSpinner(modelo);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    /** Panel simple de gráfico de barras dibujado con Java2D (sin dependencias externas). */
    private static class BarChartPanel extends JPanel {
        private Map<String, Integer> datos = new LinkedHashMap<>();

        BarChartPanel() {
            setPreferredSize(new Dimension(420, 300));
            setBackground(Color.WHITE);
        }

        void setDatos(Map<String, Integer> datos) {
            this.datos = datos;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ancho = getWidth();
            int alto = getHeight();
            int margenInferior = 70;
            int margenSuperior = 25;
            int margenLateral = 20;

            if (datos == null || datos.isEmpty()) {
                g2.drawString("Sin datos para el período seleccionado.", margenLateral, alto / 2);
                return;
            }

            int max = 1;
            for (int v : datos.values()) {
                max = Math.max(max, v);
            }

            int numBarras = datos.size();
            int anchoDisponible = ancho - 2 * margenLateral;
            int anchoBarra = Math.max(18, anchoDisponible / Math.max(1, numBarras) - 15);
            int alturaDisponible = alto - margenSuperior - margenInferior;

            int x = margenLateral;
            for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                int valor = entry.getValue();
                int alturaBarra = (int) Math.round((valor / (double) max) * alturaDisponible);
                int y = alto - margenInferior - alturaBarra;

                g2.setColor(new Color(66, 133, 200));
                g2.fillRect(x, y, anchoBarra, alturaBarra);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x, y, anchoBarra, alturaBarra);

                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(valor), x + anchoBarra / 2 - 4, Math.max(12, y - 5));

                Graphics2D g2r = (Graphics2D) g2.create();
                g2r.translate(x + anchoBarra / 2.0 + 4, alto - margenInferior + 14);
                g2r.rotate(Math.toRadians(35));
                g2r.drawString(entry.getKey(), 0, 0);
                g2r.dispose();

                x += anchoBarra + 15;
            }

            g2.setColor(Color.GRAY);
            g2.drawLine(margenLateral, alto - margenInferior, ancho - margenLateral, alto - margenInferior);
        }
    }

    /** Prueba manual independiente de esta vista (sin necesidad de pasar por el login). */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Estadísticas - Prueba");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 560);
            frame.setLocationRelativeTo(null);
            frame.add(new EstadisticasView());
            frame.setVisible(true);
        });
    }
}
