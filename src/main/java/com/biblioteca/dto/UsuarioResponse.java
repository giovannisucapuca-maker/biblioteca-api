package com.biblioteca.dto;

import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UsuarioResponse(Long id, String nombre, String apellido, String email,
                              boolean activo, Set<String> roles, LocalDateTime fechaRegistro) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.isActivo(),
                u.getRoles().stream().map(Rol::getNombre).map(Enum::name).collect(Collectors.toSet()),
                u.getFechaRegistro());
    }
}
