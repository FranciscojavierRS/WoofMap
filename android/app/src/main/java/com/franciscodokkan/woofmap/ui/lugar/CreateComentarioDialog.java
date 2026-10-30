package com.franciscodokkan.woofmap.ui.lugar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.databinding.DialogComentarioBinding;

public class CreateComentarioDialog extends DialogFragment {

    public static final String RESULTADO = "comentario_creado";
    public static final String CLAVE_TEXTO = "texto";
    public static final String CLAVE_VALORACION = "valoracion";

    private DialogComentarioBinding binding;

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
            Bundle resultado = new Bundle();
            resultado.putString(CLAVE_TEXTO, texto);
            resultado.putShort(CLAVE_VALORACION, (short) valoracion);
            getParentFragmentManager().setFragmentResult(RESULTADO, resultado);
            dialog.dismiss();
        }));

        return dialog;
    }
}
