

import Vistas.MainView;
import modelo.Funcionario;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Funcionario prueba = new Funcionario();
            prueba.setId("111");
            MainView ventana = new MainView(prueba);
            ventana.setVisible(true);
        });
    }
}

//prueba
