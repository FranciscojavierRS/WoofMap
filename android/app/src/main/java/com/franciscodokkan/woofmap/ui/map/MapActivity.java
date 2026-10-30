package com.franciscodokkan.woofmap.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;
import com.franciscodokkan.woofmap.databinding.ActivityMapBinding;
import com.franciscodokkan.woofmap.ui.MapaEstilo;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;
import com.franciscodokkan.woofmap.ui.lugar.CreateLugarActivity;
import com.franciscodokkan.woofmap.ui.lugar.LugarDetailSheet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private static final LatLng MADRID = new LatLng(40.4168, -3.7038);
    private static final int PETICION_UBICACION = 1;

    private ActivityMapBinding binding;
    private MapView mapaView;
    private MapLibreMap mapa;
    private MapViewModel viewModel;
    private FusedLocationProviderClient clienteUbicacion;

    private boolean estiloCargado;
    private boolean datosCargados;
    private List<LugarResponse> lugares = Collections.emptyList();
    private final Map<Marker, LugarResponse> marcadores = new HashMap<>();

    private final ActivityResultLauncher<Intent> createLugarLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    viewModel.recargar();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapaView = binding.mapa;
        mapaView.onCreate(savedInstanceState);
        clienteUbicacion = LocationServices.getFusedLocationProviderClient(this);

        LugarRepository repositorio = new LugarRepository(ApiClient.lugares(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(() -> new MapViewModel(repositorio)))
                .get(MapViewModel.class);

        observarViewModel();
        configurarFiltroCategoria();
        binding.botonSalir.setOnClickListener(v -> cerrarSesion());
        binding.botonPerfil.setOnClickListener(v -> Toast.makeText(
                this, R.string.mapa_perfil_no_disponible, Toast.LENGTH_SHORT).show());
        binding.botonCrearLugar.setOnClickListener(v -> abrirCrearLugar());
        getSupportFragmentManager().setFragmentResultListener(
                LugarDetailSheet.RESULTADO_CAMBIO, this, (clave, resultado) -> viewModel.recargar());

        configurarMapa();
        pedirUbicacion();
    }

    private void configurarFiltroCategoria() {
        String[] categoriasArray = getResources().getStringArray(R.array.categorias_lugar);
        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.mapa_filtro_todos));
        Collections.addAll(opciones, categoriasArray);

        binding.botonFiltro.setText(getString(R.string.mapa_filtro_todos));
        binding.botonFiltro.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            for (int i = 0; i < opciones.size(); i++) {
                menu.getMenu().add(Menu.NONE, i, i, opciones.get(i));
            }
            menu.setOnMenuItemClickListener(item -> {
                String seleccion = opciones.get(item.getItemId());
                binding.botonFiltro.setText(seleccion);
                viewModel.setCategoria(
                        getString(R.string.mapa_filtro_todos).equalsIgnoreCase(seleccion) ? null : seleccion);
                return true;
            });
            menu.show();
        });
    }

    private void abrirCrearLugar() {
        Intent intent = new Intent(this, CreateLugarActivity.class);
        createLugarLauncher.launch(intent);
    }

    private void configurarMapa() {
        mapaView.getMapAsync(map -> {
            mapa = map;
            mapa.getUiSettings().setCompassEnabled(true);
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(MADRID, 11));
            mapa.setStyle(new Style.Builder().fromUri(MapaEstilo.URL), estilo -> {
                estiloCargado = true;
                pintarLugares();
            });

            mapa.setOnMarkerClickListener(marker -> {
                LugarResponse seleccionado = marcadores.get(marker);
                if (seleccionado != null) {
                    abrirDetalleLugar(seleccionado);
                }
                return true;
            });
        });
    }

    private void abrirDetalleLugar(LugarResponse lugar) {
        LugarDetailSheet.newInstance(lugar)
                .show(getSupportFragmentManager(), "detalle");
    }

    private void observarViewModel() {
        viewModel.getLugares().observe(this, lista -> {
            lugares = lista != null ? lista : Collections.emptyList();
            datosCargados = lista != null;
            pintarLugares();
        });
        viewModel.getCargando().observe(this, cargando -> {
            binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE);
            pintarLugares();
        });
        viewModel.getError().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getSesionExpirada().observe(this, expirada -> {
            if (Boolean.TRUE.equals(expirada)) {
                Toast.makeText(this, R.string.mapa_sesion_expirada, Toast.LENGTH_LONG).show();
                cerrarSesion();
            }
        });
    }

    private void pintarLugares() {
        if (mapa == null || !estiloCargado) {
            return;
        }
        mapa.clear();
        marcadores.clear();
        for (LugarResponse lugar : lugares) {
            if (lugar.lat == null || lugar.lon == null) {
                continue;
            }
            Marker marker = mapa.addMarker(new MarkerOptions()
                    .position(new LatLng(lugar.lat, lugar.lon))
                    .title(lugar.nombre)
                    .snippet(lugar.categoria));
            marcadores.put(marker, lugar);
        }
        binding.textoVacio.setVisibility(
                datosCargados && lugares.isEmpty()
                        && !Boolean.TRUE.equals(viewModel.getCargando().getValue())
                        ? View.VISIBLE : View.GONE);
    }

    private void pedirUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PETICION_UBICACION);
        }
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {
        clienteUbicacion.getLastLocation()
                .addOnSuccessListener(this, ubicacion -> {
                    if (ubicacion != null) {
                        if (mapa != null) {
                            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude()), 13));
                        }
                        viewModel.cargarLugares(ubicacion.getLatitude(), ubicacion.getLongitude());
                    } else {
                        viewModel.cargarLugares(null, null);
                    }
                })
                .addOnFailureListener(this, error -> {
                    Toast.makeText(this, R.string.mapa_error_ubicacion, Toast.LENGTH_SHORT).show();
                    viewModel.cargarLugares(null, null);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PETICION_UBICACION) {
            return;
        }
        boolean concedido = false;
        for (int resultado : grantResults) {
            if (resultado == PackageManager.PERMISSION_GRANTED) {
                concedido = true;
                break;
            }
        }
        if (concedido) {
            obtenerUbicacion();
        } else {
            Toast.makeText(this, R.string.mapa_permiso_denegado, Toast.LENGTH_LONG).show();
            viewModel.cargarLugares(null, null);
        }
    }

    private void cerrarSesion() {
        TokenStore.getInstance(this).limpiar();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapaView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapaView.onResume();
    }

    @Override
    protected void onPause() {
        mapaView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapaView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapaView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapaView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapaView.onLowMemory();
    }
}
