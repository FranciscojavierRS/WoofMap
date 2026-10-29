package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.ErrorResponse;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class RespuestaError {

    private static final Gson GSON = new Gson();

    private RespuestaError() {
    }

    public static String mensaje(Response<?> respuesta) {
        try (ResponseBody cuerpo = respuesta.errorBody()) {
            if (cuerpo != null) {
                ErrorResponse error = GSON.fromJson(cuerpo.charStream(), ErrorResponse.class);
                if (error != null && error.message != null && !error.message.isEmpty()) {
                    return error.message;
                }
            }
        } catch (Exception ignorada) {
            // El cuerpo no era el JSON de error esperado.
        }
        return "Error del servidor (" + respuesta.code() + ")";
    }
}
