package controladores;

import modelo.Categoria;
import modelo.Recurso;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la funcionalidad "Lista de recursos" (funcionalidad 5 del
 * enunciado). Solo accesible para el usuario tipo administrador; esa
 * restricción de rol se maneja desde MainView, igual que en las demás
 * pestañas.
 *
 * A diferencia de CategoriaController, aquí el id NO se autogenera: el
 * enunciado pide "su id o número de activo" (ej. "Laptop #238715"), es
 * decir, lo ingresa el administrador y debe ser único.
 */
public class RecursoController {

    private static final String RUTA_RECURSOS_POR_DEFECTO = "data/recursos.xml";
    private static final String RUTA_CATEGORIAS_POR_DEFECTO = "data/categorias.xml";

    private final String rutaRecursos;
    private final String rutaCategorias;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public RecursoController() {
        this(RUTA_RECURSOS_POR_DEFECTO, RUTA_CATEGORIAS_POR_DEFECTO);
    }

    /** Constructor con rutas inyectables, pensado para pruebas con @TempDir. */
    public RecursoController(String rutaRecursos, String rutaCategorias) {
        this.rutaRecursos = rutaRecursos;
        this.rutaCategorias = rutaCategorias;
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    /** Para llenar el combo de categorías en la vista (igual que CalendarizacionController). */
    public List<Categoria> obtenerCategorias() throws IOException {
        return gestorXML.cargarDatos(rutaCategorias);
    }

    public List<Recurso> listar() throws IOException {
        return gestorXML.cargarDatos(rutaRecursos);
    }

    /**
     * Filtrado combinado: por categoría (obligatoria en el mockup del
     * enunciado) y opcionalmente por texto de descripción. Cualquiera de
     * los dos parámetros puede venir nulo/vacío para no aplicar ese filtro.
     */
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

    /** Inclusión. El id lo escribe el administrador (número de activo). */
    public Recurso crear(String id, Categoria categoria, String descripcion) throws IOException {
        validar(id, categoria, descripcion);
        List<Recurso> recursos = listar();

        if (buscarEnLista(recursos, id) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con id/activo " + id);
        }

        Recurso nuevo = new Recurso();
        nuevo.setId(id.trim());
        nuevo.setCategoria(categoria);
        nuevo.setDescripcion(descripcion.trim());

        recursos.add(nuevo);
        gestorXML.guardarDatos(recursos, rutaRecursos);
        return nuevo;
    }

    /** Modificación: el id no cambia (es la clave del recurso), solo categoría y descripción. */
    public void modificar(String id, Categoria nuevaCategoria, String nuevaDescripcion) throws IOException {
        validar(id, nuevaCategoria, nuevaDescripcion);
        List<Recurso> recursos = listar();

        Recurso existente = buscarEnLista(recursos, id);
        if (existente == null) {
            throw new IllegalArgumentException("No existe un recurso con id " + id);
        }
        existente.setCategoria(nuevaCategoria);
        existente.setDescripcion(nuevaDescripcion.trim());

        gestorXML.guardarDatos(recursos, rutaRecursos);
    }

    public void borrar(String id) throws IOException {
        List<Recurso> recursos = listar();
        boolean eliminado = recursos.removeIf(r -> r.getId().equals(id));

        if (!eliminado) {
            throw new IllegalArgumentException("No existe un recurso con id " + id);
        }

        gestorXML.guardarDatos(recursos, rutaRecursos);
    }

    /** Reporte en PDF, requerido por el enunciado para todas las funcionalidades. */
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
        // La categoría debe ser una de las disponibles (enunciado: "categoría
        // seleccionada de las disponibles"), no un objeto cualquiera que
        // haya quedado desactualizado en la UI.
        if (buscarCategoriaPorId(categoria.getId()) == null) {
            throw new IllegalArgumentException("La categoría seleccionada ya no existe.");
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
