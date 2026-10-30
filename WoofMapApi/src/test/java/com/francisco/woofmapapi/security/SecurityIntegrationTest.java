package com.francisco.woofmapapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.francisco.woofmapapi.config.SecurityConfig;
import com.francisco.woofmapapi.controller.AuthController;
import com.francisco.woofmapapi.controller.LugarController;
import com.francisco.woofmapapi.dto.LugarRequest;
import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.service.AuthService;
import com.francisco.woofmapapi.service.LugarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {LugarController.class, AuthController.class},
        properties = "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
)
@Import({SecurityConfig.class, JwtService.class})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private LugarService lugarService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioCreador;
    private Usuario usuarioAjeno;
    private String tokenCreador;
    private String tokenAjeno;

    @BeforeEach
    void setUp() {
        usuarioCreador = new Usuario(1L, "Creador", "creador@woofmap.com", "hash1", Instant.now());
        usuarioAjeno = new Usuario(2L, "Ajeno", "ajeno@woofmap.com", "hash2", Instant.now());

        when(usuarioRepository.findByEmail("creador@woofmap.com")).thenReturn(Optional.of(usuarioCreador));
        when(usuarioRepository.findByEmail("ajeno@woofmap.com")).thenReturn(Optional.of(usuarioAjeno));

        tokenCreador = jwtService.generarToken(usuarioCreador);
        tokenAjeno = jwtService.generarToken(usuarioAjeno);
    }

    @Test
    @DisplayName("GET /lugares debe ser publico (200 OK sin token)")
    void getLugaresDebeSerPublico() throws Exception {
        when(lugarService.listarLugares(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/lugares"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /lugares sin token debe devolver 401 Unauthorized")
    void postLugaresSinTokenDebeDevolver401() throws Exception {
        LugarRequest request = new LugarRequest("Parque", "Desc", "PARQUE", 40.0, -3.0);

        mockMvc.perform(post("/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /lugares con token invalido debe devolver 401 Unauthorized")
    void postLugaresConTokenInvalidoDebeDevolver401() throws Exception {
        LugarRequest request = new LugarRequest("Parque", "Desc", "PARQUE", 40.0, -3.0);

        mockMvc.perform(post("/lugares")
                        .header("Authorization", "Bearer token-totalmente-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /lugares con token valido debe asociar creador y devolver 201 Created")
    void postLugaresConTokenValidoDebeDevolver201() throws Exception {
        LugarRequest request = new LugarRequest("Parque Central", "Parque vallado", "PARQUE", 40.4168, -3.7038);
        LugarResponse response = new LugarResponse(10L, "Parque Central", "Parque vallado", "PARQUE", 40.4168, -3.7038, 1L, "Creador", Instant.now(), null, null, null);

        when(lugarService.crearLugar(any(LugarRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/lugares")
                        .header("Authorization", "Bearer " + tokenCreador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    @Test
    @DisplayName("PUT /lugares/{id} con token del creador debe permitir editar (200 OK)")
    void putLugaresConTokenCreadorDebeDevolver200() throws Exception {
        LugarRequest request = new LugarRequest("Parque Editado", "Nueva desc", "PARQUE", 40.4168, -3.7038);
        LugarResponse response = new LugarResponse(10L, "Parque Editado", "Nueva desc", "PARQUE", 40.4168, -3.7038, 1L, "Creador", Instant.now(), null, null, null);

        when(lugarService.editarLugar(eq(10L), any(LugarRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/lugares/10")
                        .header("Authorization", "Bearer " + tokenCreador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Parque Editado"));
    }

    @Test
    @DisplayName("PUT /lugares/{id} con token de otro usuario debe devolver 403 Forbidden")
    void putLugaresConTokenAjenoDebeDevolver403() throws Exception {
        LugarRequest request = new LugarRequest("Parque Intruso", "Desc", "PARQUE", 40.4168, -3.7038);

        when(lugarService.editarLugar(eq(10L), any(LugarRequest.class), eq(2L)))
                .thenThrow(new AccessDeniedException("No tienes permisos para modificar este lugar. Solo el creador puede editarlo."));

        mockMvc.perform(put("/lugares/10")
                        .header("Authorization", "Bearer " + tokenAjeno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No tienes permisos para modificar este lugar. Solo el creador puede editarlo."));
    }

    @Test
    @DisplayName("DELETE /lugares/{id} con token de otro usuario debe devolver 403 Forbidden")
    void deleteLugaresConTokenAjenoDebeDevolver403() throws Exception {
        doThrow(new AccessDeniedException("No tienes permisos para eliminar este lugar. Solo el creador puede eliminarlo."))
                .when(lugarService).eliminarLugar(eq(10L), eq(2L));

        mockMvc.perform(delete("/lugares/10")
                        .header("Authorization", "Bearer " + tokenAjeno))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("DELETE /lugares/{id} con token del creador debe devolver 204 No Content")
    void deleteLugaresConTokenCreadorDebeDevolver204() throws Exception {
        mockMvc.perform(delete("/lugares/10")
                        .header("Authorization", "Bearer " + tokenCreador))
                .andExpect(status().isNoContent());
    }
}
