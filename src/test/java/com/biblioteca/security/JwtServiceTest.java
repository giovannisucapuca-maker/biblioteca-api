package com.biblioteca.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("clave-de-prueba-de-al-menos-32-bytes!!".getBytes());

    private final JwtService jwtService = new JwtService(SECRET, 15);

    @Test
    void generaYValidaTokenConRoles() {
        String token = jwtService.generarAccessToken("admin@biblioteca.edu", List.of("ADMIN"));
        Claims claims = jwtService.parsear(token);

        assertEquals("admin@biblioteca.edu", claims.getSubject());
        assertEquals(List.of("ADMIN"), jwtService.roles(claims));
        assertEquals(JwtService.TYPE_ACCESS, claims.get(JwtService.CLAIM_TYPE, String.class));
    }

    @Test
    void rechazaTokenManipulado() {
        String token = jwtService.generarAccessToken("a@b.com", List.of("USUARIO"));
        String manipulado = token.substring(0, token.length() - 3) + "abc";
        assertThrows(JwtException.class, () -> jwtService.parsear(manipulado));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService expirado = new JwtService(SECRET, -1);
        String token = expirado.generarAccessToken("a@b.com", List.of("USUARIO"));
        assertThrows(JwtException.class, () -> jwtService.parsear(token));
    }
}
