package Inferencia;
import java.io.*;
import java.util.*;

/**
 * Formato estructura.txt:
 *   Padre -> Hijo
 *   (una relación por línea; los nodos raíz sin padres se declaran solos)
 *   Rain
 *   Maintenance
 *   Rain -> Train
 *
 * Formato tablas.txt:
 *   [NombreNodo]
 *   states: estado1 estado2 ...
 *   parents: padre1 padre2 ...   <- omitir si es raíz
 *   vPadre1,vPadre2,vNodo = prob
 *   vPadre1,vPadre2,vNodo = prob
 *   ...
 *   (línea en blanco entre bloques)
 */
public class LectorArchivos {

    public static Redbayesiana cargarEstructura(String ruta) throws IOException {
        Redbayesiana red = new Redbayesiana();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (linea.contains("->")) {
                    String[] partes = linea.split("->");
                    String padre = partes[0].trim();
                    String hijo = partes[1].trim();
                    red.agregarArista(padre, hijo);
                } else {
                    red.agregarNodo(linea);
                }
            }
        }
        return red;
    }

    public static void cargarTablas(String ruta, Redbayesiana red) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            Nodo nodoActual = null;
            List<String> padresActuales = new ArrayList<>();
            TablaProbabilidad tablaActual = null;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                if (linea.startsWith("[") && linea.endsWith("]")) {
                    // Nuevo bloque de nodo
                    String nombre = linea.substring(1, linea.length() - 1).trim();
                    nodoActual = red.getNodo(nombre);
                    if (nodoActual == null)
                        throw new RuntimeException("Nodo '" + nombre + "' no encontrado en la red.");
                    padresActuales = new ArrayList<>();
                    tablaActual = null;

                } else if (linea.startsWith("states:")) {
                    String[] estados = linea.substring(7).trim().split("\\s+");
                    for (String e : estados) nodoActual.agregarEstado(e);

                } else if (linea.startsWith("parents:")) {
                    String[] padres = linea.substring(8).trim().split("\\s+");
                    padresActuales = Arrays.asList(padres);
                    tablaActual = new TablaProbabilidad(nodoActual.getNombre(), padresActuales);
                    nodoActual.setTabla(tablaActual);

                } else if (linea.contains("=")) {
                    // Si aún no se creó la tabla (nodo raíz sin parents:)
                    if (tablaActual == null) {
                        tablaActual = new TablaProbabilidad(nodoActual.getNombre(), new ArrayList<>());
                        nodoActual.setTabla(tablaActual);
                    }
                    // Formato: clave = probabilidad
                    // clave puede ser "vNodo" o "vPadre1,vPadre2,vNodo"
                    int idx = linea.lastIndexOf('=');
                    String clave = linea.substring(0, idx).trim();
                    double prob = Double.parseDouble(linea.substring(idx + 1).trim());
                    tablaActual.agregarEntrada(clave, prob);
                }
            }
        }
    }
}