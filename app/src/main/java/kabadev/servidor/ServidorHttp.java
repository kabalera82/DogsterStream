package kabadev.servidor;

// Importaciones de las clases NATIVAS de Java (no personalizadas)
import java.io.IOException;                      // Excepción NATIVA de Java para entrada/salida
import java.net.InetSocketAddress;               // Dirección socket NATIVA de Java
import java.util.HashMap;                        // HashMap NATIVO de Java
import java.util.Map;                           // Map NATIVO de Java
import java.util.concurrent.ExecutorService;          // Executors NATIVO de Java
import java.util.concurrent.Executors;    // ExecutorService NATIVO de Java

import com.sun.net.httpserver.HttpExchange;       // HttpServer NATIVO de Java
import com.sun.net.httpserver.HttpHandler;      // HttpHandler NATIVO de Java
import com.sun.net.httpserver.HttpServer;     // HttpExchange NATIVO de Java

/**
 * Servidor HTTP Personalizado Envoltorio (wrapper) del HttpServer nativo de
 * Java que proporciona una interfaz más simple para crear servidores web
 *
 * NOTA: Usa clases NATIVAS de Java (IOException, InetSocketAddress) en lugar de
 * crear clases personalizadas innecesarias
 */
public class ServidorHttp {

    // ========== ATRIBUTOS USANDO CLASES NATIVAS ==========
    private final HttpServer servidorNativo;                              // HttpServer NATIVO de Java
    private final Map<String, IControladorHttp> controladores;            // Map NATIVO con nuestros controladores
    private final ExecutorService poolHilos;                              // ExecutorService NATIVO de Java
    private boolean iniciado;                                       // boolean NATIVO de Java
    private final InetSocketAddress direccion;                            // InetSocketAddress NATIVO de Java

    /**
     * Constructor privado que usa InetSocketAddress NATIVO
     *
     * @param direccion InetSocketAddress nativo de Java (no clase
     * personalizada)
     * @throws IOException Excepción NATIVA de Java (no personalizada)
     */
    private ServidorHttp(InetSocketAddress direccion) throws IOException {
        // ========== USANDO CLASES NATIVAS DE JAVA ==========
        this.direccion = direccion;                                 // InetSocketAddress NATIVO
        this.servidorNativo = HttpServer.create(direccion, 0);      // HttpServer NATIVO
        this.controladores = new HashMap<>();                       // HashMap NATIVO
        this.poolHilos = Executors.newCachedThreadPool();          // Executors NATIVO
        this.iniciado = false;                                      // boolean NATIVO

        // Configura el pool de hilos NATIVO
        this.servidorNativo.setExecutor(this.poolHilos);
    }

    /**
     * Método factory usando InetSocketAddress NATIVO
     *
     * @param direccion InetSocketAddress NATIVO de Java (IP + Puerto)
     * @param backlog Número máximo de conexiones pendientes
     * @return Nueva instancia de ServidorHttp
     * @throws IOException Excepción NATIVA de Java
     */
    public static ServidorHttp crear(InetSocketAddress direccion, int backlog) throws IOException {
        // ========== VALIDACIONES USANDO MÉTODOS NATIVOS ==========
        if (direccion == null) {
            throw new IllegalArgumentException("La dirección no puede ser null");
        }

        if (direccion.getPort() < 1 || direccion.getPort() > 65535) {
            throw new IllegalArgumentException("Puerto inválido: " + direccion.getPort());
        }

        return new ServidorHttp(direccion);
    }

    /**
     * Registra un controlador para una ruta específica
     *
     * @param ruta URL/ruta que el controlador manejará
     * @param controlador Implementación de IControladorHttp
     * @throws IllegalArgumentException Si los parámetros son inválidos (NATIVO)
     * @throws IllegalStateException Si el servidor ya está iniciado (NATIVO)
     */
    // ========== MÉTODO crearContexto CORREGIDO ==========
    public void crearContexto(String ruta, IControladorHttp controlador) {
        // ========== VALIDACIONES CON EXCEPCIONES NATIVAS ==========
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta no puede ser null o vacía");
        }

        if (controlador == null) {
            throw new IllegalArgumentException("El controlador no puede ser null");
        }

        if (this.iniciado) {
            throw new IllegalStateException("No se pueden agregar contextos después de iniciar");
        }

        // ========== PROCESAMIENTO CON MÉTODOS NATIVOS ==========
        String rutaNormalizada = ruta.startsWith("/") ? ruta : "/" + ruta;
        this.controladores.put(rutaNormalizada, controlador);

        // ========== ADAPTADOR USANDO HttpHandler NATIVO ==========
        HttpHandler adaptador = (HttpExchange exchange) -> {
            try {
                controlador.controlar(exchange);
            } catch (IOException e) {
                throw e; // Re-lanza IOException directamente
            } catch (RuntimeException e) {
                throw new IOException("Error en tiempo de ejecución: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new IOException("Error inesperado: " + e.getMessage(), e);
            }
        };

        // ========== REGISTRA EN EL SERVIDOR NATIVO ==========
        this.servidorNativo.createContext(rutaNormalizada, adaptador);

        System.out.println("Contexto registrado: " + rutaNormalizada
                + " -> " + controlador.getClass().getSimpleName());
    } 

    /**
     * Inicia el servidor HTTP
     *
     * @throws IllegalStateException Si el servidor ya está iniciado (NATIVO)
     */
    public void iniciar() {
        if (this.iniciado) {
            throw new IllegalStateException("El servidor ya está iniciado");
        }

        if (this.controladores.isEmpty()) {
            System.out.println("Advertencia: Servidor iniciado sin controladores");
        }

        // ========== INICIO CON MÉTODOS NATIVOS ==========
        this.servidorNativo.start();
        this.iniciado = true;

        // ========== INFORMACIÓN USANDO MÉTODOS NATIVOS ==========
        System.out.println("Servidor HTTP personalizado iniciado");
        System.out.println("Dirección: http://" + direccion.getHostString()
                + ":" + direccion.getPort() + "/");
        System.out.println("Pool de hilos: CachedThreadPool");
        System.out.println("Contextos registrados: " + this.controladores.size());

        this.controladores.forEach((ruta, controlador)
                -> System.out.println("   ├── " + ruta + " (" + controlador.getClass().getSimpleName() + ")")
        );
    }

    /**
     * Detiene el servidor de manera elegante
     *
     * @param tiempoEspera Segundos a esperar antes del cierre forzado
     */
    public void detener(int tiempoEspera) {
        if (!this.iniciado) {
            System.out.println("El servidor ya está detenido");
            return;
        }

        System.out.println("Deteniendo servidor HTTP...");

        // ========== CIERRE CON MÉTODOS NATIVOS ==========
        this.servidorNativo.stop(tiempoEspera);          // HttpServer NATIVO
        this.poolHilos.shutdown();                       // ExecutorService NATIVO

        this.iniciado = false;
        System.out.println("Servidor HTTP detenido exitosamente");
    }

    /**
     * Métodos de información usando tipos NATIVOS
     */
    public boolean estaIniciado() {
        return this.iniciado;
    }

    public InetSocketAddress obtenerDireccion() {
        return this.servidorNativo.getAddress();
    }
}
