package com.franciscodokkan.woofmap.data.remote;

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
