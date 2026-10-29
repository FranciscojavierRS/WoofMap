package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.FotoApi;
import com.franciscodokkan.woofmap.data.remote.RespuestaError;
import com.franciscodokkan.woofmap.data.remote.model.FotoResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Response;

public class FotoRepository {

    private final FotoApi api;

    public FotoRepository(FotoApi api) {
        this.api = api;
    }

    public ApiResult<FotoResponse> subir(long lugarId, MultipartBody.Part foto) {
        try {
            Response<FotoResponse> respuesta = api.subirFoto(lugarId, foto).execute();
            if (respuesta.isSuccessful()) {
                return ApiResult.exito(respuesta.body());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    public ApiResult<List<FotoResponse>> listar(long lugarId) {
        try {
            Response<List<FotoResponse>> respuesta = api.listarFotos(lugarId).execute();
            if (respuesta.isSuccessful()) {
                List<FotoResponse> fotos = respuesta.body();
                return ApiResult.exito(fotos != null ? fotos : Collections.emptyList());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }
}
