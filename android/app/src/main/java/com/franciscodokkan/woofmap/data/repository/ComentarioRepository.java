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

    public ApiResult<ComentarioResponse> editar(long lugarId, long comentarioId, ComentarioRequest request) {
        return ApiResult.ejecutar(api.editarComentario(lugarId, comentarioId, request));
    }

    public ApiResult<Void> eliminar(long lugarId, long comentarioId) {
        return ApiResult.ejecutar(api.eliminarComentario(lugarId, comentarioId));
    }
}
