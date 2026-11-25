package com.biblioteca.service;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.exception.BusinessRuleException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.EstadoPrestamo;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Lógica de negocio de préstamos y devoluciones (separada de la capa web y de datos). */
@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final int maxActivos;
    private final BigDecimal multaDiaria;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           UsuarioRepository usuarioRepository,
                           LibroRepository libroRepository,
                           @Value("${app.prestamo.max-activos:3}") int maxActivos,
                           @Value("${app.prestamo.multa-diaria:1.00}") BigDecimal multaDiaria) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.maxActivos = maxActivos;
        this.multaDiaria = multaDiaria;
    }

    @Transactional
    public PrestamoResponse registrar(PrestamoRequest req) {
        Usuario usuario = usuarioRepository.findById(req.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", req.usuarioId()));
        Libro libro = libroRepository.findById(req.libroId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", req.libroId()));
        LocalDate hoy = LocalDate.now();

        if (!usuario.isActivo()) {
            throw new BusinessRuleException("El usuario está desactivado y no puede realizar préstamos");
        }
        if (libro.getEjemplaresDisponibles() <= 0) {
            throw new BusinessRuleException("No hay ejemplares disponibles de '" + libro.getTitulo() + "'");
        }
        if (prestamoRepository.countVencidosByUsuario(usuario.getId(), hoy) > 0) {
            throw new BusinessRuleException("El usuario tiene préstamos vencidos pendientes de devolución");
        }
        if (prestamoRepository.countByUsuarioIdAndEstado(usuario.getId(), EstadoPrestamo.ACTIVO) >= maxActivos) {
            throw new BusinessRuleException("El usuario alcanzó el máximo de " + maxActivos + " préstamos activos");
        }
        if (prestamoRepository.existsByUsuarioIdAndLibroIdAndEstado(usuario.getId(), libro.getId(), EstadoPrestamo.ACTIVO)) {
            throw new BusinessRuleException("El usuario ya tiene un préstamo activo de este libro");
        }

        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1);

        Prestamo p = new Prestamo();
        p.setUsuario(usuario);
        p.setLibro(libro);
        p.setFechaPrestamo(hoy);
        p.setFechaVencimiento(hoy.plusDays(req.diasPrestamo()));
        p.setEstado(EstadoPrestamo.ACTIVO);
        return PrestamoResponse.from(prestamoRepository.save(p));
    }

    @Transactional
    public PrestamoResponse devolver(Long prestamoId) {
        Prestamo p = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo", prestamoId));
        if (p.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessRuleException("El préstamo " + prestamoId + " ya fue devuelto");
        }
        LocalDate hoy = LocalDate.now();
        long diasRetraso = Math.max(0, ChronoUnit.DAYS.between(p.getFechaVencimiento(), hoy));

        p.setFechaDevolucion(hoy);
        p.setEstado(EstadoPrestamo.DEVUELTO);
        p.setMulta(multaDiaria.multiply(BigDecimal.valueOf(diasRetraso)));

        Libro libro = p.getLibro();
        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() + 1);
        return PrestamoResponse.from(p);
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> listar(EstadoPrestamo estado, Pageable pageable) {
        var page = estado == null ? prestamoRepository.findAll(pageable)
                : prestamoRepository.findByEstado(estado, pageable);
        return PageResponse.from(page.map(PrestamoResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> vencidos(Pageable pageable) {
        return PageResponse.from(prestamoRepository.findVencidos(LocalDate.now(), pageable).map(PrestamoResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> historialDeUsuario(Long usuarioId, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario", usuarioId);
        }
        return PageResponse.from(prestamoRepository.findByUsuarioId(usuarioId, pageable).map(PrestamoResponse::from));
    }

    /** Historial del usuario autenticado (identificado por el subject del JWT). */
    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> miHistorial(String email, Pageable pageable) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return PageResponse.from(prestamoRepository.findByUsuarioId(u.getId(), pageable).map(PrestamoResponse::from));
    }
}
