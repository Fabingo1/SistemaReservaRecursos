package Vistas;

/**
 * Vistas que saben recargar sus datos desde los XML. MainView llama a
 * refrescar() cada vez que el usuario cambia de pestaña, así por ejemplo una
 * categoría recién creada aparece de una vez en Recursos o Calendarización.
 */
public interface Refrescable {
    void refrescar();
}
