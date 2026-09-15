package vistas;

import controladores.LoginController;
import modelo.Administrador;
import modelo.Funcionario;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal. Muestra las pestañas según el rol del usuario:
 *  - Administrador: Funcionarios, Categorías, Recursos (funcionalidades 3, 4, 5)
 *  - Funcionario:   Reservas (funcionalidad 2)
 *  - Ambos:         Calendarización, Actividades, Estadísticas (6, 7, 8)
 */
public class MainView extends JFrame {

    public MainView(Usuario usuario) {
        boolean esAdmin = usuario instanceof Administrador;
        String rol = esAdmin ? "ADMINISTRADOR" : "FUNCIONARIO";
        String nombre = (usuario instanceof Funcionario && ((Funcionario) usuario).getNombre() != null)
                ? ((Funcionario) usuario).getNombre() : usuario.getId();

        setTitle("SISTEMA DE RESERVA DE RECURSOS - " + nombre + " (" + rol + ")");
        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Barra superior: cambiar clave "en cualquier momento" (funcionalidad 1) y cerrar sesión
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.add(new JLabel("Usuario: " + usuario.getId() + "  |  Rol: " + rol + "   "));
        JButton btnClave = new JButton("🔑 Cambiar clave");
        btnClave.addActionListener(e ->
                new CambiarClaveDialog(this, new LoginController(), usuario.getId()).setVisible(true));
        JButton btnSalir = new JButton("🚪 Cerrar sesión");
        btnSalir.addActionListener(e -> {
            dispose();
            new LoginView().setVisible(true);
        });
        barra.add(btnClave);
        barra.add(btnSalir);
        add(barra, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (esAdmin) {
            tabs.addTab("👤 Funcionarios", new FuncionarioView());
            tabs.addTab("🏷️ Categorías", new CategoriaView());
            tabs.addTab("📦 Recursos", new RecursoView());
        } else {
            tabs.addTab("📝 Reservas", new ReservaView((Funcionario) usuario));
        }
        tabs.addTab("📅 Calendarización", new CalendarizacionView());
        tabs.addTab("📋 Actividades", new ActividadesView());
        tabs.addTab("📊 Estadísticas", new EstadisticasView());

        // Al cambiar de pestaña se recargan los datos (p. ej. una categoría nueva
        // aparece en Recursos sin reiniciar la aplicación).
        tabs.addChangeListener(e -> {
            Component seleccionada = tabs.getSelectedComponent();
            if (seleccionada instanceof Refrescable) {
                ((Refrescable) seleccionada).refrescar();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }
}