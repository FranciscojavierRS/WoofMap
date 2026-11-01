package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.PerroApi;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.remote.model.VotoRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PerroRepository {

    private final PerroApi api;

    public PerroRepository(PerroApi api) {
        this.api = api;
    }

    public ApiResult<List<PerroResponse>> ranking(String periodo) {
        return ApiResult.lista(api.ranking(periodo));
    }

    public ApiResult<List<PerroResponse>> mazo() {
        return ApiResult.lista(api.mazo());
    }

    public ApiResult<PerroResponse> subir(MultipartBody.Part foto, RequestBody nombre) {
        return ApiResult.ejecutar(api.subir(foto, nombre));
    }

    public ApiResult<Void> votar(long perroId, boolean positivo) {
        return ApiResult.ejecutar(api.votar(perroId, new VotoRequest(positivo)));
    }
}
