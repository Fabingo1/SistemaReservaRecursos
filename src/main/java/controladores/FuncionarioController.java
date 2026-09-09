package controladores;

import modelo.Administrador;
import modelo.Funcionario;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la funcionalidad "Lista de Funcionarios" (funcionalidad 3
 * del enunciado). Solo accesible para el usuario tipo administrador; esa
 * restricción de rol se maneja desde MainView, igual que en las demás
 * pestañas.
 *
 * El id del funcionario lo asigna el administrador (no se autogenera, el
 * enunciado no lo pide). Al crear un funcionario, su clave queda igual al
 * id; cambiarla después es responsabilidad de la funcionalidad 1
 * (Ingreso/cambio de clave), no de este controlador.
 */
public class FuncionarioController {

    private static final String RUTA_FUNCIONARIOS_POR_DEFECTO = "data/funcionarios.xml";
    private static final String RUTA_ADMINISTRADORES_POR_DEFECTO = "data/administradores.xml";

    private final String rutaFuncionarios;
    private final String rutaAdministradores;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public FuncionarioController() {
        this(RUTA_FUNCIONARIOS_POR_DEFECTO, RUTA_ADMINISTRADORES_POR_DEFECTO);
    }

    /** Constructor con rutas inyectables, pensado para pruebas con @TempDir. */
    public FuncionarioController(String rutaFuncionarios, String rutaAdministradores) {
        this.rutaFuncionarios = rutaFuncionarios;
        this.rutaAdministradores = rutaAdministradores;
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    public List<Funcionario> listar() throws IOException {
        return gestorXML.cargarDatos(rutaFuncionarios);
    }

    /**
     * Búsqueda por id o nombre (coincidencia parcial, sin distinguir
     * mayúsculas/minúsculas), tal como lo pide el enunciado. Si el texto
     * viene vacío, retorna todos.
     */
    public List<Funcionario> buscar(String texto) throws IOException {
        List<Funcionario> todos = listar();
        if (texto == null || texto.isBlank()) {
            return todos;
        }

        String filtro = texto.trim().toLowerCase();
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : todos) {
            boolean coincideId = f.getId() != null && f.getId().toLowerCase().contains(filtro);
            boolean coincideNombre = f.getNombre() != null && f.getNombre().toLowerCase().contains(filtro);
            if (coincideId || coincideNombre) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public Funcionario buscarPorId(String id) throws IOException {
        for (Funcionario f : listar()) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    /** Inclusión. La clave inicial queda igual al id, como pide el enunciado. */
    public Funcionario crear(String id, String nombre, String telefono) throws IOException {
        validarDatos(id, nombre, telefono);
        List<Funcionario> funcionarios = listar();

        if (buscarEnLista(funcionarios, id) != null) {
            throw new IllegalArgumentException("Ya existe un funcionario con id " + id);
        }
        if (existeComoAdministrador(id)) {
            throw new IllegalArgumentException(
                    "El id " + id + " ya está en uso por un administrador; el login se confundiría.");
        }

        Funcionario nuevo = new Funcionario();
        nuevo.setId(id.trim());
        nuevo.setClave(id.trim()); // clave inicial = id, se cambia luego desde "Cambiar clave"
        nuevo.setNombre(nombre.trim());
        nuevo.setTelefono(telefono.trim());

        funcionarios.add(nuevo);
        gestorXML.guardarDatos(funcionarios, rutaFuncionarios);
        return nuevo;
    }

    /**
     * Modificación: el id y la clave NO se tocan aquí. El id es la clave
     * primaria del registro, y la clave del usuario se administra desde la
     * funcionalidad de "Cambiar clave" (funcionalidad 1), no desde el CRUD
     * de administrador.
     */
    public void modificar(String id, String nuevoNombre, String nuevoTelefono) throws IOException {
        validarDatos(id, nuevoNombre, nuevoTelefono);
        List<Funcionario> funcionarios = listar();

        Funcionario existente = buscarEnLista(funcionarios, id);
        if (existente == null) {
            throw new IllegalArgumentException("No existe un funcionario con id " + id);
        }
        existente.setNombre(nuevoNombre.trim());
        existente.setTelefono(nuevoTelefono.trim());

        gestorXML.guardarDatos(funcionarios, rutaFuncionarios);
    }

    public void borrar(String id) throws IOException {
        List<Funcionario> funcionarios = listar();
        boolean eliminado = funcionarios.removeIf(f -> f.getId().equals(id));

        if (!eliminado) {
            throw new IllegalArgumentException("No existe un funcionario con id " + id);
        }

        gestorXML.guardarDatos(funcionarios, rutaFuncionarios);
    }

    /** Reporte en PDF, requerido por el enunciado para todas las funcionalidades. */
    public void generarReportePDF(List<Funcionario> funcionarios, String rutaSalida) throws IOException {
        List<Object[]> filas = new ArrayList<>();
        for (Funcionario f : funcionarios) {
            filas.add(new Object[]{f.getId(), f.getNombre(), f.getTelefono()});
        }
        pdfService.generarReporte("Listado de Funcionarios", new String[]{"Id", "Nombre", "Teléfono"}, filas, rutaSalida);
    }

    private void validarDatos(String id, String nombre, String telefono) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del funcionario no puede estar vacío.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del funcionario no puede estar vacío.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono del funcionario no puede estar vacío.");
        }
    }

    private boolean existeComoAdministrador(String id) throws IOException {
        List<Administrador> administradores = gestorXML.cargarDatos(rutaAdministradores);
        for (Administrador a : administradores) {
            if (a.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private Funcionario buscarEnLista(List<Funcionario> funcionarios, String id) {
        for (Funcionario f : funcionarios) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }
}
