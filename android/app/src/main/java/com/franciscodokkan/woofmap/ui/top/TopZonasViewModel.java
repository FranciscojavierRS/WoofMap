package com.franciscodokkan.woofmap.ui.top;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopZonasViewModel extends ViewModel {

    private final LugarRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<List<LugarResponse>> lugares = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);

    public TopZonasViewModel(LugarRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public TopZonasViewModel(LugarRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<List<LugarResponse>> getLugares() { return lugares; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }

    public void cargar() {
        cargando.setValue(true);
        ejecutor.execute(() -> {
            ApiResult<List<LugarResponse>> resultado = repositorio.listarTop(null);
            cargando.postValue(false);
            if (resultado.esExito()) {
                lugares.postValue(resultado.datos);
            } else if (resultado.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(resultado.error);
            }
        });
    }

    @Override
    protected void onCleared() {
        if (ejecutor instanceof ExecutorService) {
            ((ExecutorService) ejecutor).shutdown();
        }
    }
}
