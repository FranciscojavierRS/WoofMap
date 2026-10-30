package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.LugarApi;
import com.franciscodokkan.woofmap.data.remote.model.LugarRequest;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import java.util.List;

public class LugarRepository {

    private static final double RADIO_METROS = 10_000;

    private final LugarApi api;

    public LugarRepository(LugarApi api) {
        this.api = api;
    }

    public ApiResult<List<LugarResponse>> listar(Double lat, Double lon) {
        return listar(lat, lon, null);
    }

    public ApiResult<List<LugarResponse>> listar(Double lat, Double lon, String categoria) {
        Double radio = (lat != null && lon != null) ? RADIO_METROS : null;
        return ApiResult.lista(api.listar(lat, lon, radio, categoria));
    }

    public ApiResult<LugarResponse> crear(LugarRequest request) {
        return ApiResult.ejecutar(api.crear(request));
    }

    public ApiResult<LugarResponse> obtener(long id) {
        return ApiResult.ejecutar(api.obtener(id));
    }

    public ApiResult<LugarResponse> editar(long id, LugarRequest request) {
        return ApiResult.ejecutar(api.editar(id, request));
    }

    public ApiResult<Void> eliminar(long id) {
        return ApiResult.ejecutar(api.eliminar(id));
    }
}
