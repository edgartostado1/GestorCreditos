package com.example.gestorcreditos;

public class ProgramaEducativo {
    private int id;
    private String nombre;
    private int total_creditos_requeridos;

    // Constructor necesario para CREAR (POST) nuevos registros desde Android
    public ProgramaEducativo(String nombre, int total_creditos_requeridos) {
        this.nombre = nombre;
        this.total_creditos_requeridos = total_creditos_requeridos;
    }

    // Getters para LEER (GET) los datos recibidos
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getTotal_creditos_requeridos() { return total_creditos_requeridos; }
}
