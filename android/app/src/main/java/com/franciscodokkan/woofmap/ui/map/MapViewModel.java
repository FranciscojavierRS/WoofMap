package com.franciscodokkan.woofmap.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapViewModel extends ViewModel {

    private final LugarRepository repositorio;
    private final Executor ejecutor;
    private final boolean cerrarEjecutor;

    private final MutableLiveData<List<LugarResponse>> lugares =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);

    public MapViewModel(LugarRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor(), true);
    }

    MapViewModel(LugarRepository repositorio, Executor ejecutor) {
        this(repositorio, ejecutor, false);
    }

    private MapViewModel(LugarRepository repositorio, Executor ejecutor, boolean cerrarEjecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
        this.cerrarEjecutor = cerrarEjecutor;
    }

    public LiveData<List<LugarResponse>> getLugares() {
        return lugares;
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getSesionExpirada() {
        return sesionExpirada;
    }

    public void cargarLugares(Double lat, Double lon) {
        cargando.setValue(true);
        error.setValue(null);
        ejecutor.execute(() -> {
            ApiResult<List<LugarResponse>> resultado;
            try {
                resultado = repositorio.listar(lat, lon);
            } catch (Exception e) {
                resultado = ApiResult.error(0, "Error inesperado");
            }
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
        if (cerrarEjecutor && ejecutor instanceof ExecutorService) {
            ((ExecutorService) ejecutor).shutdown();
        }
    }
}
