package com.franciscodokkan.woofmap.data.remote.model;

public class LugarResponse {

    public Long id;
    public String nombre;
    public String descripcion;
    public String categoria;
    public Double lat;
    public Double lon;
    public Long usuarioId;
    public String fechaCreacion;
    public Double distanciaMetros;
    public Double valoracionMedia;
    public Long totalComentarios;

    public LugarResponse() {
    }

    public LugarResponse(Long id, String nombre, String categoria, Double lat, Double lon) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.lat = lat;
        this.lon = lon;
    }
}
