package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritoApi {

    @GET("favoritos")
    Call<List<LugarResponse>> listar();

    @POST("favoritos/{lugarId}")
    Call<Void> anadir(@Path("lugarId") long lugarId);

    @DELETE("favoritos/{lugarId}")
    Call<Void> eliminar(@Path("lugarId") long lugarId);
}
