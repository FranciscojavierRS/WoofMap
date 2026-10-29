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

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.comentario_titulo)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.comentario_publicar, (d, w) -> {
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
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
