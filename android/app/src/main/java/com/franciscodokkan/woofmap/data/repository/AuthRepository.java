package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.AuthApi;
import com.franciscodokkan.woofmap.data.remote.model.AuthResponse;
import com.franciscodokkan.woofmap.data.remote.model.LoginRequest;
import com.franciscodokkan.woofmap.data.remote.model.RegisterRequest;

public class AuthRepository {

    private final AuthApi api;
    private final TokenStore tokenStore;

    public AuthRepository(AuthApi api, TokenStore tokenStore) {
        this.api = api;
        this.tokenStore = tokenStore;
    }

    public ApiResult<AuthResponse> login(String email, String password) {
        return guardarSesion(ApiResult.ejecutar(api.login(new LoginRequest(email, password))));
    }

    public ApiResult<AuthResponse> registrar(String nombre, String email, String password) {
        return guardarSesion(ApiResult.ejecutar(api.registrar(new RegisterRequest(nombre, email, password))));
    }

    private ApiResult<AuthResponse> guardarSesion(ApiResult<AuthResponse> resultado) {
        if (resultado.esExito() && resultado.datos != null) {
            AuthResponse sesion = resultado.datos;
            tokenStore.guardarSesion(sesion.token, sesion.usuarioId);
        }
        return resultado;
    }
}
