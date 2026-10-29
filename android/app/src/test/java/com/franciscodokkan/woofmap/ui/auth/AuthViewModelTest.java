package com.franciscodokkan.woofmap.ui.auth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.AuthResponse;
import com.franciscodokkan.woofmap.data.repository.AuthRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AuthViewModelTest {

    @Rule
    public final InstantTaskExecutorRule regla = new InstantTaskExecutorRule();

    private AuthRepository repositorio;
    private AuthViewModel viewModel;

    @Before
    public void setUp() {
        repositorio = mock(AuthRepository.class);
        viewModel = new AuthViewModel(repositorio, Runnable::run);
    }

    @Test
    public void loginCorrectoMarcaLaSesionComoAutenticada() {
        when(repositorio.login("fran@woofmap.com", "12345678"))
                .thenReturn(ApiResult.exito(new AuthResponse()));

        viewModel.login("fran@woofmap.com", "12345678");

        assertEquals(Boolean.TRUE, valor(viewModel.getAutenticado()));
        assertEquals(Boolean.FALSE, valor(viewModel.getCargando()));
    }

    @Test
    public void loginIncorrectoExponeElErrorYNoAutentica() {
        when(repositorio.login("fran@woofmap.com", "mala"))
                .thenReturn(ApiResult.error(401, "Credenciales incorrectas"));

        viewModel.login("fran@woofmap.com", "mala");

        assertEquals("Credenciales incorrectas", valor(viewModel.getError()));
        assertEquals(Boolean.FALSE, valor(viewModel.getAutenticado()));
        assertEquals(Boolean.FALSE, valor(viewModel.getCargando()));
    }

    @Test
    public void registroCorrectoMarcaLaSesionComoAutenticada() {
        when(repositorio.registrar("Ana", "ana@woofmap.com", "12345678"))
                .thenReturn(ApiResult.exito(new AuthResponse()));

        viewModel.registrar("Ana", "ana@woofmap.com", "12345678");

        assertEquals(Boolean.TRUE, valor(viewModel.getAutenticado()));
    }

    @Test
    public void registroConEmailDuplicadoExponeElError() {
        when(repositorio.registrar("Ana", "ana@woofmap.com", "12345678"))
                .thenReturn(ApiResult.error(409, "El email ya está registrado"));

        viewModel.registrar("Ana", "ana@woofmap.com", "12345678");

        assertEquals("El email ya está registrado", valor(viewModel.getError()));
        assertEquals(Boolean.FALSE, valor(viewModel.getAutenticado()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T valor(LiveData<T> liveData) {
        Object[] capturado = new Object[1];
        Observer<T> observador = valor -> capturado[0] = valor;
        liveData.observeForever(observador);
        liveData.removeObserver(observador);
        return (T) capturado[0];
    }
}
