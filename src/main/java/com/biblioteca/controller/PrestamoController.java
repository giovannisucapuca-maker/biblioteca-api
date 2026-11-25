package com.biblioteca.controller;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.model.EstadoPrestamo;
import com.biblioteca.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Préstamos y devoluciones", description = "Operaciones de ADMIN; cada usuario consulta su propio historial")
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un préstamo (solo ADMIN)")
    public PrestamoResponse registrar(@Valid @RequestBody PrestamoRequest request) {
        return prestamoService.registrar(request);
    }

    @PutMapping("/{id}/devolucion")
    @Operation(summary = "Registrar la devolución de un préstamo y calcular multa (solo ADMIN)")
    public PrestamoResponse devolver(@PathVariable Long id) {
        return prestamoService.devolver(id);
    }

    @GetMapping
    @Operation(summary = "Listar préstamos, opcionalmente por estado (solo ADMIN)")
    public PageResponse<PrestamoResponse> listar(@RequestParam(required = false) EstadoPrestamo estado,
                                                 @ParameterObject @PageableDefault(size = 10, sort = "fechaPrestamo", direction = Sort.Direction.DESC) Pageable pageable) {
        return prestamoService.listar(estado, pageable);
    }

    @GetMapping("/vencidos")
    @Operation(summary = "Préstamos vencidos sin devolver (solo ADMIN)")
    public PageResponse<PrestamoResponse> vencidos(@ParameterObject @PageableDefault(size = 10, sort = "fechaVencimiento") Pageable pageable) {
        return prestamoService.vencidos(pageable);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Historial de préstamos de cualquier usuario (solo ADMIN)")
    public PageResponse<PrestamoResponse> historialDeUsuario(@PathVariable Long usuarioId,
                                                             @ParameterObject @PageableDefault(size = 10, sort = "fechaPrestamo", direction = Sort.Direction.DESC) Pageable pageable) {
        return prestamoService.historialDeUsuario(usuarioId, pageable);
    }

    @GetMapping("/mi-historial")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Historial de préstamos del usuario autenticado")
    public PageResponse<PrestamoResponse> miHistorial(Authentication authentication,
                                                      @ParameterObject @PageableDefault(size = 10, sort = "fechaPrestamo", direction = Sort.Direction.DESC) Pageable pageable) {
        return prestamoService.miHistorial(authentication.getName(), pageable);
    }
}
