package com.biblioteca.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración de la seguridad JWT y los roles contra una BD H2 en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private static final String LIBRO_JSON = """
            {"isbn":"9780321125217","titulo":"Domain-Driven Design","autor":"Eric Evans",
             "anioPublicacion":2003,"categoriaId":1,"ejemplaresTotales":2}""";

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mvc.perform(get("/api/libros").header("Authorization", "Bearer token.falso.xyz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token es inválido"));
    }

    @Test
    void loginConCredencialesIncorrectasDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@biblioteca.edu\",\"password\":\"incorrecta1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioPuedeConsultarCatalogoPeroNoCrearLibros() throws Exception {
        String token = login("usuario@biblioteca.edu", "Usuario123");
        mvc.perform(get("/api/libros").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(post("/api/libros").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(LIBRO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNoPuedeListarUsuariosNiRegistrarPrestamos() throws Exception {
        String token = login("usuario@biblioteca.edu", "Usuario123");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/prestamos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"libroId\":1,\"diasPrestamo\":7}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioConsultaSuHistorial() throws Exception {
        String token = login("usuario@biblioteca.edu", "Usuario123");
        mvc.perform(get("/api/prestamos/mi-historial").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void adminCreaLibroYValidaDatos() throws Exception {
        String token = login("admin@biblioteca.edu", "Admin123!");
        mvc.perform(post("/api/libros").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(LIBRO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ejemplaresDisponibles").value(2));

        // ISBN duplicado -> 409
        mvc.perform(post("/api/libros").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(LIBRO_JSON))
                .andExpect(status().isConflict());

        // Datos inválidos -> 400 con detalle por campo
        mvc.perform(post("/api/libros").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"abc\",\"titulo\":\"\",\"autor\":\"X\",\"anioPublicacion\":1,\"categoriaId\":1,\"ejemplaresTotales\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isNotEmpty());
    }
}
