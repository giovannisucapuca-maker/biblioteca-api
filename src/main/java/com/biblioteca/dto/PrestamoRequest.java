package com.biblioteca.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PrestamoRequest(
        @NotNull(message = "El usuario es obligatorio") Long usuarioId,
        @NotNull(message = "El libro es obligatorio") Long libroId,
        @NotNull(message = "Los días de préstamo son obligatorios")
        @Min(value = 1, message = "El préstamo debe ser de al menos 1 día")
        @Max(value = 30, message = "El préstamo no puede superar 30 días")
        Integer diasPrestamo) {
}
