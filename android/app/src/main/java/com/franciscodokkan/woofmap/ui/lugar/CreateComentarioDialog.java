package com.franciscodokkan.woofmap.ui.lugar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.databinding.DialogComentarioBinding;

public class CreateComentarioDialog extends DialogFragment {

    private static final String ARG_LUGAR_ID = "lugar_id";

    private DialogComentarioBinding binding;

    public static CreateComentarioDialog newInstance(long lugarId) {
        CreateComentarioDialog f = new CreateComentarioDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_LUGAR_ID, lugarId);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogComentarioBinding.inflate(LayoutInflater.from(requireContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.comentario_titulo)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.comentario_publicar, null)
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
            if (requireActivity() instanceof LugarDetailActivity activity) {
                activity.onComentarioCreado(texto, (short) valoracion);
            }
            dialog.dismiss();
        }));

        return dialog;
    }
}
