package controladores;

import modelo.Administrador;
import modelo.Funcionario;
import modelo.Usuario;
import servicios.GestorXML;

import java.io.IOException;
import java.util.List;

public class LoginController {

    private static final String RUTA_ADMINS = "data/administradores.xml";
    private static final String RUTA_FUNCIONARIOS = "data/funcionarios.xml";

    private final GestorXML gestorXML;

    public LoginController() {
        this.gestorXML = new GestorXML();
    }

    /**
     * Intenta autenticar contra administradores.xml y funcionarios.xml.
     * Retorna el Usuario autenticado, o null si no hay coincidencia.
     */
    public Usuario autenticar(String id, String clave) throws IOException {
        List<Administrador> admins = gestorXML.cargarDatos(RUTA_ADMINS);
        for (Administrador a : admins) {
            if (a.getId().equals(id) && a.getClave().equals(clave)) {
                return a;
            }
        }

        List<Funcionario> funcionarios = gestorXML.cargarDatos(RUTA_FUNCIONARIOS);
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
        List<Administrador> admins = gestorXML.cargarDatos(RUTA_ADMINS);
        for (Administrador a : admins) {
            if (a.getId().equals(id)) {
                validarClaveActual(a, claveActual);
                a.cambiarClave(claveNueva);
                gestorXML.guardarDatos(admins, RUTA_ADMINS);
                return;
            }
        }

        List<Funcionario> funcionarios = gestorXML.cargarDatos(RUTA_FUNCIONARIOS);
        for (Funcionario f : funcionarios) {
            if (f.getId().equals(id)) {
                validarClaveActual(f, claveActual);
                f.cambiarClave(claveNueva);
                gestorXML.guardarDatos(funcionarios, RUTA_FUNCIONARIOS);
                return;
            }
        }

        throw new IllegalArgumentException("No se encontro un usuario con ese id.");
    }

    private void validarClaveActual(Usuario usuario, String claveActual) {
        if (!usuario.getClave().equals(claveActual)) {
            throw new IllegalArgumentException("La clave actual no es correcta.");
        }
    }
}