package Inferencia;
import java.util.*;


public class MotorInferencia {
    private final Redbayesiana red;
    private int nivelTraza = 0; // para indentación visual

    public MotorInferencia(Redbayesiana red) {
        this.red = red;
    }

    /**
     * Punto de entrada principal.
     * @param varConsulta nombre de la variable X
     * @param evidencia   mapa de variable->valor observado
     * @return mapa estado->probabilidad normalizado
     */
    public Map<String, Double> inferir(String varConsulta, Map<String, String> evidencia) {
        Nodo nodoConsulta = red.getNodo(varConsulta);
        if (nodoConsulta == null)
            throw new RuntimeException("Variable de consulta '" + varConsulta + "' no existe en la red.");

        List<Nodo> ordenTopologico = red.getOrdenTopologico();

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         MOTOR DE INFERENCIA POR ENUMERACIÓN          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("  Consulta : P(" + varConsulta + " | " + formatearEvidencia(evidencia) + ")");
        System.out.println("  Variables ocultas (Y): " + getVariablesOcultas(varConsulta, evidencia, ordenTopologico));
        System.out.println("  Orden topológico: " + ordenTopologico);
        System.out.println("──────────────────────────────────────────────────────\n");

        // Calcular P(X=xi, e) para cada estado xi de la variable consulta
        Map<String, Double> distribucion = new LinkedHashMap<>();
        for (String estado : nodoConsulta.getEstados()) {
            System.out.println("▶ Calculando P(" + varConsulta + "=" + estado
                    + " ∧ e) por enumeración...");

            // Extender evidencia con el estado de consulta actual
            Map<String, String> evidenciaExtendida = new LinkedHashMap<>(evidencia);
            evidenciaExtendida.put(varConsulta, estado);

            nivelTraza = 0;
            double valor = enumerarTodo(ordenTopologico, 0, evidenciaExtendida);
            distribucion.put(estado, valor);

            System.out.printf("%n  ➤ P(%s=%s ∧ e) = %.8f%n%n", varConsulta, estado, valor);
            System.out.println("──────────────────────────────────────────────────────");
        }

        // Normalización con factor α
        System.out.println("\n╔══════ NORMALIZACIÓN ══════╗");
        double suma = distribucion.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.printf("  Suma total (1/α) = %.8f%n", suma);
        double alpha = 1.0 / suma;
        System.out.printf("  α = %.8f%n%n", alpha);

        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : distribucion.entrySet()) {
            double probNorm = e.getValue() * alpha;
            resultado.put(e.getKey(), probNorm);
            System.out.printf("  P(%s=%s | e) = %.8f * %.8f = %.6f%n",
                    varConsulta, e.getKey(), e.getValue(), alpha, probNorm);
        }
        System.out.println("╚═══════════════════════════╝\n");

        return resultado;
    }

    /**
     * Enumeración recursiva sobre la lista de variables en orden topológico.
     *
     * @param vars      lista completa de variables en orden topológico
     * @param indice    índice actual en la lista
     * @param evidencia asignación actual (crece a medida que se extiende)
     * @return suma ponderada de probabilidades
     */
    private double enumerarTodo(List<Nodo> vars, int indice, Map<String, String> evidencia) {
        // Caso base: todas las variables han sido procesadas
        if (indice == vars.size()) {
            traza(nivelTraza, "→ [BASE] Producto de todas las P = retornando 1.0 (acumulado en llamadas superiores)");
            return 1.0;
        }

        Nodo actual = vars.get(indice);
        String nombreActual = actual.getNombre();
        String sangria = "  ".repeat(nivelTraza);

        if (evidencia.containsKey(nombreActual)) {
            // Variable con valor conocido (evidencia o variable de consulta asignada)
            String valor = evidencia.get(nombreActual);
            double p = actual.getTabla().obtenerProb(evidencia, valor);
            traza(nivelTraza, String.format(
                    "[OBS] P(%s=%s | padres) = %.6f  →  multiplicar y seguir", nombreActual, valor, p));

            nivelTraza++;
            double resto = enumerarTodo(vars, indice + 1, evidencia);
            nivelTraza--;

            double resultado = p * resto;
            traza(nivelTraza, String.format(
                    "← P(%s=%s)=%.6f  x  resto=%.6f  =  %.6f",
                    nombreActual, valor, p, resto, resultado));
            return resultado;

        } else {
            // Variable oculta: sumar sobre todos sus estados
            traza(nivelTraza, String.format("[SUMA] Sumando sobre estados de '%s': %s",
                    nombreActual, actual.getEstados()));
            double suma = 0.0;

            for (String estado : actual.getEstados()) {
                Map<String, String> evidenciaExtendida = new LinkedHashMap<>(evidencia);
                evidenciaExtendida.put(nombreActual, estado);

                double p = actual.getTabla().obtenerProb(evidenciaExtendida, estado);
                traza(nivelTraza, String.format(
                        "  ├─ P(%s=%s | padres) = %.6f", nombreActual, estado, p));

                nivelTraza++;
                double resto = enumerarTodo(vars, indice + 1, evidenciaExtendida);
                nivelTraza--;

                double termino = p * resto;
                traza(nivelTraza, String.format(
                        "  └─ término[%s=%s] = %.6f x %.6f = %.6f",
                        nombreActual, estado, p, resto, termino));
                suma += termino;
            }

            traza(nivelTraza, String.format("← Suma sobre '%s' = %.6f", nombreActual, suma));
            return suma;
        }
    }

    // ── Utilidades ────────────────────────────────────────

    private void traza(int nivel, String mensaje) {
        System.out.println("  ".repeat(nivel) + mensaje);
    }

    private String formatearEvidencia(Map<String, String> evidencia) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : evidencia.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    private List<String> getVariablesOcultas(String consulta, Map<String, String> evidencia,
                                              List<Nodo> orden) {
        List<String> ocultas = new ArrayList<>();
        for (Nodo n : orden) {
            String nombre = n.getNombre();
            if (!nombre.equals(consulta) && !evidencia.containsKey(nombre))
                ocultas.add(nombre);
        }
        return ocultas;
    }
}