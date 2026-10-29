package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LugarApi {

    @GET("lugares")
    Call<List<LugarResponse>> listar(
            @Query("lat") Double lat,
            @Query("lon") Double lon,
            @Query("radio") Double radio);
}
