package controladores;

import modelo.Administrador;
import modelo.Funcionario;
import modelo.Reserva;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// El id lo asigna el administrador (no se autogenera). La clave inicial queda igual al id
public class FuncionarioController {

    private static final String RUTA_FUNCIONARIOS_POR_DEFECTO = "data/funcionarios.xml";
    private static final String RUTA_ADMINISTRADORES_POR_DEFECTO = "data/administradores.xml";

    private final String rutaFuncionarios;
    private final String rutaAdministradores;
    private final String rutaReservas;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public FuncionarioController() {
        this(RUTA_FUNCIONARIOS_POR_DEFECTO, RUTA_ADMINISTRADORES_POR_DEFECTO);
    }

    public FuncionarioController(String rutaFuncionarios, String rutaAdministradores) {
        this.rutaFuncionarios = rutaFuncionarios;
        this.rutaAdministradores = rutaAdministradores;
        this.rutaReservas = CategoriaController.rutaHermana(rutaFuncionarios, "reservas.xml");
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    public List<Funcionario> listar() throws IOException {
        return gestorXML.cargarDatos(rutaFuncionarios);
    }

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
        nuevo.setClave(id.trim());
        nuevo.setNombre(nombre.trim());
        nuevo.setTelefono(telefono.trim());

        funcionarios.add(nuevo);
        gestorXML.guardarDatos(funcionarios, rutaFuncionarios);
        return nuevo;
    }

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
        propagarDatos(id, nuevoNombre.trim(), nuevoTelefono.trim());
    }

    public void borrar(String id) throws IOException {
        if (tieneReservasFuturas(id)) {
            throw new IllegalArgumentException("No se puede borrar al funcionario " + id
                    + ": tiene reservas activas pendientes. Deben cancelarse primero.");
        }
        List<Funcionario> funcionarios = listar();
        boolean eliminado = funcionarios.removeIf(f -> f.getId().equals(id));

        if (!eliminado) {
            throw new IllegalArgumentException("No existe un funcionario con id " + id);
        }

        gestorXML.guardarDatos(funcionarios, rutaFuncionarios);
    }

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
        if (id.trim().contains(" ")) {
            throw new IllegalArgumentException("El id del funcionario no puede contener espacios.");
        }
        // Teléfono: solo dígitos, espacios, guiones, paréntesis o +, con al menos 8 dígitos
        String soloDigitos = telefono.replaceAll("[^0-9]", "");
        if (!telefono.trim().matches("[0-9+()\\-\\s]+") || soloDigitos.length() < 8) {
            throw new IllegalArgumentException("El teléfono no es válido (ej. 8888-8888).");
        }
    }


    private boolean tieneReservasFuturas(String idFuncionario) throws IOException {
        List<Reserva> reservas = gestorXML.cargarDatos(rutaReservas);
        for (Reserva r : reservas) {
            if (r.estaActiva() && !r.yaPaso() && r.getFuncionario() != null
                    && idFuncionario.equals(r.getFuncionario().getId())) {
                return true;
            }
        }
        return false;
    }


    private void propagarDatos(String idFuncionario, String nombre, String telefono) throws IOException {
        List<Reserva> reservas = gestorXML.cargarDatos(rutaReservas);
        boolean cambio = false;
        for (Reserva r : reservas) {
            if (r.getFuncionario() != null && idFuncionario.equals(r.getFuncionario().getId())) {
                r.getFuncionario().setNombre(nombre);
                r.getFuncionario().setTelefono(telefono);
                cambio = true;
            }
        }
        if (cambio) {
            gestorXML.guardarDatos(reservas, rutaReservas);
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
