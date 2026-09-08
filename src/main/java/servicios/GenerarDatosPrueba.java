package servicios;

import modelo.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenerarDatosPrueba {

    public static void main(String[] args) throws IOException, Exception {
        GestorXML gestor = new GestorXML();

        // --- Administrador de prueba ---
        Administrador admin = new Administrador();
        admin.setId("admin1");
        admin.setClave("admin1");
        List<Administrador> admins = new ArrayList<>();
        admins.add(admin);
        gestor.guardarDatos(admins, "data/administradores.xml");

        // --- Funcionario de prueba ---
        Funcionario func = new Funcionario();
        func.setId("func1");
        func.setClave("func1");
        func.setNombre("Juan Pérez");
        func.setTelefono("8888-8888");
        List<Funcionario> funcionarios = new ArrayList<>();
        funcionarios.add(func);
        gestor.guardarDatos(funcionarios, "data/funcionarios.xml");

        // --- Categorías de prueba ---
        Categoria catSala = new Categoria();
        catSala.setId("cat1");
        catSala.setDescripcion("Sala para 10 personas");

        Categoria catLaptop = new Categoria();
        catLaptop.setId("cat2");
        catLaptop.setDescripcion("Laptop windows 11");

        List<Categoria> categorias = new ArrayList<>();
        categorias.add(catSala);
        categorias.add(catLaptop);
        gestor.guardarDatos(categorias, "data/categorias.xml");

        // --- Recursos de prueba ---
        Recurso sala1 = new Recurso();
        sala1.setId("R1");
        sala1.setCategoria(catSala);
        sala1.setDescripcion("Sala 1 primer piso");

        Recurso sala2 = new Recurso();
        sala2.setId("R2");
        sala2.setCategoria(catSala);
        sala2.setDescripcion("Sala 2 segundo piso");

        Recurso laptop1 = new Recurso();
        laptop1.setId("R3");
        laptop1.setCategoria(catLaptop);
        laptop1.setDescripcion("Laptop #238715");

        List<Recurso> recursos = new ArrayList<>();
        recursos.add(sala1);
        recursos.add(sala2);
        recursos.add(laptop1);
        gestor.guardarDatos(recursos, "data/recursos.xml");

        // --- Reserva de prueba ---
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Reserva reserva = new Reserva();
        reserva.setId("res1");
        reserva.setActividad("Sesión de Junta Directiva");
        reserva.setFecha(sdf.parse("2026-09-05 00:00"));
        reserva.setHoraInicio(sdf.parse("2026-09-05 09:00"));
        reserva.setHoraFin(sdf.parse("2026-09-05 11:00"));
        reserva.setFuncionario(func);

        DetalleReserva detalle = new DetalleReserva();
        detalle.setCategoriaSolicitada(catSala);
        detalle.setRecursoAsignado(sala1);
        reserva.getDetalles().add(detalle);

        List<Reserva> reservas = new ArrayList<>();
        reservas.add(reserva);
        gestor.guardarDatos(reservas, "data/reservas.xml");

        System.out.println("Datos de prueba generados en data/");
    }
}