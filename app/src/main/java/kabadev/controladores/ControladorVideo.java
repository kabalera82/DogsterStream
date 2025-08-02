package kabadev.controladores;

// Importaciones de las clases nativas de Java para manejo de entrada/salida
import java.io.IOException;          // Excepción para errores de entrada/salida
import java.io.InputStream;          // Flujo de entrada de datos
import java.io.OutputStream;         // Flujo de salida de datos

import com.sun.net.httpserver.HttpExchange; // Objeto que contiene petición y respuesta HTTP
import com.sun.net.httpserver.HttpHandler;  // Interfaz para manejar peticiones HTTP

/**
 * Controlador de Videos
 * Maneja las solicitudes para obtener la lista de videos disponibles
 * Lee directamente el archivo asterix.json y lo devuelve al cliente
 * 
 * Simple y directo: sirve el contenido JSON preconfigurado
 * El frontend procesará estos datos para mostrar la interfaz visual
 */
public class ControladorVideo implements HttpHandler {

    /**
     * Maneja las peticiones HTTP para obtener la lista de videos
     * @param intercambio Objeto que contiene la petición del cliente y permite enviar respuesta
     * @throws IOException Si hay error al leer el archivo JSON o enviar respuesta
     */
    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        
        // ========== VALIDACIÓN DEL MÉTODO HTTP ==========
        // Verifica que la petición sea GET (solo permitimos consultas)
        if (!"GET".equals(intercambio.getRequestMethod())) {
            intercambio.sendResponseHeaders(405, -1);
            return;
        }
        
        try {
            // ========== LECTURA DEL ARCHIVO JSON ==========
            // Lee el archivo asterix.json desde los recursos del proyecto
            InputStream archivoJson = getClass().getClassLoader().getResourceAsStream("asterix.json");
            
            if (archivoJson == null) {
                String error = "{\"error\":\"Archivo asterix.json no encontrado\"}";
                intercambio.sendResponseHeaders(404, error.getBytes().length);
                try (OutputStream salida = intercambio.getResponseBody()) {
                    salida.write(error.getBytes());
                }
                return;
            }
            
            // ========== LECTURA Y ENVÍO DEL CONTENIDO ==========
            byte[] contenidoJson = archivoJson.readAllBytes();
            
            // Configura la respuesta como JSON
            intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            intercambio.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            intercambio.sendResponseHeaders(200, contenidoJson.length);
            
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(contenidoJson);
            }
            
        } catch (Exception e) {
            // ========== MANEJO DE ERRORES ==========
            String error = "{\"error\":\"Error al leer lista de videos: " + e.getMessage() + "\"}";
            intercambio.sendResponseHeaders(500, error.getBytes().length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(error.getBytes());
            }
        }
    }
}