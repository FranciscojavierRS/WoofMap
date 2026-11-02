package com.franciscodokkan.woofmap.ui.perfil;

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
import com.franciscodokkan.woofmap.data.remote.model.PerfilResponse;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.repository.PerfilRepository;
import com.franciscodokkan.woofmap.databinding.ActivityProfileBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.PerroRankingAdapter;
import com.franciscodokkan.woofmap.ui.adapter.TopLugarAdapter;
import com.franciscodokkan.woofmap.ui.admin.AdminActivity;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;
import com.franciscodokkan.woofmap.ui.lugar.LugarDetailSheet;

import java.util.Collections;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private TopLugarAdapter lugaresAdapter;
    private PerroRankingAdapter perrosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PerfilRepository repositorio = new PerfilRepository(ApiClient.usuarios(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new ProfileViewModel(repositorio)))
                .get(ProfileViewModel.class);

        lugaresAdapter = new TopLugarAdapter(lugar -> LugarDetailSheet.newInstance(lugar)
                .show(getSupportFragmentManager(), "detalle"));
        binding.recyclerLugares.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerLugares.setAdapter(lugaresAdapter);

        perrosAdapter = new PerroRankingAdapter();
        binding.recyclerPerros.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPerros.setAdapter(perrosAdapter);

        binding.botonVolver.setOnClickListener(v -> finish());
        binding.botonGuardar.setOnClickListener(v -> guardar());
        binding.botonAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));

        observarViewModel();
        viewModel.cargar();
    }

    private void guardar() {
        String nombre = texto(binding.campoNombre.getText()).trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, R.string.registro_campos_obligatorios, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.guardar(nombre);
    }

    private void observarViewModel() {
        viewModel.getPerfil().observe(this, this::pintarPerfil);
        viewModel.getLugares().observe(this, lista -> {
            List<?> lugares = lista != null ? lista : Collections.emptyList();
            lugaresAdapter.submitList(lista != null ? lista : Collections.emptyList());
            binding.textoVacioLugares.setVisibility(lugares.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getPerros().observe(this, lista -> {
            List<PerroResponse> perros = lista != null ? lista : Collections.emptyList();
            perrosAdapter.submitList(perros);
            binding.textoVacioPerros.setVisibility(perros.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getCargando().observe(this, cargando ->
                binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getGuardado().observe(this, guardado -> {
            if (Boolean.TRUE.equals(guardado)) {
                Toast.makeText(this, R.string.perfil_guardado, Toast.LENGTH_SHORT).show();
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

    private void pintarPerfil(PerfilResponse perfil) {
        if (perfil == null) {
            return;
        }
        binding.campoNombre.setText(perfil.nombre);
        binding.campoEmail.setText(perfil.email);
        binding.textoEmail.setText(perfil.email);
        binding.botonAdmin.setVisibility(perfil.administrador ? View.VISIBLE : View.GONE);
    }

    private static String texto(CharSequence valor) {
        return valor == null ? "" : valor.toString();
    }
}
