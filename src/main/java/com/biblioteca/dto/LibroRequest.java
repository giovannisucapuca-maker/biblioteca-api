package com.biblioteca.dto;

import jakarta.validation.constraints.*;

public record LibroRequest(
        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^(\\d{10}|\\d{13})$", message = "El ISBN debe tener 10 o 13 dígitos numéricos")
        String isbn,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 150, message = "El autor no puede superar 150 caracteres")
        String autor,

        @NotNull(message = "El año de publicación es obligatorio")
        @Min(value = 1450, message = "El año debe ser mayor o igual a 1450")
        @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
        Integer anioPublicacion,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "El número de ejemplares es obligatorio")
        @Min(value = 1, message = "Debe existir al menos 1 ejemplar")
        @Max(value = 1000, message = "No se permiten más de 1000 ejemplares")
        Integer ejemplaresTotales) {
}
