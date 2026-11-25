package com.biblioteca.dto;

import java.util.Set;

public record TokenResponse(String accessToken, String refreshToken, String tipo,
                            long expiraEnSegundos, String email, Set<String> roles) {
}
