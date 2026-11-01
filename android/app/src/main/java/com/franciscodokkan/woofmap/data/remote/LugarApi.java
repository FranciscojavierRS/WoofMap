package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.LugarRequest;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LugarApi {

    @GET("lugares")
    Call<List<LugarResponse>> listar(
            @Query("lat") Double lat,
            @Query("lon") Double lon,
            @Query("radio") Double radio,
            @Query("categoria") String categoria);

    @GET("lugares/top")
    Call<List<LugarResponse>> top(
            @Query("categoria") String categoria,
            @Query("limite") Integer limite);

    @POST("lugares")
    Call<LugarResponse> crear(@Body LugarRequest request);

    @GET("lugares/{id}")
    Call<LugarResponse> obtener(@Path("id") long id);

    @PUT("lugares/{id}")
    Call<LugarResponse> editar(
            @Path("id") long id,
            @Body LugarRequest request);

    @DELETE("lugares/{id}")
    Call<Void> eliminar(@Path("id") long id);
}
