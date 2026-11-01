package com.franciscodokkan.woofmap.ui.perro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.repository.PerroRepository;
import com.franciscodokkan.woofmap.databinding.ActivityPerroMesBinding;
import com.franciscodokkan.woofmap.databinding.DialogSubirPerroBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.PerroRankingAdapter;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;
import com.google.android.material.tabs.TabLayout;

public class PerroMesActivity extends AppCompatActivity {

    private ActivityPerroMesBinding binding;
    private PerroMesViewModel viewModel;
    private PerroRankingAdapter adapter;
    private String periodo = "SEMANA";

    private final ActivityResultLauncher<PickVisualMediaRequest> pickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    pedirNombreYSubir(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPerroMesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PerroRepository repositorio = new PerroRepository(ApiClient.perros(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new PerroMesViewModel(repositorio)))
                .get(PerroMesViewModel.class);

        adapter = new PerroRankingAdapter();
        binding.recyclerRanking.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRanking.setAdapter(adapter);

        binding.tabsPeriodo.addTab(binding.tabsPeriodo.newTab().setText(R.string.perro_mes_semana));
        binding.tabsPeriodo.addTab(binding.tabsPeriodo.newTab().setText(R.string.perro_mes_mes));
        binding.tabsPeriodo.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                periodo = tab.getPosition() == 0 ? "SEMANA" : "MES";
                viewModel.cargarRanking(periodo);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.botonVolver.setOnClickListener(v -> finish());
        binding.botonSubir.setOnClickListener(v -> pickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        observarViewModel();
        viewModel.cargarRanking(periodo);
    }

    private void observarViewModel() {
        viewModel.getRanking().observe(this, lista -> {
            adapter.submitList(lista);
            binding.textoVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getCargando().observe(this, cargando ->
                binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE));
        viewModel.getSubiendo().observe(this, subiendo -> {
            boolean ocupado = Boolean.TRUE.equals(subiendo);
            binding.progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
            binding.botonSubir.setEnabled(!ocupado);
        });
        viewModel.getSubido().observe(this, subido -> {
            if (Boolean.TRUE.equals(subido)) {
                Toast.makeText(this, R.string.perro_mes_subido, Toast.LENGTH_SHORT).show();
                viewModel.cargarRanking(periodo);
            }
        });
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

    private void pedirNombreYSubir(Uri uri) {
        DialogSubirPerroBinding dialogBinding = DialogSubirPerroBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.perro_mes_dialogo_titulo)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.perro_mes_dialogo_aceptar, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = dialogBinding.campoNombre.getText() != null
                    ? dialogBinding.campoNombre.getText().toString().trim() : "";
            if (nombre.isEmpty()) {
                Toast.makeText(this, R.string.error_nombre_perro, Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.subirPerro(uri, nombre, getContentResolver(), getCacheDir());
            dialog.dismiss();
        }));
        dialog.show();
    }
}
