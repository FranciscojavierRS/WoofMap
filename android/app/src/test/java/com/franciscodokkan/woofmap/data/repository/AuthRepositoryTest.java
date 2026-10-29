package com.franciscodokkan.woofmap.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.AuthApi;
import com.franciscodokkan.woofmap.data.remote.model.AuthResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthRepositoryTest {

    private MockWebServer servidor;
    private TokenStore tokenStore;
    private AuthRepository repositorio;

    @Before
    public void setUp() throws Exception {
        servidor = new MockWebServer();
        servidor.start();
        tokenStore = mock(TokenStore.class);
        AuthApi api = new Retrofit.Builder()
                .baseUrl(servidor.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi.class);
        repositorio = new AuthRepository(api, tokenStore);
    }

    @After
    public void tearDown() throws Exception {
        servidor.shutdown();
    }

    @Test
    public void loginCorrectoGuardaLaSesion() {
        servidor.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"token\":\"abc123\",\"tipo\":\"Bearer\",\"usuarioId\":1,"
                        + "\"nombre\":\"Fran\",\"email\":\"fran@woofmap.com\"}"));

        ApiResult<AuthResponse> resultado = repositorio.login("fran@woofmap.com", "12345678");

        assertTrue(resultado.esExito());
        assertEquals("abc123", resultado.datos.token);
        verify(tokenStore).guardarSesion("abc123", "Fran", "fran@woofmap.com");
    }

    @Test
    public void loginConCredencialesIncorrectasDevuelveElMensajeDelServidor() {
        servidor.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":401,\"message\":\"Credenciales incorrectas\"}"));

        ApiResult<AuthResponse> resultado = repositorio.login("fran@woofmap.com", "mala");

        assertFalse(resultado.esExito());
        assertEquals(401, resultado.codigo);
        assertEquals("Credenciales incorrectas", resultado.error);
        verify(tokenStore, never()).guardarSesion(any(), any(), any());
    }

    @Test
    public void registroCorrectoGuardaLaSesion() {
        servidor.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"token\":\"nuevo\",\"tipo\":\"Bearer\",\"usuarioId\":2,"
                        + "\"nombre\":\"Ana\",\"email\":\"ana@woofmap.com\"}"));

        ApiResult<AuthResponse> resultado =
                repositorio.registrar("Ana", "ana@woofmap.com", "12345678");

        assertTrue(resultado.esExito());
        verify(tokenStore).guardarSesion("nuevo", "Ana", "ana@woofmap.com");
    }

    @Test
    public void registroConEmailDuplicadoDevuelveError() {
        servidor.enqueue(new MockResponse()
                .setResponseCode(409)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":409,\"message\":\"El email ya está registrado\"}"));

        ApiResult<AuthResponse> resultado =
                repositorio.registrar("Ana", "ana@woofmap.com", "12345678");

        assertFalse(resultado.esExito());
        assertEquals(409, resultado.codigo);
        assertEquals("El email ya está registrado", resultado.error);
    }
}
