package com.biblioteca.service;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.UsuarioAdminRequest;
import com.biblioteca.dto.UsuarioResponse;
import com.biblioteca.exception.DuplicateResourceException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.NombreRol;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.RolRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario crear(String nombre, String apellido, String email, String password, Set<NombreRol> roles) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + emailNormalizado);
        }
        Set<Rol> entidadesRol = new HashSet<>();
        for (NombreRol nombreRol : roles) {
            entidadesRol.add(rolRepository.findByNombre(nombreRol)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + nombreRol)));
        }
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setApellido(apellido.trim());
        u.setEmail(emailNormalizado);
        u.setPassword(passwordEncoder.encode(password));
        u.setRoles(entidadesRol);
        return usuarioRepository.save(u);
    }

    @Transactional
    public UsuarioResponse crearComoAdmin(UsuarioAdminRequest req) {
        return UsuarioResponse.from(crear(req.nombre(), req.apellido(), req.email(), req.password(), req.roles()));
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Pageable pageable) {
        return PageResponse.from(usuarioRepository.findAll(pageable).map(UsuarioResponse::from));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String email) {
        return UsuarioResponse.from(buscarPorEmail(email));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        u.setActivo(activo);
        return UsuarioResponse.from(u);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }
}
