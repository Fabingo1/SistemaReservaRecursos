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
}