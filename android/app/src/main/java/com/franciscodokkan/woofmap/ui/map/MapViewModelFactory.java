package com.franciscodokkan.woofmap.ui.map;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.data.repository.LugarRepository;

public class MapViewModelFactory implements ViewModelProvider.Factory {

    private final LugarRepository repositorio;

    public MapViewModelFactory(LugarRepository repositorio) {
        this.repositorio = repositorio;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MapViewModel.class)) {
            return (T) new MapViewModel(repositorio);
        }
        throw new IllegalArgumentException("ViewModel desconocido: " + modelClass.getName());
    }
}
