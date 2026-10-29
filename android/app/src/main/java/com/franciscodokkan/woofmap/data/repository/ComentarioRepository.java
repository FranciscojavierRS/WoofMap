package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.ComentarioApi;
import com.franciscodokkan.woofmap.data.remote.RespuestaError;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioRequest;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;

public class ComentarioRepository {

    private final ComentarioApi api;

    public ComentarioRepository(ComentarioApi api) {
        this.api = api;
    }

    public ApiResult<List<ComentarioResponse>> listar(long lugarId) {
        try {
            Response<List<ComentarioResponse>> respuesta = api.listarComentarios(lugarId).execute();
            if (respuesta.isSuccessful()) {
                List<ComentarioResponse> lista = respuesta.body();
                return ApiResult.exito(lista != null ? lista : Collections.emptyList());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    public ApiResult<ComentarioResponse> crear(long lugarId, ComentarioRequest request) {
        try {
            Response<ComentarioResponse> respuesta = api.crearComentario(lugarId, request).execute();
            if (respuesta.isSuccessful()) {
                return ApiResult.exito(respuesta.body());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }
}
