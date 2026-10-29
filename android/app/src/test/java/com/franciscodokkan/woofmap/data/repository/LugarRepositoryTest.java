package com.franciscodokkan.woofmap.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.LugarApi;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LugarRepositoryTest {

    private MockWebServer servidor;
    private LugarRepository repositorio;

    @Before
    public void setUp() throws Exception {
        servidor = new MockWebServer();
        servidor.start();
        LugarApi api = new Retrofit.Builder()
                .baseUrl(servidor.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LugarApi.class);
        repositorio = new LugarRepository(api);
    }

    @After
    public void tearDown() throws Exception {
        servidor.shutdown();
    }

    @Test
    public void listarConUbicacionDevuelveLugaresYEnviaRadio() throws Exception {
        servidor.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[{\"id\":1,\"nombre\":\"Parque Central\",\"categoria\":\"PARQUE\","
                        + "\"lat\":40.4,\"lon\":-3.7}]"));

        ApiResult<List<LugarResponse>> resultado = repositorio.listar(40.4, -3.7);

        assertTrue(resultado.esExito());
        assertEquals(1, resultado.datos.size());
        assertEquals("Parque Central", resultado.datos.get(0).nombre);
        RecordedRequest peticion = servidor.takeRequest();
        assertTrue(peticion.getPath().contains("lat=40.4"));
        assertTrue(peticion.getPath().contains("lon=-3.7"));
        assertTrue(peticion.getPath().contains("radio=10000"));
    }

    @Test
    public void listarSinUbicacionNoEnviaCoordenadas() throws Exception {
        servidor.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        ApiResult<List<LugarResponse>> resultado = repositorio.listar(null, null);

        assertTrue(resultado.esExito());
        assertTrue(resultado.datos.isEmpty());
        RecordedRequest peticion = servidor.takeRequest();
        assertFalse(peticion.getPath().contains("lat="));
        assertFalse(peticion.getPath().contains("radio="));
    }

    @Test
    public void listarConSesionCaducadaDevuelveNoAutorizado() {
        servidor.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":401,\"message\":\"No autorizado\"}"));

        ApiResult<List<LugarResponse>> resultado = repositorio.listar(40.4, -3.7);

        assertFalse(resultado.esExito());
        assertEquals(401, resultado.codigo);
        assertTrue(resultado.esNoAutorizado());
    }

    @Test
    public void listarConErrorDelServidorDevuelveMensaje() {
        servidor.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":500,\"message\":\"Error interno\"}"));

        ApiResult<List<LugarResponse>> resultado = repositorio.listar(40.4, -3.7);

        assertFalse(resultado.esExito());
        assertEquals("Error interno", resultado.error);
    }
}
