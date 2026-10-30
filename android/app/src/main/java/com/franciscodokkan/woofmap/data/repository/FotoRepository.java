package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.FotoApi;
import com.franciscodokkan.woofmap.data.remote.model.FotoResponse;

import java.util.List;

import okhttp3.MultipartBody;

public class FotoRepository {

    private final FotoApi api;

    public FotoRepository(FotoApi api) {
        this.api = api;
    }

    public ApiResult<FotoResponse> subir(long lugarId, MultipartBody.Part foto) {
        return ApiResult.ejecutar(api.subirFoto(lugarId, foto));
    }

    public ApiResult<List<FotoResponse>> listar(long lugarId) {
        return ApiResult.lista(api.listarFotos(lugarId));
    }
}
