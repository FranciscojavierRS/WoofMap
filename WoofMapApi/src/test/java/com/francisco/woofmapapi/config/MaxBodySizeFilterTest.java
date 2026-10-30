package com.francisco.woofmapapi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MaxBodySizeFilterTest {

    private final MaxBodySizeFilter filtro = new MaxBodySizeFilter();

    @Test
    @DisplayName("Corta con 413 un JSON mayor de 1 MB sin llamar al resto de la cadena")
    void jsonGrande_devuelve413() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/register");
        request.setContentType("application/json");
        request.setContent(new byte[2 * 1024 * 1024]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(request, response, cadena);

        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(cadena.getRequest()).isNull();
    }

    @Test
    @DisplayName("Deja pasar un JSON pequeno")
    void jsonPequeno_pasa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setContentType("application/json");
        request.setContent("{\"email\":\"a@b.c\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(request, response, cadena);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(cadena.getRequest()).isSameAs(request);
    }

    @Test
    @DisplayName("No limita los multipart: tienen su propio tope")
    void multipartGrande_pasa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/lugares/1/fotos");
        request.setContentType("multipart/form-data; boundary=x");
        request.setContent(new byte[2 * 1024 * 1024]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(request, response, cadena);

        assertThat(cadena.getRequest()).isSameAs(request);
    }
}
