package com.franciscodokkan.woofmap.ui.lugar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;
import com.franciscodokkan.woofmap.databinding.DialogComentarioBinding;

public class CreateComentarioDialog extends DialogFragment {

    public static final String RESULTADO = "comentario_creado";
    public static final String CLAVE_ID = "id";
    public static final String CLAVE_TEXTO = "texto";
    public static final String CLAVE_VALORACION = "valoracion";

    private static final String ARG_ID = "comentario_id";
    private static final String ARG_TEXTO = "comentario_texto";
    private static final String ARG_VALORACION = "comentario_valoracion";

    private DialogComentarioBinding binding;

    public static CreateComentarioDialog nuevo() {
        return new CreateComentarioDialog();
    }

    public static CreateComentarioDialog editar(ComentarioResponse comentario) {
        CreateComentarioDialog dialogo = new CreateComentarioDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, comentario.id != null ? comentario.id : -1L);
        args.putString(ARG_TEXTO, comentario.texto);
        args.putShort(ARG_VALORACION, comentario.valoracion != null ? comentario.valoracion : 0);
        dialogo.setArguments(args);
        return dialogo;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogComentarioBinding.inflate(LayoutInflater.from(requireContext()));
        Bundle args = getArguments();
        long id = args != null ? args.getLong(ARG_ID, -1L) : -1L;
        boolean editando = id != -1L;
        if (args != null) {
            binding.campoTexto.setText(args.getString(ARG_TEXTO));
            binding.ratingBar.setRating(args.getShort(ARG_VALORACION, (short) 0));
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(editando ? R.string.comentario_editar_titulo : R.string.comentario_titulo)
                .setView(binding.getRoot())
                .setPositiveButton(editando ? R.string.editar_lugar_confirmar : R.string.comentario_publicar, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        // AlertDialog cierra al pulsar cualquier boton; validamos antes de cerrar.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String texto = binding.campoTexto.getText() != null
                    ? binding.campoTexto.getText().toString().trim() : "";
            int valoracion = (int) binding.ratingBar.getRating();
            if (texto.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_comentario_vacio, Toast.LENGTH_SHORT).show();
                return;
            }
            if (valoracion == 0) {
                Toast.makeText(requireContext(), R.string.error_valoracion_obligatoria, Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle resultado = new Bundle();
            resultado.putLong(CLAVE_ID, id);
            resultado.putString(CLAVE_TEXTO, texto);
            resultado.putShort(CLAVE_VALORACION, (short) valoracion);
            getParentFragmentManager().setFragmentResult(RESULTADO, resultado);
            dialog.dismiss();
        }));

        return dialog;
    }
}
