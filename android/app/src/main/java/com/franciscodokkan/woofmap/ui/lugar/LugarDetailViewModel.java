package com.franciscodokkan.woofmap.ui.lugar;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioRequest;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;
import com.franciscodokkan.woofmap.data.remote.model.FotoResponse;
import com.franciscodokkan.woofmap.data.remote.model.LugarRequest;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.ComentarioRepository;
import com.franciscodokkan.woofmap.data.repository.FavoritoRepository;
import com.franciscodokkan.woofmap.data.repository.FotoRepository;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;
import com.franciscodokkan.woofmap.data.repository.ReporteRepository;

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

public class LugarDetailViewModel extends ViewModel {

    private final LugarRepository lugarRepo;
    private final FotoRepository fotoRepo;
    private final ComentarioRepository comentarioRepo;
    private final FavoritoRepository favoritoRepo;
    private final ReporteRepository reporteRepo;
    private final Executor ejecutor;

    private final MutableLiveData<LugarResponse> lugar = new MutableLiveData<>();
    private final MutableLiveData<List<FotoResponse>> fotos = new MutableLiveData<>();
    private final MutableLiveData<List<ComentarioResponse>> comentarios = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> subiendoFoto = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lugarEliminado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> lugarEditado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sesionExpirada = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> favorito = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> reporteEnviado = new MutableLiveData<>(false);

    public LugarDetailViewModel(LugarRepository lugarRepo, FotoRepository fotoRepo,
                                ComentarioRepository comentarioRepo, FavoritoRepository favoritoRepo,
                                ReporteRepository reporteRepo) {
        this(lugarRepo, fotoRepo, comentarioRepo, favoritoRepo, reporteRepo,
                Executors.newSingleThreadExecutor());
    }

    public LugarDetailViewModel(LugarRepository lugarRepo, FotoRepository fotoRepo,
                                ComentarioRepository comentarioRepo, FavoritoRepository favoritoRepo,
                                ReporteRepository reporteRepo, Executor ejecutor) {
        this.lugarRepo = lugarRepo;
        this.fotoRepo = fotoRepo;
        this.comentarioRepo = comentarioRepo;
        this.favoritoRepo = favoritoRepo;
        this.reporteRepo = reporteRepo;
        this.ejecutor = ejecutor;
    }

