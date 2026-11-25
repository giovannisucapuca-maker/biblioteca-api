package com.biblioteca.dto;

import com.biblioteca.model.NombreRol;
import jakarta.validation.constraints.*;

import java.util.Set;

/** Alta de usuario por un administrador (permite asignar roles). */
public record UsuarioAdminRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotBlank(message = "El apellido es obligatorio") @Size(max = 80) String apellido,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") @Size(max = 120) String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe contener letras y números")
        String password,
        @NotEmpty(message = "Debe asignar al menos un rol") Set<NombreRol> roles) {
}
