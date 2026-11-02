package com.franciscodokkan.woofmap.ui.perfil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerfilResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.repository.PerfilRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends ViewModel {

    private final PerfilRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<PerfilResponse> perfil = new MutableLiveData<>();
    private final MutableLiveData<List<LugarResponse>> lugares = new MutableLiveData<>();
    private final MutableLiveData<List<PerroResponse>> perros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> guardado = new MutableLiveData<>(false);

    public ProfileViewModel(PerfilRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public ProfileViewModel(PerfilRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<PerfilResponse> getPerfil() { return perfil; }
    public LiveData<List<LugarResponse>> getLugares() { return lugares; }
    public LiveData<List<PerroResponse>> getPerros() { return perros; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getGuardado() { return guardado; }

    public void cargar() {
        cargando.setValue(true);
        ejecutor.execute(() -> {
            ApiResult<PerfilResponse> resPerfil = repositorio.perfil();
            if (!resPerfil.esExito()) {
                cargando.postValue(false);
                fallo(resPerfil);
                return;
            }
            perfil.postValue(resPerfil.datos);

            ApiResult<List<LugarResponse>> resLugares = repositorio.misLugares();
            if (resLugares.esExito()) {
                lugares.postValue(resLugares.datos);
            } else if (resLugares.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            }

            ApiResult<List<PerroResponse>> resPerros = repositorio.misPerros();
            if (resPerros.esExito()) {
                perros.postValue(resPerros.datos);
            } else if (resPerros.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            }
            cargando.postValue(false);
        });
    }

    public void guardar(String nombre) {
        cargando.setValue(true);
        guardado.setValue(false);
        ejecutor.execute(() -> {
            ApiResult<PerfilResponse> res = repositorio.actualizar(nombre);
            cargando.postValue(false);
            if (res.esExito()) {
                perfil.postValue(res.datos);
                guardado.postValue(true);
            } else {
                fallo(res);
            }
        });
    }

    private void fallo(ApiResult<?> resultado) {
        if (resultado.esNoAutorizado()) {
            sesionExpirada.postValue(true);
        } else {
            error.postValue(resultado.error);
        }
    }

    @Override
    protected void onCleared() {
        if (ejecutor instanceof ExecutorService) {
            ((ExecutorService) ejecutor).shutdown();
        }
    }
}
