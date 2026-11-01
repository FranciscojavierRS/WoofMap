package com.franciscodokkan.woofmap.ui.perro;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.repository.PerroRepository;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValorarPerrosViewModelTest {

    @Test
    public void votar_delegaEnElRepositorioConElSentidoDelSwipe() {
        PerroRepository repositorio = mock(PerroRepository.class);
        when(repositorio.votar(5L, true)).thenReturn(ApiResult.exito(null));

        ValorarPerrosViewModel viewModel = new ValorarPerrosViewModel(repositorio, Runnable::run);
        viewModel.votar(5L, true);

        verify(repositorio).votar(5L, true);
    }
}
