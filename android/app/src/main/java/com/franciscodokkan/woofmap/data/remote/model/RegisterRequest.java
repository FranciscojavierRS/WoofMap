package com.franciscodokkan.woofmap.data.remote.model;

public class RegisterRequest {

    public final String nombre;
    public final String email;
    public final String password;

    public RegisterRequest(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }
}
