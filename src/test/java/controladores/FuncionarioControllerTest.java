package controladores;

import modelo.Administrador;
import modelo.Funcionario;
import servicios.GestorXML;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuncionarioControllerTest {

    private FuncionarioController controller;
    private String rutaAdministradores;

    @BeforeEach
    void configurar(@TempDir Path tempDir) {
        String rutaFuncionarios = tempDir.resolve("funcionarios.xml").toString();
        rutaAdministradores = tempDir.resolve("administradores.xml").toString();
        controller = new FuncionarioController(rutaFuncionarios, rutaAdministradores);
    }

    private void precargarAdministrador(String id) throws IOException {
        Administrador admin = new Administrador();
        admin.setId(id);
        admin.setClave("claveAdmin");
        new GestorXML().guardarDatos(new ArrayList<>(List.of(admin)), rutaAdministradores);
    }

    @Test
    void listar_sinArchivoPrevio_retornaListaVacia() throws IOException {
        assertTrue(controller.listar().isEmpty());
    }

    @Test
    void crear_conDatosValidos_persisteConClaveIgualAlId() throws IOException {
        Funcionario creado = controller.crear("111", "Juan Perez", "8888-1111");

        assertEquals("111", creado.getId());
        assertEquals("111", creado.getClave());
        assertEquals("Juan Perez", creado.getNombre());
        assertEquals("8888-1111", creado.getTelefono());
        assertEquals(1, controller.listar().size());
    }

    @Test
    void crear_idDuplicadoEnFuncionarios_lanzaExcepcion() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");

        assertThrows(IllegalArgumentException.class,
                () -> controller.crear("111", "Maria Solis", "8888-2222"));
    }

    @Test
    void crear_idYaUsadoPorAdministrador_lanzaExcepcion() throws IOException {
        precargarAdministrador("admin1");

        assertThrows(IllegalArgumentException.class,
                () -> controller.crear("admin1", "Juan Perez", "8888-1111"));
    }

    @Test
    void crear_idVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> controller.crear("  ", "Juan Perez", "8888-1111"));
    }

    @Test
    void crear_nombreVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> controller.crear("111", "", "8888-1111"));
    }

    @Test
    void crear_telefonoVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> controller.crear("111", "Juan Perez", ""));
    }

    @Test
    void buscarPorId_existente_retornaElFuncionario() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");

        Funcionario encontrado = controller.buscarPorId("111");

        assertNotNull(encontrado);
        assertEquals("Juan Perez", encontrado.getNombre());
    }

    @Test
    void buscarPorId_inexistente_retornaNull() throws IOException {
        assertNull(controller.buscarPorId("999"));
    }

    @Test
    void buscar_porIdParcial_encuentraCoincidencias() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");
        controller.crear("222", "Maria Solis", "8888-2222");

        List<Funcionario> resultado = controller.buscar("11");

        assertEquals(1, resultado.size());
        assertEquals("111", resultado.get(0).getId());
    }

    @Test
    void buscar_porNombreParcialSinDistinguirMayusculas() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");
        controller.crear("222", "Maria Solis", "8888-2222");

        List<Funcionario> resultado = controller.buscar("perez");

        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
    }

    @Test
    void buscar_textoVacio_retornaTodos() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");
        controller.crear("222", "Maria Solis", "8888-2222");

        assertEquals(2, controller.buscar("").size());
    }

    @Test
    void modificar_actualizaNombreYTelefono_sinCambiarIdNiClave() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");

        controller.modificar("111", "Juan Perez Solano", "8888-9999");

        Funcionario actualizado = controller.buscarPorId("111");
        assertEquals("Juan Perez Solano", actualizado.getNombre());
        assertEquals("8888-9999", actualizado.getTelefono());
        assertEquals("111", actualizado.getId());
        assertEquals("111", actualizado.getClave()); // la clave no la toca este método
    }

    @Test
    void modificar_idInexistente_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.modificar("999", "Nombre", "Telefono"));
    }

    @Test
    void borrar_existente_loElimina() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");

        controller.borrar("111");

        assertTrue(controller.listar().isEmpty());
    }

    @Test
    void borrar_inexistente_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> controller.borrar("999"));
    }

    @Test
    void borrar_noAfectaOtrosFuncionarios() throws IOException {
        controller.crear("111", "Juan Perez", "8888-1111");
        controller.crear("222", "Maria Solis", "8888-2222");

        controller.borrar("111");

        List<Funcionario> restantes = controller.listar();
        assertEquals(1, restantes.size());
        assertEquals("222", restantes.get(0).getId());
    }
}