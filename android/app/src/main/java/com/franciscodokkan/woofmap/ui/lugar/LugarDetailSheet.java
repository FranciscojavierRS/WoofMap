package com.franciscodokkan.woofmap.ui.lugar;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.BundleCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.local.TokenStore;
import com.franciscodokkan.woofmap.data.remote.ApiClient;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.data.repository.ComentarioRepository;
import com.franciscodokkan.woofmap.data.repository.FotoRepository;
import com.franciscodokkan.woofmap.data.repository.LugarRepository;
import com.franciscodokkan.woofmap.databinding.DialogEditLugarBinding;
import com.franciscodokkan.woofmap.databinding.SheetLugarDetailBinding;
import com.franciscodokkan.woofmap.ui.SimpleViewModelFactory;
import com.franciscodokkan.woofmap.ui.adapter.ComentarioAdapter;
import com.franciscodokkan.woofmap.ui.adapter.FotoAdapter;
import com.franciscodokkan.woofmap.ui.auth.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/** Detalle de un lugar como hoja inferior sobre el mapa. */
public class LugarDetailSheet extends BottomSheetDialogFragment {

    private static final String ARG_LUGAR_ID = "lugar_id";
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_DESCRIPCION = "descripcion";
    private static final String ARG_CATEGORIA = "categoria";
    private static final String ARG_LAT = "lat";
    private static final String ARG_LON = "lon";
    private static final String ARG_USUARIO_ID = "usuario_id";
    private static final String ARG_USUARIO_NOMBRE = "usuario_nombre";
    private static final String ARG_VALORACION = "valoracion";
    private static final String ARG_COMENTARIOS = "comentarios";
    private static final String ESTADO_CAMARA_URI = "camara_uri";

    // Debe coincidir con MAX_FOTOS_POR_LUGAR del backend.
    private static final int MAX_FOTOS = 20;

    public static final String RESULTADO_CAMBIO = "lugar_cambiado";

    private SheetLugarDetailBinding binding;
    private LugarDetailViewModel viewModel;
    private FotoAdapter fotoAdapter;
    private ComentarioAdapter comentarioAdapter;

