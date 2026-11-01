package com.franciscodokkan.woofmap.ui.top;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;
import com.franciscodokkan.woofmap.databinding.ActivityTopZonasBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.TopLugarAdapter;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;

public class TopZonasActivity extends AppCompatActivity {

    private ActivityTopZonasBinding binding;
    private TopZonasViewModel viewModel;
    private TopLugarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTopZonasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LugarRepository repositorio = new LugarRepository(ApiClient.lugares(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new TopZonasViewModel(repositorio)))
                .get(TopZonasViewModel.class);

        adapter = new TopLugarAdapter();
        binding.recyclerTop.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTop.setAdapter(adapter);

        binding.botonVolver.setOnClickListener(v -> finish());
        observarViewModel();
        viewModel.cargar();
    }

    private void observarViewModel() {
        viewModel.getLugares().observe(this, lista -> {
            adapter.submitList(lista);
            binding.textoVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getCargando().observe(this, cargando ->
                binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getSesionExpirada().observe(this, expirada -> {
            if (Boolean.TRUE.equals(expirada)) {
                TokenStore.getInstance(this).limpiar();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
