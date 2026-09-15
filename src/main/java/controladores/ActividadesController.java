package controladores;

import modelo.Reserva;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Controlador de la funcionalidad 7 del enunciado: "Visualización de
 * programación de actividades". Muestra, para una semana dada, una matriz
 * hora x día con las actividades programadas (sin filtrar por categoría de
 * recurso, a diferencia de la Calendarización de recursos).
 */
public class ActividadesController {

    private final String rutaReservas;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public ActividadesController() {
        this("data");
    }

    /** Permite usar otra carpeta de datos (pruebas con @TempDir). */
    public ActividadesController(String carpetaDatos) {
        this.rutaReservas = new File(carpetaDatos, "reservas.xml").getPath();
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    /** Carga todas las reservas activas (no canceladas) almacenadas en el XML. */
    public List<Reserva> cargarReservasActivas() throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(rutaReservas);
        List<Reserva> activas = new ArrayList<>();
        for (Reserva r : todas) {
            if (r.estaActiva()) {
                activas.add(r);
            }
        }
        return activas;
    }

    /** Retorna la fecha (a las 00:00) del lunes de la semana que contiene fecha. */
    public Date obtenerLunesDeSemana(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(fecha);
        // Retroceder día a día hasta llegar al lunes (evita ambigüedades de
        // Calendar.set(DAY_OF_WEEK) con el domingo según el Locale).
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        limpiarHora(cal);
        return cal.getTime();
    }

    /** Retorna los 7 días (lunes a domingo) de la semana que contiene fechaReferencia. */
    public List<Date> obtenerDiasDeSemana(Date fechaReferencia) {
        List<Date> dias = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(obtenerLunesDeSemana(fechaReferencia));
        for (int i = 0; i < 7; i++) {
            dias.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dias;
    }

    /**
     * De las reservas activas, retorna las que caen dentro de la semana
     * (lunes a domingo) que contiene fechaReferencia.
     */
    public List<Reserva> obtenerReservasSemana(Date fechaReferencia) throws IOException {
        Date lunes = obtenerLunesDeSemana(fechaReferencia);
        Calendar calFin = Calendar.getInstance();
        calFin.setTime(lunes);
        calFin.add(Calendar.DAY_OF_MONTH, 7);
        Date finSemanaExclusivo = calFin.getTime();

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : cargarReservasActivas()) {
            if (r.getFecha() == null) continue;
            Date dia = truncarADia(r.getFecha());
            if (!dia.before(lunes) && dia.before(finSemanaExclusivo)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * Indica si la reserva ocupa (aunque sea parcialmente) la franja de la
     * hora dada en ese día. Usa la misma regla que Calendarización
     * (Reserva.ocupaHora), considerando minutos.
     */
    public boolean ocurreEn(Reserva r, Date dia, int hora) {
        return r.ocupaHora(dia, hora);
    }

    /** Texto a mostrar en la celda de la matriz para una reserva dada. */
    public String descripcionCelda(Reserva r) {
        String funcionario = r.getFuncionario() != null ? r.getFuncionario().getNombre() : "N/D";
        return r.getActividad() + " (" + funcionario + ")";
    }

    /** Reporte PDF de la matriz semanal (la vista le pasa las filas ya armadas). */
    public void generarReportePDF(String titulo, String[] columnas, List<Object[]> filas, String rutaSalida)
            throws IOException {
        pdfService.generarReporte(titulo, columnas, filas, rutaSalida);
    }

    private Date truncarADia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        limpiarHora(cal);
        return cal.getTime();
    }

    private void limpiarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
