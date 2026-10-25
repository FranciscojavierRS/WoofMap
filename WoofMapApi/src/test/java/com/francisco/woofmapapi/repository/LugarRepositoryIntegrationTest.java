package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
})
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class LugarRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:17-3.5")
                    .asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.saveAndFlush(
                new Usuario(null, "Test", "integracion@woofmap.com", "hash", Instant.now()));
    }

    private Lugar guardarLugar(String nombre, String categoria, double lat, double lon) {
        return lugarRepository.saveAndFlush(new Lugar(
                null, nombre, "descripcion", categoria,
                GeometryUtils.createPoint(lat, lon), usuario, Instant.now()));
    }

    @Test
    @DisplayName("buscarCercanos filtra por radio y categoria")
    void buscarCercanosFiltraPorRadioYCategoria() {
        Lugar cerca = guardarLugar("Cerca", "PARQUE", 40.4168, -3.7038);
        Lugar lejos = guardarLugar("Lejos", "PARQUE", 41.0000, -3.7038);
        Lugar otraCategoria = guardarLugar("Playa", "PLAYA", 40.4169, -3.7039);

        List<Lugar> resultado = lugarRepository.buscarCercanos(40.4168, -3.7038, 5000.0, "PARQUE", 200);

        assertThat(resultado).extracting(Lugar::getId).containsExactly(cerca.getId());
        assertThat(resultado).doesNotContain(lejos, otraCategoria);
    }

    @Test
    @DisplayName("buscarCercanos sin categoria devuelve todas las cercanas")
    void buscarCercanosSinCategoriaDevuelveTodas() {
        Lugar parque = guardarLugar("Parque", "PARQUE", 40.4168, -3.7038);
        Lugar playa = guardarLugar("Playa", "PLAYA", 40.4170, -3.7040);
        guardarLugar("Lejos", "PARQUE", 41.0000, -3.7038);

        List<Lugar> resultado = lugarRepository.buscarCercanos(40.4168, -3.7038, 5000.0, null, 200);

        assertThat(resultado).extracting(Lugar::getId)
                .containsExactlyInAnyOrder(parque.getId(), playa.getId());
    }

    @Test
    @DisplayName("buscarCercanos respeta el limite de resultados")
    void buscarCercanosRespetaLimite() {
        guardarLugar("Uno", "PARQUE", 40.4168, -3.7038);
        guardarLugar("Dos", "PARQUE", 40.4169, -3.7039);

        List<Lugar> resultado = lugarRepository.buscarCercanos(40.4168, -3.7038, 5000.0, null, 1);

        assertThat(resultado).hasSize(1);
    }
}
