package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.AuthResponse;
import com.franciscodokkan.woofmap.data.remote.model.LoginRequest;
import com.franciscodokkan.woofmap.data.remote.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest peticion);

    @POST("auth/register")
    Call<AuthResponse> registrar(@Body RegisterRequest peticion);
}
