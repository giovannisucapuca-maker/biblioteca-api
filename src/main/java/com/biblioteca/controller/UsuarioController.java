package com.biblioteca.controller;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.UsuarioAdminRequest;
import com.biblioteca.dto.UsuarioResponse;
import com.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Gestión de cuentas (ADMIN), salvo /me")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Perfil del usuario autenticado")
    public UsuarioResponse me(Authentication authentication) {
        return usuarioService.perfil(authentication.getName());
    }

    @GetMapping
    @Operation(summary = "Listar usuarios (solo ADMIN)")
    public PageResponse<UsuarioResponse> listar(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario asignando roles (solo ADMIN)")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest request) {
        return usuarioService.crearComoAdmin(request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar una cuenta (solo ADMIN)")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return usuarioService.cambiarEstado(id, activo);
    }
}
