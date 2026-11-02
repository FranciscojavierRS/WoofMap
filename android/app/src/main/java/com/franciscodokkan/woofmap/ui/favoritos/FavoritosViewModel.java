package com.franciscodokkan.woofmap.ui.favoritos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.FavoritoRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritosViewModel extends ViewModel {

    private final FavoritoRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<List<LugarResponse>> lugares = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public FavoritosViewModel(FavoritoRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public FavoritosViewModel(FavoritoRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<List<LugarResponse>> getLugares() { return lugares; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }
    public LiveData<String> getError() { return error; }

    public void cargar() {
        cargando.setValue(true);
        ejecutor.execute(() -> {
            ApiResult<List<LugarResponse>> res = repositorio.listar();
            cargando.postValue(false);
            if (res.esExito()) {
                lugares.postValue(res.datos);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
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
