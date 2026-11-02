package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.ReporteApi;
import com.franciscodokkan.woofmap.data.remote.model.ReporteRequest;
import com.franciscodokkan.woofmap.data.remote.model.ReporteResponse;

import java.util.List;

public class ReporteRepository {

    private final ReporteApi api;

    public ReporteRepository(ReporteApi api) {
        this.api = api;
    }

    public ApiResult<ReporteResponse> crear(String tipo, long referenciaId, String motivo) {
        return ApiResult.ejecutar(api.crear(new ReporteRequest(tipo, referenciaId, motivo)));
    }

    public ApiResult<List<ReporteResponse>> listar() {
        return ApiResult.lista(api.listar());
    }

    public ApiResult<Void> resolver(long id) {
        return ApiResult.ejecutar(api.resolver(id));
    }

    public ApiResult<Void> eliminarContenido(String tipo, long id) {
        return ApiResult.ejecutar(api.eliminarContenido(tipo, id));
    }
}
