package controladores;

import modelo.Administrador;
import modelo.Funcionario;
import modelo.Usuario;
import servicios.GestorXML;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LoginController {

    private final String rutaAdmins;
    private final String rutaFuncionarios;

    private final GestorXML gestorXML;

    public LoginController() {
        this("data");
    }

    /** Permite usar otra carpeta de datos (pruebas con @TempDir). */
    public LoginController(String carpetaDatos) {
        this.rutaAdmins = new File(carpetaDatos, "administradores.xml").getPath();
        this.rutaFuncionarios = new File(carpetaDatos, "funcionarios.xml").getPath();
        this.gestorXML = new GestorXML();
    }

    /**
     * Intenta autenticar contra administradores.xml y funcionarios.xml.
     * Retorna el Usuario autenticado, o null si no hay coincidencia.
     */
    public Usuario autenticar(String id, String clave) throws IOException {
        if (id == null || clave == null) {
            return null;
        }
        id = id.trim();
        List<Administrador> admins = gestorXML.cargarDatos(rutaAdmins);
        for (Administrador a : admins) {
            if (a.getId().equals(id) && a.getClave().equals(clave)) {
                return a;
            }
        }

        List<Funcionario> funcionarios = gestorXML.cargarDatos(rutaFuncionarios);
        for (Funcionario f : funcionarios) {
            if (f.getId().equals(id) && f.getClave().equals(clave)) {
                return f;
            }
        }

        return null; // no encontrado / clave incorrecta
    }

    /**
     * Cambia la clave de un usuario (administrador o funcionario) validando
     * primero la clave actual. Persiste el cambio de una vez en el XML
     * correspondiente. Reutiliza Usuario.cambiarClave(), que valida que la
     * clave nueva no esté vacía.
     */
    public void cambiarClave(String id, String claveActual, String claveNueva) throws IOException {
        if (claveNueva == null || claveNueva.isBlank()) {
            throw new IllegalArgumentException("La clave nueva no puede estar vacía.");
        }
        if (claveNueva.length() < 4) {
            throw new IllegalArgumentException("La clave nueva debe tener al menos 4 caracteres.");
        }
        if (claveNueva.equals(claveActual)) {
            throw new IllegalArgumentException("La clave nueva debe ser distinta de la actual.");
        }
        List<Administrador> admins = gestorXML.cargarDatos(rutaAdmins);
        for (Administrador a : admins) {
            if (a.getId().equals(id)) {
                validarClaveActual(a, claveActual);
                a.cambiarClave(claveNueva);
                gestorXML.guardarDatos(admins, rutaAdmins);
                return;
            }
        }

        List<Funcionario> funcionarios = gestorXML.cargarDatos(rutaFuncionarios);
        for (Funcionario f : funcionarios) {
            if (f.getId().equals(id)) {
                validarClaveActual(f, claveActual);
                f.cambiarClave(claveNueva);
                gestorXML.guardarDatos(funcionarios, rutaFuncionarios);
                return;
            }
        }

        throw new IllegalArgumentException("No se encontró un usuario con ese id.");
    }

    private void validarClaveActual(Usuario usuario, String claveActual) {
        if (!usuario.getClave().equals(claveActual)) {
            throw new IllegalArgumentException("La clave actual no es correcta.");
        }
    }
}