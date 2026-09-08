package controladores;

import modelo.Reserva;
import servicios.GestorXML;

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
 *
 * La lógica que cruza varias entidades (filtrar reservas por semana, decidir
 * si una reserva ocupa cierto día/hora) vive aquí, en el Controller, según lo
 * definido en las decisiones de arquitectura del proyecto.
 */
public class ActividadesController {

    private static final String RUTA_RESERVAS = "data/reservas.xml";

    private final GestorXML gestorXML;

    public ActividadesController() {
        this.gestorXML = new GestorXML();
    }

    /** Carga todas las reservas activas (no canceladas) almacenadas en el XML. */
    public List<Reserva> cargarReservasActivas() throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(RUTA_RESERVAS);
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
        cal.setTime(fecha);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
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
     * Indica si la reserva ocupa el día (mismo año/mes/día) y la hora (0-23)
     * dados. Se considera que ocupa la hora H si horaInicio <= H:00 < horaFin.
     */
    public boolean ocurreEn(Reserva r, Date dia, int hora) {
        if (r.getFecha() == null || r.getHoraInicio() == null || r.getHoraFin() == null) {
            return false;
        }
        if (!mismoDia(r.getFecha(), dia)) {
            return false;
        }

        double inicioDecimal = obtenerHora(r.getHoraInicio()) + obtenerMinuto(r.getHoraInicio()) / 60.0;
        double finDecimal = obtenerHora(r.getHoraFin()) + obtenerMinuto(r.getHoraFin()) / 60.0;

        return hora >= inicioDecimal && hora < finDecimal;
    }

    /** Texto a mostrar en la celda de la matriz para una reserva dada. */
    public String descripcionCelda(Reserva r) {
        String funcionario = r.getFuncionario() != null ? r.getFuncionario().getNombre() : "N/D";
        return r.getActividad() + " (" + funcionario + ")";
    }

    private boolean mismoDia(Date a, Date b) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
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

    private int obtenerHora(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    private int obtenerMinuto(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        return cal.get(Calendar.MINUTE);
    }
}
