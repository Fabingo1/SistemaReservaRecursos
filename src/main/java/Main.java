import servicios.GenerarDatosPrueba;
import vistas.LoginView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Si es la primera ejecución (data/ vacío, p. ej. recién clonado el repo),
        // se crean datos de prueba para poder ingresar: admin1/admin1, func1/func1.
        try {
            if (GenerarDatosPrueba.generarSiNoExisten("data")) {
                System.out.println("Primera ejecución: se generaron datos de prueba en data/");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudieron crear los datos iniciales: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            LoginView login = new LoginView();
            login.setVisible(true);
        });
    }
}
