package kabadev.servidor;

import java.io.IOException;                      // IOException NATIVO de Java

import com.sun.net.httpserver.HttpExchange;     // HttpExchange NATIVO de Java

/**
 * Interfaz para Controladores HTTP Personalizados
 * Usa HttpExchange NATIVO de Java en lugar de crear clases personalizadas
 * 
 * NOTA: Usa IOException y HttpExchange NATIVOS de Java
 * en lugar de crear ExcepcionIO y HttpIntercambio personalizados
 */
public interface IControladorHttp {

    /**
     * Gestiona la solicitud HTTP usando HttpExchange NATIVO
     * 
     * @param intercambio HttpExchange NATIVO de Java que contiene petición y respuesta
     * @throws IOException Excepción NATIVA de Java para errores de entrada/salida
     * 
     * @see com.sun.net.httpserver.HttpExchange Clase NATIVA de Java
     * @see java.io.IOException Excepción NATIVA de Java
     */
    public void controlar(HttpExchange intercambio) throws IOException;
}