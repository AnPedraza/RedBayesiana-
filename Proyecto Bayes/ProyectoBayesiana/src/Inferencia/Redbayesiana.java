package inferencia;

import java.util.*;

public class RedBayesiana {
    // Orden de inserción importa para el orden topológico
    private final Map<String, Nodo> nodos = new LinkedHashMap<>();

    public void agregarNodo(String nombre) {
        nodos.putIfAbsent(nombre, new Nodo(nombre));
    }

    public Nodo getNodo(String nombre) {
        return nodos.get(nombre);
    }

    public Collection<Nodo> getNodos() {
        return nodos.values();
    }

    public void agregarArista(String padreNombre, String hijoNombre) {
        agregarNodo(padreNombre);
        agregarNodo(hijoNombre);
        Nodo padre = nodos.get(padreNombre);
        Nodo hijo = nodos.get(hijoNombre);
        padre.agregarHijo(hijo);
        hijo.agregarPadre(padre);
    }

    /**
     * Retorna los nodos en orden topológico (Kahn's algorithm).
     * Necesario para que la enumeración procese padres antes que hijos.
     */
    public List<Nodo> getOrdenTopologico() {
        Map<Nodo, Integer> gradoEntrada = new LinkedHashMap<>();
        for (Nodo n : nodos.values()) gradoEntrada.put(n, n.getPadres().size());

        Queue<Nodo> cola = new LinkedList<>();
        for (Map.Entry<Nodo, Integer> e : gradoEntrada.entrySet())
            if (e.getValue() == 0) cola.add(e.getKey());

        List<Nodo> orden = new ArrayList<>();
        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            orden.add(actual);
            for (Nodo hijo : actual.getHijos()) {
                gradoEntrada.put(hijo, gradoEntrada.get(hijo) - 1);
                if (gradoEntrada.get(hijo) == 0) cola.add(hijo);
            }
        }
        if (orden.size() != nodos.size())
            throw new RuntimeException("¡La red tiene ciclos! No es un DAG válido.");
        return orden;
    }

    public void imprimirEstructura() {
        System.out.println("========================================");
        System.out.println("  ESTRUCTURA DE LA RED BAYESIANA");
        System.out.println("========================================");
        for (Nodo n : getOrdenTopologico()) {
            if (n.getPadres().isEmpty()) {
                System.out.println("  [RAÍZ] " + n.getNombre()
                        + "  estados: " + n.getEstados());
            } else {
                List<String> padresNombres = new ArrayList<>();
                for (Nodo p : n.getPadres()) padresNombres.add(p.getNombre());
                System.out.println("  " + n.getNombre()
                        + "  estados: " + n.getEstados()
                        + "  <- padres: " + padresNombres);
            }
        }
        System.out.println("========================================\n");
    }

    public void imprimirTablas() {
        System.out.println("========================================");
        System.out.println("  TABLAS DE PROBABILIDAD CONDICIONAL");
        System.out.println("========================================");
        for (Nodo n : getOrdenTopologico()) {
            n.getTabla().imprimirTabla();
            System.out.println();
        }
        System.out.println("========================================\n");
    }
}