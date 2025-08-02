package kabadev.controladores;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Controlador de Streaming MP4
 * Sirve videos MP4 SOLO bajo petición específica
 */
public class ControladorStream implements HttpHandler {

    @Override
    public void handle(HttpExchange intercambio) throws IOException {
        
        if (!"GET".equals(intercambio.getRequestMethod())) {
            intercambio.sendResponseHeaders(405, -1);
            return;
        }
        
        String query = intercambio.getRequestURI().getQuery();
        
        if (query == null || !query.startsWith("path=")) {
            String error = "Parámetro 'path' requerido";
            intercambio.sendResponseHeaders(400, error.getBytes().length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(error.getBytes());
            }
            return;
        }
        
        String rutaVideo = query.substring(5);
        
        try {
            rutaVideo = java.net.URLDecoder.decode(rutaVideo, "UTF-8");
            enviarMP4(intercambio, rutaVideo);
            
        } catch (Exception e) {
            String error = "Error al reproducir video: " + e.getMessage();
            intercambio.sendResponseHeaders(500, error.getBytes().length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(error.getBytes());
            }
        }
    }
    
    /**
     * Envía archivo MP4 por chunks
     */
    private void enviarMP4(HttpExchange intercambio, String rutaVideo) throws IOException {
        
        Path archivoVideo = Paths.get(rutaVideo);
        
        if (!Files.exists(archivoVideo)) {
            String error = "Video no encontrado: " + rutaVideo;
            intercambio.sendResponseHeaders(404, error.getBytes().length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(error.getBytes());
            }
            return;
        }
        
        long tamano = Files.size(archivoVideo);
        
        // Headers para MP4
        intercambio.getResponseHeaders().add("Content-Type", "video/mp4");
        intercambio.getResponseHeaders().add("Accept-Ranges", "bytes");
        intercambio.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        intercambio.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        intercambio.getResponseHeaders().add("Access-Control-Allow-Headers", "Range");
        
        intercambio.sendResponseHeaders(200, tamano);
        
        // Streaming por chunks
        try (InputStream input = Files.newInputStream(archivoVideo);
             OutputStream output = intercambio.getResponseBody()) {
            
            byte[] buffer = new byte[8192];
            int bytes;
            
            while ((bytes = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytes);
            }
        }
    }
}