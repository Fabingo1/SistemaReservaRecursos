package controladores;

import modelo.DetalleReserva;
import modelo.Reserva;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de la funcionalidad 8 del enunciado: "Estadísticas". Calcula,
 * para un rango de fechas [desde, hasta]:
 *  - la cantidad de reservas por categoría de recurso, y
 *  - la cantidad de actividades programadas en CADA semana del período
 *    (incluyendo las semanas con 0 actividades, como pide el enunciado).
 */
public class EstadisticasController {

    private final String rutaReservas;
    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public EstadisticasController() {
        this("data");
    }

    /** Permite usar otra carpeta de datos (pruebas con @TempDir). */
    public EstadisticasController(String carpetaDatos) {
        this.rutaReservas = new File(carpetaDatos, "reservas.xml").getPath();
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    /**
     * Cantidad de veces que se reservó cada categoría de recurso dentro del
     * período [desde, hasta], ordenado de mayor a menor cantidad. Se agrupa
     * por id de categoría (dos categorías con igual descripción no se mezclan).
     */
    public Map<String, Integer> estadisticasRecursos(Date desde, Date hasta) throws IOException {
        validarRango(desde, hasta);
        Map<String, Integer> conteoPorId = new HashMap<>();
        Map<String, String> descripcionPorId = new HashMap<>();
        for (Reserva r : cargarReservasActivasEnRango(desde, hasta)) {
            for (DetalleReserva d : r.getDetalles()) {
                if (d.getCategoriaSolicitada() == null) continue;
                String id = d.getCategoriaSolicitada().getId();
                conteoPorId.merge(id, 1, Integer::sum);
                descripcionPorId.putIfAbsent(id, d.getCategoriaSolicitada().getDescripcion());
            }
        }

        Map<String, Integer> resultado = new LinkedHashMap<>();
        conteoPorId.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> {
                    String etiqueta = descripcionPorId.get(e.getKey());
                    if (resultado.containsKey(etiqueta)) {
                        etiqueta = etiqueta + " (" + e.getKey() + ")";
                    }
                    resultado.put(etiqueta, e.getValue());
                });
        return resultado;
    }

    /**
     * Cantidad de actividades (reservas activas) en cada semana (lunes a
     * domingo) comprendida en [desde, hasta], en orden cronológico.
     * Las semanas sin actividades aparecen con 0.
     */
    public Map<String, Integer> estadisticasActividades(Date desde, Date hasta) throws IOException {
        validarRango(desde, hasta);

        // 1. Crear todas las semanas del período con conteo 0
        Map<Date, Integer> conteoPorLunes = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(lunesDe(desde));
        Date limite = finDelDia(hasta);
        while (!cal.getTime().after(limite)) {
            conteoPorLunes.put(cal.getTime(), 0);
            cal.add(Calendar.DAY_OF_MONTH, 7);
        }

        // 2. Sumar las actividades de cada semana
        for (Reserva r : cargarReservasActivasEnRango(desde, hasta)) {
            conteoPorLunes.merge(lunesDe(r.getFecha()), 1, Integer::sum);
        }

        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (Map.Entry<Date, Integer> entrada : conteoPorLunes.entrySet()) {
            resultado.put(etiquetaSemana(entrada.getKey()), entrada.getValue());
        }
        return resultado;
    }

    /** Reporte PDF de una tabla de estadísticas (etiqueta, cantidad). */
    public void generarReportePDF(String titulo, String nombreColumna, Map<String, Integer> datos,
                                  String rutaSalida) throws IOException {
        List<Object[]> filas = new ArrayList<>();
        int total = 0;
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            filas.add(new Object[]{e.getKey(), e.getValue()});
            total += e.getValue();
        }
        filas.add(new Object[]{"TOTAL", total});
        pdfService.generarReporte(titulo, new String[]{nombreColumna, "Cantidad"}, filas, rutaSalida);
    }

    private void validarRango(Date desde, Date hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las fechas 'desde' y 'hasta'.");
        }
        if (truncarADia(desde).after(truncarADia(hasta))) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser posterior a 'Hasta'.");
        }
    }

    private List<Reserva> cargarReservasActivasEnRango(Date desde, Date hasta) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(rutaReservas);
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

    private Date lunesDe(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(truncarADia(fecha));
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        return cal.getTime();
    }

    private String etiquetaSemana(Date lunes) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yy");
        Calendar cal = Calendar.getInstance();
        cal.setTime(lunes);
        cal.add(Calendar.DAY_OF_MONTH, 6);
        return formato.format(lunes) + " - " + formato.format(cal.getTime());
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
