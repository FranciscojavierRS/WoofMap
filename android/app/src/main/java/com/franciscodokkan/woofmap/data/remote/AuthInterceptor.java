package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.local.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public Response intercept(Chain cadena) throws IOException {
        Request peticion = cadena.request();
        String token = tokenStore.getToken();
        // Las rutas de autenticación no llevan token: un 401 ahí son credenciales
        // incorrectas, no una sesión caducada.
        if (token == null || peticion.url().encodedPath().startsWith("/auth/")) {
            return cadena.proceed(peticion);
        }
        return cadena.proceed(peticion.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build());
    }
}
