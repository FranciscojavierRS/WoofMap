package com.franciscodokkan.woofmap.ui.lugar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;
import com.franciscodokkan.woofmap.data.remote.model.FotoResponse;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.ComentarioRepository;
import com.franciscodokkan.woofmap.data.repository.FotoRepository;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class LugarDetailViewModelTest {

    @Rule
    public final InstantTaskExecutorRule regla = new InstantTaskExecutorRule();

    private LugarRepository lugarRepo;
    private FotoRepository fotoRepo;
    private ComentarioRepository comentarioRepo;
    private LugarDetailViewModel viewModel;

    @Before
    public void setUp() {
        lugarRepo = mock(LugarRepository.class);
        fotoRepo = mock(FotoRepository.class);
        comentarioRepo = mock(ComentarioRepository.class);
        viewModel = new LugarDetailViewModel(lugarRepo, fotoRepo, comentarioRepo, Runnable::run);
    }

    @Test
    public void cargarFotosPublicaListaDeFotos() {
        List<FotoResponse> fotos = Collections.singletonList(new FotoResponse());
        when(fotoRepo.listar(1L)).thenReturn(ApiResult.exito(fotos));

        viewModel.cargarFotos(1L);

        assertNotNull(valor(viewModel.getFotos()));
        assertEquals(1, valor(viewModel.getFotos()).size());
    }

    @Test
    public void cargarComentariosPublicaListaDeComentarios() {
        List<ComentarioResponse> comentarios = Collections.singletonList(new ComentarioResponse());
        when(comentarioRepo.listar(1L)).thenReturn(ApiResult.exito(comentarios));

        viewModel.cargarComentarios(1L);

        assertNotNull(valor(viewModel.getComentarios()));
        assertEquals(1, valor(viewModel.getComentarios()).size());
    }

    @Test
    public void cargarFotosConErrorExponeElMensaje() {
        when(fotoRepo.listar(1L)).thenReturn(ApiResult.error(500, "Error interno"));

        viewModel.cargarFotos(1L);

        assertEquals("Error interno", valor(viewModel.getError()));
    }

    @Test
    public void eliminarLugarExitosoNotificaEliminado() {
        when(lugarRepo.eliminar(1L)).thenReturn(ApiResult.exito(null));

        viewModel.eliminar(1L);

        assertEquals(Boolean.TRUE, valor(viewModel.getLugarEliminado()));
        assertEquals(Boolean.FALSE, valor(viewModel.getCargando()));
    }

    @Test
    public void editarLugarExitosoNotificaYRecargaElLugar() {
        LugarResponse lugar = new LugarResponse(1L, "Nuevo Nombre", "PARQUE", 40.4, -3.7);
        when(lugarRepo.editar(eq(1L), any())).thenReturn(ApiResult.exito(lugar));
        when(lugarRepo.obtener(1L)).thenReturn(ApiResult.exito(lugar));

        viewModel.editarLugar(1L, "Nuevo Nombre", "Desc", "PARQUE", 40.4, -3.7);

        assertEquals(Boolean.TRUE, valor(viewModel.getLugarEditado()));
        assertNotNull(valor(viewModel.getLugar()));
        assertEquals("Nuevo Nombre", valor(viewModel.getLugar()).nombre);
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
