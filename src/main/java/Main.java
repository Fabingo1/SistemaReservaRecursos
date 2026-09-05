import vistas.LoginView;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginView login = new LoginView();
            login.setVisible(true);
        });
    }
}
