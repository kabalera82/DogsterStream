// ========== CONFIGURACIÓN INICIAL Y LOGGING ==========
// Mensaje de log para indicar que el script ha comenzado a cargar
console.log("Cargando listado de películas...");

// ========== CONFIGURACIÓN DEL SERVIDOR ==========
// IP del servidor donde está ejecutándose el backend Java
// NOTA: Esta IP hardcodeada puede causar problemas en dispositivos remotos
// TODO: Hacer esto dinámico para mejor compatibilidad de red
const IP_SERVIDOR = "192.168.1.135";

// ========== CONTROL DEL SPLASH SCREEN ==========
/**
 * Configuración del splash screen que se muestra al cargar la página
 * Se ejecuta cuando toda la página (incluyendo imágenes) ha terminado de cargar
 * 
 * Funcionalidad:
 * - Detecta clicks en el splash screen
 * - Oculta el splash cuando el usuario hace click
 * - Permite que el usuario omita manualmente la pantalla de bienvenida
 */
window.onload = function() {
    // Obtiene referencia al elemento splash del DOM
    const splash = document.getElementById('splash');
    
    // Añade listener para detectar clicks en cualquier parte del splash
    splash.addEventListener('click', function() {
        // Oculta el splash cambiando su display a 'none'
        // Esto permite que el usuario acceda al contenido principal
        splash.style.display = 'none';
    });
};

// ========== GENERADOR DE EFECTOS VISUALES (ESTRELLAS) ==========
/**
 * Genera un efecto visual de estrellas usando box-shadow CSS
 * Crea múltiples puntos de luz distribuidos aleatoriamente por la pantalla
 * 
 * @param {number} amount - Cantidad de estrellas a generar
 * @param {number} opacity - Opacidad de las estrellas (0.0 a 1.0)
 * @param {number} blur - Cantidad de desenfoque para el efecto glow
 * @returns {string} - String CSS con múltiples box-shadows separados por comas
 * 
 * Ejemplo de uso:
 * generateBoxShadow(100, 0.5, 2) → "10px 20px 2px rgba(255,255,255,0.5), 30px 40px 2px rgba(255,255,255,0.5), ..."
 */
function generateBoxShadow(amount, opacity = 0.3, blur = 0) {
  // Array para almacenar cada box-shadow individual
  const arr = [];
  
  // Genera la cantidad especificada de estrellas
  for (let i = 0; i < amount; i++) {
    // Posición X aleatoria dentro del ancho de la ventana
    const x = Math.random() * window.innerWidth;
    
    // Posición Y aleatoria (doble altura para efecto parallax)
    const y = Math.random() * window.innerHeight * 2;
    
    // Crea el string CSS para esta estrella específica
    // Formato: "Xpx Ypx blurpx rgba(255,255,255,opacity)"
    arr.push(`${x}px ${y}px ${blur}px rgba(255, 255, 255, ${opacity})`);
  }
  
  // Une todos los box-shadows con comas para crear un CSS válido
  return arr.join(', ');
}

// ========== INICIALIZACIÓN DEL EFECTO VISUAL ==========
/**
 * Se ejecuta cuando el DOM está completamente cargado
 * Crea el efecto de campo de estrellas en el fondo de la página
 * 
 * Capas de estrellas:
 * 1. starsSmall: 1000 estrellas pequeñas, muy tenues (opacidad 0.1)
 * 2. starsSmall2: 1000 estrellas pequeñas adicionales para densidad
 * 3. starsMedium: 200 estrellas medianas con más brillo (opacidad 0.3)
 * 4. starsBig: 100 estrellas grandes con efecto glow (blur 1px)
 */
document.addEventListener('DOMContentLoaded', () => {
  // ========== GENERACIÓN DE DIFERENTES CAPAS DE ESTRELLAS ==========
  
  // Capa 1: Estrellas de fondo muy sutiles
  const starsSmall = generateBoxShadow(1000, 0.1, 0);
  
  // Capa 2: Más estrellas de fondo para mayor densidad
  const starsSmall2 = generateBoxShadow(1000, 0.1, 0);
  
  // Capa 3: Estrellas medianas más visibles
  const starsMedium = generateBoxShadow(200, 0.3, 1);
  
  // Capa 4: Estrellas principales con efecto brillante
  const starsBig = generateBoxShadow(100, 0.4, 1);

  // ========== CREACIÓN E INSERCIÓN DE ELEMENTOS DOM ==========
  
  // Crea un contenedor para todas las capas de estrellas
  const container = document.createElement('div');
  
  // Genera el HTML con las 4 capas de estrellas
  // Cada div tiene un ID único y su propio conjunto de box-shadows
  container.innerHTML = `
    <div id="stars" style="box-shadow: ${starsSmall};"></div>
    <div id="starsB" style="box-shadow: ${starsSmall2};"></div>
    <div id="stars2" style="box-shadow: ${starsMedium};"></div>
    <div id="stars3" style="box-shadow: ${starsBig};"></div>
  `;

  // Añade el contenedor de estrellas al final del body
  document.body.appendChild(container);
});

