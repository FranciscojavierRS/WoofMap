package com.franciscodokkan.woofmap.data.remote;

import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.remote.model.VotoRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PerroApi {

    @GET("perros/ranking")
    Call<List<PerroResponse>> ranking(@Query("periodo") String periodo);

    @GET("perros/mazo")
    Call<List<PerroResponse>> mazo();

    @Multipart
    @POST("perros")
    Call<PerroResponse> subir(
            @Part MultipartBody.Part foto,
            @Part("nombre") RequestBody nombre);

    @POST("perros/{id}/votos")
    Call<Void> votar(@Path("id") long perroId, @Body VotoRequest request);
}
