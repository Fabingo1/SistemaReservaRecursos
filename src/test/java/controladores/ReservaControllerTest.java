package controladores;

import modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaControllerTest {

    private ReservaController controller;
    private Categoria categoriaLaptop;
    private Recurso laptop1;
    private Recurso laptop2;

    @BeforeEach
    void setUp() {
        controller = new ReservaController();

        categoriaLaptop = new Categoria();
        categoriaLaptop.setId("CAT-000001");
        categoriaLaptop.setDescripcion("Laptop windows");

        laptop1 = new Recurso();
        laptop1.setId("238715");
        laptop1.setCategoria(categoriaLaptop);

        laptop2 = new Recurso();
        laptop2.setId("45238");
        laptop2.setCategoria(categoriaLaptop);
    }

    //seSuperponen

    @Test
    void seSuperponen_horariosQueSeCruzan_devuelveTrue() {
        Date inicio1 = new Date(2026 - 1900, 7, 5, 9, 0);
        Date fin1 = new Date(2026 - 1900, 7, 5, 11, 0);
        Date inicio2 = new Date(2026 - 1900, 7, 5, 10, 0);
        Date fin2 = new Date(2026 - 1900, 7, 5, 12, 0);

        assertTrue(controller.seSuperponen(inicio1, fin1, inicio2, fin2));
    }

    @Test
    void seSuperponen_horariosSeguidosSinHueco_devuelveFalse() {
        Date inicio1 = new Date(2026 - 1900, 7, 5, 9, 0);
        Date fin1 = new Date(2026 - 1900, 7, 5, 10, 0);
        Date inicio2 = new Date(2026 - 1900, 7, 5, 10, 0);
        Date fin2 = new Date(2026 - 1900, 7, 5, 11, 0);

        assertFalse(controller.seSuperponen(inicio1, fin1, inicio2, fin2));
    }

    @Test
    void seSuperponen_horariosSeparados_devuelveFalse() {
        Date inicio1 = new Date(2026 - 1900, 7, 5, 9, 0);
        Date fin1 = new Date(2026 - 1900, 7, 5, 10, 0);
        Date inicio2 = new Date(2026 - 1900, 7, 5, 14, 0);
        Date fin2 = new Date(2026 - 1900, 7, 5, 15, 0);

        assertFalse(controller.seSuperponen(inicio1, fin1, inicio2, fin2));
    }

    //buscarRecursoDisponible

    @Test
    void buscarRecursoDisponible_sinReservasPrevias_devuelveElPrimero() {
        List<Recurso> recursos = List.of(laptop1, laptop2);
        List<Reserva> reservas = new ArrayList<>();

        Date fecha = new Date(2026 - 1900, 7, 5);
        Date horaInicio = new Date(2026 - 1900, 7, 5, 9, 0);
        Date horaFin = new Date(2026 - 1900, 7, 5, 11, 0);

        Recurso resultado = controller.buscarRecursoDisponible(
                categoriaLaptop, recursos, reservas, fecha, horaInicio, horaFin);

        assertEquals(laptop1, resultado);
    }

    @Test
    void buscarRecursoDisponible_primeroOcupado_devuelveElSegundo() {
        List<Recurso> recursos = List.of(laptop1, laptop2);

        Reserva reservaExistente = new Reserva();
        reservaExistente.setFecha(new Date(2026 - 1900, 7, 5));
        reservaExistente.setHoraInicio(new Date(2026 - 1900, 7, 5, 9, 0));
        reservaExistente.setHoraFin(new Date(2026 - 1900, 7, 5, 11, 0));

        DetalleReserva detalle = new DetalleReserva();
        detalle.setCategoriaSolicitada(categoriaLaptop);
        detalle.setRecursoAsignado(laptop1);
        reservaExistente.getDetalles().add(detalle);

        List<Reserva> reservas = List.of(reservaExistente);

        Date horaInicio = new Date(2026 - 1900, 7, 5, 10, 0);
        Date horaFin = new Date(2026 - 1900, 7, 5, 12, 0);

        Recurso resultado = controller.buscarRecursoDisponible(
                categoriaLaptop, recursos, reservas, reservaExistente.getFecha(), horaInicio, horaFin);

        assertEquals(laptop2, resultado);
    }

    @Test
    void buscarRecursoDisponible_todosOcupados_devuelveNull() {
        List<Recurso> recursos = List.of(laptop1);

        Reserva reservaExistente = new Reserva();
        reservaExistente.setFecha(new Date(2026 - 1900, 7, 5));
        reservaExistente.setHoraInicio(new Date(2026 - 1900, 7, 5, 9, 0));
        reservaExistente.setHoraFin(new Date(2026 - 1900, 7, 5, 11, 0));

        DetalleReserva detalle = new DetalleReserva();
        detalle.setCategoriaSolicitada(categoriaLaptop);
        detalle.setRecursoAsignado(laptop1);
        reservaExistente.getDetalles().add(detalle);

        List<Reserva> reservas = List.of(reservaExistente);

        Date horaInicio = new Date(2026 - 1900, 7, 5, 10, 0);
        Date horaFin = new Date(2026 - 1900, 7, 5, 12, 0);

        Recurso resultado = controller.buscarRecursoDisponible(
                categoriaLaptop, recursos, reservas, reservaExistente.getFecha(), horaInicio, horaFin);

        assertNull(resultado);
    }
}
