package com.franciscodokkan.woofmap.ui;

import com.franciscodokkan.woofmap.BuildConfig;

public final class ImagenUrl {

    private ImagenUrl() {
    }

    /** Convierte las rutas relativas de la API (/uploads/...) en URLs completas. */
    public static String completa(String url) {
        if (url == null || url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        String ruta = url.startsWith("/") ? url.substring(1) : url;
        return BuildConfig.API_BASE_URL + ruta;
    }
}
