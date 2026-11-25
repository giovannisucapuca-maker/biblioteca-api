package com.biblioteca.config;

import com.biblioteca.model.*;
import com.biblioteca.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Carga inicial: roles, usuarios de demostración y un pequeño catálogo de libros. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;
    @Value("${app.seed.admin-email}")
    private String adminEmail;
    @Value("${app.seed.admin-password}")
    private String adminPassword;
    @Value("${app.seed.usuario-email}")
    private String usuarioEmail;
    @Value("${app.seed.usuario-password}")
    private String usuarioPassword;

    @Override
    @Transactional
    public void run(String... args) {
        for (NombreRol nombre : NombreRol.values()) {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                rolRepository.save(new Rol(null, nombre));
            }
        }
        if (!seedEnabled) {
            return;
        }
        crearUsuarioSiNoExiste("Admin", "Biblioteca", adminEmail, adminPassword, NombreRol.ADMIN);
        crearUsuarioSiNoExiste("Usuario", "Demo", usuarioEmail, usuarioPassword, NombreRol.USUARIO);

        if (libroRepository.count() == 0) {
            Categoria sistemas = categoriaRepository.save(new Categoria("Ingeniería de Software"));
            Categoria literatura = categoriaRepository.save(new Categoria("Literatura"));
            Categoria ciencias = categoriaRepository.save(new Categoria("Ciencias"));
            guardarLibro("9780132350884", "Clean Code", "Robert C. Martin", 2008, sistemas, 5);
            guardarLibro("9780134685991", "Effective Java", "Joshua Bloch", 2018, sistemas, 3);
            guardarLibro("9788437604947", "Cien años de soledad", "Gabriel García Márquez", 1967, literatura, 4);
            guardarLibro("9780553380163", "Breve historia del tiempo", "Stephen Hawking", 1988, ciencias, 2);
            log.info("Datos de demostración cargados");
        }
    }

    private void crearUsuarioSiNoExiste(String nombre, String apellido, String email, String pass, NombreRol rol) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pass));
        u.setRoles(Set.of(rolRepository.findByNombre(rol).orElseThrow()));
        usuarioRepository.save(u);
    }

    private void guardarLibro(String isbn, String titulo, String autor, int anio, Categoria cat, int ejemplares) {
        Libro l = new Libro();
        l.setIsbn(isbn);
        l.setTitulo(titulo);
        l.setAutor(autor);
        l.setAnioPublicacion(anio);
        l.setCategoria(cat);
        l.setEjemplaresTotales(ejemplares);
        l.setEjemplaresDisponibles(ejemplares);
        libroRepository.save(l);
    }
}
