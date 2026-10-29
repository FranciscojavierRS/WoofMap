package com.franciscodokkan.woofmap.ui.lugar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CreateLugarViewModelTest {

    @Rule
    public final InstantTaskExecutorRule regla = new InstantTaskExecutorRule();

    private LugarRepository repositorio;
    private CreateLugarViewModel viewModel;

    @Before
    public void setUp() {
        repositorio = mock(LugarRepository.class);
        viewModel = new CreateLugarViewModel(repositorio, Runnable::run);
    }

    @Test
    public void crearLugarExitosoPublicaLugarCreado() {
        LugarResponse lugar = new LugarResponse(1L, "Parque Canino", "PARQUE", 40.4168, -3.7038);
        when(repositorio.crear(any())).thenReturn(ApiResult.exito(lugar));

        viewModel.crear("Parque Canino", "Un gran parque", "PARQUE", 40.4168, -3.7038);

        assertNotNull(valor(viewModel.getLugarCreado()));
        assertEquals("Parque Canino", valor(viewModel.getLugarCreado()).nombre);
        assertEquals(Boolean.FALSE, valor(viewModel.getCargando()));
    }

    @Test
    public void crearLugarErrorExponeMensaje() {
        when(repositorio.crear(any())).thenReturn(ApiResult.error(400, "Error en los datos"));

        viewModel.crear("Parque", "Desc", "PARQUE", 40.4168, -3.7038);

        assertNull(valor(viewModel.getLugarCreado()));
        assertEquals("Error en los datos", valor(viewModel.getError()));
        assertEquals(Boolean.FALSE, valor(viewModel.getCargando()));
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
