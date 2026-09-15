package controladores;

import modelo.Categoria;
import modelo.Recurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaControllerTest {

    private CategoriaController controller;
    private RecursoController recursoController;

    @BeforeEach
    void preparar(@TempDir Path carpeta) {
        String rutaCategorias = carpeta.resolve("categorias.xml").toString();
        controller = new CategoriaController(rutaCategorias);
        recursoController = new RecursoController(carpeta.resolve("recursos.xml").toString(), rutaCategorias);
    }

    @Test
    void crear_generaIdsConsecutivos() throws IOException {
        assertEquals("CAT-001", controller.crear("Sala").getId());
        assertEquals("CAT-002", controller.crear("Laptop").getId());
        assertEquals(2, controller.listar().size());
    }

    @Test
    void crear_descripcionVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> controller.crear("   "));
    }

    @Test
    void crear_descripcionRepetida_lanzaExcepcion() throws IOException {
        controller.crear("Sala");
        assertThrows(IllegalArgumentException.class, () -> controller.crear("sala"));
    }

    @Test
    void buscarPorDescripcion_parcialSinMayusculas() throws IOException {
        controller.crear("Sala para 10 personas");
        controller.crear("Laptop windows 11");
        assertEquals(1, controller.buscarPorDescripcion("SALA").size());
        assertEquals(2, controller.buscarPorDescripcion("").size());
    }

    @Test
    void modificar_actualizaTambienLaCopiaEnRecursos() throws IOException {
        Categoria c = controller.crear("Sala");
        recursoController.crear("S1", c, "Sala 1");

        controller.modificar(c.getId(), "Sala grande");

        assertEquals("Sala grande", controller.buscarPorId(c.getId()).getDescripcion());
        Recurso r = recursoController.buscarPorId("S1");
        assertEquals("Sala grande", r.getCategoria().getDescripcion());
    }

    @Test
    void borrar_conRecursosAsociados_lanzaExcepcion() throws IOException {
        Categoria c = controller.crear("Sala");
        recursoController.crear("S1", c, "Sala 1");

        assertThrows(IllegalArgumentException.class, () -> controller.borrar(c.getId()));
        assertNotNull(controller.buscarPorId(c.getId()));
    }

    @Test
    void borrar_sinRecursos_laElimina() throws IOException {
        Categoria c = controller.crear("Sala");
        controller.borrar(c.getId());
        assertNull(controller.buscarPorId(c.getId()));
    }
}
