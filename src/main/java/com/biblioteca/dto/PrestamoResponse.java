package com.biblioteca.dto;

import com.biblioteca.model.EstadoPrestamo;
import com.biblioteca.model.Prestamo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoResponse(Long id, Long usuarioId, String usuario, Long libroId, String libro,
                               LocalDate fechaPrestamo, LocalDate fechaVencimiento, LocalDate fechaDevolucion,
                               EstadoPrestamo estado, BigDecimal multa, boolean vencido) {

    public static PrestamoResponse from(Prestamo p) {
        boolean vencido = p.getEstado() == EstadoPrestamo.ACTIVO && p.getFechaVencimiento().isBefore(LocalDate.now());
        return new PrestamoResponse(p.getId(),
                p.getUsuario().getId(), p.getUsuario().getNombre() + " " + p.getUsuario().getApellido(),
                p.getLibro().getId(), p.getLibro().getTitulo(),
                p.getFechaPrestamo(), p.getFechaVencimiento(), p.getFechaDevolucion(),
                p.getEstado(), p.getMulta(), vencido);
    }
}
