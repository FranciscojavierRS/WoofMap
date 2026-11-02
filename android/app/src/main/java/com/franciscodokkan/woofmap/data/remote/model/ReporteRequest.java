package com.franciscodokkan.woofmap.data.remote.model;

public class ReporteRequest {
    public String tipo;
    public Long referenciaId;
    public String motivo;

    public ReporteRequest(String tipo, Long referenciaId, String motivo) {
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.motivo = motivo;
    }
}
