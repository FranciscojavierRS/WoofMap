package com.franciscodokkan.woofmap.ui.lugar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.LugarRequest;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateLugarViewModel extends ViewModel {

    private final LugarRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<LugarResponse> lugarCreado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);

    public CreateLugarViewModel(LugarRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public CreateLugarViewModel(LugarRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<String> getError() { return error; }
    public LiveData<LugarResponse> getLugarCreado() { return lugarCreado; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }

    public void crear(String nombre, String descripcion, String categoria, Double lat, Double lon) {
        cargando.setValue(true);
        LugarRequest request = new LugarRequest(nombre, descripcion, categoria, lat, lon);
        ejecutor.execute(() -> {
            ApiResult<LugarResponse> resultado = repositorio.crear(request);
            cargando.postValue(false);
            if (resultado.esExito()) {
                lugarCreado.postValue(resultado.datos);
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
