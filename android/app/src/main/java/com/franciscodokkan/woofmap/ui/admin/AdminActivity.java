package com.franciscodokkan.woofmap.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.remote.model.ReporteResponse;
import com.franciscodokkan.woofmap.data.repository.ReporteRepository;
import com.franciscodokkan.woofmap.databinding.ActivityAdminBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.ReporteAdapter;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private AdminViewModel viewModel;
    private ReporteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(
                () -> new AdminViewModel(new ReporteRepository(ApiClient.reportes(this)))))
                .get(AdminViewModel.class);

        adapter = new ReporteAdapter(new ReporteAdapter.OnAccionListener() {
            @Override
            public void onResolver(ReporteResponse reporte) {
                viewModel.resolver(reporte.id);
            }

            @Override
            public void onEliminar(ReporteResponse reporte) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setMessage(R.string.admin_eliminar_confirmar)
                        .setPositiveButton(R.string.eliminar_lugar_confirmar,
                                (d, w) -> viewModel.eliminarContenido(reporte))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        binding.recyclerReportes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReportes.setAdapter(adapter);

        binding.botonVolver.setOnClickListener(v -> finish());
        observarViewModel();
        viewModel.cargar();
    }

    private void observarViewModel() {
        viewModel.getReportes().observe(this, lista -> {
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
        viewModel.getAccionCompletada().observe(this, completada -> {
            if (Boolean.TRUE.equals(completada)) {
                Toast.makeText(this, R.string.admin_accion_ok, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getSesionExpirada().observe(this, expirada -> {
            if (Boolean.TRUE.equals(expirada)) {
                Toast.makeText(this, R.string.admin_sin_permiso, Toast.LENGTH_LONG).show();
                TokenStore.getInstance(this).limpiar();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
