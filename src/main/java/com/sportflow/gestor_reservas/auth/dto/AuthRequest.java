package com.sportflow.gestor_reservas.auth.dto;

public class AuthRequest {
    private String email;
    private String password;

    // getters y setters

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
