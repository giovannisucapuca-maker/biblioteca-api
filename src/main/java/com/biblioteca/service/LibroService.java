package com.biblioteca.service;

import com.biblioteca.dto.*;
import com.biblioteca.exception.BusinessRuleException;
import com.biblioteca.exception.DuplicateResourceException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Libro;
import com.biblioteca.repository.CategoriaRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> buscar(String titulo, String autor, Long categoriaId,
                                              boolean soloDisponibles, Pageable pageable) {
        Specification<Libro> spec = (root, query, cb) -> cb.conjunction();
        if (titulo != null && !titulo.isBlank()) {
            String patron = "%" + titulo.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("titulo")), patron));
        }
        if (autor != null && !autor.isBlank()) {
            String patron = "%" + autor.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("autor")), patron));
        }
        if (categoriaId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        }
        if (soloDisponibles) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("ejemplaresDisponibles"), 0));
        }
        return PageResponse.from(libroRepository.findAll(spec, pageable).map(LibroResponse::from));
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return LibroResponse.from(buscarEntidad(id));
    }

    @Transactional
    public LibroResponse crear(LibroRequest req) {
        if (libroRepository.existsByIsbn(req.isbn())) {
            throw new DuplicateResourceException("Ya existe un libro con el ISBN " + req.isbn());
        }
        Libro l = new Libro();
        aplicar(l, req);
        l.setEjemplaresDisponibles(req.ejemplaresTotales());
        return LibroResponse.from(libroRepository.save(l));
    }

    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest req) {
        Libro l = buscarEntidad(id);
        if (libroRepository.existsByIsbnAndIdNot(req.isbn(), id)) {
            throw new DuplicateResourceException("Ya existe otro libro con el ISBN " + req.isbn());
        }
        int prestados = l.getEjemplaresTotales() - l.getEjemplaresDisponibles();
        if (req.ejemplaresTotales() < prestados) {
            throw new BusinessRuleException("No se pueden reducir los ejemplares a " + req.ejemplaresTotales()
                    + " porque hay " + prestados + " en préstamo");
        }
        aplicar(l, req);
        l.setEjemplaresDisponibles(req.ejemplaresTotales() - prestados);
        return LibroResponse.from(l);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro l = buscarEntidad(id);
        if (prestamoRepository.existsByLibroId(id)) {
            throw new BusinessRuleException("No se puede eliminar un libro con historial de préstamos");
        }
        libroRepository.delete(l);
    }

    @Transactional(readOnly = true)
    public List<LibroPopularResponse> masPrestados(int limite) {
        return libroRepository.findMasPrestados(PageRequest.of(0, Math.min(Math.max(limite, 1), 20)));
    }

    private Libro buscarEntidad(Long id) {
        return libroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Libro", id));
    }

    private void aplicar(Libro l, LibroRequest req) {
        Categoria cat = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", req.categoriaId()));
        l.setIsbn(req.isbn());
        l.setTitulo(req.titulo().trim());
        l.setAutor(req.autor().trim());
        l.setAnioPublicacion(req.anioPublicacion());
        l.setEjemplaresTotales(req.ejemplaresTotales());
        l.setCategoria(cat);
    }
}
