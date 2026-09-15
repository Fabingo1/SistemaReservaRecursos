package controladores;

import modelo.Categoria;
import modelo.Funcionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas unitarias de las validaciones de datos al crear una reserva. */
class ReservaControllerValidacionTest {

    private ReservaController controller;
    private Funcionario funcionario;
    private Categoria categoria;

    @BeforeEach
    void preparar(@TempDir Path carpeta) {
        controller = new ReservaController(carpeta.toString());
        funcionario = new Funcionario();
        funcionario.setId("func1");
        categoria = new Categoria();
        categoria.setId("CAT-001");
        categoria.setDescripcion("Sala");
    }

    private static Date manana(int hora) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, 0);
        return cal.getTime();
    }

    @Test
    void actividadVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.crearReserva(funcionario, "  ", manana(0), manana(9), manana(10), List.of(categoria)));
    }

    @Test
    void horaFinAntesDeInicio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.crearReserva(funcionario, "X", manana(0), manana(10), manana(9), List.of(categoria)));
    }

    @Test
    void horaFinIgualAInicio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.crearReserva(funcionario, "X", manana(0), manana(10), manana(10), List.of(categoria)));
    }

    @Test
    void fechaPasada_lanzaExcepcion() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 15, 9, 0);
        Date inicio = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        Date fin = cal.getTime();
        assertThrows(IllegalArgumentException.class, () ->
                controller.crearReserva(funcionario, "X", inicio, inicio, fin, List.of(categoria)));
    }

    @Test
    void sinCategorias_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.crearReserva(funcionario, "X", manana(0), manana(9), manana(10), List.of()));
    }

    @Test
    void sinFuncionario_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.crearReserva(null, "X", manana(0), manana(9), manana(10), List.of(categoria)));
    }
}
