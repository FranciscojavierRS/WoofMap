package com.francisco.woofmapapi;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;

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

}
