package com.franciscodokkan.woofmap.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.repository.AuthRepository;
import com.franciscodokkan.woofmap.databinding.ActivityRegisterBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.map.MapActivity;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AuthRepository repositorio =
                new AuthRepository(ApiClient.auth(this), TokenStore.getInstance(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new AuthViewModel(repositorio)))
                .get(AuthViewModel.class);

        observarViewModel();
        binding.botonCrear.setOnClickListener(v -> crearCuenta());
        binding.botonLogin.setOnClickListener(v -> finish());
    }

    private void observarViewModel() {
        viewModel.getCargando().observe(this, cargando -> {
            boolean ocupado = Boolean.TRUE.equals(cargando);
            binding.progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
            binding.botonCrear.setEnabled(!ocupado);
            binding.botonLogin.setEnabled(!ocupado);
        });
        viewModel.getError().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getAutenticado().observe(this, autenticado -> {
            if (Boolean.TRUE.equals(autenticado)) {
                irAlMapa();
            }
        });
    }

    private void crearCuenta() {
        String nombre = texto(binding.nombre.getText()).trim();
        String email = texto(binding.email.getText()).trim();
        String password = texto(binding.password.getText());
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.registro_campos_obligatorios, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.email_invalido, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8) {
            Toast.makeText(this, R.string.password_corta, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.registrar(nombre, email, password);
    }

    private static String texto(CharSequence valor) {
        return valor == null ? "" : valor.toString();
    }

    private void irAlMapa() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
