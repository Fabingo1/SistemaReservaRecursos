package controladores;

import modelo.Administrador;
import modelo.Funcionario;
import modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import servicios.GestorXML;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private LoginController controller;

    @BeforeEach
    void preparar(@TempDir Path carpeta) throws IOException {
        GestorXML gestor = new GestorXML();
        Administrador admin = new Administrador();
        admin.setId("admin1");
        admin.setClave("admin1");
        gestor.guardarDatos(new ArrayList<>(List.of(admin)), carpeta.resolve("administradores.xml").toString());

        Funcionario f = new Funcionario();
        f.setId("func1");
        f.setClave("func1");
        f.setNombre("Juan");
        f.setTelefono("8888-8888");
        gestor.guardarDatos(new ArrayList<>(List.of(f)), carpeta.resolve("funcionarios.xml").toString());

        controller = new LoginController(carpeta.toString());
    }

    @Test
    void autenticar_administradorCorrecto_retornaAdministrador() throws IOException {
        Usuario u = controller.autenticar("admin1", "admin1");
        assertInstanceOf(Administrador.class, u);
    }

    @Test
    void autenticar_funcionarioCorrecto_retornaFuncionario() throws IOException {
        Usuario u = controller.autenticar("func1", "func1");
        assertInstanceOf(Funcionario.class, u);
    }

    @Test
    void autenticar_claveIncorrecta_retornaNull() throws IOException {
        assertNull(controller.autenticar("func1", "otra"));
        assertNull(controller.autenticar("noexiste", "x"));
    }

    @Test
    void cambiarClave_correcta_permiteIngresarConLaNueva() throws IOException {
        controller.cambiarClave("func1", "func1", "nueva123");
        assertNull(controller.autenticar("func1", "func1"));
        assertNotNull(controller.autenticar("func1", "nueva123"));
    }

    @Test
    void cambiarClave_claveActualIncorrecta_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.cambiarClave("func1", "mala", "nueva123"));
    }

    @Test
    void cambiarClave_nuevaIgualALaActual_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.cambiarClave("admin1", "admin1", "admin1"));
    }

    @Test
    void cambiarClave_usuarioInexistente_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.cambiarClave("nadie", "x", "nueva123"));
    }
}
