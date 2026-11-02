package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerfilRequest;
import com.franciscodokkan.woofmap.data.remote.model.PerfilResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UsuarioApi {

    @GET("usuarios/me")
    Call<PerfilResponse> perfil();

    @PUT("usuarios/me")
    Call<PerfilResponse> actualizar(@Body PerfilRequest request);

    @GET("usuarios/me/lugares")
    Call<List<LugarResponse>> misLugares();

    @GET("usuarios/me/perros")
    Call<List<PerroResponse>> misPerros();
}
