package com.franciscodokkan.woofmap.ui.lugar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.ComentarioRepository;
import com.franciscodokkan.woofmap.data.repository.FotoRepository;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;
import com.franciscodokkan.woofmap.databinding.ActivityLugarDetailBinding;
import com.franciscodokkan.woofmap.databinding.DialogEditLugarBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.ComentarioAdapter;
import com.franciscodokkan.woofmap.ui.adapter.FotoAdapter;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;

import java.io.File;
import java.io.IOException;

public class LugarDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LUGAR_ID = "extra_lugar_id";
    public static final String EXTRA_LUGAR_NOMBRE = "extra_lugar_nombre";
    public static final String EXTRA_LUGAR_DESCRIPCION = "extra_lugar_descripcion";
    public static final String EXTRA_LUGAR_CATEGORIA = "extra_lugar_categoria";
    public static final String EXTRA_LUGAR_LAT = "extra_lugar_lat";
    public static final String EXTRA_LUGAR_LON = "extra_lugar_lon";
    public static final String EXTRA_LUGAR_USUARIO_ID = "extra_lugar_usuario_id";
    public static final String EXTRA_LUGAR_VALORACION = "extra_lugar_valoracion";
    public static final String EXTRA_LUGAR_COMENTARIOS = "extra_lugar_comentarios";
    public static final String RESULTADO_ELIMINADO = "lugar_eliminado";
    public static final String RESULTADO_EDITADO = "lugar_editado";

    private ActivityLugarDetailBinding binding;
    private LugarDetailViewModel viewModel;
    private FotoAdapter fotoAdapter;
    private ComentarioAdapter comentarioAdapter;

    private long lugarId;
    private long usuarioIdActual;
    private long usuarioIdLugar;
    private Uri cameraTempUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    viewModel.subirFoto(lugarId, uri, getContentResolver(), getCacheDir());
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (Boolean.TRUE.equals(exito) && cameraTempUri != null) {
                    viewModel.subirFoto(lugarId, cameraTempUri, getContentResolver(), getCacheDir());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLugarDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Extras
        lugarId = getIntent().getLongExtra(EXTRA_LUGAR_ID, -1);
        String nombre = getIntent().getStringExtra(EXTRA_LUGAR_NOMBRE);
        String descripcion = getIntent().getStringExtra(EXTRA_LUGAR_DESCRIPCION);
        String categoria = getIntent().getStringExtra(EXTRA_LUGAR_CATEGORIA);
        double lat = getIntent().getDoubleExtra(EXTRA_LUGAR_LAT, 0);
        double lon = getIntent().getDoubleExtra(EXTRA_LUGAR_LON, 0);
        usuarioIdLugar = getIntent().getLongExtra(EXTRA_LUGAR_USUARIO_ID, -1);
        double valoracion = getIntent().getDoubleExtra(EXTRA_LUGAR_VALORACION, 0);
        long totalComentarios = getIntent().getLongExtra(EXTRA_LUGAR_COMENTARIOS, 0);

        // Recover current user from TokenStore
        usuarioIdActual = TokenStore.getInstance(this).getUsuarioId();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nombre != null ? nombre : getString(R.string.detalle_titulo));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ViewModel
        LugarRepository lugarRepo = new LugarRepository(ApiClient.lugares(this));
        FotoRepository fotoRepo = new FotoRepository(ApiClient.fotos(this));
        ComentarioRepository comentarioRepo = new ComentarioRepository(ApiClient.comentarios(this));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(
                () -> new LugarDetailViewModel(lugarRepo, fotoRepo, comentarioRepo)))
                .get(LugarDetailViewModel.class);

        // Populate initial static info
        binding.textoNombre.setText(nombre);
        binding.textoCategoria.setText(categoria);
        binding.textoDescripcion.setText(descripcion != null && !descripcion.isEmpty() ? descripcion : getString(R.string.detalle_sin_descripcion));
        binding.textoValoracion.setText(valoracion > 0
                ? getString(R.string.detalle_valoracion, valoracion, totalComentarios)
                : getString(R.string.detalle_sin_valoracion));

        setupRecyclers();
        observarViewModel();

        // Load async data
        viewModel.cargarLugar(lugarId);
        viewModel.cargarFotos(lugarId);
        viewModel.cargarComentarios(lugarId);

        binding.botonAnadirFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        binding.botonAnadirComentario.setOnClickListener(v -> mostrarDialogoComentario());

        // Hide edit/delete if not owner
        boolean esCreador = usuarioIdActual != -1 && usuarioIdActual == usuarioIdLugar;
        binding.botonEditar.setVisibility(esCreador ? View.VISIBLE : View.GONE);
        binding.botonEliminar.setVisibility(esCreador ? View.VISIBLE : View.GONE);

        binding.botonEditar.setOnClickListener(v -> abrirEdicion());
        binding.botonEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void setupRecyclers() {
        fotoAdapter = new FotoAdapter();
        binding.recyclerFotos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerFotos.setAdapter(fotoAdapter);

        comentarioAdapter = new ComentarioAdapter();
        binding.recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComentarios.setAdapter(comentarioAdapter);
    }

    private void observarViewModel() {
        viewModel.getFotos().observe(this, lista -> {
            fotoAdapter.submitList(lista);
            binding.textoSinFotos.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getComentarios().observe(this, lista -> {
            comentarioAdapter.submitList(lista);
            binding.textoSinComentarios.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSubiendoFoto().observe(this, subiendo ->
                binding.progresoFoto.setVisibility(Boolean.TRUE.equals(subiendo) ? View.VISIBLE : View.GONE));
        viewModel.getCargando().observe(this, cargando ->
                binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
        viewModel.getLugarEliminado().observe(this, eliminado -> {
            if (Boolean.TRUE.equals(eliminado)) {
                setResult(RESULT_OK, new Intent().putExtra(RESULTADO_ELIMINADO, true));
                finish();
            }
        });
        viewModel.getSesionExpirada().observe(this, exp -> {
            if (Boolean.TRUE.equals(exp)) {
                TokenStore.getInstance(this).limpiar();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
        viewModel.getLugar().observe(this, l -> {
            if (l != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(l.nombre);
                }
                binding.textoNombre.setText(l.nombre);
                binding.textoCategoria.setText(l.categoria);
                binding.textoDescripcion.setText(l.descripcion != null && !l.descripcion.isEmpty()
                        ? l.descripcion : getString(R.string.detalle_sin_descripcion));
                double v = l.valoracionMedia != null ? l.valoracionMedia : 0;
                long c = l.totalComentarios != null ? l.totalComentarios : 0;
                binding.textoValoracion.setText(v > 0
                        ? getString(R.string.detalle_valoracion, v, c)
                        : getString(R.string.detalle_sin_valoracion));

                setResult(RESULT_OK, new Intent().putExtra(RESULTADO_EDITADO, true));
            }
        });
    }

    private void mostrarOpcionesFoto() {
        CharSequence[] opciones = {
                getString(R.string.foto_origen_galeria),
                getString(R.string.foto_origen_camara)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.foto_origen_titulo)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        pickerLauncher.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else if (which == 1) {
                        tomarFotoCamara();
                    }
                })
                .show();
    }

    private void tomarFotoCamara() {
        try {
            File dir = new File(getCacheDir(), "images");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("No se pudo crear el directorio de la cámara");
            }
            File photoFile = File.createTempFile("camera_foto_", ".jpg", dir);
            cameraTempUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraTempUri);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(this, "Error al preparar la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoComentario() {
        CreateComentarioDialog.newInstance(lugarId)
                .show(getSupportFragmentManager(), "comentario");
    }

    private void abrirEdicion() {
        LugarResponse l = viewModel.getLugar().getValue();
        String nombreActual = l != null ? l.nombre : getIntent().getStringExtra(EXTRA_LUGAR_NOMBRE);
        String descActual = l != null ? l.descripcion : getIntent().getStringExtra(EXTRA_LUGAR_DESCRIPCION);
        String catActual = l != null ? l.categoria : getIntent().getStringExtra(EXTRA_LUGAR_CATEGORIA);
        double lat = l != null && l.lat != null ? l.lat : getIntent().getDoubleExtra(EXTRA_LUGAR_LAT, 0);
        double lon = l != null && l.lon != null ? l.lon : getIntent().getDoubleExtra(EXTRA_LUGAR_LON, 0);

        DialogEditLugarBinding editBinding = DialogEditLugarBinding.inflate(LayoutInflater.from(this));
        editBinding.campoNombreEdit.setText(nombreActual);
        editBinding.campoDescripcionEdit.setText(descActual);

        String[] categorias = getResources().getStringArray(R.array.categorias_lugar);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categorias);
        editBinding.spinnerCategoriaEdit.setAdapter(adapter);
        editBinding.spinnerCategoriaEdit.setText(catActual, false);

        new AlertDialog.Builder(this)
                .setTitle(R.string.editar_lugar_titulo)
                .setView(editBinding.getRoot())
                .setPositiveButton(R.string.editar_lugar_confirmar, (d, w) -> {
                    String nuevoNombre = editBinding.campoNombreEdit.getText() != null
                            ? editBinding.campoNombreEdit.getText().toString().trim() : "";
                    String nuevaDesc = editBinding.campoDescripcionEdit.getText() != null
                            ? editBinding.campoDescripcionEdit.getText().toString().trim() : "";
                    String nuevaCat = editBinding.spinnerCategoriaEdit.getText() != null
                            ? editBinding.spinnerCategoriaEdit.getText().toString().trim() : "";

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, R.string.error_nombre_obligatorio, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nuevaCat.isEmpty()) {
                        Toast.makeText(this, R.string.error_categoria_obligatoria, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.editarLugar(lugarId, nuevoNombre, nuevaDesc, nuevaCat, lat, lon);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar_lugar_titulo)
                .setMessage(R.string.eliminar_lugar_mensaje)
                .setPositiveButton(R.string.eliminar_lugar_confirmar, (d, w) -> viewModel.eliminar(lugarId))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // Callback from dialog
    public void onComentarioCreado(String texto, short valoracion) {
        viewModel.crearComentario(lugarId, texto, valoracion);
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
