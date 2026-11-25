package com.biblioteca.service;

import com.biblioteca.exception.InvalidTokenException;
import com.biblioteca.model.RefreshToken;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Duration duracion;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${app.jwt.refresh-expiration-days}") long dias) {
        this.repository = repository;
        this.duracion = Duration.ofDays(dias);
    }

    @Transactional
    public RefreshToken crear(Usuario usuario) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        rt.setUsuario(usuario);
        rt.setExpiraEn(Instant.now().plus(duracion));
        return repository.save(rt);
    }

    /** Valida (existe, no revocado, no expirado) y devuelve el token con su usuario cargado. */
    @Transactional(readOnly = true)
    public RefreshToken validar(String token) {
        RefreshToken rt = repository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido"));
        if (rt.isRevocado()) {
            throw new InvalidTokenException("El refresh token fue revocado");
        }
        if (rt.getExpiraEn().isBefore(Instant.now())) {
            throw new InvalidTokenException("El refresh token ha expirado");
        }
        rt.getUsuario().getRoles().size(); // inicializa el usuario dentro de la transacción
        return rt;
    }

    @Transactional
    public void revocar(String token) {
        repository.findByToken(token).ifPresent(rt -> rt.setRevocado(true));
    }
}
