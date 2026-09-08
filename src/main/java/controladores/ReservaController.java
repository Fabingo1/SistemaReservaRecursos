package controladores;

import modelo.*;
import servicios.GestorXML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservaController {
    private static final String RUTA_RESERVAS = "data/reservas.xml";
    private static final String RUTA_RECURSOS = "data/recursos.xml";

    private final GestorXML gestorXML;

    public ReservaController() {
        this.gestorXML = new GestorXML();
    }


    public List<Reserva> obtenerReservasDe(Funcionario funcionario) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(RUTA_RESERVAS);
        List<Reserva> misReservas = new ArrayList<>();
        for (Reserva r : todas) {
            if (r.perteneceA(funcionario)) {
                misReservas.add(r);
            }
        }
        return misReservas;
    }

    public ResultadoReserva crearReserva(Funcionario funcionario, String actividad, Date fecha,
                                         Date horaInicio, Date horaFin,
                                         List<Categoria> categoriasSolicitadas) throws IOException {
        List<Reserva> todasLasReservas = gestorXML.cargarDatos(RUTA_RESERVAS);
        List<Recurso> todosLosRecursos = gestorXML.cargarDatos(RUTA_RECURSOS);

        List<DetalleReserva> detalles = new ArrayList<>();
        List<Categoria> categoriasNoDisponibles = new ArrayList<>();

        for (Categoria categoria : categoriasSolicitadas) {
            Recurso disponible = buscarRecursoDisponible(
                    categoria, todosLosRecursos, todasLasReservas, fecha, horaInicio, horaFin);

            if (disponible != null) {
                DetalleReserva detalle = new DetalleReserva();
                detalle.setCategoriaSolicitada(categoria);
                detalle.setRecursoAsignado(disponible);
                detalles.add(detalle);
            } else {
                categoriasNoDisponibles.add(categoria);
            }
        }

        if (!categoriasNoDisponibles.isEmpty()) {
            return ResultadoReserva.fallo(categoriasNoDisponibles);
        }

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setId(generarId(todasLasReservas));
        nuevaReserva.setActividad(actividad);
        nuevaReserva.setFecha(fecha);
        nuevaReserva.setHoraInicio(horaInicio);
        nuevaReserva.setHoraFin(horaFin);
        nuevaReserva.setFuncionario(funcionario);
        nuevaReserva.setDetalles(detalles);

        todasLasReservas.add(nuevaReserva);
        gestorXML.guardarDatos(todasLasReservas, RUTA_RESERVAS);

        return ResultadoReserva.exito(nuevaReserva);
    }

    public void cancelarReserva(String idReserva) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(RUTA_RESERVAS);
        Reserva reserva = buscarPorId(todas, idReserva);

        if (reserva == null) {
            throw new IllegalArgumentException("No existe una reserva con id " + idReserva);
        }
        if (esPasada(reserva)) {
            throw new IllegalStateException("No se puede cancelar una reserva que ya pasó.");
        }

        reserva.cancelar();
        gestorXML.guardarDatos(todas, RUTA_RESERVAS);
    }


    Recurso buscarRecursoDisponible(Categoria categoria, List<Recurso> todosLosRecursos,
                                    List<Reserva> todasLasReservas, Date fecha, Date horaInicio, Date horaFin) {
        for (Recurso recurso : todosLosRecursos) {
            if (recurso.getCategoria() != null
                    && recurso.getCategoria().getId().equals(categoria.getId())
                    && estaLibre(recurso, todasLasReservas, fecha, horaInicio, horaFin)) {
                return recurso;
            }
        }
        return null;
    }

    boolean estaLibre(Recurso recurso, List<Reserva> todasLasReservas,
                      Date fecha, Date horaInicio, Date horaFin) {
        for (Reserva reserva : todasLasReservas) {
            if (!reserva.estaActiva() || !mismaFecha(reserva.getFecha(), fecha)) {
                continue;
            }
            for (DetalleReserva detalle : reserva.getDetalles()) {
                boolean esElMismoRecurso = detalle.getRecursoAsignado() != null
                        && detalle.getRecursoAsignado().getId().equals(recurso.getId());
                if (esElMismoRecurso && seSuperponen(reserva.getHoraInicio(), reserva.getHoraFin(), horaInicio, horaFin)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean mismaFecha(Date f1, Date f2) {
        return f1.getYear() == f2.getYear()
                && f1.getMonth() == f2.getMonth()
                && f1.getDate() == f2.getDate();
    }

    boolean seSuperponen(Date inicio1, Date fin1, Date inicio2, Date fin2) {
        int i1 = minutosDelDia(inicio1);
        int f1 = minutosDelDia(fin1);
        int i2 = minutosDelDia(inicio2);
        int f2 = minutosDelDia(fin2);
        return i1 < f2 && i2 < f1;
    }

    //utilidades privadas
    private int minutosDelDia(Date hora) {
        return hora.getHours() * 60 + hora.getMinutes();
    }

    private boolean esPasada(Reserva reserva) {
        Date fecha = reserva.getFecha();
        Date horaFin = reserva.getHoraFin();
        Date momentoFin = new Date(fecha.getYear(), fecha.getMonth(), fecha.getDate(),
                horaFin.getHours(), horaFin.getMinutes());
        return momentoFin.before(new Date());
    }

    private Reserva buscarPorId(List<Reserva> reservas, String id) {
        for (Reserva r : reservas) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    private String generarId(List<Reserva> existentes) {
        int max = 0;
        for (Reserva r : existentes) {
            try {
                int numero = Integer.parseInt(r.getId().replace("RES-", ""));
                max = Math.max(max, numero);
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("RES-%06d", max + 1);
    }
}
