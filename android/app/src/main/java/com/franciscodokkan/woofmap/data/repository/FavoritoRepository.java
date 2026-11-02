package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.FavoritoApi;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import java.util.List;

public class FavoritoRepository {

    private final FavoritoApi api;

    public FavoritoRepository(FavoritoApi api) {
        this.api = api;
    }

    public ApiResult<List<LugarResponse>> listar() {
        return ApiResult.lista(api.listar());
    }

    public ApiResult<Void> anadir(long lugarId) {
        return ApiResult.ejecutar(api.anadir(lugarId));
    }

    public ApiResult<Void> eliminar(long lugarId) {
        return ApiResult.ejecutar(api.eliminar(lugarId));
    }
}
