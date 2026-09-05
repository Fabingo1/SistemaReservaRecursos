package controladores;

import modelo.Usuario;

public class ProbarLogin {
    public static void main(String[] args) throws Exception {
        LoginController controller = new LoginController();

        Usuario u1 = controller.autenticar("admin1", "admin1");
        System.out.println("admin1/admin1 -> " + (u1 != null ? "OK, clase: " + u1.getClass().getSimpleName() : "FALLÓ"));

        Usuario u2 = controller.autenticar("func1", "func1");
        System.out.println("func1/func1 -> " + (u2 != null ? "OK, clase: " + u2.getClass().getSimpleName() : "FALLÓ"));

        Usuario u3 = controller.autenticar("admin1", "claveMala");
        System.out.println("admin1/claveMala -> " + (u3 != null ? "FALLÓ (no debería loguear)" : "OK, rechazado correctamente"));
    }
}