    public LiveData<LugarResponse> getLugar() { return lugar; }
    public LiveData<List<FotoResponse>> getFotos() { return fotos; }
    public LiveData<List<ComentarioResponse>> getComentarios() { return comentarios; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Boolean> getSubiendoFoto() { return subiendoFoto; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getLugarEliminado() { return lugarEliminado; }
    public LiveData<Boolean> getLugarEditado() { return lugarEditado; }
    public LiveData<Boolean> getSesionExpirada() { return sesionExpirada; }
    public LiveData<Boolean> getFavorito() { return favorito; }
    public LiveData<Boolean> getReporteEnviado() { return reporteEnviado; }

    public void cargarFotos(long lugarId) {
        ejecutor.execute(() -> {
            ApiResult<List<FotoResponse>> res = fotoRepo.listar(lugarId);
            if (res.esExito()) {
                fotos.postValue(res.datos);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void cargarLugar(long lugarId) {
        ejecutor.execute(() -> {
            ApiResult<LugarResponse> res = lugarRepo.obtener(lugarId);
            if (res.esExito()) {
                lugar.postValue(res.datos);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void cargarComentarios(long lugarId) {
        ejecutor.execute(() -> {
            ApiResult<List<ComentarioResponse>> res = comentarioRepo.listar(lugarId);
            if (res.esExito()) {
                comentarios.postValue(res.datos);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void subirFoto(long lugarId, Uri uri, ContentResolver resolver, File cacheDir) {
        subiendoFoto.setValue(true);
        ejecutor.execute(() -> {
            File temporal = null;
            try {
                String mime = resolver.getType(uri);
                if (mime == null) {
                    mime = "image/jpeg";
                }
                String extension = "image/png".equals(mime) ? ".png"
                        : "image/webp".equals(mime) ? ".webp" : ".jpg";
                temporal = File.createTempFile("subida_", extension, cacheDir);
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
                ApiResult<FotoResponse> res = fotoRepo.subir(lugarId, part);
                subiendoFoto.postValue(false);
                if (res.esExito()) {
                    cargarFotos(lugarId);
                } else if (res.esNoAutorizado()) {
                    sesionExpirada.postValue(true);
                } else {
                    error.postValue(res.error);
                }
            } catch (Exception e) {
                subiendoFoto.postValue(false);
                error.postValue("Error al leer la foto");
            } finally {
                if (temporal != null) {
                    temporal.delete();
                }
            }
        });
    }

    public void crearComentario(long lugarId, String texto, short valoracion) {
        ejecutor.execute(() -> {
            ComentarioRequest req = new ComentarioRequest(texto, valoracion);
            ApiResult<ComentarioResponse> res = comentarioRepo.crear(lugarId, req);
            if (res.esExito()) {
                cargarComentarios(lugarId);
                cargarLugar(lugarId);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void eliminar(long lugarId) {
        cargando.setValue(true);
        ejecutor.execute(() -> {
            ApiResult<Void> res = lugarRepo.eliminar(lugarId);
            cargando.postValue(false);
            if (res.esExito()) {
                lugarEliminado.postValue(true);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void editarLugar(long lugarId, String nombre, String descripcion, String categoria, Double lat, Double lon) {
        cargando.setValue(true);
        LugarRequest req = new LugarRequest(nombre, descripcion, categoria, lat, lon);
        ejecutor.execute(() -> {
            ApiResult<LugarResponse> res = lugarRepo.editar(lugarId, req);
            cargando.postValue(false);
            if (res.esExito()) {
                lugarEditado.postValue(true);
                // La respuesta del PUT llega sin valoracion; recargamos para no borrarla en pantalla.
                cargarLugar(lugarId);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void editarComentario(long lugarId, long comentarioId, String texto, short valoracion) {
        ejecutor.execute(() -> {
            ApiResult<ComentarioResponse> res = comentarioRepo.editar(
                    lugarId, comentarioId, new ComentarioRequest(texto, valoracion));
            if (res.esExito()) {
                cargarComentarios(lugarId);
                cargarLugar(lugarId);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void eliminarComentario(long lugarId, long comentarioId) {
        ejecutor.execute(() -> {
            ApiResult<Void> res = comentarioRepo.eliminar(lugarId, comentarioId);
            if (res.esExito()) {
                cargarComentarios(lugarId);
                cargarLugar(lugarId);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void eliminarFoto(long lugarId, long fotoId) {
        ejecutor.execute(() -> {
            ApiResult<Void> res = fotoRepo.eliminar(lugarId, fotoId);
            if (res.esExito()) {
                cargarFotos(lugarId);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void cargarFavorito(long lugarId) {
        ejecutor.execute(() -> {
            ApiResult<List<com.franciscodokkan.woofmap.data.remote.model.LugarResponse>> res =
                    favoritoRepo.listar();
            if (res.esExito()) {
                boolean marcado = res.datos != null && res.datos.stream()
                        .anyMatch(lugar -> lugar.id != null && lugar.id == lugarId);
                favorito.postValue(marcado);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            }
        });
    }

    public void alternarFavorito(long lugarId) {
        boolean actual = Boolean.TRUE.equals(favorito.getValue());
        ejecutor.execute(() -> {
            ApiResult<Void> res = actual ? favoritoRepo.eliminar(lugarId) : favoritoRepo.anadir(lugarId);
            if (res.esExito()) {
                favorito.postValue(!actual);
            } else if (res.esNoAutorizado()) {
                sesionExpirada.postValue(true);
            } else {
                error.postValue(res.error);
            }
        });
    }

    public void reportar(String tipo, long referenciaId, String motivo) {
        ejecutor.execute(() -> {
            ApiResult<?> res = reporteRepo.crear(tipo, referenciaId, motivo);
            if (res.esExito()) {
                reporteEnviado.postValue(true);
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
