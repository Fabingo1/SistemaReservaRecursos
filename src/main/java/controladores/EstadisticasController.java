package controladores;

import modelo.DetalleReserva;
import modelo.Reserva;
import servicios.GestorXML;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Controlador de la funcionalidad 8 del enunciado: "Estadísticas". Calcula,
 * para un rango de fechas [desde, hasta]:
 *  - la cantidad de reservas por categoría de recurso (para el gráfico de
 *    recursos reservados), y
 *  - la cantidad de actividades (reservas) programadas por semana (para el
 *    gráfico de actividades calendarizadas).
 *
 * Ambos cálculos cruzan varias entidades (Reserva, DetalleReserva, Categoria),
 * por lo que, según las decisiones de arquitectura del proyecto, viven en el
 * Controller y no en el modelo de dominio.
 */
public class EstadisticasController {

    private static final String RUTA_RESERVAS = "data/reservas.xml";
    private static final SimpleDateFormat FORMATO_DIA = new SimpleDateFormat("dd/MM");

    private final GestorXML gestorXML;

    public EstadisticasController() {
        this.gestorXML = new GestorXML();
    }

    /**
     * Cantidad de veces que se reservó cada categoría de recurso dentro del
     * período [desde, hasta], ordenado de mayor a menor cantidad.
     */
    public Map<String, Integer> estadisticasRecursos(Date desde, Date hasta) throws IOException {
        Map<String, Integer> conteo = new HashMap<>();
        for (Reserva r : cargarReservasActivasEnRango(desde, hasta)) {
            for (DetalleReserva d : r.getDetalles()) {
                if (d.getCategoriaSolicitada() == null) continue;
                String descripcion = d.getCategoriaSolicitada().getDescripcion();
                conteo.merge(descripcion, 1, Integer::sum);
            }
        }
        return ordenarPorValorDescendente(conteo);
    }

    /**
     * Cantidad de actividades (reservas) programadas en cada semana
     * comprendida en el período [desde, hasta], ordenado cronológicamente.
     */
    public Map<String, Integer> estadisticasActividades(Date desde, Date hasta) throws IOException {
        TreeMap<Date, Integer> conteoPorLunes = new TreeMap<>();
        for (Reserva r : cargarReservasActivasEnRango(desde, hasta)) {
            Date lunes = lunesDe(r.getFecha());
            conteoPorLunes.merge(lunes, 1, Integer::sum);
        }

        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (Map.Entry<Date, Integer> entrada : conteoPorLunes.entrySet()) {
            resultado.put(etiquetaSemana(entrada.getKey()), entrada.getValue());
        }
        return resultado;
    }

    private List<Reserva> cargarReservasActivasEnRango(Date desde, Date hasta) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(RUTA_RESERVAS);
        List<Reserva> resultado = new ArrayList<>();
        Date desdeDia = truncarADia(desde);
        Date hastaFinDia = finDelDia(hasta);
        for (Reserva r : todas) {
            if (!r.estaActiva() || r.getFecha() == null) continue;
            if (!r.getFecha().before(desdeDia) && !r.getFecha().after(hastaFinDia)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    private Map<String, Integer> ordenarPorValorDescendente(Map<String, Integer> mapa) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        mapa.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> resultado.put(e.getKey(), e.getValue()));
        return resultado;
    }

    private Date lunesDe(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return truncarADia(cal.getTime());
    }

    private String etiquetaSemana(Date lunes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lunes);
        cal.add(Calendar.DAY_OF_MONTH, 6);
        return FORMATO_DIA.format(lunes) + " - " + FORMATO_DIA.format(cal.getTime());
    }

    private Date truncarADia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date finDelDia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
