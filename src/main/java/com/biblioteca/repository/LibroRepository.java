package com.biblioteca.repository;

import com.biblioteca.dto.LibroPopularResponse;
import com.biblioteca.model.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long>, JpaSpecificationExecutor<Libro> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /** Búsqueda dinámica (Specification) cargando la categoría en la misma consulta (evita N+1). */
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Libro> findAll(Specification<Libro> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Libro> findById(Long id);

    /** Consulta personalizada JPQL con proyección a DTO: ranking de libros más prestados. */
    @Query("""
            select new com.biblioteca.dto.LibroPopularResponse(l.id, l.titulo, l.autor, count(p))
            from Prestamo p join p.libro l
            group by l.id, l.titulo, l.autor
            order by count(p) desc
            """)
    List<LibroPopularResponse> findMasPrestados(Pageable pageable);
}
