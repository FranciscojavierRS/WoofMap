package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.ComentarioRequest;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ComentarioApi {

    @GET("lugares/{lugarId}/comentarios")
    Call<List<ComentarioResponse>> listarComentarios(
            @Path("lugarId") long lugarId);

    @POST("lugares/{lugarId}/comentarios")
    Call<ComentarioResponse> crearComentario(
            @Path("lugarId") long lugarId,
            @Body ComentarioRequest request);

    @PUT("lugares/{lugarId}/comentarios/{id}")
    Call<ComentarioResponse> editarComentario(
            @Path("lugarId") long lugarId,
            @Path("id") long comentarioId,
            @Body ComentarioRequest request);

    @DELETE("lugares/{lugarId}/comentarios/{id}")
    Call<Void> eliminarComentario(
            @Path("lugarId") long lugarId,
            @Path("id") long comentarioId);
}
