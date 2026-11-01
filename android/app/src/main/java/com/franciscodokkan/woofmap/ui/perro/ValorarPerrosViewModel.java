package com.franciscodokkan.woofmap.ui.perro;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.repository.PerroRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValorarPerrosViewModel extends ViewModel {

    private final PerroRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<List<PerroResponse>> mazo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);

    public ValorarPerrosViewModel(PerroRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public ValorarPerrosViewModel(PerroRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<List<PerroResponse>> getMazo() { return mazo; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }

    public void cargarMazo() {
        cargando.setValue(true);
        ejecutor.execute(() -> {
            ApiResult<List<PerroResponse>> resultado = repositorio.mazo();
            cargando.postValue(false);
            if (resultado.esExito()) {
                mazo.postValue(resultado.datos);
            } else if (resultado.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(resultado.error);
            }
        });
    }

    public void votar(long perroId, boolean positivo) {
        ejecutor.execute(() -> {
            ApiResult<Void> resultado = repositorio.votar(perroId, positivo);
            if (resultado.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else if (!resultado.esExito()) {
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
