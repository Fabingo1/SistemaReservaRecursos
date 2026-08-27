

import vistas.ReservasView;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ReservasView ventana = new ReservasView();
            ventana.setVisible(true);
        });
    }
}
