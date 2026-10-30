package com.franciscodokkan.woofmap.data.remote;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ApiResult<T> {

    public final T datos;
    public final int codigo;
    public final String error;

    private ApiResult(T datos, int codigo, String error) {
        this.datos = datos;
        this.codigo = codigo;
        this.error = error;
    }

    public static <T> ApiResult<T> exito(T datos) {
        return new ApiResult<>(datos, 200, null);
    }

    /** Ejecuta una llamada Retrofit y traduce el resultado al formato de la app. */
    public static <T> ApiResult<T> ejecutar(Call<T> llamada) {
        try {
            Response<T> respuesta = llamada.execute();
            if (respuesta.isSuccessful()) {
                return exito(respuesta.body());
            }
            return error(respuesta.code(), RespuestaError.mensaje(respuesta));
        } catch (IOException e) {
            return error(0, "No se pudo conectar con el servidor");
        }
    }

    /** Igual que {@link #ejecutar}, pero un cuerpo nulo se convierte en lista vacia. */
    public static <T> ApiResult<List<T>> lista(Call<List<T>> llamada) {
        ApiResult<List<T>> resultado = ejecutar(llamada);
        return resultado.esExito() && resultado.datos == null
                ? exito(Collections.emptyList())
                : resultado;
    }

    public static <T> ApiResult<T> error(int codigo, String mensaje) {
        return new ApiResult<>(null, codigo, mensaje);
    }

    public boolean esExito() {
        return error == null;
    }

    public boolean esNoAutorizado() {
        return codigo == 401;
    }
}
