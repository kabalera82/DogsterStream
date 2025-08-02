package kabadev;

import java.io.IOException;
import java.net.InetSocketAddress;          // Dirección de socket de Internet (IP + Puerto)
import java.util.concurrent.Executors;         // Utilidad para crear pools de hilos

import com.sun.net.httpserver.HttpServer;   // Servidor HTTP básico de Java

import kabadev.controladores.ControladorArchivosEstaticos;   // Controlador para archivos estáticos
import kabadev.controladores.ControladorRaiz;               // Controlador para página principal  
import kabadev.controladores.ControladorStream;             // Controlador para streaming MP4
import kabadev.controladores.ControladorVideo;              // Controlador para API JSON

/**
 * Clase principal de la aplicación Dogster
 * Configura e inicia el servidor HTTP para servir películas
 */
public class App {
    
    /**
     * Método principal que ejecuta el servidor
     * @param args Argumentos de línea de comandos (no utilizados)
     * @throws IOException Si hay error al crear o iniciar el servidor
     */
    public static void main(String[] args) throws IOException {
        
        // Inicializamos un Servidor de tipo HttpServer inicializamos un Inetsocket en el puerto 8080
        // Inet = Internet; Socket = Punto de conexión; Address = Dirección
        // con un Backlog de 0 (el sistema operativo Controla las conexiones entrantes)
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        // ========== CONFIGURACIÓN DE ENDPOINTS (RUTAS) =====================================================================================
        // El servidor crea los contextos "Endpoints" para Controlar las peticiones HTTP
        // Cada createContext asocia una URL con un controlador específico
  
        servidor.createContext("/", new ControladorRaiz());
        servidor.createContext("/static/", new ControladorArchivosEstaticos());
        servidor.createContext("/video", new ControladorVideo());
        servidor.createContext("/stream", new ControladorStream());          


        // ========== CONFIGURACIÓN DE HILOS =================================================================================================
        // Configura el servidor para usar un pool de hilos en caché según necesidad
        // newCachedThreadPool() crea hilos dinámicamente cuando hay demanda
        // y los elimina cuando no se usan (después de 60 segundos)
        servidor.setExecutor(Executors.newFixedThreadPool(10)); // <---- Aquí se configura el executor de hilos
        
        // ========== INICIO DEL SERVIDOR ====================================================================================================
        // Inicia el servidor HTTP en el puerto 8080
        // A partir de este momento el servidor acepta conexiones entrantes
        servidor.start();
        
        // Mensaje informativo en consola indicando que el servidor está funcionando =========================================================
        System.out.println("Servidor Dogster iniciado en http://localhost:8080/");
        System.out.println("Sirviendo archivos estáticos desde /static/");
        System.out.println("API de videos disponible en /video");
        System.out.println("Streaming de videos en /stream");
        System.out.println("Usando pool de hilos dinámico para máximo rendimiento");
        System.out.println("Presiona Ctrl+C para detener el servidor");
    }
}