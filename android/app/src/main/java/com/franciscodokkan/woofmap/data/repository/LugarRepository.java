package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.LugarApi;
import com.franciscodokkan.woofmap.data.remote.RespuestaError;
import com.franciscodokkan.woofmap.data.remote.model.LugarRequest;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;

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
        try {
            Double radio = (lat != null && lon != null) ? RADIO_METROS : null;
            Response<List<LugarResponse>> respuesta = api.listar(lat, lon, radio, categoria).execute();
            if (respuesta.isSuccessful()) {
                List<LugarResponse> lugares = respuesta.body();
                return ApiResult.exito(lugares != null ? lugares : Collections.emptyList());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    public ApiResult<LugarResponse> crear(LugarRequest request) {
        try {
            Response<LugarResponse> respuesta = api.crear(request).execute();
            if (respuesta.isSuccessful()) {
                return ApiResult.exito(respuesta.body());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    public ApiResult<LugarResponse> editar(long id, LugarRequest request) {
        try {
            Response<LugarResponse> respuesta = api.editar(id, request).execute();
            if (respuesta.isSuccessful()) {
                return ApiResult.exito(respuesta.body());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    public ApiResult<Void> eliminar(long id) {
        try {
            Response<Void> respuesta = api.eliminar(id).execute();
            if (respuesta.isSuccessful()) {
                return ApiResult.exito(null);
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }
}
