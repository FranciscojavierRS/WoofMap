package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.ComentarioRequest;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ComentarioApi {

    @GET("lugares/{lugarId}/comentarios")
    Call<List<ComentarioResponse>> listarComentarios(
            @Path("lugarId") long lugarId);

    @POST("lugares/{lugarId}/comentarios")
    Call<ComentarioResponse> crearComentario(
            @Path("lugarId") long lugarId,
            @Body ComentarioRequest request);
}
