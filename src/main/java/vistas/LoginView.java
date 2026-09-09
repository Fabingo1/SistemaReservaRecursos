package vistas;

import controladores.LoginController;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginView extends JFrame {

    private JTextField txtId;
    private JPasswordField txtClave;
    private LoginController controller;

    public LoginView() {
        controller = new LoginController();

        setTitle("Sistema de Reserva de Recursos - Login");
        setSize(370, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);

        txtId = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Clave:"), gbc);

        txtClave = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtClave, gbc);

        JButton btnLogin = new JButton("Ingresar");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnCambiar = new JButton("Cambiar clave");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.add(btnLogin);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnCambiar);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        btnLogin.addActionListener(e -> intentarLogin());
        btnCancelar.addActionListener(e -> System.exit(0));
        btnCambiar.addActionListener(e -> abrirCambiarClave());

        add(panel);
    }

    private void intentarLogin() {
        String id = txtId.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (id.isBlank() || clave.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar id y clave.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Usuario usuario = controller.autenticar(id, clave);

            if (usuario == null) {
                JOptionPane.showMessageDialog(this, "Id o clave incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tanto Administrador como Funcionario entran a la misma ventana
            // principal (MainView), que ajusta sus pestañas según el rol.
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                MainView main = new MainView(usuario);
                main.setVisible(true);
            });

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirCambiarClave() {
        String id = txtId.getText().trim();
        if (id.isBlank()) {
            JOptionPane.showMessageDialog(this, "Escriba su id antes de cambiar la clave.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CambiarClaveDialog dialog = new CambiarClaveDialog(this, controller, id);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView login = new LoginView();
            login.setVisible(true);
        });
    }
}
