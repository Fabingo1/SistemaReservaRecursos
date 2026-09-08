package Vistas;

import modelo.Administrador;
import modelo.Funcionario;
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
            tabs.addTab("Reservas", new ReservaView((Funcionario) usuario));
        }

        tabs.addTab("Calendarización", crearPanelVacio("Calendarización"));
        tabs.addTab("Actividades", crearPanelVacio("Actividades"));
        tabs.addTab("Estadísticas", crearPanelVacio("Estadísticas"));

        add(tabs);
    }

    private JPanel crearPanelVacio(String nombreModulo) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Módulo de " + nombreModulo + " (en construcción)"));
        return panel;
    }
}