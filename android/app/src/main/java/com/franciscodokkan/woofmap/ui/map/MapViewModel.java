package com.franciscodokkan.woofmap.ui.map;

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

public class MapViewModel extends ViewModel {

    private final LugarRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<List<LugarResponse>> lugares = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);
    private final MutableLiveData<String> categoriaFiltro = new MutableLiveData<>(null);

    private Double ultimaLat;
    private Double ultimaLon;

    public MapViewModel(LugarRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public MapViewModel(LugarRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<List<LugarResponse>> getLugares() { return lugares; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }

    public void cargarLugares(Double lat, Double lon) {
        ultimaLat = lat;
        ultimaLon = lon;
        recargar();
    }

    public void setCategoria(String categoria) {
        categoriaFiltro.setValue(categoria);
        recargar();
    }

    public void recargar() {
        cargando.setValue(true);
        String categoria = categoriaFiltro.getValue();
        ejecutor.execute(() -> {
            ApiResult<List<LugarResponse>> resultado = repositorio.listar(ultimaLat, ultimaLon, categoria);
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
