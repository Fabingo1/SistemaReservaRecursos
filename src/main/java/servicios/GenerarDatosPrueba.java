package servicios;

import modelo.Administrador;
import modelo.Funcionario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerarDatosPrueba {

    public static void main(String[] args) throws IOException {
        GestorXML gestor = new GestorXML();

        // --- Administrador de prueba ---
        Administrador admin = new Administrador();
        admin.setId("admin1");
        admin.setClave("admin1"); // clave inicial = id, según regla del enunciado

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

        System.out.println("Datos de prueba generados en data/");
    }
}