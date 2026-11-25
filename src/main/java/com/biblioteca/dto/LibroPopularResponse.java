package com.biblioteca.dto;

/** Proyección de la consulta personalizada de libros más prestados. */
public record LibroPopularResponse(Long id, String titulo, String autor, Long totalPrestamos) {
}
