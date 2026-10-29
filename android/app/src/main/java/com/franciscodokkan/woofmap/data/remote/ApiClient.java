package com.franciscodokkan.woofmap.data.remote;

import android.content.Context;

import com.franciscodokkan.woofmap.BuildConfig;
import com.franciscodokkan.woofmap.data.local.TokenStore;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static AuthApi auth(Context contexto) {
        return get(contexto).create(AuthApi.class);
    }

    public static LugarApi lugares(Context contexto) {
        return get(contexto).create(LugarApi.class);
    }

    public static FotoApi fotos(Context contexto) {
        return get(contexto).create(FotoApi.class);
    }

    public static ComentarioApi comentarios(Context contexto) {
        return get(contexto).create(ComentarioApi.class);
    }

    private static synchronized Retrofit get(Context contexto) {
        if (retrofit == null) {
            OkHttpClient cliente = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(TokenStore.getInstance(contexto)))
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(cliente)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
