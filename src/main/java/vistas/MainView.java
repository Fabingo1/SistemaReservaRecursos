package vistas;

import modelo.Administrador;
import modelo.Usuario;


import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    private JTabbedPane tabs;

    public MainView(Usuario usuario) {
        String rol = (usuario instanceof Administrador) ? "ADMIN" : "FUNCIONARIO";
        setTitle("SISTEMA DE RESERVAS - " + usuario.getId() + " (" + rol + ")");
        setSize(750, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabs = new JTabbedPane();

        if (usuario instanceof Administrador) {
            tabs.addTab("Funcionarios", crearPanelVacio("Funcionarios"));
            tabs.addTab("Categorías", crearPanelVacio("Categorías"));
            tabs.addTab("Recursos", crearPanelVacio("Recursos"));
        } else {
            // Misma maqueta visual que ya existia en ReservasView (aun sin
            // logica real; pendiente de Persona A), reutilizada tal cual.
            tabs.addTab("Reservas", ReservasView.crearPanelReservas());
        }

        tabs.addTab("Calendarización", new CalendarizacionView());
        tabs.addTab("Actividades", new ActividadesView());
        tabs.addTab("Estadísticas", new EstadisticasView());

        add(tabs);
    }

    private JPanel crearPanelVacio(String nombreModulo) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Módulo de " + nombreModulo + " (en construcción)"));
        return panel;
    }
}
