package controladores;

import modelo.*;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarizacionControllerTest {

    private final CalendarizacionController controller = new CalendarizacionController();

    private static Date fecha(int hora, int minuto) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2026, Calendar.SEPTEMBER, 9, hora, minuto);
        return cal.getTime();
    }

    @Test
    void buscarReservaEnCelda_consideraMinutosYRecurso() {
        Recurso sala1 = new Recurso();
        sala1.setId("S1");
        Recurso sala2 = new Recurso();
        sala2.setId("S2");

        Reserva r = new Reserva();
        r.setActividad("Reunion");
        r.setFecha(fecha(0, 0));
        r.setHoraInicio(fecha(9, 30));
        r.setHoraFin(fecha(10, 30));
        DetalleReserva d = new DetalleReserva();
        d.setRecursoAsignado(sala1);
        r.getDetalles().add(d);

        List<Reserva> delDia = List.of(r);

        assertNull(controller.buscarReservaEnCelda(delDia, sala1, 8));
        assertSame(r, controller.buscarReservaEnCelda(delDia, sala1, 9));
        assertSame(r, controller.buscarReservaEnCelda(delDia, sala1, 10));
        assertNull(controller.buscarReservaEnCelda(delDia, sala1, 11));
        assertNull(controller.buscarReservaEnCelda(delDia, sala2, 9), "otro recurso está libre");
    }
}
