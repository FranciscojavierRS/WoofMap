package com.franciscodokkan.woofmap.ui.lugar;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.data.repository.LugarRepository;

public class CreateLugarViewModelFactory implements ViewModelProvider.Factory {

    private final LugarRepository repositorio;

    public CreateLugarViewModelFactory(LugarRepository repositorio) {
        this.repositorio = repositorio;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CreateLugarViewModel(repositorio);
    }
}
