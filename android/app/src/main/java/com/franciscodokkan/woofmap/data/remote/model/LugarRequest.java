package com.franciscodokkan.woofmap.data.remote.model;

public class LugarRequest {
    public String nombre;
    public String descripcion;
    public String categoria;
    public Double lat;
    public Double lon;

    public LugarRequest(String nombre, String descripcion, String categoria, Double lat, Double lon) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.lat = lat;
        this.lon = lon;
    }
}
