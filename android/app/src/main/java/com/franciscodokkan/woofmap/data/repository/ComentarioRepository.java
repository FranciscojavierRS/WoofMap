package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.ComentarioApi;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioRequest;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;

import java.util.List;

public class ComentarioRepository {

    private final ComentarioApi api;

    public ComentarioRepository(ComentarioApi api) {
        this.api = api;
    }

    public ApiResult<List<ComentarioResponse>> listar(long lugarId) {
        return ApiResult.lista(api.listarComentarios(lugarId));
    }

    public ApiResult<ComentarioResponse> crear(long lugarId, ComentarioRequest request) {
        return ApiResult.ejecutar(api.crearComentario(lugarId, request));
    }
}
