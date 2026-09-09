package vistas;

import controladores.ReservaController;
import controladores.ResultadoReserva;
import modelo.Categoria;
import modelo.DetalleReserva;
import modelo.Funcionario;
import modelo.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservaView extends JPanel {
    private final ReservaController controller;
    private final Funcionario funcionarioActual;

    private JTextField txtFrase;
    private JButton btnExtraer;
    private JTextField txtActividad;
    private JSpinner spnFecha;
    private JSpinner spnHoraInicio;
    private JSpinner spnHoraFin;
    private JList<Categoria> listCategorias;

    private JTable tablaReservas;
    private DefaultTableModel modeloTabla;

    public ReservaView(Funcionario funcionarioActual) {
        this.controller = new ReservaController();
        this.funcionarioActual = funcionarioActual;

        setLayout(new BorderLayout(10, 10));
        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(construirPanelTabla(), BorderLayout.CENTER);

        cargarMisReservas();
    }

    //interfaz
    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nueva reserva"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Frase:"), gbc);
        txtFrase = new JTextField();
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(txtFrase, gbc);
        btnExtraer = new JButton("Extraer (IA)");
        btnExtraer.setEnabled(false);
        btnExtraer.setToolTipText("Se habilita cuando LLMService esté conectado");
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(btnExtraer, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Actividad:"), gbc);
        txtActividad = new JTextField();
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        panel.add(txtActividad, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel("Fecha:"), gbc);
        spnFecha = new JSpinner(new SpinnerDateModel());
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "yyyy-MM-dd"));
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(spnFecha, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Hora inicio:"), gbc);
        spnHoraInicio = new JSpinner(new SpinnerDateModel());
        spnHoraInicio.setEditor(new JSpinner.DateEditor(spnHoraInicio, "HH:mm"));
        gbc.gridx = 3; gbc.weightx = 1;
        panel.add(spnHoraInicio, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panel.add(new JLabel("Hora fin:"), gbc);
        spnHoraFin = new JSpinner(new SpinnerDateModel());
        spnHoraFin.setEditor(new JSpinner.DateEditor(spnHoraFin, "HH:mm"));
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(spnHoraFin, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        panel.add(new JLabel("Categorías:"), gbc);
        DefaultListModel<Categoria> modeloCategorias = new DefaultListModel<>();
        for (Categoria c : cargarCategorias()) {
            modeloCategorias.addElement(c);
        }
        listCategorias = new JList<>(modeloCategorias);
        listCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listCategorias.setVisibleRowCount(3);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(listCategorias), gbc);

        JPanel panelBotones = new JPanel();
        JButton btnReservar = new JButton("Reservar");
        JButton btnCancelar = new JButton("Cancelar reserva seleccionada");
        JButton btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnReservar);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnLimpiar);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(panelBotones, gbc);

        btnReservar.addActionListener(e -> reservar());
        btnCancelar.addActionListener(e -> cancelarSeleccionada());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Mis reservas"));

        String[] columnas = {"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReservas = new JTable(modeloTabla);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setEnabled(false);
        btnImprimir.setToolTipText("Se habilita cuando PDFService esté conectado");
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnImprimir);
        panel.add(panelBoton, BorderLayout.SOUTH);

        return panel;
    }

    //Acciones

    private void reservar() {
        String actividad = txtActividad.getText().trim();
        if (actividad.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La actividad no puede estar vacía.");
            return;
        }

        List<Categoria> seleccionadas = listCategorias.getSelectedValuesList();
        if (seleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccioná al menos una categoría.");
            return;
        }

        Date fecha = (Date) spnFecha.getValue();
        Date horaInicio = (Date) spnHoraInicio.getValue();
        Date horaFin = (Date) spnHoraFin.getValue();

        if (horaFin.getHours() < horaInicio.getHours()
                || (horaFin.getHours() == horaInicio.getHours() && horaFin.getMinutes() <= horaInicio.getMinutes())) {
            JOptionPane.showMessageDialog(this, "La hora de fin debe ser posterior a la hora de inicio.");
            return;
        }

        try {
            ResultadoReserva resultado = controller.crearReserva(
                    funcionarioActual, actividad, fecha, horaInicio, horaFin, seleccionadas);

            if (resultado.isExitosa()) {
                JOptionPane.showMessageDialog(this, "Reserva creada: " + resultado.getReserva().getId());
                limpiarFormulario();
                cargarMisReservas();
            } else {
                StringBuilder mensaje = new StringBuilder("No hubo disponibilidad para:\n");
                for (Categoria c : resultado.getCategoriasNoDisponibles()) {
                    mensaje.append("- ").append(c.getDescripcion()).append("\n");
                }
                JOptionPane.showMessageDialog(this, mensaje.toString(), "Sin disponibilidad", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar la reserva: " + e.getMessage());
        }
    }

    private void cancelarSeleccionada() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná una reserva de la tabla primero.");
            return;
        }

        String id = (String) modeloTabla.getValueAt(fila, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la reserva " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.cancelarReserva(id);
            cargarMisReservas();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cancelar: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtFrase.setText("");
        txtActividad.setText("");
        spnFecha.setValue(new Date());
        spnHoraInicio.setValue(new Date());
        spnHoraFin.setValue(new Date());
        listCategorias.clearSelection();
    }

    //Carga de datos

    private List<Categoria> cargarCategorias() {
        try {
            return controller.obtenerCategoriasDisponibles();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las categorías: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void cargarMisReservas() {
        try {
            List<Reserva> misReservas = controller.obtenerReservasDe(funcionarioActual);
            modeloTabla.setRowCount(0);

            SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");

            for (Reserva r : misReservas) {
                String horario = sdfHora.format(r.getHoraInicio()) + " - " + sdfHora.format(r.getHoraFin());

                StringBuilder recursos = new StringBuilder();
                for (DetalleReserva detalle : r.getDetalles()) {
                    if (recursos.length() > 0) recursos.append(", ");
                    recursos.append(detalle.getRecursoAsignado().getId());
                }

                modeloTabla.addRow(new Object[]{
                        r.getId(), r.getActividad(), sdfFecha.format(r.getFecha()),
                        horario, recursos.toString(), r.getEstado()
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar tus reservas: " + e.getMessage());
        }
    }
}