    private long lugarId;
    private long usuarioIdActual;
    private long usuarioIdLugar;
    private Uri cameraTempUri;
    private boolean hojaConfigurada;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    viewModel.subirFoto(lugarId, uri, requireContext().getContentResolver(), requireContext().getCacheDir());
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (Boolean.TRUE.equals(exito) && cameraTempUri != null) {
                    viewModel.subirFoto(lugarId, cameraTempUri, requireContext().getContentResolver(), requireContext().getCacheDir());
                }
            });

    public static LugarDetailSheet newInstance(LugarResponse lugar) {
        LugarDetailSheet hoja = new LugarDetailSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_LUGAR_ID, lugar.id != null ? lugar.id : -1L);
        args.putString(ARG_NOMBRE, lugar.nombre);
        args.putString(ARG_DESCRIPCION, lugar.descripcion);
        args.putString(ARG_CATEGORIA, lugar.categoria);
        args.putDouble(ARG_LAT, lugar.lat != null ? lugar.lat : 0.0);
        args.putDouble(ARG_LON, lugar.lon != null ? lugar.lon : 0.0);
        args.putLong(ARG_USUARIO_ID, lugar.usuarioId != null ? lugar.usuarioId : -1L);
        args.putString(ARG_USUARIO_NOMBRE, lugar.usuarioNombre);
        args.putDouble(ARG_VALORACION, lugar.valoracionMedia != null ? lugar.valoracionMedia : 0.0);
        args.putLong(ARG_COMENTARIOS, lugar.totalComentarios != null ? lugar.totalComentarios : 0L);
        hoja.setArguments(args);
        return hoja;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = requireArguments();
        lugarId = args.getLong(ARG_LUGAR_ID, -1);
        usuarioIdLugar = args.getLong(ARG_USUARIO_ID, -1);
        usuarioIdActual = TokenStore.getInstance(requireContext()).getUsuarioId();
        if (savedInstanceState != null) {
            cameraTempUri = BundleCompat.getParcelable(savedInstanceState, ESTADO_CAMARA_URI, Uri.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetLugarDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();

        LugarRepository lugarRepo = new LugarRepository(ApiClient.lugares(requireContext()));
        FotoRepository fotoRepo = new FotoRepository(ApiClient.fotos(requireContext()));
        ComentarioRepository comentarioRepo = new ComentarioRepository(ApiClient.comentarios(requireContext()));
        viewModel = new ViewModelProvider(this, new SimpleViewModelFactory<>(
                () -> new LugarDetailViewModel(lugarRepo, fotoRepo, comentarioRepo)))
                .get(LugarDetailViewModel.class);

        binding.textoNombre.setText(args.getString(ARG_NOMBRE));
        binding.textoNombreCabecera.setText(args.getString(ARG_NOMBRE));
        binding.textoDescripcion.setText(args.getString(ARG_DESCRIPCION));
        pintarMeta(args.getString(ARG_CATEGORIA), args.getString(ARG_USUARIO_NOMBRE));
        pintarValoracion(args.getDouble(ARG_VALORACION, 0),
                args.getLong(ARG_COMENTARIOS, 0));
        binding.botonAnadirFoto.setText(getString(R.string.detalle_anadir_fotos, 0, MAX_FOTOS));

        setupRecyclers();
        observarViewModel();

        viewModel.cargarLugar(lugarId);
        viewModel.cargarFotos(lugarId);
        viewModel.cargarComentarios(lugarId);

        binding.botonVolver.setOnClickListener(v -> dismiss());
        binding.botonAnadirFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        binding.botonAnadirComentario.setOnClickListener(v -> mostrarDialogoComentario());

        boolean esCreador = usuarioIdActual != -1 && usuarioIdActual == usuarioIdLugar;
        binding.botonEditar.setVisibility(esCreador ? View.VISIBLE : View.GONE);
        binding.botonEliminar.setVisibility(esCreador ? View.VISIBLE : View.GONE);
        binding.botonEditar.setOnClickListener(v -> abrirEdicion());
        binding.botonEliminar.setOnClickListener(v -> confirmarEliminar());

        getParentFragmentManager().setFragmentResultListener(
                CreateComentarioDialog.RESULTADO,
                getViewLifecycleOwner(),
                (clave, resultado) -> viewModel.crearComentario(
                        lugarId,
                        resultado.getString(CreateComentarioDialog.CLAVE_TEXTO),
                        resultado.getShort(CreateComentarioDialog.CLAVE_VALORACION)));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (!(dialog instanceof BottomSheetDialog)) {
            return;
        }
        FrameLayout hoja = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (hoja != null && !hojaConfigurada) {
            BottomSheetBehavior<FrameLayout> comportamiento = BottomSheetBehavior.from(hoja);
            comportamiento.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.55f));
            comportamiento.setState(BottomSheetBehavior.STATE_COLLAPSED);
            // El peek debe cortar justo debajo del carrusel de fotos, como en los mockups.
            binding.contenedorFotos.post(() -> {
                if (binding == null) {
                    return;
                }
                int medido = binding.contenedorFotos.getBottom();
                if (medido > 0) {
                    comportamiento.setPeekHeight(medido);
                }
            });
            hojaConfigurada = true;
        }
        if (dialog.getWindow() != null) {
            // Sin fondo oscuro: el mapa sigue viendose arriba, como en Google Maps.
            dialog.getWindow().setDimAmount(0f);
        }
    }

    private void setupRecyclers() {
        int anchoFoto = (int) (getResources().getDisplayMetrics().widthPixels * 0.88f);
        fotoAdapter = new FotoAdapter(anchoFoto);
        binding.recyclerFotos.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerFotos.setAdapter(fotoAdapter);
        new PagerSnapHelper().attachToRecyclerView(binding.recyclerFotos);

        comentarioAdapter = new ComentarioAdapter();
        binding.recyclerComentarios.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerComentarios.setAdapter(comentarioAdapter);
    }

    private void observarViewModel() {
        viewModel.getFotos().observe(getViewLifecycleOwner(), lista -> {
            fotoAdapter.submitList(lista);
            boolean vacia = lista.isEmpty();
            binding.textoSinFotos.setVisibility(vacia ? View.VISIBLE : View.GONE);
            binding.recyclerFotos.setVisibility(vacia ? View.GONE : View.VISIBLE);
            binding.textoNumFotos.setVisibility(vacia ? View.GONE : View.VISIBLE);
            if (!vacia) {
                binding.textoNumFotos.setText(getResources().getQuantityString(
                        R.plurals.detalle_num_fotos, lista.size(), lista.size()));
            }
            binding.botonAnadirFoto.setText(
                    getString(R.string.detalle_anadir_fotos, lista.size(), MAX_FOTOS));
            binding.botonAnadirFoto.setEnabled(lista.size() < MAX_FOTOS);
        });
        viewModel.getComentarios().observe(getViewLifecycleOwner(), lista -> {
            comentarioAdapter.submitList(lista);
            binding.textoSinComentarios.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSubiendoFoto().observe(getViewLifecycleOwner(), subiendo ->
                binding.progresoFoto.setVisibility(Boolean.TRUE.equals(subiendo) ? View.VISIBLE : View.GONE));
        viewModel.getCargando().observe(getViewLifecycleOwner(), cargando ->
                binding.progreso.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getLugarEliminado().observe(getViewLifecycleOwner(), eliminado -> {
            if (Boolean.TRUE.equals(eliminado)) {
                getParentFragmentManager().setFragmentResult(RESULTADO_CAMBIO, new Bundle());
                dismiss();
            }
        });
        viewModel.getLugarEditado().observe(getViewLifecycleOwner(), editado -> {
            if (Boolean.TRUE.equals(editado)) {
                getParentFragmentManager().setFragmentResult(RESULTADO_CAMBIO, new Bundle());
            }
        });
        viewModel.getSesionExpirada().observe(getViewLifecycleOwner(), exp -> {
            if (Boolean.TRUE.equals(exp)) {
                TokenStore.getInstance(requireContext()).limpiar();
                Intent i = new Intent(requireContext(), LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                requireActivity().finish();
            }
        });
        viewModel.getLugar().observe(getViewLifecycleOwner(), l -> {
            if (l == null) {
                return;
            }
            binding.textoNombre.setText(l.nombre);
            binding.textoNombreCabecera.setText(l.nombre);
            binding.textoDescripcion.setText(l.descripcion != null && !l.descripcion.isEmpty()
                    ? l.descripcion : getString(R.string.detalle_sin_descripcion));
            pintarMeta(l.categoria, l.usuarioNombre);
            pintarValoracion(
                    l.valoracionMedia != null ? l.valoracionMedia : 0,
                    l.totalComentarios != null ? l.totalComentarios : 0);
        });
    }

    private void pintarMeta(String categoria, String autor) {
        String cat = categoria != null ? categoria.toLowerCase(Locale.getDefault()) : "";
        if (autor == null || autor.isEmpty()) {
            binding.textoCategoria.setText(cat);
        } else {
            binding.textoCategoria.setText(
                    getString(R.string.detalle_meta, cat, getString(R.string.detalle_por_autor, autor)));
        }
    }

    private void pintarValoracion(double media, long total) {
        if (total <= 0) {
            binding.textoValoracion.setText(R.string.detalle_sin_valoraciones);
        } else if (media > 0) {
            binding.textoValoracion.setText(getResources().getQuantityString(
                    R.plurals.detalle_valoraciones, (int) total, media, total));
        } else {
            binding.textoValoracion.setText(getResources().getQuantityString(
                    R.plurals.detalle_valoraciones_sin_media, (int) total, total));
        }
        pintarEstrellas(media);
    }

    private void pintarEstrellas(double media) {
        int llenas = (int) Math.round(media);
        for (int i = 0; i < binding.estrellas.getChildCount(); i++) {
            ImageView estrella = (ImageView) binding.estrellas.getChildAt(i);
            boolean activa = i < llenas;
            estrella.setImageResource(activa ? R.drawable.ic_estrella_llena : R.drawable.ic_estrella);
            estrella.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    activa ? R.color.woofmap_lavanda : R.color.woofmap_texto_secundario)));
        }
    }

    private void mostrarOpcionesFoto() {
        CharSequence[] opciones = {
                getString(R.string.foto_origen_galeria),
                getString(R.string.foto_origen_camara)
        };
        new AlertDialog.Builder(requireContext())
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
            File dir = new File(requireContext().getCacheDir(), "images");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("No se pudo crear el directorio de la camara");
            }
            File photoFile = File.createTempFile("camera_foto_", ".jpg", dir);
            cameraTempUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraTempUri);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(requireContext(), "Error al preparar la camara", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoComentario() {
        new CreateComentarioDialog()
                .show(getParentFragmentManager(), "comentario");
    }

    private void abrirEdicion() {
        Bundle args = requireArguments();
        LugarResponse l = viewModel.getLugar().getValue();
        String nombreActual = l != null ? l.nombre : args.getString(ARG_NOMBRE);
        String descActual = l != null ? l.descripcion : args.getString(ARG_DESCRIPCION);
        String catActual = l != null ? l.categoria : args.getString(ARG_CATEGORIA);
        double lat = l != null && l.lat != null ? l.lat : args.getDouble(ARG_LAT, 0);
        double lon = l != null && l.lon != null ? l.lon : args.getDouble(ARG_LON, 0);

        DialogEditLugarBinding editBinding = DialogEditLugarBinding.inflate(LayoutInflater.from(requireContext()));
        editBinding.campoNombreEdit.setText(nombreActual);
        editBinding.campoDescripcionEdit.setText(descActual);

        String[] categorias = getResources().getStringArray(R.array.categorias_lugar);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categorias);
        editBinding.spinnerCategoriaEdit.setAdapter(adapter);
        editBinding.spinnerCategoriaEdit.setText(catActual, false);

        new AlertDialog.Builder(requireContext())
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
                        Toast.makeText(requireContext(), R.string.error_nombre_obligatorio, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nuevaCat.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.error_categoria_obligatoria, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.editarLugar(lugarId, nuevoNombre, nuevaDesc, nuevaCat, lat, lon);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.eliminar_lugar_titulo)
                .setMessage(R.string.eliminar_lugar_mensaje)
                .setPositiveButton(R.string.eliminar_lugar_confirmar, (d, w) -> viewModel.eliminar(lugarId))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cameraTempUri != null) {
            outState.putParcelable(ESTADO_CAMARA_URI, cameraTempUri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
