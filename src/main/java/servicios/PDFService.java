package servicios;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Servicio genérico para generar reportes en PDF a partir de un título, un
 * arreglo de columnas y una lista de filas. Es reutilizable por cualquier
 * funcionalidad del sistema (Reservas, Funcionarios, Categorías, Recursos,
 * Calendarización, Actividades, Estadísticas), tal como lo exige el
 * enunciado ("Todas las funcionalidades deben incluir la opción de generar
 * reporte en formato PDF").
 *
 * Uso típico desde una vista o controlador:
 *   PDFService pdf = new PDFService();
 *   pdf.generarReporte("Mis reservas", new String[]{"Actividad", "Fecha"}, filas, "data/reporte.pdf");
 */
public class PDFService {

    private static final float MARGEN = 40f;
    private static final float ALTO_FILA = 18f;
    private static final float TAMANO_TITULO = 16f;
    private static final float TAMANO_TEXTO = 10f;

    /**
     * Genera un PDF tabular con paginación automática cuando las filas no
     * caben en una sola página carta.
     *
     * @param titulo      título del reporte, mostrado en la parte superior
     * @param columnas    encabezados de columna
     * @param filas       filas de datos; cada Object[] debe tener el mismo
     *                    tamaño que columnas (se usa toString() de cada valor)
     * @param rutaSalida  ruta del archivo .pdf a generar (se crean las
     *                    carpetas necesarias si no existen)
     */
    public void generarReporte(String titulo, String[] columnas, List<Object[]> filas, String rutaSalida)
            throws IOException {

        File archivoSalida = new File(rutaSalida);
        File carpeta = archivoSalida.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }

        PDFont fuenteNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);

            float altoPagina = pagina.getMediaBox().getHeight();
            float anchoUtil = pagina.getMediaBox().getWidth() - 2 * MARGEN;
            float anchoColumna = columnas.length > 0 ? anchoUtil / columnas.length : anchoUtil;

            PDPageContentStream contenido = new PDPageContentStream(documento, pagina);
            float y = altoPagina - MARGEN;

            y = escribirEncabezado(contenido, titulo, fuenteNegrita, fuenteNormal, y);
            y = escribirFilaEncabezadoTabla(contenido, columnas, fuenteNegrita, anchoColumna, y);

            for (Object[] fila : filas) {
                if (y < MARGEN + ALTO_FILA) {
                    contenido.close();
                    pagina = new PDPage(PDRectangle.LETTER);
                    documento.addPage(pagina);
                    contenido = new PDPageContentStream(documento, pagina);
                    y = altoPagina - MARGEN;
                    y = escribirFilaEncabezadoTabla(contenido, columnas, fuenteNegrita, anchoColumna, y);
                }
                y = escribirFilaDatos(contenido, fila, fuenteNormal, anchoColumna, y);
            }

            contenido.close();
            documento.save(archivoSalida);
        }
    }

    private float escribirEncabezado(PDPageContentStream contenido, String titulo, PDFont fuenteNegrita,
                                      PDFont fuenteNormal, float y) throws IOException {
        contenido.beginText();
        contenido.setFont(fuenteNegrita, TAMANO_TITULO);
        contenido.newLineAtOffset(MARGEN, y);
        contenido.showText(limpiar(titulo));
        contenido.endText();

        float y2 = y - TAMANO_TITULO - 4;
        String fechaGeneracion = "Generado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        contenido.beginText();
        contenido.setFont(fuenteNormal, TAMANO_TEXTO - 1);
        contenido.newLineAtOffset(MARGEN, y2);
        contenido.showText(limpiar(fechaGeneracion));
        contenido.endText();

        return y2 - ALTO_FILA;
    }

    private float escribirFilaEncabezadoTabla(PDPageContentStream contenido, String[] columnas,
                                               PDFont fuenteNegrita, float anchoColumna, float y) throws IOException {
        float x = MARGEN;
        contenido.setFont(fuenteNegrita, TAMANO_TEXTO);
        for (String columna : columnas) {
            contenido.beginText();
            contenido.newLineAtOffset(x, y);
            contenido.showText(limpiar(columna));
            contenido.endText();
            x += anchoColumna;
        }

        float lineaY = y - 4;
        contenido.setLineWidth(0.5f);
        contenido.moveTo(MARGEN, lineaY);
        contenido.lineTo(MARGEN + anchoColumna * columnas.length, lineaY);
        contenido.stroke();

        return lineaY - ALTO_FILA;
    }

    private float escribirFilaDatos(PDPageContentStream contenido, Object[] fila, PDFont fuenteNormal,
                                     float anchoColumna, float y) throws IOException {
        float x = MARGEN;
        contenido.setFont(fuenteNormal, TAMANO_TEXTO);
        for (Object valor : fila) {
            String texto = recortar(limpiar(valor == null ? "" : valor.toString()), 45);
            contenido.beginText();
            contenido.newLineAtOffset(x, y);
            contenido.showText(texto);
            contenido.endText();
            x += anchoColumna;
        }
        return y - ALTO_FILA;
    }

    /**
     * Las fuentes estándar de PDFBox (Helvetica) pueden fallar con
     * IllegalArgumentException ante ciertos caracteres fuera de su
     * codificación. Para evitar romper la generación del reporte por una
     * tilde o una ñ, se normalizan antes de escribir.
     */
    private String limpiar(String texto) {
        if (texto == null) return "";
        return texto
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N");
    }

    private String recortar(String texto, int maxLength) {
        if (texto.length() <= maxLength) return texto;
        return texto.substring(0, maxLength - 3) + "...";
    }

    public static void main(String[] args) throws IOException {
        // Prueba manual rápida: genera data/reporte_prueba.pdf
        PDFService service = new PDFService();
        List<Object[]> filas = List.of(
                new Object[]{"Sala de Juntas", 5},
                new Object[]{"Laptop Windows 11", 3}
        );
        service.generarReporte("Reporte de prueba", new String[]{"Categoria", "Cantidad"}, filas,
                "data/reporte_prueba.pdf");
        System.out.println("Reporte de prueba generado en data/reporte_prueba.pdf");
    }
}
