package com.biblioteca.service;

import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.exception.BusinessRuleException;
import com.biblioteca.model.*;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock PrestamoRepository prestamoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock LibroRepository libroRepository;

    PrestamoService service;
    Usuario usuario;
    Libro libro;

    @BeforeEach
    void setUp() {
        service = new PrestamoService(prestamoRepository, usuarioRepository, libroRepository, 3, new BigDecimal("1.00"));
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Ana");
        usuario.setApellido("Lopez");
        libro = new Libro();
        libro.setId(10L);
        libro.setTitulo("Clean Code");
        libro.setEjemplaresTotales(2);
        libro.setEjemplaresDisponibles(2);
    }

    @Test
    void registrarDescuentaStockYCalculaVencimiento() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        PrestamoResponse r = service.registrar(new PrestamoRequest(1L, 10L, 7));

        assertEquals(1, libro.getEjemplaresDisponibles());
        assertEquals(LocalDate.now().plusDays(7), r.fechaVencimiento());
        assertEquals(EstadoPrestamo.ACTIVO, r.estado());
    }

    @Test
    void registrarSinStockLanzaExcepcion() {
        libro.setEjemplaresDisponibles(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));

        assertThrows(BusinessRuleException.class, () -> service.registrar(new PrestamoRequest(1L, 10L, 7)));
    }

    @Test
    void devolverConRetrasoCalculaMulta() {
        libro.setEjemplaresDisponibles(1);
        Prestamo p = new Prestamo();
        p.setId(5L);
        p.setUsuario(usuario);
        p.setLibro(libro);
        p.setFechaPrestamo(LocalDate.now().minusDays(12));
        p.setFechaVencimiento(LocalDate.now().minusDays(5));
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(p));

        PrestamoResponse r = service.devolver(5L);

        assertEquals(EstadoPrestamo.DEVUELTO, r.estado());
        assertEquals(0, new BigDecimal("5.00").compareTo(r.multa()));
        assertEquals(2, libro.getEjemplaresDisponibles());
    }

    @Test
    void devolverDosVecesLanzaExcepcion() {
        Prestamo p = new Prestamo();
        p.setUsuario(usuario);
        p.setLibro(libro);
        p.setEstado(EstadoPrestamo.DEVUELTO);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThrows(BusinessRuleException.class, () -> service.devolver(5L));
    }
}
