package controladores;

import modelo.*;
import servicios.GestorXML;
import servicios.PDFService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Validaciones de negocio centralizadas aquí (no en la vista) para proteger a cualquier cliente: vista, IA, pruebas.
public class ReservaController {

    private static final String CARPETA_POR_DEFECTO = "data";

    private final String rutaCategorias;
    private final String rutaReservas;
    private final String rutaRecursos;

    private final GestorXML gestorXML;
    private final PDFService pdfService;

    public ReservaController() {
        this(CARPETA_POR_DEFECTO);
    }

    /** Permite usar otra carpeta de datos (pruebas con @TempDir). */
    public ReservaController(String carpetaDatos) {
        this.rutaCategorias = new File(carpetaDatos, "categorias.xml").getPath();
        this.rutaReservas = new File(carpetaDatos, "reservas.xml").getPath();
        this.rutaRecursos = new File(carpetaDatos, "recursos.xml").getPath();
        this.gestorXML = new GestorXML();
        this.pdfService = new PDFService();
    }

    public List<Categoria> obtenerCategoriasDisponibles() throws IOException {
        return gestorXML.cargarDatos(rutaCategorias);
    }

    public List<Reserva> obtenerReservasDe(Funcionario funcionario) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(rutaReservas);
        List<Reserva> misReservas = new ArrayList<>();
        for (Reserva r : todas) {
            if (r.perteneceA(funcionario)) {
                misReservas.add(r);
            }
        }
        return misReservas;
    }

    /** Lanza IllegalArgumentException si los datos son inválidos; si falta disponibilidad, retorna un ResultadoReserva de fallo. */
    public ResultadoReserva crearReserva(Funcionario funcionario, String actividad, Date fecha,
                                         Date horaInicio, Date horaFin,
                                         List<Categoria> categoriasSolicitadas) throws IOException {
        validarDatos(funcionario, actividad, fecha, horaInicio, horaFin, categoriasSolicitadas);
        List<Categoria> categoriasUnicas = quitarRepetidas(categoriasSolicitadas);

        List<Reserva> todasLasReservas = gestorXML.cargarDatos(rutaReservas);
        List<Recurso> todosLosRecursos = gestorXML.cargarDatos(rutaRecursos);

        List<DetalleReserva> detalles = new ArrayList<>();
        List<Categoria> categoriasNoDisponibles = new ArrayList<>();

        for (Categoria categoria : categoriasUnicas) {
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
        nuevaReserva.setActividad(actividad.trim());
        nuevaReserva.setFecha(fecha);
        nuevaReserva.setHoraInicio(horaInicio);
        nuevaReserva.setHoraFin(horaFin);
        nuevaReserva.setFuncionario(copiaSinClave(funcionario));
        nuevaReserva.setDetalles(detalles);

        todasLasReservas.add(nuevaReserva);
        gestorXML.guardarDatos(todasLasReservas, rutaReservas);

        return ResultadoReserva.exito(nuevaReserva);
    }

    public void cancelarReserva(String idReserva, Funcionario funcionario) throws IOException {
        List<Reserva> todas = gestorXML.cargarDatos(rutaReservas);
        Reserva reserva = buscarPorId(todas, idReserva);

        if (reserva == null) {
            throw new IllegalArgumentException("No existe una reserva con id " + idReserva);
        }
        if (funcionario != null && !reserva.perteneceA(funcionario)) {
            throw new IllegalArgumentException("Solo puede cancelar sus propias reservas.");
        }
        if (!reserva.estaActiva()) {
            throw new IllegalStateException("La reserva " + idReserva + " ya está cancelada.");
        }
        if (reserva.yaPaso()) {
            throw new IllegalStateException("No se puede cancelar una reserva que ya pasó.");
        }

        reserva.cancelar();
        gestorXML.guardarDatos(todas, rutaReservas);
    }

    public void cancelarReserva(String idReserva) throws IOException {
        cancelarReserva(idReserva, null);
    }

    public void generarReportePDF(List<Reserva> reservas, String titulo, String rutaSalida) throws IOException {
        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
        List<Object[]> filas = new ArrayList<>();
        for (Reserva r : reservas) {
            filas.add(new Object[]{
                    r.getId(),
                    r.getActividad(),
                    r.getFecha() == null ? "" : sdfFecha.format(r.getFecha()),
                    sdfHora.format(r.getHoraInicio()) + "-" + sdfHora.format(r.getHoraFin()),
                    textoRecursos(r),
                    r.getEstado()
            });
        }
        pdfService.generarReporte(titulo,
                new String[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"},
                filas, rutaSalida);
    }

    public String textoRecursos(Reserva r) {
        StringBuilder sb = new StringBuilder();
        for (DetalleReserva d : r.getDetalles()) {
            if (d.getRecursoAsignado() == null) continue;
            if (sb.length() > 0) sb.append(", ");
            String desc = d.getRecursoAsignado().getDescripcion();
            sb.append(desc != null ? desc : d.getRecursoAsignado().getId());
        }
        return sb.toString();
    }

    // Package-private para que las pruebas unitarias accedan directo.
    Recurso buscarRecursoDisponible(Categoria categoria, List<Recurso> todosLosRecursos,
                                    List<Reserva> todasLasReservas, Date fecha, Date horaInicio, Date horaFin) {
        for (Recurso recurso : todosLosRecursos) {
            if (recurso.getCategoria() != null
                    && recurso.getCategoria().getId().equals(categoria.getId())
                    && estaLibre(recurso, todasLasReservas, fecha, horaInicio, horaFin)) {
                return recurso; // primer recurso disponible de la categoría
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
        return Reserva.mismoDia(f1, f2);
    }

    boolean seSuperponen(Date inicio1, Date fin1, Date inicio2, Date fin2) {
        int i1 = Reserva.minutosDelDia(inicio1);
        int f1 = Reserva.minutosDelDia(fin1);
        int i2 = Reserva.minutosDelDia(inicio2);
        int f2 = Reserva.minutosDelDia(fin2);
        return i1 < f2 && i2 < f1;
    }

    private void validarDatos(Funcionario funcionario, String actividad, Date fecha,
                              Date horaInicio, Date horaFin, List<Categoria> categorias) {
        if (funcionario == null || funcionario.getId() == null) {
            throw new IllegalArgumentException("Debe haber un funcionario autenticado para reservar.");
        }
        if (actividad == null || actividad.isBlank()) {
            throw new IllegalArgumentException("La descripción de la actividad no puede estar vacía.");
        }
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Debe indicar la fecha, la hora de inicio y la hora de fin.");
        }
        if (Reserva.minutosDelDia(horaFin) <= Reserva.minutosDelDia(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (Reserva.combinar(fecha, horaInicio).before(new Date())) {
            throw new IllegalArgumentException("No se puede reservar en una fecha u hora que ya pasó.");
        }
        if (categorias == null || categorias.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría de recurso.");
        }
    }

    /** Si la misma categoría viene dos veces, se toma una sola vez. */
    private List<Categoria> quitarRepetidas(List<Categoria> categorias) {
        Set<String> vistos = new HashSet<>();
        List<Categoria> unicas = new ArrayList<>();
        for (Categoria c : categorias) {
            if (c != null && c.getId() != null && vistos.add(c.getId())) {
                unicas.add(c);
            }
        }
        if (unicas.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría de recurso válida.");
        }
        return unicas;
    }

    /** Evita guardar la clave del funcionario dentro de reservas.xml. */
    private Funcionario copiaSinClave(Funcionario f) {
        Funcionario copia = new Funcionario();
        copia.setId(f.getId());
        copia.setNombre(f.getNombre());
        copia.setTelefono(f.getTelefono());
        return copia;
    }

    private Reserva buscarPorId(List<Reserva> reservas, String id) {
        for (Reserva r : reservas) {
            if (r.getId() != null && r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    private String generarId(List<Reserva> existentes) {
        int max = 0;
        for (Reserva r : existentes) {
            if (r.getId() == null) continue;
            try {
                int numero = Integer.parseInt(r.getId().replace("RES-", ""));
                max = Math.max(max, numero);
            } catch (NumberFormatException ignored) {
                // ids con otro formato (ej. "res1" de datos de prueba) no cuentan
            }
        }
        return String.format("RES-%06d", max + 1);
    }
}
