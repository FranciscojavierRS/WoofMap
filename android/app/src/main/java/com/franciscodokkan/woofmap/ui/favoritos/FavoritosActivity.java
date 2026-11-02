package com.franciscodokkan.woofmap.ui.favoritos;

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
import com.franciscodokkan.woofmap.data.repository.FavoritoRepository;
import com.franciscodokkan.woofmap.databinding.ActivityFavoritosBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.TopLugarAdapter;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;

public class FavoritosActivity extends AppCompatActivity {

    public static final String EXTRA_LUGAR_ID = "lugar_id";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LON = "lon";

    private ActivityFavoritosBinding binding;
    private FavoritosViewModel viewModel;
    private TopLugarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(
                () -> new FavoritosViewModel(new FavoritoRepository(ApiClient.favoritos(this)))))
                .get(FavoritosViewModel.class);

        adapter = new TopLugarAdapter(lugar -> {
            Intent resultado = new Intent();
            resultado.putExtra(EXTRA_LUGAR_ID, lugar.id);
            if (lugar.lat != null && lugar.lon != null) {
                resultado.putExtra(EXTRA_LAT, lugar.lat);
                resultado.putExtra(EXTRA_LON, lugar.lon);
            }
            setResult(RESULT_OK, resultado);
            finish();
        });
        binding.recyclerFavoritos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFavoritos.setAdapter(adapter);

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
