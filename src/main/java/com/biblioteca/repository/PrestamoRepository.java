package com.biblioteca.repository;

import com.biblioteca.model.EstadoPrestamo;
import com.biblioteca.model.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "libro"})
    Optional<Prestamo> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByEstado(EstadoPrestamo estado, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByUsuarioId(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    boolean existsByUsuarioIdAndLibroIdAndEstado(Long usuarioId, Long libroId, EstadoPrestamo estado);

    boolean existsByLibroId(Long libroId);

    /** Préstamos activos del usuario cuya fecha de vencimiento ya pasó. */
    @Query("""
            select count(p) from Prestamo p
            where p.usuario.id = :usuarioId
              and p.estado = com.biblioteca.model.EstadoPrestamo.ACTIVO
              and p.fechaVencimiento < :hoy
            """)
    long countVencidosByUsuario(@Param("usuarioId") Long usuarioId, @Param("hoy") LocalDate hoy);

    /** Todos los préstamos vencidos y aún no devueltos. */
    @EntityGraph(attributePaths = {"usuario", "libro"})
    @Query(value = """
            select p from Prestamo p
            where p.estado = com.biblioteca.model.EstadoPrestamo.ACTIVO
              and p.fechaVencimiento < :hoy
            """,
            countQuery = """
            select count(p) from Prestamo p
            where p.estado = com.biblioteca.model.EstadoPrestamo.ACTIVO
              and p.fechaVencimiento < :hoy
            """)
    Page<Prestamo> findVencidos(@Param("hoy") LocalDate hoy, Pageable pageable);
}
