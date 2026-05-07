package inferencia;

import java.util.*;

/**
 * Tabla de Probabilidad Condicional (CPD).
 * La clave del mapa es una cadena compuesta:
 *   "ValorPadre1,ValorPadre2,...,ValorNodo"
 * Para nodos raíz (sin padres) la clave es solo "ValorNodo".
 */
public class TablaProbabilidad {
    private final String nombreNodo;
    private final List<String> nombresPadres;
    // Clave: "vPadre1,vPadre2,...,vNodo" -> probabilidad
    private final Map<String, Double> entradas;

    public TablaProbabilidad(String nombreNodo, List<String> nombresPadres) {
        this.nombreNodo = nombreNodo;
        this.nombresPadres = new ArrayList<>(nombresPadres);
        this.entradas = new LinkedHashMap<>();
    }

    /** Agrega una entrada. contextoKey es "vPadre1,vPadre2,...,vNodo" */
    public void agregarEntrada(String contextoKey, double prob) {
        entradas.put(contextoKey, prob);
    }

    /**
     * Obtiene P(nodo=estadoNodo | padres=valoresPadres).
     * @param evidenciaActual mapa completo de variable->valor en el contexto actual
     * @param estadoNodo      valor de este nodo
     */
    public double obtenerProb(Map<String, String> evidenciaActual, String estadoNodo) {
        StringBuilder sb = new StringBuilder();
        for (String padre : nombresPadres) {
            String val = evidenciaActual.get(padre);
            if (val == null) {
                throw new RuntimeException("Falta valor del padre '" + padre
                        + "' al consultar tabla de '" + nombreNodo + "'");
            }
            sb.append(val).append(",");
        }
        sb.append(estadoNodo);
        String clave = sb.toString();
        Double prob = entradas.get(clave);
        if (prob == null) {
            throw new RuntimeException("No se encontró entrada '" + clave
                    + "' en tabla de '" + nombreNodo + "'");
        }
        return prob;
    }

    public void imprimirTabla() {
        System.out.println("  Tabla CPD de: " + nombreNodo);
        if (!nombresPadres.isEmpty()) {
            System.out.println("  Padres: " + nombresPadres);
        } else {
            System.out.println("  (nodo raíz - probabilidad marginal)");
        }
        for (Map.Entry<String, Double> e : entradas.entrySet()) {
            System.out.printf("    P(%s | %s) = %.4f%n",
                    nombreNodo, e.getKey(), e.getValue());
        }
    }
}