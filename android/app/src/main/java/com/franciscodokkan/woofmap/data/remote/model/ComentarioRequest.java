package com.franciscodokkan.woofmap.data.remote.model;

public class ComentarioRequest {
    public String texto;
    public Short valoracion;

    public ComentarioRequest(String texto, Short valoracion) {
        this.texto = texto;
        this.valoracion = valoracion;
    }
}
