package com.franciscodokkan.woofmap.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.data.repository.AuthRepository;

public class AuthViewModelFactory implements ViewModelProvider.Factory {

    private final AuthRepository repositorio;

    public AuthViewModelFactory(AuthRepository repositorio) {
        this.repositorio = repositorio;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(repositorio);
        }
        throw new IllegalArgumentException("ViewModel desconocido: " + modelClass.getName());
    }
}
