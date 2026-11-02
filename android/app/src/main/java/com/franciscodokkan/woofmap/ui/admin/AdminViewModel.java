package com.franciscodokkan.woofmap.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.ReporteResponse;
import com.franciscodokkan.woofmap.data.repository.ReporteRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminViewModel extends ViewModel {

    private final ReporteRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<List<ReporteResponse>> reportes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accionCompletada = new MutableLiveData<>(false);

    public AdminViewModel(ReporteRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public AdminViewModel(ReporteRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<List<ReporteResponse>> getReportes() { return reportes; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getAccionCompletada() { return accionCompletada; }

    public void cargar() {
        cargando.postValue(true);
        ejecutor.execute(() -> {
            ApiResult<List<ReporteResponse>> res = repositorio.listar();
            cargando.postValue(false);
            if (res.esExito()) {
                reportes.postValue(res.datos);
            } else {
                fallo(res);
            }
        });
    }

    public void resolver(long reporteId) {
        ejecutor.execute(() -> manejar(repositorio.resolver(reporteId)));
    }

    public void eliminarContenido(ReporteResponse reporte) {
        ejecutor.execute(() -> {
            ApiResult<Void> borrado = repositorio.eliminarContenido(reporte.tipo, reporte.referenciaId);
            // El contenido ya no existe: el reporte queda resuelto para no repetir la accion.
            manejar(borrado.esExito() ? repositorio.resolver(reporte.id) : borrado);
        });
    }

    private void manejar(ApiResult<Void> resultado) {
        if (resultado.esExito()) {
            accionCompletada.postValue(true);
            cargar();
        } else {
            fallo(resultado);
        }
    }

    private void fallo(ApiResult<?> resultado) {
        if (resultado.esNoAutorizado() || resultado.codigo == 403) {
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
