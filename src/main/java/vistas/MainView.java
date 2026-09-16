package vistas;

import controladores.LoginController;
import modelo.Administrador;
import modelo.Funcionario;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

// Muestra las pestañas según el rol: Administrador ve Funcionarios/Categorías/Recursos,
// Funcionario ve Reservas; ambos comparten Calendarización, Actividades y Estadísticas.
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

        Color[] coloresTabs = esAdmin
                ? new Color[]{new Color(0x4285C8), new Color(0xEC407A), new Color(0xFFC107),
                new Color(0x26A69A), new Color(0x7E57C2), new Color(0xFF9800)}
                : new Color[]{new Color(0x4285C8), new Color(0x26A69A), new Color(0x7E57C2), new Color(0xFF9800)};

        Runnable actualizarColoresTabs = () -> {
            int seleccionada = tabs.getSelectedIndex();
            for (int i = 0; i < tabs.getTabCount(); i++) {
                Color base = coloresTabs[i];
                if (i == seleccionada) {
                    tabs.setBackgroundAt(i, base);
                } else {
                    // Atenuado hacia el gris de fondo para que solo la pestaña activa resalte.
                    int r = (base.getRed() + 60 * 2) / 3;
                    int g = (base.getGreen() + 63 * 2) / 3;
                    int b = (base.getBlue() + 68 * 2) / 3;
                    tabs.setBackgroundAt(i, new Color(r, g, b));
                }
            }
        };
        actualizarColoresTabs.run();

        // Al cambiar de pestaña se refrescan sus datos (ej. una categoría nueva ya aparece en Recursos).
        tabs.addChangeListener(e -> {
            actualizarColoresTabs.run();
            Component seleccionada = tabs.getSelectedComponent();
            if (seleccionada instanceof Refrescable) {
                ((Refrescable) seleccionada).refrescar();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }
}