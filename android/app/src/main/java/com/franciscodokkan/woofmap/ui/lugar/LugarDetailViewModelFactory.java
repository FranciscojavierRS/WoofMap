package com.franciscodokkan.woofmap.ui.lugar;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.data.repository.ComentarioRepository;
import com.franciscodokkan.woofmap.data.repository.FotoRepository;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;

public class LugarDetailViewModelFactory implements ViewModelProvider.Factory {

    private final LugarRepository lugarRepo;
    private final FotoRepository fotoRepo;
    private final ComentarioRepository comentarioRepo;

    public LugarDetailViewModelFactory(LugarRepository lugarRepo, FotoRepository fotoRepo, ComentarioRepository comentarioRepo) {
        this.lugarRepo = lugarRepo;
        this.fotoRepo = fotoRepo;
        this.comentarioRepo = comentarioRepo;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LugarDetailViewModel(lugarRepo, fotoRepo, comentarioRepo);
    }
}
