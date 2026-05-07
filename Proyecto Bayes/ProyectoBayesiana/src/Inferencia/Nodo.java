package inferencia;

import java.util.*;

public class Nodo {
    private final String nombre;
    private final List<String> estados;
    private final List<Nodo> padres;
    private final List<Nodo> hijos;
    private TablaProbabilidad tabla;

    public Nodo(String nombre) {
        this.nombre = nombre;
        this.estados = new ArrayList<>();
        this.padres = new ArrayList<>();
        this.hijos = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public List<String> getEstados() { return estados; }
    public List<Nodo> getPadres() { return padres; }
    public List<Nodo> getHijos() { return hijos; }
    public TablaProbabilidad getTabla() { return tabla; }
    public void setTabla(TablaProbabilidad tabla) { this.tabla = tabla; }
    public void agregarEstado(String estado) { estados.add(estado); }
    public void agregarPadre(Nodo padre) { if (!padres.contains(padre)) padres.add(padre); }
    public void agregarHijo(Nodo hijo) { if (!hijos.contains(hijo)) hijos.add(hijo); }

    @Override
    public String toString() { return nombre; }
}