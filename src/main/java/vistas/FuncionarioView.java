package vistas;

import controladores.FuncionarioController;
import modelo.Funcionario;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista de la funcionalidad "Lista de Funcionarios" (funcionalidad 3). El id
 * lo asigna el administrador y solo es editable al crear uno nuevo; una vez
 * seleccionado un funcionario existente queda bloqueado, porque
 * FuncionarioController no permite cambiarlo (ni la clave) desde este CRUD.
 *
 * El enunciado pide búsqueda "por id o nombre": se ofrecen dos campos
 * separados (igual que el mockup), pero solo se usa uno a la vez — si el
 * campo Id tiene texto, se busca por id; si no, se busca por nombre.
 */
public class FuncionarioView extends JPanel {

    private final FuncionarioController controller;

    private JTextField txtBusquedaId;
    private JTextField txtBusquedaNombre;
    private JButton btnBuscar;
    private JButton btnImprimir;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    private JTable tablaListado;
    private DefaultTableModel modeloTabla;

    /** Funcionarios actualmente mostrados en la tabla, en el mismo orden que las filas. */
    private List<Funcionario> resultadosActuales = new ArrayList<>();

    /** Id del funcionario seleccionado en la tabla, o null si se está creando uno nuevo. */
    private String idSeleccionado;

    public FuncionarioView() {
        controller = new FuncionarioController();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearPanelBusqueda(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(crearPanelFormulario(), BorderLayout.NORTH);
        centro.add(crearPanelListado(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        buscar();
    }

    // ---------- Construcción de paneles ----------

    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Búsqueda"));

        panel.add(new JLabel("ID:"));
        txtBusquedaId = new JTextField(10);
        panel.add(txtBusquedaId);

        panel.add(new JLabel("Nombre:"));
        txtBusquedaNombre = new JTextField(15);
        panel.add(txtBusquedaNombre);

        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        panel.add(btnBuscar);

        btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> imprimir());
        panel.add(btnImprimir);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Funcionario"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("ID:"), c);
        c.gridx = 1;
        txtId = new JTextField(15);
        panel.add(txtId, c);

        c.gridx = 2;
        JLabel avisoClave = new JLabel("La clave inicial del usuario será igual al ID.");
        avisoClave.setFont(avisoClave.getFont().deriveFont(Font.ITALIC, 11f));
        avisoClave.setForeground(Color.GRAY);
        panel.add(avisoClave, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Nombre:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        txtNombre = new JTextField(25);
        panel.add(txtNombre, c);
        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Teléfono:"), c);
        c.gridx = 1;
        txtTelefono = new JTextField(15);
        panel.add(txtTelefono, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardar());
        botones.add(btnGuardar);

        btnBorrar = new JButton("Borrar");
        btnBorrar.addActionListener(e -> borrar());
        botones.add(btnBorrar);

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiar());
        botones.add(btnLimpiar);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        panel.add(botones, c);

        return panel;
    }

    private JScrollPane crearPanelListado() {
        modeloTabla = new DefaultTableModel(new Object[]{"Id", "Nombre", "Teléfono"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // la tabla es solo de consulta; se edita por el formulario
            }
        };
        tablaListado = new JTable(modeloTabla);
        tablaListado.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaListado.getSelectionModel().addListSelectionListener(this::alSeleccionarFila);

        JScrollPane scroll = new JScrollPane(tablaListado);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado"));
        return scroll;
    }

    // ---------- Acciones ----------

    private void buscar() {
        try {
            String texto = !txtBusquedaId.getText().isBlank()
                    ? txtBusquedaId.getText()
                    : txtBusquedaNombre.getText();
            cargarTabla(controller.buscar(texto));
        } catch (IOException ex) {
            mostrarError("Error al buscar funcionarios", ex);
        }
    }

    private void guardar() {
        try {
            String nombre = txtNombre.getText();
            String telefono = txtTelefono.getText();

            if (idSeleccionado == null) {
                Funcionario creado = controller.crear(txtId.getText(), nombre, telefono);
                limpiar();
                buscar();
                JOptionPane.showMessageDialog(this,
                        "Funcionario creado.\nClave inicial: " + creado.getClave()
                                + "\n(comuníquesela para que la cambie en su primer ingreso)");
            } else {
                controller.modificar(idSeleccionado, nombre, telefono);
                limpiar();
                buscar();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            mostrarError("Error al guardar el funcionario", ex);
        }
    }

    private void borrar() {
        if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la lista para borrar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea borrar al funcionario " + idSeleccionado + "?",
                "Confirmar borrado", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.borrar(idSeleccionado);
            limpiar();
            buscar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo borrar", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            mostrarError("Error al borrar el funcionario", ex);
        }
    }

    private void limpiar() {
        idSeleccionado = null;
        txtId.setText("");
        txtId.setEditable(true); // vuelve a permitir escribir el id porque es un funcionario nuevo
        txtNombre.setText("");
        txtTelefono.setText("");
        tablaListado.clearSelection();
    }

    private void imprimir() {
        try {
            if (resultadosActuales.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay funcionarios para incluir en el reporte.");
                return;
            }

            JFileChooser selector = new JFileChooser();
            selector.setSelectedFile(new File("funcionarios.pdf"));
            if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            String ruta = selector.getSelectedFile().getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".pdf")) {
                ruta += ".pdf";
            }

            controller.generarReportePDF(resultadosActuales, ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado en:\n" + ruta);
        } catch (IOException ex) {
            mostrarError("Error al generar el reporte PDF", ex);
        }
    }

    private void alSeleccionarFila(ListSelectionEvent evento) {
        if (evento.getValueIsAdjusting()) {
            return;
        }
        int fila = tablaListado.getSelectedRow();
        if (fila < 0 || fila >= resultadosActuales.size()) {
            return;
        }

        Funcionario funcionario = resultadosActuales.get(fila);
        idSeleccionado = funcionario.getId();

        txtId.setText(funcionario.getId());
        txtId.setEditable(false); // el id de un funcionario existente no se puede cambiar
        txtNombre.setText(funcionario.getNombre());
        txtTelefono.setText(funcionario.getTelefono());
    }

    // ---------- Utilidades ----------

    private void cargarTabla(List<Funcionario> funcionarios) {
        resultadosActuales = funcionarios;
        modeloTabla.setRowCount(0);
        for (Funcionario f : funcionarios) {
            modeloTabla.addRow(new Object[]{f.getId(), f.getNombre(), f.getTelefono()});
        }
    }

    private void mostrarError(String titulo, Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }

    // Para probar esta vista sola, sin pasar por LoginView
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Prueba Funcionarios");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(750, 550);
            frame.add(new FuncionarioView());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}