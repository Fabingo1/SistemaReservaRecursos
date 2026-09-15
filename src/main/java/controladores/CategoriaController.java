package controladores;

import modelo.Categoria;
import modelo.DetalleReserva;
import modelo.Recurso;
import modelo.Reserva;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la funcionalidad "Lista de categorías de recursos"
 * (funcionalidad 4 del enunciado). Solo debe ser accesible para el
 * usuario tipo administrador; esa restricción de rol se maneja desde
 * MainView (igual que ya se hace con las demás pestañas), no aquí.
 */
public class CategoriaController {

    private static final String RUTA_CATEGORIAS_POR_DEFECTO = "data/categorias.xml";

    private final String rutaCategorias;
    private final String rutaRecursos;
    private final String rutaReservas;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public CategoriaController() {
        this(RUTA_CATEGORIAS_POR_DEFECTO);
    }

    /**
     * Permite inyectar una ruta distinta a la de producción. Pensado para
     * pruebas unitarias con @TempDir, así no se escribe sobre el
     * data/categorias.xml real del proyecto al correr los tests.
     */
    public CategoriaController(String rutaCategorias) {
        this.rutaCategorias = rutaCategorias;
        // recursos.xml y reservas.xml se buscan en la MISMA carpeta que categorias.xml
        this.rutaRecursos = rutaHermana(rutaCategorias, "recursos.xml");
        this.rutaReservas = rutaHermana(rutaCategorias, "reservas.xml");
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    /** Consulta: todas las categorías registradas. */
    public List<Categoria> listar() throws IOException {
        return gestorXML.cargarDatos(rutaCategorias);
    }

    /**
     * Búsqueda por descripción (parcial, sin distinguir mayúsculas/minúsculas),
     * tal como lo pide el enunciado. Si el texto viene vacío, retorna todas.
     */
    public List<Categoria> buscarPorDescripcion(String texto) throws IOException {
        List<Categoria> todas = listar();
        if (texto == null || texto.isBlank()) {
            return todas;
        }
        String filtro = texto.trim().toLowerCase();
        List<Categoria> resultado = new ArrayList<>();
        for (Categoria c : todas) {
            if (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(filtro)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Útil para que RecursoController valide contra una categoría concreta. */
    public Categoria buscarPorId(String id) throws IOException {
        for (Categoria c : listar()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Inclusión. El id es autogenerado (así lo exige el enunciado), la
     * descripción la da el administrador.
     */
    public Categoria crear(String descripcion) throws IOException {
        validarDescripcion(descripcion);
        List<Categoria> categorias = listar();
        validarDescripcionUnica(categorias, descripcion, null);

        Categoria nueva = new Categoria();
        nueva.setId(generarId(categorias));
        nueva.setDescripcion(descripcion.trim());

        categorias.add(nueva);
        gestorXML.guardarDatos(categorias, rutaCategorias);
        return nueva;
    }

    /** Modificación: el id nunca cambia, solo la descripción. */
    public void modificar(String id, String nuevaDescripcion) throws IOException {
        validarDescripcion(nuevaDescripcion);
        List<Categoria> categorias = listar();

        boolean encontrada = false;
        for (Categoria c : categorias) {
            if (c.getId().equals(id)) {
                c.setDescripcion(nuevaDescripcion.trim());
                validarDescripcionUnica(categorias, nuevaDescripcion, id);
                encontrada = true;
                break;
            }
        }

        if (!encontrada) {
            throw new IllegalArgumentException("No existe una categoría con id " + id);
        }

        gestorXML.guardarDatos(categorias, rutaCategorias);
        propagarDescripcion(id, nuevaDescripcion.trim());
    }

    /** Borrado por id. */
    public void borrar(String id) throws IOException {
        List<Categoria> categorias = listar();
        validarSinRecursos(id);
        boolean eliminada = categorias.removeIf(c -> c.getId().equals(id));

        if (!eliminada) {
            throw new IllegalArgumentException("No existe una categoría con id " + id);
        }

        gestorXML.guardarDatos(categorias, rutaCategorias);
    }

    /**
     * Reporte en PDF, requerido por el enunciado para todas las
     * funcionalidades. Reutiliza el PDFService genérico del equipo.
     */
    public void generarReportePDF(List<Categoria> categorias, String rutaSalida) throws IOException {
        List<Object[]> filas = new ArrayList<>();
        for (Categoria c : categorias) {
            filas.add(new Object[]{c.getId(), c.getDescripcion()});
        }
        pdfService.generarReporte("Listado de Categorías", new String[]{"Id", "Descripción"}, filas, rutaSalida);
    }

    /** No se permiten dos categorías con la misma descripción (sin importar mayúsculas). */
    private void validarDescripcionUnica(List<Categoria> categorias, String descripcion, String idExcluido) {
        String buscada = descripcion.trim();
        for (Categoria c : categorias) {
            if (c.getId().equals(idExcluido)) continue;
            if (c.getDescripcion() != null && c.getDescripcion().trim().equalsIgnoreCase(buscada)) {
                throw new IllegalArgumentException("Ya existe una categoría con la descripción \"" + buscada + "\".");
            }
        }
    }

    /** Evita dejar recursos "huérfanos" apuntando a una categoría borrada. */
    private void validarSinRecursos(String idCategoria) throws IOException {
        List<Recurso> recursos = gestorXML.cargarDatos(rutaRecursos);
        int cantidad = 0;
        for (Recurso r : recursos) {
            if (r.getCategoria() != null && idCategoria.equals(r.getCategoria().getId())) {
                cantidad++;
            }
        }
        if (cantidad > 0) {
            throw new IllegalArgumentException("No se puede borrar la categoría: tiene " + cantidad
                    + " recurso(s) asociado(s). Borre o reasigne esos recursos primero.");
        }
    }

    /**
     * XMLEncoder guarda copias de la categoría dentro de recursos.xml y
     * reservas.xml; al cambiar la descripción se actualizan esas copias para
     * que Recursos, Calendarización y Estadísticas muestren el texto nuevo.
     */
    private void propagarDescripcion(String idCategoria, String nuevaDescripcion) throws IOException {
        List<Recurso> recursos = gestorXML.cargarDatos(rutaRecursos);
        boolean cambioRecursos = false;
        for (Recurso r : recursos) {
            if (r.getCategoria() != null && idCategoria.equals(r.getCategoria().getId())) {
                r.getCategoria().setDescripcion(nuevaDescripcion);
                cambioRecursos = true;
            }
        }
        if (cambioRecursos) {
            gestorXML.guardarDatos(recursos, rutaRecursos);
        }

        List<Reserva> reservas = gestorXML.cargarDatos(rutaReservas);
        boolean cambioReservas = false;
        for (Reserva reserva : reservas) {
            for (DetalleReserva d : reserva.getDetalles()) {
                if (d.getCategoriaSolicitada() != null && idCategoria.equals(d.getCategoriaSolicitada().getId())) {
                    d.getCategoriaSolicitada().setDescripcion(nuevaDescripcion);
                    cambioReservas = true;
                }
                if (d.getRecursoAsignado() != null && d.getRecursoAsignado().getCategoria() != null
                        && idCategoria.equals(d.getRecursoAsignado().getCategoria().getId())) {
                    d.getRecursoAsignado().getCategoria().setDescripcion(nuevaDescripcion);
                    cambioReservas = true;
                }
            }
        }
        if (cambioReservas) {
            gestorXML.guardarDatos(reservas, rutaReservas);
        }
    }

    static String rutaHermana(String rutaArchivo, String nombre) {
        File padre = new File(rutaArchivo).getParentFile();
        return padre == null ? nombre : new File(padre, nombre).getPath();
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la categoría no puede estar vacía.");
        }
    }

    /**
     * Genera el siguiente id disponible con formato CAT-XXX, tomando el
     * número más alto ya usado. Ignora ids con formato distinto (por
     * ejemplo, los datos de prueba "cat1", "cat2") en vez de fallar.
     */
    private String generarId(List<Categoria> existentes) {
        int max = 0;
        for (Categoria c : existentes) {
            String soloNumero = c.getId().replaceAll("[^0-9]", "");
            if (!soloNumero.isEmpty()) {
                try {
                    int numero = Integer.parseInt(soloNumero);
                    max = Math.max(max, numero);
                } catch (NumberFormatException ignored) {
                    // id con formato no numérico; no participa en el cálculo
                }
            }
        }
        return String.format("CAT-%03d", max + 1);
    }
}