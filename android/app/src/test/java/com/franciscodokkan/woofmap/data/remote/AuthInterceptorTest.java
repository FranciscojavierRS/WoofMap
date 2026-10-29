package com.franciscodokkan.woofmap.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.franciscodokkan.woofmap.data.local.TokenStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class AuthInterceptorTest {

    private MockWebServer servidor;
    private TokenStore tokenStore;
    private OkHttpClient cliente;

    @Before
    public void setUp() throws Exception {
        servidor = new MockWebServer();
        servidor.start();
        tokenStore = mock(TokenStore.class);
        cliente = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .build();
    }

    @After
    public void tearDown() throws Exception {
        servidor.shutdown();
    }

    @Test
    public void anadeElTokenEnLasRutasProtegidas() throws Exception {
        when(tokenStore.getToken()).thenReturn("abc123");
        servidor.enqueue(new MockResponse().setResponseCode(200));

        cliente.newCall(new Request.Builder().url(servidor.url("/lugares")).build()).execute();

        RecordedRequest peticion = servidor.takeRequest();
        assertEquals("Bearer abc123", peticion.getHeader("Authorization"));
    }

    @Test
    public void noAnadeElTokenEnLasRutasDeAutenticacion() throws Exception {
        when(tokenStore.getToken()).thenReturn("abc123");
        servidor.enqueue(new MockResponse().setResponseCode(401));

        cliente.newCall(new Request.Builder().url(servidor.url("/auth/login")).build()).execute();

        RecordedRequest peticion = servidor.takeRequest();
        assertNull(peticion.getHeader("Authorization"));
    }

    @Test
    public void noAnadeCabeceraSiNoHayToken() throws Exception {
        when(tokenStore.getToken()).thenReturn(null);
        servidor.enqueue(new MockResponse().setResponseCode(401));

        cliente.newCall(new Request.Builder().url(servidor.url("/lugares")).build()).execute();

        RecordedRequest peticion = servidor.takeRequest();
        assertNull(peticion.getHeader("Authorization"));
    }
}
