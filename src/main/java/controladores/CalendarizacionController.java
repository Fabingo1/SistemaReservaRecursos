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
import java.util.Date;
import java.util.List;

/**
 * Controlador de la funcionalidad 6: "Visualización de calendarización de
 * recursos" (matriz hora x recurso para una fecha y categoría).
 */
public class CalendarizacionController {

    private final String rutaCategorias;
    private final String rutaRecursos;
    private final String rutaReservas;

    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public CalendarizacionController() {
        this("data");
    }

    /** Permite usar otra carpeta de datos (pruebas con @TempDir). */
    public CalendarizacionController(String carpetaDatos) {
        this.rutaCategorias = new File(carpetaDatos, "categorias.xml").getPath();
        this.rutaRecursos = new File(carpetaDatos, "recursos.xml").getPath();
        this.rutaReservas = new File(carpetaDatos, "reservas.xml").getPath();
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    public List<Categoria> obtenerCategorias() throws IOException {
        return gestorXML.cargarDatos(rutaCategorias);
    }

    public List<Recurso> obtenerRecursosPorCategoria(Categoria categoria) throws IOException {
        List<Recurso> todos = gestorXML.cargarDatos(rutaRecursos);
        List<Recurso> filtrados = new ArrayList<>();
        for (Recurso r : todos) {
            if (r.getCategoria() != null && r.getCategoria().getId().equals(categoria.getId())) {
                filtrados.add(r);
            }
        }
        return filtrados;
    }

    public List<Reserva> obtenerReservasDelDia(Date fecha) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(rutaReservas);
        List<Reserva> delDia = new ArrayList<>();
        for (Reserva r : todas) {
            if (r.estaActiva() && r.esDelDia(fecha)) {
                delDia.add(r);
            }
        }
        return delDia;
    }

    /**
     * Retorna la reserva que tiene asignado ese recurso en esa hora del día
     * (o null si está libre). Usa Reserva.ocupaHora, que considera minutos.
     */
    public Reserva buscarReservaEnCelda(List<Reserva> reservasDelDia, Recurso recurso, int hora) {
        for (Reserva r : reservasDelDia) {
            for (DetalleReserva d : r.getDetalles()) {
                if (d.getRecursoAsignado() != null
                        && d.getRecursoAsignado().getId().equals(recurso.getId())
                        && r.ocupaHora(r.getFecha(), hora)) {
                    return r;
                }
            }
        }
        return null;
    }

    /** Texto de la celda: actividad y funcionario responsable. */
    public String descripcionCelda(Reserva r) {
        String nombre = r.getFuncionario() != null ? r.getFuncionario().getNombre() : "N/D";
        return r.getActividad() + " (" + nombre + ")";
    }

    /** Reporte PDF de la matriz (la vista le pasa las filas ya armadas). */
    public void generarReportePDF(String titulo, String[] columnas, List<Object[]> filas, String rutaSalida)
            throws IOException {
        pdfService.generarReporte(titulo, columnas, filas, rutaSalida);
    }
}
