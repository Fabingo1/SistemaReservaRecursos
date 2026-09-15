package controladores;

import modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import servicios.GestorXML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración (Failsafe) del flujo completo de reservas:
 * controlador + GestorXML + archivos XML reales en una carpeta temporal.
 */
class ReservaControllerIT {

    @TempDir
    Path carpeta;

    private ReservaController controller;
    private Funcionario funcionario;
    private Categoria catSala;
    private Categoria catProyector;

    @BeforeEach
    void preparar() throws IOException {
        GestorXML gestor = new GestorXML();

        funcionario = new Funcionario();
        funcionario.setId("func1");
        funcionario.setClave("func1");
        funcionario.setNombre("Juan Perez");
        funcionario.setTelefono("8888-8888");

        catSala = categoria("CAT-001", "Sala");
        catProyector = categoria("CAT-002", "Proyector");
        gestor.guardarDatos(new ArrayList<>(List.of(catSala, catProyector)), ruta("categorias.xml"));

        // Dos salas y un solo proyector
        List<Recurso> recursos = new ArrayList<>();
        recursos.add(recurso("S1", catSala, "Sala 1"));
        recursos.add(recurso("S2", catSala, "Sala 2"));
        recursos.add(recurso("P1", catProyector, "Proyector 1"));
        gestor.guardarDatos(recursos, ruta("recursos.xml"));

        controller = new ReservaController(carpeta.toString());
    }

    @Test
    void crearReserva_conDisponibilidad_asignaPrimerRecursoYPersiste() throws IOException {
        ResultadoReserva r = controller.crearReserva(funcionario, "Reunion", manana(0, 0),
                manana(9, 0), manana(10, 0), List.of(catSala, catProyector));

        assertTrue(r.isExitosa());
        assertEquals("S1", r.getReserva().getDetalles().get(0).getRecursoAsignado().getId());
        assertEquals("P1", r.getReserva().getDetalles().get(1).getRecursoAsignado().getId());

        List<Reserva> guardadas = controller.obtenerReservasDe(funcionario);
        assertEquals(1, guardadas.size());
        assertNull(guardadas.get(0).getFuncionario().getClave(), "no debe guardarse la clave en reservas.xml");
    }

    @Test
    void crearReserva_segundaReservaMismoHorario_tomaLaSiguienteSala() throws IOException {
        controller.crearReserva(funcionario, "A", manana(0, 0), manana(9, 0), manana(10, 0), List.of(catSala));
        ResultadoReserva r = controller.crearReserva(funcionario, "B", manana(0, 0),
                manana(9, 30), manana(10, 30), List.of(catSala));

        assertTrue(r.isExitosa());
        assertEquals("S2", r.getReserva().getDetalles().get(0).getRecursoAsignado().getId());
    }

    @Test
    void crearReserva_sinDisponibilidad_indicaCategoriasYNoGuarda() throws IOException {
        controller.crearReserva(funcionario, "A", manana(0, 0), manana(9, 0), manana(11, 0), List.of(catProyector));
        ResultadoReserva r = controller.crearReserva(funcionario, "B", manana(0, 0),
                manana(10, 0), manana(12, 0), List.of(catSala, catProyector));

        assertFalse(r.isExitosa());
        assertEquals(1, r.getCategoriasNoDisponibles().size());
        assertEquals("CAT-002", r.getCategoriasNoDisponibles().get(0).getId());
        assertEquals(1, controller.obtenerReservasDe(funcionario).size(), "la reserva fallida no se guarda");
    }

    @Test
    void cancelarReserva_liberaLosRecursos() throws IOException {
        ResultadoReserva primera = controller.crearReserva(funcionario, "A", manana(0, 0),
                manana(9, 0), manana(11, 0), List.of(catProyector));
        controller.cancelarReserva(primera.getReserva().getId(), funcionario);

        ResultadoReserva segunda = controller.crearReserva(funcionario, "B", manana(0, 0),
                manana(9, 0), manana(11, 0), List.of(catProyector));
        assertTrue(segunda.isExitosa());
        assertEquals("P1", segunda.getReserva().getDetalles().get(0).getRecursoAsignado().getId());
    }

    @Test
    void cancelarReserva_dosVeces_lanzaExcepcion() throws IOException {
        ResultadoReserva r = controller.crearReserva(funcionario, "A", manana(0, 0),
                manana(9, 0), manana(10, 0), List.of(catSala));
        controller.cancelarReserva(r.getReserva().getId(), funcionario);

        assertThrows(IllegalStateException.class,
                () -> controller.cancelarReserva(r.getReserva().getId(), funcionario));
    }

    @Test
    void cancelarReserva_deOtroFuncionario_lanzaExcepcion() throws IOException {
        ResultadoReserva r = controller.crearReserva(funcionario, "A", manana(0, 0),
                manana(9, 0), manana(10, 0), List.of(catSala));
        Funcionario otro = new Funcionario();
        otro.setId("func2");

        assertThrows(IllegalArgumentException.class,
                () -> controller.cancelarReserva(r.getReserva().getId(), otro));
    }

    @Test
    void cancelarReserva_pasada_lanzaExcepcion() throws IOException {
        // Se escribe directamente en el XML una reserva de hace una semana
        Reserva vieja = new Reserva();
        vieja.setId("RES-000099");
        vieja.setActividad("Vieja");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        vieja.setFecha(cal.getTime());
        vieja.setHoraInicio(cal.getTime());
        vieja.setHoraFin(cal.getTime());
        vieja.setFuncionario(funcionario);
        new GestorXML().guardarDatos(new ArrayList<>(List.of(vieja)), ruta("reservas.xml"));

        assertThrows(IllegalStateException.class, () -> controller.cancelarReserva("RES-000099", funcionario));
    }

    @Test
    void generarReportePDF_creaElArchivo() throws IOException {
        controller.crearReserva(funcionario, "Reunion", manana(0, 0), manana(9, 0), manana(10, 0), List.of(catSala));
        String salida = ruta("reservas.pdf");

        controller.generarReportePDF(controller.obtenerReservasDe(funcionario), "Reservas", salida);

        File pdf = new File(salida);
        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 0);
    }

    // ---------------- utilidades ----------------

    private String ruta(String archivo) {
        return carpeta.resolve(archivo).toString();
    }

    private static Date manana(int hora, int minuto) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, minuto);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Categoria categoria(String id, String descripcion) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setDescripcion(descripcion);
        return c;
    }

    private static Recurso recurso(String id, Categoria categoria, String descripcion) {
        Recurso r = new Recurso();
        r.setId(id);
        r.setCategoria(categoria);
        r.setDescripcion(descripcion);
        return r;
    }
}
