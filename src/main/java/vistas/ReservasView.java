package vistas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReservasView extends JFrame {

    private JTabbedPane tabs;
    public ReservasView() {
        // Configuración de la ventana principal
        setTitle("SISTEMA DE RESERVAS - 111 (FUNCIONARIO)");
        setSize(750, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el componente de pestañas
        tabs = new JTabbedPane();

        // Crear los paneles correspondientes a la imagen
        JPanel panelReservas = crearPanelReservas();
        JPanel panelCalendarizacion = crearPanelVacio("Calendarización");
        JPanel panelActividades = crearPanelVacio("Actividades");
        JPanel panelEstadisticas = crearPanelVacio("Estadísticas");

        // Agregar las pestañas
        tabs.addTab("Reservas", panelReservas);
        tabs.addTab("Calendarización", panelCalendarizacion);
        tabs.addTab("Actividades", panelActividades);
        tabs.addTab("Estadísticas", panelEstadisticas);

        add(tabs);
    }

    // =====================================================
    // TAB 1: RESERVAS (Pantalla 1 del PDF)
    // =====================================================
    private JPanel crearPanelReservas() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // -------------------------------------------------
        // SECCIÓN SUPERIOR: Panel "Nueva reserva"
        // -------------------------------------------------
        JPanel panelNuevaReserva = new JPanel(new GridBagLayout());
        panelNuevaReserva.setBorder(BorderFactory.createTitledBorder("Nueva reserva"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Campo Frase + Botón Extraer
        gbc.gridx = 0; gbc.gridy = 0;
        panelNuevaReserva.add(new JLabel("Frase"), gbc);

        JTextField txtFrase = new JTextField();
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panelNuevaReserva.add(txtFrase, gbc);

        JButton btnExtraer = new JButton("Extraer");
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.0;
        panelNuevaReserva.add(btnExtraer, gbc);

        // 2. Campo Actividad
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panelNuevaReserva.add(new JLabel("Actividad"), gbc);

        JTextField txtActividad = new JTextField("Sesion de Junta Directiva");
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelNuevaReserva.add(txtActividad, gbc);

        // 3. Campo Fecha
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panelNuevaReserva.add(new JLabel("Fecha"), gbc);

        JTextField txtFecha = new JTextField("5 de agosto de 2026");
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelNuevaReserva.add(txtFecha, gbc);

        // 4. Horarios (Hora inicio / Hora fin)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panelNuevaReserva.add(new JLabel("Hora inicio"), gbc);

        JComboBox<String> cbHoraInicio = new JComboBox<>(new String[]{"9:00 a. m.", "8:00 a. m.", "10:00 a. m."});
        gbc.gridx = 1;
        panelNuevaReserva.add(cbHoraInicio, gbc);

        gbc.gridx = 2;
        panelNuevaReserva.add(new JLabel("Hora fin"), gbc);

        JComboBox<String> cbHoraFin = new JComboBox<>(new String[]{"11:00 a. m.", "10:00 a. m.", "12:00 p. m."});
        gbc.gridx = 3;
        panelNuevaReserva.add(cbHoraFin, gbc);

        // 5. Categorías requeridas
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        panelNuevaReserva.add(new JLabel("Categorias requeridas (seleccion multiple)"), gbc);

        String[] opcionesCategorias = {"Laptop windows", "Sala de Juntas", "Sala para 10 personas"};
        JList<String> listCategorias = new JList<>(opcionesCategorias);
        listCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollCategorias = new JScrollPane(listCategorias);
        scrollCategorias.setPreferredSize(new Dimension(100, 60));

        gbc.gridy = 5;
        panelNuevaReserva.add(scrollCategorias, gbc);

        // 6. Botones (Reservar / Limpiar)
        JPanel panelBotonesNueva = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnReservar = new JButton("Reservar");
        JButton btnLimpiar = new JButton("Limpiar");
        panelBotonesNueva.add(btnReservar);
        panelBotonesNueva.add(btnLimpiar);

        gbc.gridy = 6;
        panelNuevaReserva.add(panelBotonesNueva, gbc);

        // -------------------------------------------------
        // SECCIÓN INFERIOR: Panel "Mis reservas"
        // -------------------------------------------------
        JPanel panelMisReservas = new JPanel(new BorderLayout(5, 5));
        panelMisReservas.setBorder(BorderFactory.createTitledBorder("Mis reservas"));

        // Tabla con el modelo de datos de la captura
        String[] columnas = {"id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"};
        Object[][] datosTabla = {
        };

        DefaultTableModel modeloTabla = new DefaultTableModel(datosTabla, columnas);
        JTable tablaReservas = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaReservas);
        scrollTabla.setPreferredSize(new Dimension(500, 130));

        panelMisReservas.add(scrollTabla, BorderLayout.CENTER);

        // Botones de acción inferiores
        JPanel panelBotonesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnCancelar = new JButton("Cancelar reserva seleccionada");
        JButton btnImprimir = new JButton("Imprimir");
        panelBotonesTabla.add(btnCancelar);
        panelBotonesTabla.add(btnImprimir);

        panelMisReservas.add(panelBotonesTabla, BorderLayout.SOUTH);

        // Agrupar ambos paneles verticalmente
        JPanel contenedorCentral = new JPanel();
        contenedorCentral.setLayout(new BoxLayout(contenedorCentral, BoxLayout.Y_AXIS));
        contenedorCentral.add(panelNuevaReserva);
        contenedorCentral.add(Box.createVerticalStrut(10));
        contenedorCentral.add(panelMisReservas);

        mainPanel.add(contenedorCentral, BorderLayout.CENTER);

        return mainPanel;
    }

    // Panel temporal para las demás pestañas
    private JPanel crearPanelVacio(String nombreModulo) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Módulo: " + nombreModulo));
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReservasView ventana = new ReservasView();
            ventana.setVisible(true);
        });
    }
}
