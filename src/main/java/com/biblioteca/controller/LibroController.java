package com.biblioteca.controller;

import com.biblioteca.dto.*;
import com.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@Tag(name = "Libros", description = "Catálogo: consulta para cualquier usuario autenticado, escritura solo ADMIN")
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar libros con filtros y paginación")
    public PageResponse<LibroResponse> buscar(@RequestParam(required = false) String titulo,
                                              @RequestParam(required = false) String autor,
                                              @RequestParam(required = false) Long categoriaId,
                                              @RequestParam(defaultValue = "false") boolean soloDisponibles,
                                              @ParameterObject @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        return libroService.buscar(titulo, autor, categoriaId, soloDisponibles, pageable);
    }

    @GetMapping("/populares")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ranking de libros más prestados (consulta JPQL personalizada)")
    public List<LibroPopularResponse> populares(@RequestParam(defaultValue = "5") int limite) {
        return libroService.masPrestados(limite);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener un libro por id")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar un libro (solo ADMIN)")
    public LibroResponse crear(@Valid @RequestBody LibroRequest request) {
        return libroService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un libro (solo ADMIN)")
    public LibroResponse actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
        return libroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un libro sin historial de préstamos (solo ADMIN)")
    public void eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
    }
}
