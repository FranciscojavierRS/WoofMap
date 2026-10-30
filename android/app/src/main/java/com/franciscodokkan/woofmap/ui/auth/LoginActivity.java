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
import com.franciscodokkan.woofmap.databinding.ActivityLoginBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.map.MapActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenStore tokenStore = TokenStore.getInstance(this);
        if (tokenStore.haySesion()) {
            irAlMapa();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AuthRepository repositorio = new AuthRepository(ApiClient.auth(this), tokenStore);
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new AuthViewModel(repositorio)))
                .get(AuthViewModel.class);

        observarViewModel();
        binding.botonEntrar.setOnClickListener(v -> entrar());
        binding.botonRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void observarViewModel() {
        viewModel.getCargando().observe(this, cargando -> {
            boolean ocupado = Boolean.TRUE.equals(cargando);
            binding.progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
            binding.botonEntrar.setEnabled(!ocupado);
            binding.botonRegistro.setEnabled(!ocupado);
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

    private void entrar() {
        String email = texto(binding.email.getText()).trim();
        String password = texto(binding.password.getText());
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_campos_obligatorios, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.email_invalido, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.login(email, password);
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
