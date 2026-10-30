package com.francisco.woofmapapi;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.password=test",
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
})
class WoofMapApiApplicationTests {

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private Flyway flyway;

    @Test
    void contextLoads() {
    }

    @Test
    void elModuloDeAutoConfiguracionDeFlywayEstaEnElClasspath() {
        // Spring Boot 4 separa las auto-configuraciones: sin spring-boot-flyway no se crea el esquema.
        assertThat(ClassUtils.isPresent(
                "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
                getClass().getClassLoader())).isTrue();
    }

}
