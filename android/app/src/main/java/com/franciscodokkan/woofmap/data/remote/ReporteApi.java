package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.ReporteRequest;
import com.franciscodokkan.woofmap.data.remote.model.ReporteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReporteApi {

    @POST("reportes")
    Call<ReporteResponse> crear(@Body ReporteRequest request);

    @GET("reportes")
    Call<List<ReporteResponse>> listar();

    @PUT("reportes/{id}/resolver")
    Call<Void> resolver(@Path("id") long id);

    @DELETE("admin/contenido/{tipo}/{id}")
    Call<Void> eliminarContenido(@Path("tipo") String tipo, @Path("id") long id);
}
