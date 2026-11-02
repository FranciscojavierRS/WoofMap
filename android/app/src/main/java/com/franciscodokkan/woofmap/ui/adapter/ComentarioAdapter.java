package com.franciscodokkan.woofmap.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.franciscodokkan.woofmap.data.remote.model.ComentarioResponse;
import com.franciscodokkan.woofmap.databinding.ItemComentarioBinding;

public class ComentarioAdapter extends ListAdapter<ComentarioResponse, ComentarioAdapter.ViewHolder> {

    public interface OnMenuListener {
        void onMenu(ComentarioResponse comentario, View ancla);
    }

    private final OnMenuListener listener;

    public ComentarioAdapter(OnMenuListener listener) {
        super(new DiffUtil.ItemCallback<ComentarioResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull ComentarioResponse a, @NonNull ComentarioResponse b) {
                return a.id != null && a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull ComentarioResponse a, @NonNull ComentarioResponse b) {
                return a.id != null && a.id.equals(b.id) && a.texto != null && a.texto.equals(b.texto)
                        && a.valoracion != null && a.valoracion.equals(b.valoracion);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemComentarioBinding b = ItemComentarioBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemComentarioBinding binding;

        ViewHolder(ItemComentarioBinding b) {
            super(b.getRoot());
            this.binding = b;
            binding.botonMenuComentario.setOnClickListener(v -> {
                int posicion = getBindingAdapterPosition();
                if (posicion != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMenu(getItem(posicion), v);
                }
            });
        }

        void bind(ComentarioResponse c) {
            binding.textoComentario.setText(c.texto);
            binding.ratingBarItem.setRating(c.valoracion != null ? c.valoracion : 0);
            binding.textoUsuario.setText(binding.textoUsuario.getContext().getString(
                    com.franciscodokkan.woofmap.R.string.comentario_usuario, c.usuarioId));
        }
    }
}
