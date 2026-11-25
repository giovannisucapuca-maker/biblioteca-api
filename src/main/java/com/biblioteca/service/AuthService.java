package com.biblioteca.service;

import com.biblioteca.dto.*;
import com.biblioteca.model.NombreRol;
import com.biblioteca.model.RefreshToken;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.exception.InvalidTokenException;
import com.biblioteca.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /** Registro público: siempre crea usuarios con rol USUARIO (no se puede auto-asignar ADMIN). */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        Usuario u = usuarioService.crear(req.nombre(), req.apellido(), req.email(), req.password(),
                Set.of(NombreRol.USUARIO));
        return UsuarioResponse.from(u);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        // Lanza BadCredentialsException / DisabledException si corresponde
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        Usuario u = usuarioService.buscarPorEmail(email);
        return emitirTokens(u);
    }

    @Transactional
    public TokenResponse refrescar(RefreshRequest req) {
        RefreshToken actual = refreshTokenService.validar(req.refreshToken());
        Usuario u = actual.getUsuario();
        if (!u.isActivo()) {
            throw new InvalidTokenException("La cuenta está desactivada");
        }
        // Rotación: el refresh token usado se revoca y se emite uno nuevo
        refreshTokenService.revocar(actual.getToken());
        return emitirTokens(u);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokenService.revocar(req.refreshToken());
    }

    private TokenResponse emitirTokens(Usuario u) {
        Set<String> roles = u.getRoles().stream().map(Rol::getNombre).map(Enum::name).collect(Collectors.toSet());
        String access = jwtService.generarAccessToken(u.getEmail(), roles);
        RefreshToken refresh = refreshTokenService.crear(u);
        return new TokenResponse(access, refresh.getToken(), "Bearer",
                jwtService.getAccessExpirationSeconds(), u.getEmail(), roles);
    }
}
