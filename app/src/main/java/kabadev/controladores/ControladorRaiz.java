package kabadev.controladores;

// Importaciones necesarias para el manejo de archivos estáticos
import java.io.IOException;          // Excepción para errores de entrada/salida
import java.io.InputStream;          // Flujo de entrada para leer archivos
import java.io.OutputStream;         // Flujo de salida para enviar respuestas

import com.sun.net.httpserver.HttpExchange; // Objeto que contiene petición HTTP y permite enviar respuesta
import com.sun.net.httpserver.HttpHandler;  // Interfaz que deben implementar los manejadores de peticiones HTTP

/**
 * Controlador Raíz - Servidor de Archivos Estáticos
 * 
 * Este controlador maneja las peticiones HTTP dirigidas a la raíz del servidor ("/")
 * y sirve archivos estáticos como HTML, CSS, JavaScript e imágenes.
 * 
 * Funcionalidades principales:
 * - Sirve index.html cuando se accede a la raíz "/"
 * - Mapea automáticamente rutas a archivos en la carpeta static/
 * - Detecta y configura tipos MIME apropiados para cada tipo de archivo
 * - Maneja errores 404 cuando los archivos no existen
 * 
 * Ejemplos de uso:
 * - GET / → Devuelve static/index.html
 * - GET /style.css → Devuelve static/style.css
 * - GET /main.js → Devuelve static/main.js
 * - GET /logo.png → Devuelve static/logo.png
 */
public class ControladorRaiz implements HttpHandler {

    /**
     * Método principal que maneja todas las peticiones HTTP dirigidas a este controlador
     * 
     * Flujo de procesamiento:
     * 1. Extrae la ruta solicitada de la URI de la petición
     * 2. Mapea la ruta a un archivo en la carpeta static/
     * 3. Busca el archivo en los recursos del proyecto
     * 4. Si encuentra el archivo: determina tipo MIME y lo envía
     * 5. Si no encuentra el archivo: devuelve error 404
     * 
     * @param intercambio Objeto HttpExchange que contiene:
     *                   - La petición HTTP del cliente (URI, método, headers, etc.)
     *                   - Métodos para enviar la respuesta HTTP al cliente
     * @throws IOException Si ocurre un error durante:
     *                    - La lectura del archivo solicitado
     *                    - El envío de la respuesta HTTP
     *                    - Cualquier operación de entrada/salida
     */
    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        
        // ========== EXTRACCIÓN Y MAPEO DE LA RUTA SOLICITADA =====================================
        // Obtiene la ruta completa de la URI (ej: "/", "/style.css", "/images/logo.png")
        String path = intercambio.getRequestURI().getPath();
        
        // Mapea la ruta solicitada a un archivo en la carpeta static/
        // Si la ruta es "/" (raíz), sirve index.html por defecto
        // Para cualquier otra ruta, añade el prefijo "static" (ej: "/style.css" → "static/style.css")
        String recurso = path.equals("/") ? "static/index.html" : "static" + path;
        
        // ========== BÚSQUEDA DEL ARCHIVO EN LOS RECURSOS DEL PROYECTO ============================
        // Utiliza el ClassLoader para buscar el archivo en src/main/resources/
        // getResourceAsStream() devuelve un InputStream si encuentra el archivo, null si no existe
        InputStream archivo = getClass().getClassLoader().getResourceAsStream(recurso);
        
        // ========== MANEJO DE ARCHIVO NO ENCONTRADO (ERROR 404) ===============================
        if (archivo == null) {
            // Prepara mensaje de error informativo para el cliente
            String error = "Archivo no encontrado: " + recurso;
            
            // Envía headers de respuesta HTTP con:
            // - Código 404 (Not Found)
            // - Longitud del mensaje de error en bytes
            intercambio.sendResponseHeaders(404, error.getBytes().length);
            
            // Envía el cuerpo de la respuesta con el mensaje de error
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(error.getBytes());
            }
            // Termina el procesamiento aquí si el archivo no existe
            return;
        }
        
        // ========== CONFIGURACIÓN DEL TIPO MIME ==============================================
        // Determina el tipo MIME basándose en la extensión del archivo
        // El tipo MIME le dice al navegador cómo interpretar el contenido
        String tipoMime = obtenerTipoMime(recurso);
        
        // Añade el header Content-Type a la respuesta HTTP
        // Esto es crucial para que el navegador procese correctamente el archivo
        intercambio.getResponseHeaders().add("Content-Type", tipoMime);
        
        // ========== LECTURA Y ENVÍO DEL ARCHIVO ===============================================
        // Lee todo el contenido del archivo en un array de bytes
        // readAllBytes() carga el archivo completo en memoria
        byte[] contenido = archivo.readAllBytes();
        
        // Envía headers de respuesta HTTP con:
        // - Código 200 (OK - éxito)
        // - Longitud exacta del archivo en bytes
        intercambio.sendResponseHeaders(200, contenido.length);
        
        // Envía el contenido del archivo como cuerpo de la respuesta HTTP
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(contenido);
        }
        // El try-with-resources garantiza que el OutputStream se cierre automáticamente
    }
    
    /**
     * Determina el tipo MIME (Multipurpose Internet Mail Extensions) de un archivo
     * basándose en su extensión
     * 
     * El tipo MIME es fundamental para que el navegador web sepa cómo procesar
     * cada tipo de archivo. Sin el tipo MIME correcto, el navegador podría:
     * - No ejecutar archivos JavaScript
     * - No aplicar estilos CSS
     * - No mostrar imágenes correctamente
     * - Descargar archivos en lugar de mostrarlos
     * 
     * Tipos MIME soportados:
     * - HTML: text/html con charset UTF-8 para caracteres especiales
     * - CSS: text/css para hojas de estilo
     * - JavaScript: application/javascript para código ejecutable
     * - Imágenes PNG: image/png para gráficos con transparencia
     * - Imágenes JPEG: image/jpeg para fotografías comprimidas
     * - Iconos: image/x-icon para favicons del navegador
     * 
     * @param nombreArchivo Nombre del archivo con su extensión (ej: "style.css", "script.js")
     * @return String con el tipo MIME apropiado para el archivo
     *         Si la extensión no es reconocida, devuelve "application/octet-stream"
     *         (tipo genérico para archivos binarios)
     */
    private String obtenerTipoMime(String nombreArchivo) {
        // Convierte el nombre del archivo a minúsculas para comparación case-insensitive
        // Esto permite que funcione tanto con ".CSS" como con ".css"
        String extension = nombreArchivo.toLowerCase();
        
        // ========== TIPOS MIME PARA DOCUMENTOS WEB ========================================
        if (extension.endsWith(".html")) return "text/html; charset=UTF-8";
        if (extension.endsWith(".css"))  return "text/css";
        if (extension.endsWith(".js"))   return "application/javascript";
        
        // ========== TIPOS MIME PARA IMÁGENES ==============================================
        if (extension.endsWith(".png"))  return "image/png";
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) return "image/jpeg";
        if (extension.endsWith(".ico"))  return "image/x-icon";
        
        // ========== TIPO MIME POR DEFECTO ==============================================
        // Para extensiones no reconocidas, devuelve tipo genérico
        // Esto hace que el navegador trate el archivo como binario
        return "application/octet-stream";
    }
}