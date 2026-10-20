package com.example.gestorcreditos;

public class Config {
    // Emulador: 10.0.2.2  |  Teléfono físico: la IP de tu PC (ej. 192.168.1.68)
    public static final String IP = "192.168.137.7";

    public static final String URL_BASE = "http://" + IP + "/api_creditos/";
    public static final String URL_LOGIN = URL_BASE + "login.php";
    public static final String URL_SINCRONIZAR = URL_BASE + "sincronizar_usuario.php";
}
