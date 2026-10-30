package com.franciscodokkan.woofmap.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.function.Supplier;

/** Fábrica para los ViewModels que solo necesitan un repositorio. */
public class SimpleViewModelFactory<T extends ViewModel> implements ViewModelProvider.Factory {

    private final Supplier<T> creador;

    public SimpleViewModelFactory(Supplier<T> creador) {
        this.creador = creador;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <V extends ViewModel> V create(@NonNull Class<V> modelClass) {
        return (V) creador.get();
    }
}
