package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.AuthApi;
import com.franciscodokkan.woofmap.data.remote.RespuestaError;
import com.franciscodokkan.woofmap.data.remote.model.AuthResponse;
import com.franciscodokkan.woofmap.data.remote.model.LoginRequest;
import com.franciscodokkan.woofmap.data.remote.model.RegisterRequest;

import java.io.IOException;

import retrofit2.Response;

public class AuthRepository {

    private final AuthApi api;
    private final TokenStore tokenStore;

    public AuthRepository(AuthApi api, TokenStore tokenStore) {
        this.api = api;
        this.tokenStore = tokenStore;
    }

    public ApiResult<AuthResponse> login(String email, String password) {
        try {
            Response<AuthResponse> respuesta = api.login(new LoginRequest(email, password)).execute();
            return procesar(respuesta);
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    public ApiResult<AuthResponse> registrar(String nombre, String email, String password) {
        try {
            Response<AuthResponse> respuesta = api.registrar(new RegisterRequest(nombre, email, password)).execute();
            return procesar(respuesta);
        } catch (IOException e) {
            return ApiResult.error(0, "No se pudo conectar con el servidor");
        }
    }

    private ApiResult<AuthResponse> procesar(Response<AuthResponse> respuesta) {
        if (respuesta.isSuccessful() && respuesta.body() != null) {
            AuthResponse sesion = respuesta.body();
            tokenStore.guardarSesion(sesion.token, sesion.usuarioId, sesion.nombre, sesion.email);
            return ApiResult.exito(sesion);
        }
        return ApiResult.error(respuesta.code(), RespuestaError.mensaje(respuesta));
    }
}
