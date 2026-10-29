package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.LugarApi;
import com.franciscodokkan.woofmap.data.remote.RespuestaError;
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
        try {
            Double radio = (lat != null && lon != null) ? RADIO_METROS : null;
            Response<List<LugarResponse>> respuesta = api.listar(lat, lon, radio).execute();
            if (respuesta.isSuccessful()) {
                List<LugarResponse> lugares = respuesta.body();
                return ApiResult.exito(lugares != null ? lugares : Collections.emptyList());
            }
            return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }
}
