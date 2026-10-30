package com.franciscodokkan.woofmap.ui.lugar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;
import com.franciscodokkan.woofmap.databinding.ActivityCreateLugarBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

public class CreateLugarActivity extends AppCompatActivity {

    public static final String RESULTADO_CREADO = "lugar_creado";
    private static final String ESTILO_MAPA = "https://tiles.openfreemap.org/styles/liberty";
    private static final String ESTADO_PUNTO_LAT = "punto_lat";
    private static final String ESTADO_PUNTO_LON = "punto_lon";

    private ActivityCreateLugarBinding binding;
    private CreateLugarViewModel viewModel;
    private MapLibreMap mapa;
    private LatLng puntoSeleccionado;
    private boolean estiloCargado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        binding = ActivityCreateLugarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.crear_lugar_titulo);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LugarRepository repo = new LugarRepository(ApiClient.lugares(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new CreateLugarViewModel(repo)))
                .get(CreateLugarViewModel.class);

        if (savedInstanceState != null && savedInstanceState.containsKey(ESTADO_PUNTO_LAT)) {
            puntoSeleccionado = new LatLng(
                    savedInstanceState.getDouble(ESTADO_PUNTO_LAT),
                    savedInstanceState.getDouble(ESTADO_PUNTO_LON));
        }

        configurarCategorias();
        configurarMapa(savedInstanceState);
        observarViewModel();

        binding.botonCrear.setOnClickListener(v -> enviarFormulario());
    }

    private void configurarCategorias() {
        String[] categorias = getResources().getStringArray(R.array.categorias_lugar);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categorias);
        binding.spinnerCategoria.setAdapter(adapter);
    }

    private void configurarMapa(Bundle savedInstanceState) {
        MapView mapaView = binding.miniMapa;
        mapaView.onCreate(savedInstanceState);
        mapaView.getMapAsync(map -> {
            mapa = map;
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4168, -3.7038), 11));
            mapa.setStyle(new Style.Builder().fromUri(ESTILO_MAPA), style -> {
                estiloCargado = true;
                // Restore marker if coming back from rotation
                if (puntoSeleccionado != null) {
                    seleccionarPunto(puntoSeleccionado);
                }
            });
            mapa.addOnMapClickListener(punto -> {
                seleccionarPunto(punto);
                return true;
            });
        });
    }

    private void seleccionarPunto(LatLng punto) {
        puntoSeleccionado = punto;
        actualizarMarcador(punto);
        binding.textoCoords.setText(getString(R.string.crear_lugar_coords,
                punto.getLatitude(), punto.getLongitude()));
    }

    private void actualizarMarcador(LatLng punto) {
        if (mapa == null || !estiloCargado) return;
        mapa.clear();
        mapa.addMarker(new org.maplibre.android.annotations.MarkerOptions().position(punto));
    }

    private void enviarFormulario() {
        String nombre = binding.campoNombre.getText() != null ? binding.campoNombre.getText().toString().trim() : "";
        String descripcion = binding.campoDescripcion.getText() != null ? binding.campoDescripcion.getText().toString().trim() : "";
        String categoria = binding.spinnerCategoria.getText() != null ? binding.spinnerCategoria.getText().toString().trim() : "";

        if (nombre.isEmpty()) {
            binding.layoutNombre.setError(getString(R.string.error_nombre_obligatorio));
            return;
        }
        binding.layoutNombre.setError(null);

        if (categoria.isEmpty()) {
            Toast.makeText(this, R.string.error_categoria_obligatoria, Toast.LENGTH_SHORT).show();
            return;
        }
        if (puntoSeleccionado == null) {
            Toast.makeText(this, R.string.error_punto_obligatorio, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.crear(nombre, descripcion, categoria, puntoSeleccionado.getLatitude(), puntoSeleccionado.getLongitude());
    }

    private void observarViewModel() {
        viewModel.getCargando().observe(this, cargando -> {
            binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE);
            binding.botonCrear.setEnabled(!Boolean.TRUE.equals(cargando));
        });
        viewModel.getLugarCreado().observe(this, lugar -> {
            if (lugar != null) {
                setResult(RESULT_OK, new Intent().putExtra(RESULTADO_CREADO, true));
                finish();
            }
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
        viewModel.getSesionExpirada().observe(this, exp -> {
            if (Boolean.TRUE.equals(exp)) {
                TokenStore.getInstance(this).limpiar();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    // MapView lifecycle
    @Override protected void onStart() { super.onStart(); binding.miniMapa.onStart(); }
    @Override protected void onResume() { super.onResume(); binding.miniMapa.onResume(); }
    @Override protected void onPause() { binding.miniMapa.onPause(); super.onPause(); }
    @Override protected void onStop() { binding.miniMapa.onStop(); super.onStop(); }
    @Override protected void onDestroy() { binding.miniMapa.onDestroy(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        binding.miniMapa.onSaveInstanceState(out);
        if (puntoSeleccionado != null) {
            out.putDouble(ESTADO_PUNTO_LAT, puntoSeleccionado.getLatitude());
            out.putDouble(ESTADO_PUNTO_LON, puntoSeleccionado.getLongitude());
        }
    }
    @Override public void onLowMemory() { super.onLowMemory(); binding.miniMapa.onLowMemory(); }
}
