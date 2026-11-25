package com.biblioteca.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error para toda la API. */
public record ApiError(LocalDateTime timestamp, int status, String error, String mensaje,
                       String ruta, List<CampoError> errores) {

    public record CampoError(String campo, String mensaje) {
    }

    public static ApiError of(HttpStatus status, String mensaje, String ruta) {
        return new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, ruta, List.of());
    }

    public static ApiError of(HttpStatus status, String mensaje, String ruta, List<CampoError> errores) {
        return new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, ruta, errores);
    }
}
