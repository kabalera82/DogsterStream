package kabadev.controladores;

// Importaciones de las clases nativas de Java para manejo de entrada/salida
import java.io.IOException;          // Excepción para errores de entrada/salida
import java.io.InputStream;          // Flujo de entrada de datos
import java.io.OutputStream;         // Flujo de salida de datos

import com.sun.net.httpserver.HttpExchange; // Objeto que contiene petición y respuesta HTTP
import com.sun.net.httpserver.HttpHandler;  // Interfaz para manejar peticiones HTTP

/**
 * Controlador de Archivos Estáticos
 * Maneja las solicitudes de archivos estáticos (CSS, JS, imágenes, etc.)
 * implementando la interfaz HttpHandler de Java HTTP Server
 * 
 * Este controlador sirve archivos desde la carpeta /static/ sin procesamiento
 * Los archivos se envían tal como están almacenados en el servidor
 */
public class ControladorArchivosEstaticos implements HttpHandler {

    /**
     * Maneja las peticiones HTTP para archivos estáticos
     * @param intercambio Objeto que contiene la petición del cliente y permite enviar respuesta
     * @throws IOException Si hay error al leer archivos o enviar respuesta
     */
    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        
        // ========== PROCESAMIENTO DE LA URL SOLICITADA ==========
        // Obtiene la URI completa solicitada por el cliente (ej: /static/style.css)
        String uri = intercambio.getRequestURI().getPath();
        
        // Extrae el nombre del archivo quitando el prefijo "/static/" de la URL
        String recurso = uri.replaceFirst("/static/", "");
        
        // ========== BÚSQUEDA DEL ARCHIVO EN RECURSOS ==========
        // Busca el archivo en la carpeta static/ dentro de los recursos del proyecto
        InputStream archivo = getClass().getClassLoader().getResourceAsStream("static/" + recurso);
        
        // ========== MANEJO DE ARCHIVO NO ENCONTRADO ==========
        if (archivo == null) {
            String mensajeError = "Archivo estático no encontrado: " + recurso;
            intercambio.sendResponseHeaders(404, mensajeError.getBytes().length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(mensajeError.getBytes());
            }
            return;
        }
        
        // ========== PROCESAMIENTO DEL ARCHIVO ENCONTRADO ==========
        byte[] contenido = archivo.readAllBytes();
        String tipoMime = obtenerTipoMime(recurso);
        
        // ========== ENVÍO DE LA RESPUESTA ==========
        intercambio.getResponseHeaders().add("Content-Type", tipoMime);
        intercambio.sendResponseHeaders(200, contenido.length);
        
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(contenido);
        }
    }
    
    /**
     * Determina el tipo MIME basándose en la extensión del archivo
     * El tipo MIME le dice al navegador cómo interpretar el contenido
     * 
     * @param nombreArchivo Nombre del archivo con extensión
     * @return String con el tipo MIME correspondiente
     */
    private String obtenerTipoMime(String nombreArchivo) {
        String archivo = nombreArchivo.toLowerCase();
        
        // ========== TIPOS MIME PARA ARCHIVOS WEB ==========
        if (archivo.endsWith(".css"))  return "text/css";
        if (archivo.endsWith(".js"))   return "application/javascript";
        if (archivo.endsWith(".html")) return "text/html; charset=UTF-8";
        
        // ========== TIPOS MIME PARA IMÁGENES ==========
        if (archivo.endsWith(".png"))  return "image/png";
        if (archivo.endsWith(".jpg") || archivo.endsWith(".jpeg")) return "image/jpeg";
        if (archivo.endsWith(".gif"))  return "image/gif";
        if (archivo.endsWith(".ico"))  return "image/x-icon";
        if (archivo.endsWith(".svg"))  return "image/svg+xml";
        
        // ========== TIPOS MIME PARA MULTIMEDIA ==========
        if (archivo.endsWith(".mp4"))  return "video/mp4";
        if (archivo.endsWith(".mp3"))  return "audio/mpeg";
        
        // Tipo MIME genérico para archivos no reconocidos
        return "application/octet-stream";
    }
}