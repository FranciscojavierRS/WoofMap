package com.franciscodokkan.woofmap.data.repository;

import com.franciscodokkan.woofmap.data.remote.ApiResult;
import com.franciscodokkan.woofmap.data.remote.UsuarioApi;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerfilRequest;
import com.franciscodokkan.woofmap.data.remote.model.PerfilResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;

import java.util.List;

public class PerfilRepository {

    private final UsuarioApi api;

    public PerfilRepository(UsuarioApi api) {
        this.api = api;
    }

    public ApiResult<PerfilResponse> perfil() {
        return ApiResult.ejecutar(api.perfil());
    }

    public ApiResult<PerfilResponse> actualizar(String nombre) {
        return ApiResult.ejecutar(api.actualizar(new PerfilRequest(nombre)));
    }

    public ApiResult<List<LugarResponse>> misLugares() {
        return ApiResult.lista(api.misLugares());
    }

    public ApiResult<List<PerroResponse>> misPerros() {
        return ApiResult.lista(api.misPerros());
    }
}
