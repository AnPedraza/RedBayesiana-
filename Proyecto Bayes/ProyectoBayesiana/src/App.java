import java.util.*;

public class App {
    public static void main(String[] args) throws Exception {

        RedBayesiana red = LectorArchivos.cargarEstructura("estructura.txt");
        LectorArchivos.cargarTablas("tablas.txt", red);

        red.imprimirEstructura();
        red.imprimirTablas();

        MotorInferencia motor = new MotorInferencia(red);
        Map<String, String> evidencia = new LinkedHashMap<>();
        evidencia.put("Rain", "light");
        evidencia.put("Maintenance", "no");

        Map<String, Double> resultado = motor.inferir("Appointment", evidencia);

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         RESULTADO FINAL              ║");
        System.out.println("╠══════════════════════════════════════╣");
        for (Map.Entry<String, Double> e : resultado.entrySet()) {
            System.out.printf("║  P(Appointment=%-8s | e) = %.4f  ║%n",
                    e.getKey(), e.getValue());
        }
        System.out.println("╚══════════════════════════════════════╝");
    }
}