package com.example.gestorcreditos;

public class Usuario {
    private int id;
    private String matricula;
    private String password;
    private String nombre;
    private int sincronizado; // 0 = Pendiente, 1 = Sincronizado

    public Usuario(int id, String matricula, String password, String nombre, int sincronizado) {
        this.id = id;
        this.matricula = matricula;
        this.password = password;
        this.nombre = nombre;
        this.sincronizado = sincronizado;
    }

    public int getId() { return id; }
    public String getMatricula() { return matricula; }
    public String getPassword() { return password; }
    public String getNombre() { return nombre; }
    public int getSincronizado() { return sincronizado; }
}