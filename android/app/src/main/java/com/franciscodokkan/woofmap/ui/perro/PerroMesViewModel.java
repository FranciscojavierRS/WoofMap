package com.franciscodokkan.woofmap.ui.perro;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.repository.PerroRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PerroMesViewModel extends ViewModel {

    private final PerroRepository repositorio;
    private final Executor ejecutor;

    private final MutableLiveData<List<PerroResponse>> ranking = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> subiendo = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> subido = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);

    public PerroMesViewModel(PerroRepository repositorio) {
        this(repositorio, Executors.newSingleThreadExecutor());
    }

    public PerroMesViewModel(PerroRepository repositorio, Executor ejecutor) {
        this.repositorio = repositorio;
        this.ejecutor = ejecutor;
    }

    public LiveData<List<PerroResponse>> getRanking() { return ranking; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Boolean> getSubiendo() { return subiendo; }
    public LiveData<Boolean> getSubido() { return subido; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }

    public void cargarRanking(String periodo) {
        cargando.setValue(true);
        ejecutor.execute(() -> {
            ApiResult<List<PerroResponse>> resultado = repositorio.ranking(periodo);
            cargando.postValue(false);
            if (resultado.esExito()) {
                ranking.postValue(resultado.datos);
            } else if (resultado.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(resultado.error);
            }
        });
    }

    public void subirPerro(Uri uri, String nombre, ContentResolver resolver, File cacheDir) {
        subiendo.setValue(true);
        ejecutor.execute(() -> {
            File temporal = null;
            try {
                String mime = resolver.getType(uri);
                if (mime == null) {
                    mime = "image/jpeg";
                }
                String extension = "image/png".equals(mime) ? ".png"
                        : "image/webp".equals(mime) ? ".webp" : ".jpg";
                temporal = File.createTempFile("perro_", extension, cacheDir);
                // Copiamos a fichero para no cargar la foto entera en memoria.
                try (InputStream entrada = resolver.openInputStream(uri);
                     OutputStream salida = new FileOutputStream(temporal)) {
                    if (entrada == null) {
                        throw new IOException("No se pudo abrir la foto");
                    }
                    byte[] buffer = new byte[8192];
                    int leidos;
                    while ((leidos = entrada.read(buffer)) != -1) {
                        salida.write(buffer, 0, leidos);
                    }
                }
                RequestBody rb = RequestBody.create(temporal, MediaType.parse(mime));
                MultipartBody.Part part = MultipartBody.Part.createFormData("foto", temporal.getName(), rb);
                RequestBody rbNombre = RequestBody.create(nombre, MediaType.parse("text/plain"));
                ApiResult<PerroResponse> resultado = repositorio.subir(part, rbNombre);
                subiendo.postValue(false);
                if (resultado.esExito()) {
                    subido.postValue(true);
                } else if (resultado.esNoAutorizado()) {
                    sesionExpirada.postValue(true);
                } else {
                    error.postValue(resultado.error);
                }
            } catch (Exception e) {
                subiendo.postValue(false);
                error.postValue("Error al leer la foto");
            } finally {
                if (temporal != null) {
                    temporal.delete();
                }
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
