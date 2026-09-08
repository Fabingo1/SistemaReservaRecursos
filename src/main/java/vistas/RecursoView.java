package vistas;

import controladores.RecursoController;
import modelo.Categoria;
import modelo.Recurso;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista de la funcionalidad "Lista de recursos" (funcionalidad 5). El id del
 * recurso lo escribe el administrador (número de activo) y solo es editable
 * al crear uno nuevo; una vez seleccionado un recurso existente el id queda
 * bloqueado, porque RecursoController no permite cambiarlo.
 */
public class RecursoView extends JPanel {

    private final RecursoController controller;

    private JComboBox<Categoria> cbCategoriaFiltro;
    private JTextField txtBusquedaDescripcion;
    private JButton btnBuscar;
    private JButton btnImprimir;

    private JTextField txtId;
    private JComboBox<Categoria> cbCategoriaForm;
    private JTextField txtDescripcion;
    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    private JTable tablaListado;
    private DefaultTableModel modeloTabla;

    /** Recursos actualmente mostrados en la tabla, en el mismo orden que las filas. */
    private List<Recurso> resultadosActuales = new ArrayList<>();

    /** Id del recurso seleccionado en la tabla, o null si se está creando uno nuevo. */
    private String idSeleccionado;

    public RecursoView() {
        controller = new RecursoController();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearPanelFiltro(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(crearPanelFormulario(), BorderLayout.NORTH);
        centro.add(crearPanelListado(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        cargarCategoriasEnCombo(cbCategoriaFiltro, true);
        cargarCategoriasEnCombo(cbCategoriaForm, false);
        buscar();
    }

    // ---------- Construcción de paneles ----------

    private JPanel crearPanelFiltro() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filtro"));

        panel.add(new JLabel("Categoría:"));
        cbCategoriaFiltro = new JComboBox<>();
        cbCategoriaFiltro.setPreferredSize(new Dimension(180, 25));
        panel.add(cbCategoriaFiltro);

        panel.add(new JLabel("Descripción:"));
        txtBusquedaDescripcion = new JTextField(15);
        panel.add(txtBusquedaDescripcion);

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
        panel.setBorder(BorderFactory.createTitledBorder("Recurso"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("ID / activo:"), c);
        c.gridx = 1;
        txtId = new JTextField(15);
        panel.add(txtId, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Categoría:"), c);
        c.gridx = 1;
        cbCategoriaForm = new JComboBox<>();
        cbCategoriaForm.setPreferredSize(new Dimension(220, 25));
        panel.add(cbCategoriaForm, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Descripción:"), c);
        c.gridx = 1;
        txtDescripcion = new JTextField(25);
        panel.add(txtDescripcion, c);

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
        c.gridwidth = 2;
        panel.add(botones, c);

        return panel;
    }

    private JScrollPane crearPanelListado() {
        modeloTabla = new DefaultTableModel(new Object[]{"Id", "Categoría", "Descripción"}, 0) {
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
            Categoria categoria = (Categoria) cbCategoriaFiltro.getSelectedItem();
            String texto = txtBusquedaDescripcion.getText();
            cargarTabla(controller.buscar(categoria, texto));
        } catch (IOException ex) {
            mostrarError("Error al buscar recursos", ex);
        }
    }

    private void guardar() {
        try {
            Categoria categoria = (Categoria) cbCategoriaForm.getSelectedItem();
            String descripcion = txtDescripcion.getText();

            if (idSeleccionado == null) {
                controller.crear(txtId.getText(), categoria, descripcion);
            } else {
                controller.modificar(idSeleccionado, categoria, descripcion);
            }
            limpiar();
            buscar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            mostrarError("Error al guardar el recurso", ex);
        }
    }

    private void borrar() {
        if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un recurso de la lista para borrar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea borrar el recurso " + idSeleccionado + "?",
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
            mostrarError("Error al borrar el recurso", ex);
        }
    }

    private void limpiar() {
        idSeleccionado = null;
        txtId.setText("");
        txtId.setEditable(true); // vuelve a permitir escribir el id porque es un recurso nuevo
        cbCategoriaForm.setSelectedIndex(-1);
        txtDescripcion.setText("");
        tablaListado.clearSelection();
    }

    private void imprimir() {
        try {
            if (resultadosActuales.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay recursos para incluir en el reporte.");
                return;
            }

            JFileChooser selector = new JFileChooser();
            selector.setSelectedFile(new File("recursos.pdf"));
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

        Recurso recurso = resultadosActuales.get(fila);
        idSeleccionado = recurso.getId();

        txtId.setText(recurso.getId());
        txtId.setEditable(false); // el id de un recurso existente no se puede cambiar

        if (recurso.getCategoria() != null) {
            seleccionarCategoriaEnCombo(cbCategoriaForm, recurso.getCategoria().getId());
        }
        txtDescripcion.setText(recurso.getDescripcion());
    }

    // ---------- Utilidades ----------

    private void cargarCategoriasEnCombo(JComboBox<Categoria> combo, boolean incluirOpcionTodas) {
        try {
            combo.removeAllItems();
            if (incluirOpcionTodas) {
                combo.addItem(null); // representa "todas las categorías" en el filtro
            }
            for (Categoria c : controller.obtenerCategorias()) {
                combo.addItem(c);
            }
            combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel etiqueta = new JLabel(value == null ? "Todas las categorías" : value.getDescripcion());
                if (isSelected) {
                    etiqueta.setOpaque(true);
                    etiqueta.setBackground(list.getSelectionBackground());
                    etiqueta.setForeground(list.getSelectionForeground());
                }
                return etiqueta;
            });
        } catch (IOException ex) {
            mostrarError("Error al cargar categorías", ex);
        }
    }

    private void seleccionarCategoriaEnCombo(JComboBox<Categoria> combo, String idCategoria) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Categoria item = combo.getItemAt(i);
            if (item != null && item.getId().equals(idCategoria)) {
                combo.setSelectedItem(item);
                return;
            }
        }
    }

    private void cargarTabla(List<Recurso> recursos) {
        resultadosActuales = recursos;
        modeloTabla.setRowCount(0);
        for (Recurso r : recursos) {
            String categoriaTexto = r.getCategoria() != null ? r.getCategoria().getDescripcion() : "";
            modeloTabla.addRow(new Object[]{r.getId(), categoriaTexto, r.getDescripcion()});
        }
    }

    private void mostrarError(String titulo, Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }

    // Para probar esta vista sola, sin pasar por LoginView
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Prueba Recursos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(750, 600);
            frame.add(new RecursoView());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
