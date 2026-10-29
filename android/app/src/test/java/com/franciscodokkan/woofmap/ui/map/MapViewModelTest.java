package com.franciscodokkan.woofmap.ui.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

import java.util.Collections;
import java.util.List;

public class MapViewModelTest {

    @Rule
    public final InstantTaskExecutorRule regla = new InstantTaskExecutorRule();

    private LugarRepository repositorio;
    private MapViewModel viewModel;

    @Before
    public void setUp() {
        repositorio = mock(LugarRepository.class);
        viewModel = new MapViewModel(repositorio, Runnable::run);
    }

    @Test
    public void cargarLugaresExponeLaListaRecibida() {
        List<LugarResponse> lugares = Collections.singletonList(
                new LugarResponse(1L, "Parque Central", "PARQUE", 40.4, -3.7));
        when(repositorio.listar(40.4, -3.7, null)).thenReturn(ApiResult.exito(lugares));

        viewModel.cargarLugares(40.4, -3.7);

        assertEquals(lugares, valor(viewModel.getLugares()));
        assertEquals(Boolean.FALSE, valor(viewModel.getCargando()));
    }

    @Test
    public void cargarLugaresSinUbicacionConsultaTodos() {
        when(repositorio.listar(null, null, null))
                .thenReturn(ApiResult.exito(Collections.emptyList()));

        viewModel.cargarLugares(null, null);

        assertTrue(valor(viewModel.getLugares()).isEmpty());
    }

    @Test
    public void un401MarcaLaSesionComoExpirada() {
        when(repositorio.listar(40.4, -3.7, null))
                .thenReturn(ApiResult.error(401, "No autorizado"));

        viewModel.cargarLugares(40.4, -3.7);

        assertEquals(Boolean.TRUE, valor(viewModel.getSesionExpirada()));
    }

    @Test
    public void unErrorDelServidorExponeElMensaje() {
        when(repositorio.listar(40.4, -3.7, null))
                .thenReturn(ApiResult.error(500, "Error interno"));

        viewModel.cargarLugares(40.4, -3.7);

        assertEquals("Error interno", valor(viewModel.getError()));
        assertEquals(Boolean.FALSE, valor(viewModel.getSesionExpirada()));
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
