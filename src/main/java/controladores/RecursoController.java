package controladores;

import modelo.Categoria;
import modelo.DetalleReserva;
import modelo.Recurso;
import modelo.Reserva;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// A diferencia de CategoriaController, aquí el id (número de activo) NO se autogenera: lo ingresa el administrador.
public class RecursoController {

    private static final String RUTA_RECURSOS_POR_DEFECTO = "data/recursos.xml";
    private static final String RUTA_CATEGORIAS_POR_DEFECTO = "data/categorias.xml";

    private final String rutaRecursos;
    private final String rutaCategorias;
    private final String rutaReservas;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public RecursoController() {
        this(RUTA_RECURSOS_POR_DEFECTO, RUTA_CATEGORIAS_POR_DEFECTO);
    }

    /** Constructor con rutas inyectables, pensado para pruebas con @TempDir. */
    public RecursoController(String rutaRecursos, String rutaCategorias) {
        this.rutaRecursos = rutaRecursos;
        this.rutaCategorias = rutaCategorias;
        this.rutaReservas = CategoriaController.rutaHermana(rutaRecursos, "reservas.xml");
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    public List<Categoria> obtenerCategorias() throws IOException {
        return gestorXML.cargarDatos(rutaCategorias);
    }

    public List<Recurso> listar() throws IOException {
        return gestorXML.cargarDatos(rutaRecursos);
    }

    public List<Recurso> buscar(Categoria categoria, String textoDescripcion) throws IOException {
        List<Recurso> base = filtrarPorCategoria(categoria);

        if (textoDescripcion == null || textoDescripcion.isBlank()) {
            return base;
        }

        String filtro = textoDescripcion.trim().toLowerCase();
        List<Recurso> resultado = new ArrayList<>();
        for (Recurso r : base) {
            if (r.getDescripcion() != null && r.getDescripcion().toLowerCase().contains(filtro)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public List<Recurso> filtrarPorCategoria(Categoria categoria) throws IOException {
        if (categoria == null) {
            return listar();
        }
        List<Recurso> filtrados = new ArrayList<>();
        for (Recurso r : listar()) {
            if (r.getCategoria() != null && r.getCategoria().getId().equals(categoria.getId())) {
                filtrados.add(r);
            }
        }
        return filtrados;
    }

    public Recurso buscarPorId(String id) throws IOException {
        for (Recurso r : listar()) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    public Recurso crear(String id, Categoria categoria, String descripcion) throws IOException {
        validar(id, categoria, descripcion);
        List<Recurso> recursos = listar();

        if (buscarEnLista(recursos, id) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con id/activo " + id);
        }

        Recurso nuevo = new Recurso();
        nuevo.setId(id.trim());
        nuevo.setCategoria(buscarCategoriaPorId(categoria.getId()));
        nuevo.setDescripcion(descripcion.trim());

        recursos.add(nuevo);
        gestorXML.guardarDatos(recursos, rutaRecursos);
        return nuevo;
    }

    public void modificar(String id, Categoria nuevaCategoria, String nuevaDescripcion) throws IOException {
        validar(id, nuevaCategoria, nuevaDescripcion);
        List<Recurso> recursos = listar();

        Recurso existente = buscarEnLista(recursos, id);
        if (existente == null) {
            throw new IllegalArgumentException("No existe un recurso con id " + id);
        }
        boolean cambiaCategoria = existente.getCategoria() == null
                || !existente.getCategoria().getId().equals(nuevaCategoria.getId());
        if (cambiaCategoria && tieneReservasFuturas(id)) {
            throw new IllegalArgumentException("No se puede cambiar la categoría del recurso " + id
                    + ": tiene reservas activas pendientes.");
        }
        existente.setCategoria(buscarCategoriaPorId(nuevaCategoria.getId()));
        existente.setDescripcion(nuevaDescripcion.trim());

        gestorXML.guardarDatos(recursos, rutaRecursos);
        propagarDescripcion(id, nuevaDescripcion.trim());
    }

    public void borrar(String id) throws IOException {
        if (tieneReservasFuturas(id)) {
            throw new IllegalArgumentException("No se puede borrar el recurso " + id
                    + ": tiene reservas activas pendientes. Deben cancelarse primero.");
        }
        List<Recurso> recursos = listar();
        boolean eliminado = recursos.removeIf(r -> r.getId().equals(id));

        if (!eliminado) {
            throw new IllegalArgumentException("No existe un recurso con id " + id);
        }

        gestorXML.guardarDatos(recursos, rutaRecursos);
    }

    public void generarReportePDF(List<Recurso> recursos, String rutaSalida) throws IOException {
        List<Object[]> filas = new ArrayList<>();
        for (Recurso r : recursos) {
            String categoriaTexto = r.getCategoria() != null ? r.getCategoria().getDescripcion() : "";
            filas.add(new Object[]{r.getId(), categoriaTexto, r.getDescripcion()});
        }
        pdfService.generarReporte("Listado de Recursos", new String[]{"Id", "Categoría", "Descripción"}, filas, rutaSalida);
    }

    private void validar(String id, Categoria categoria, String descripcion) throws IOException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id o número de activo del recurso no puede estar vacío.");
        }
        if (categoria == null || categoria.getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría para el recurso.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del recurso no puede estar vacía.");
        }
        // Rechaza una categoría desactualizada en la UI (ya borrada del XML).
        if (buscarCategoriaPorId(categoria.getId()) == null) {
            throw new IllegalArgumentException("La categoría seleccionada ya no existe.");
        }
    }

    // true si el recurso está asignado a alguna reserva activa que aún no terminó.
    private boolean tieneReservasFuturas(String idRecurso) throws IOException {
        List<Reserva> reservas = gestorXML.cargarDatos(rutaReservas);
        for (Reserva r : reservas) {
            if (!r.estaActiva() || r.yaPaso()) continue;
            for (DetalleReserva d : r.getDetalles()) {
                if (d.getRecursoAsignado() != null && idRecurso.equals(d.getRecursoAsignado().getId())) {
                    return true;
                }
            }
        }
        return false;
    }


    private void propagarDescripcion(String idRecurso, String nuevaDescripcion) throws IOException {
        List<Reserva> reservas = gestorXML.cargarDatos(rutaReservas);
        boolean cambio = false;
        for (Reserva r : reservas) {
            for (DetalleReserva d : r.getDetalles()) {
                if (d.getRecursoAsignado() != null && idRecurso.equals(d.getRecursoAsignado().getId())) {
                    d.getRecursoAsignado().setDescripcion(nuevaDescripcion);
                    cambio = true;
                }
            }
        }
        if (cambio) {
            gestorXML.guardarDatos(reservas, rutaReservas);
        }
    }

    private Categoria buscarCategoriaPorId(String id) throws IOException {
        for (Categoria c : obtenerCategorias()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private Recurso buscarEnLista(List<Recurso> recursos, String id) {
        for (Recurso r : recursos) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }
}