// ========== CARGA Y PROCESAMIENTO DE DATOS DE PELÍCULAS ==========
/**
 * Petición HTTP al backend para obtener el listado de películas
 * Se ejecuta inmediatamente al cargar el script
 * 
 * Flujo de procesamiento:
 * 1. Realiza fetch al endpoint /video del servidor
 * 2. Verifica que la respuesta sea exitosa
 * 3. Convierte la respuesta a JSON
 * 4. Genera el HTML para mostrar cada película
 * 5. Configura la interactividad de cada elemento
 * 6. Maneja errores de red y de reproducción
 */
fetch(`/video`)
    .then(response => {
        // ========== VALIDACIÓN DE LA RESPUESTA HTTP ==========
        // Verifica que el servidor haya respondido correctamente
        if (!response.ok) {
            // Si hay error HTTP, lanza excepción con código específico
            throw new Error('No se pudo obtener el listado de películas. Código: ' + response.status);
        }
        
        // Convierte la respuesta a formato JSON
        return response.json();
    })
    .then(data => {
        // ========== PROCESAMIENTO DE LOS DATOS RECIBIDOS ==========
        
        // Obtiene el contenedor donde se mostrarán las películas
        const container = document.getElementById('movies');
        
        // Cambia el ID del contenedor para efectos CSS específicos
        container.id = 'videoclub';
        
        // ========== VALIDACIÓN DE DATOS ==========
        // Verifica que los datos recibidos sean válidos
        if (!Array.isArray(data) || data.length === 0) {
            // Si no hay películas, muestra mensaje informativo
            container.innerHTML = '<p style="color:red;">No hay películas disponibles.</p>';
            return;
        }
        
        // ========== GENERACIÓN DINÁMICA DE CONTENIDO ==========
        // Procesa cada película en el array recibido
        data.forEach(movie => {
            // Crea un div contenedor para cada película
            const div = document.createElement('div');
            
            // ========== GENERACIÓN DEL HTML DE LA PELÍCULA ==========
            // Crea la estructura HTML completa para mostrar la película
            div.innerHTML = `
                        <h2>${movie.title} (${movie.year})</h2>
                        <img src="${movie.poster}" alt="${movie.title}" width="320" style="cursor:pointer;"/>
                        <p>Año: ${movie.year}</p>
                        <p>Duración: ${movie.duration} minutos</p>
                        <video width="320" controls style="display:none;" playsinline webkit-playsinline>
                            <source src="/stream?path=${encodeURIComponent(movie.videoUrl)}" type="video/mp4">
                            Tu navegador no soporta este formato de video.
                        </video>
                    `;
            
            // Añade el div de la película al contenedor principal
            container.appendChild(div);
            
            // ========== CONFIGURACIÓN DE INTERACTIVIDAD ==========
            
            // Obtiene referencias a los elementos imagen y video
            const imagen = div.querySelector('img');
            const video = div.querySelector('video');
            
            // ========== EVENTO: CLICK EN IMAGEN (REPRODUCIR VIDEO) ==========
            // Cuando el usuario hace click en el póster de la película
            imagen.addEventListener('click', function () {
                // Oculta la imagen del póster
                imagen.style.display = 'none';
                
                // Muestra el reproductor de video
                video.style.display = 'block';
                
                // Inicia la reproducción automáticamente
                video.play();
            });
            
            // ========== EVENTO: MOUSE SALE DEL VIDEO (PAUSAR) ==========
            // Cuando el cursor sale del área del video
            video.addEventListener('mouseleave', function () {
                // Pausa la reproducción del video
                video.pause();
                
                // Oculta el reproductor de video
                video.style.display = 'none';
                
                // Vuelve a mostrar la imagen del póster
                imagen.style.display = 'block';
            });
            
            // ========== EVENTO: ERROR EN LA REPRODUCCIÓN ==========
            // Si hay algún problema cargando o reproduciendo el video
            video.addEventListener('error', function () {
                // Añade mensaje de error visible para el usuario
                div.innerHTML += '<p style="color:red;">Error al cargar el video.</p>';
            });
        });
    })
    // ========== MANEJO DE ERRORES GLOBALES ==========
    .catch(error => {
        // Si hay cualquier error en la petición o procesamiento
        const container = document.getElementById('movies');
        
        // Muestra mensaje de error específico al usuario
        container.innerHTML = `<p style="color:red;">Error al cargar el listado de películas: ${error.message}</p>`;
    });