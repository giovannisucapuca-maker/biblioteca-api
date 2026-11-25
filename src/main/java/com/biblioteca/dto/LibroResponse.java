package com.biblioteca.dto;

import com.biblioteca.model.Libro;

public record LibroResponse(Long id, String isbn, String titulo, String autor, Integer anioPublicacion,
                            Long categoriaId, String categoria,
                            Integer ejemplaresTotales, Integer ejemplaresDisponibles) {

    public static LibroResponse from(Libro l) {
        return new LibroResponse(l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getAnioPublicacion(),
                l.getCategoria().getId(), l.getCategoria().getNombre(),
                l.getEjemplaresTotales(), l.getEjemplaresDisponibles());
    }
}
