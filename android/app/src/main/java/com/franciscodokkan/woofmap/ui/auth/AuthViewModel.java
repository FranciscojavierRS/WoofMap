package com.franciscodokkan.woofmap.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.AuthResponse;
import com.franciscodokkan.woofmap.data.repository.AuthRepository;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repositorio;
    private final Executor ejecutor;
    private final boolean cerrarEjecutor;

    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> autenticado = new MutableLiveData<>(false);

    public AuthViewModel(AuthRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor(), true);
    }

    AuthViewModel(AuthRepository repositorio, Executor ejecutor) {
        this(repositorio, ejecutor, false);
    }

    private AuthViewModel(AuthRepository repositorio, Executor ejecutor, boolean cerrarEjecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
        this.cerrarEjecutor = cerrarEjecutor;
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getAutenticado() {
        return autenticado;
    }

    public void login(String email, String password) {
        ejecutar(() -> repositorio.login(email, password));
    }

    public void registrar(String nombre, String email, String password) {
        ejecutar(() -> repositorio.registrar(nombre, email, password));
    }

    private void ejecutar(Callable<ApiResult<AuthResponse>> llamada) {
        cargando.setValue(true);
        error.setValue(null);
        ejecutor.execute(() -> {
            ApiResult<AuthResponse> resultado;
            try {
                resultado = llamada.call();
            } catch (Exception e) {
                resultado = ApiResult.error(0, "Error inesperado");
            }
            cargando.postValue(false);
            if (resultado.esExito()) {
                autenticado.postValue(true);
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
