package com.biblioteca.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Envoltorio estable para respuestas paginadas (evita serializar PageImpl directamente). */
public record PageResponse<T>(List<T> contenido, int pagina, int tamanio,
                              long totalElementos, int totalPaginas, boolean ultima) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
