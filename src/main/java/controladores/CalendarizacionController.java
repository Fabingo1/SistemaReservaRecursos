package controladores;

import modelo.Categoria;
import modelo.Recurso;
import modelo.Reserva;
import servicios.GestorXML;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.DetalleReserva;

public class CalendarizacionController {

    private static final String RUTA_CATEGORIAS = "data/categorias.xml";
    private static final String RUTA_RECURSOS = "data/recursos.xml";
    private static final String RUTA_RESERVAS = "data/reservas.xml";

    private final GestorXML gestorXML;

    public CalendarizacionController() {
        this.gestorXML = new GestorXML();
    }

    public List<Categoria> obtenerCategorias() throws Exception {
        return gestorXML.cargarDatos(RUTA_CATEGORIAS);
    }

    public List<Recurso> obtenerRecursosPorCategoria(Categoria categoria) throws Exception {
        List<Recurso> todos = gestorXML.cargarDatos(RUTA_RECURSOS);
        List<Recurso> filtrados = new ArrayList<>();
        for (Recurso r : todos) {
            if (r.getCategoria() != null && r.getCategoria().getId().equals(categoria.getId())) {
                filtrados.add(r);
            }
        }
        return filtrados;
    }

    public List<Reserva> obtenerReservasDelDia(Date fecha) throws Exception {
        List<Reserva> todas = gestorXML.cargarDatos(RUTA_RESERVAS);
        List<Reserva> delDia = new ArrayList<>();
        for (Reserva r : todas) {
            if (r.estaActiva() && esMismoDia(r.getFecha(), fecha)) {
                delDia.add(r);
            }
        }
        return delDia;
    }

    private boolean esMismoDia(Date f1, Date f2) {
        if (f1 == null || f2 == null) return false;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(f1).equals(sdf.format(f2));
    }

    public Reserva buscarReservaEnCelda(List<Reserva> reservasDelDia, Recurso recurso, int hora) {
        for (Reserva r : reservasDelDia) {
            for (DetalleReserva d : r.getDetalles()) {
                if (d.getRecursoAsignado() != null
                        && d.getRecursoAsignado().getId().equals(recurso.getId())
                        && ocupaHora(r, hora)) {
                    return r;
                }
            }
        }
        return null;
    }

    private boolean ocupaHora(Reserva r, int hora) {
        java.util.Calendar calInicio = java.util.Calendar.getInstance();
        calInicio.setTime(r.getHoraInicio());
        java.util.Calendar calFin = java.util.Calendar.getInstance();
        calFin.setTime(r.getHoraFin());

        int horaInicio = calInicio.get(java.util.Calendar.HOUR_OF_DAY);
        int horaFin = calFin.get(java.util.Calendar.HOUR_OF_DAY);

        return hora >= horaInicio && hora < horaFin;
    }
}