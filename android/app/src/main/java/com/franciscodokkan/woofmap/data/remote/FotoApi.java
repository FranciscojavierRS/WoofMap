package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.FotoResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FotoApi {

    @Multipart
    @POST("lugares/{lugarId}/fotos")
    Call<FotoResponse> subirFoto(
            @Path("lugarId") long lugarId,
            @Part MultipartBody.Part foto);

    @GET("lugares/{lugarId}/fotos")
    Call<List<FotoResponse>> listarFotos(
            @Path("lugarId") long lugarId);
}
