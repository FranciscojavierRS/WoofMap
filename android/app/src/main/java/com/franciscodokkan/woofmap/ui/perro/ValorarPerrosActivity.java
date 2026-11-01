package com.franciscodokkan.woofmap.ui.perro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.data.repository.PerroRepository;
import com.franciscodokkan.woofmap.databinding.ActivityValorarPerrosBinding;
import com.franciscodokkan.woofmap.ui.ImagenUrl;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;

import java.util.Collections;
import java.util.List;

public class ValorarPerrosActivity extends AppCompatActivity {

    private ActivityValorarPerrosBinding binding;
    private ValorarPerrosViewModel viewModel;

    private List<PerroResponse> mazo = Collections.emptyList();
    private int indice;
    private float xInicial;
    private boolean votando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityValorarPerrosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PerroRepository repositorio = new PerroRepository(ApiClient.perros(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new ValorarPerrosViewModel(repositorio)))
                .get(ValorarPerrosViewModel.class);

        binding.botonVolver.setOnClickListener(v -> finish());
        binding.botonLike.setOnClickListener(v -> votar(true));
        binding.botonDislike.setOnClickListener(v -> votar(false));
        configurarArrastre();

        observarViewModel();
        viewModel.cargarMazo();
    }

    private void configurarArrastre() {
        binding.carta.setOnTouchListener((v, event) -> {
            if (votando) {
                return false;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    xInicial = event.getRawX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - xInicial;
                    binding.carta.setTranslationX(dx);
                    binding.carta.setRotation(dx / Math.max(binding.carta.getWidth(), 1) * 12);
                    return true;
                case MotionEvent.ACTION_UP:
                    float desplazamiento = event.getRawX() - xInicial;
                    float umbral = 100 * getResources().getDisplayMetrics().density;
                    if (Math.abs(desplazamiento) > umbral) {
                        votar(desplazamiento > 0);
                    } else {
                        volverAlCentro();
                    }
                    return true;
                default:
                    return false;
            }
        });
    }

    private void observarViewModel() {
        viewModel.getMazo().observe(this, lista -> {
            mazo = lista != null ? lista : Collections.emptyList();
            indice = 0;
            mostrarSiguiente();
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

    private void mostrarSiguiente() {
        binding.carta.setTranslationX(0);
        binding.carta.setRotation(0);
        binding.carta.setAlpha(1);

        if (indice >= mazo.size()) {
            binding.carta.setVisibility(View.GONE);
            binding.botones.setVisibility(View.GONE);
            binding.textoVacio.setVisibility(View.VISIBLE);
            return;
        }

        binding.carta.setVisibility(View.VISIBLE);
        binding.botones.setVisibility(View.VISIBLE);
        binding.textoVacio.setVisibility(View.GONE);

        PerroResponse perro = mazo.get(indice);
        binding.textoNombre.setText(perro.nombre);
        Glide.with(this)
                .load(ImagenUrl.completa(perro.url))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(binding.imagenPerro);
    }

    private void votar(boolean positivo) {
        if (votando || indice >= mazo.size()) {
            return;
        }
        votando = true;
        PerroResponse perro = mazo.get(indice);
        float destino = (positivo ? 1 : -1)
                * Math.max(binding.carta.getWidth(), binding.getRoot().getWidth()) * 1.5f;

        binding.carta.animate()
                .translationX(destino)
                .rotation(positivo ? 18 : -18)
                .alpha(0)
                .setDuration(220)
                .withEndAction(() -> {
                    if (perro.id != null) {
                        viewModel.votar(perro.id, positivo);
                    }
                    indice++;
                    votando = false;
                    mostrarSiguiente();
                })
                .start();
    }

    private void volverAlCentro() {
        binding.carta.animate()
                .translationX(0)
                .rotation(0)
                .setDuration(150)
                .start();
    }
}
