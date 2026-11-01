package com.franciscodokkan.woofmap.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.remote.model.LugarResponse;
import com.franciscodokkan.woofmap.databinding.ItemTopLugarBinding;

import java.util.Locale;

public class TopLugarAdapter extends ListAdapter<LugarResponse, TopLugarAdapter.ViewHolder> {

    public TopLugarAdapter() {
        super(new DiffUtil.ItemCallback<LugarResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull LugarResponse a, @NonNull LugarResponse b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull LugarResponse a, @NonNull LugarResponse b) {
                return a.id != null && a.id.equals(b.id)
                        && a.nombre != null && a.nombre.equals(b.nombre)
                        && a.categoria != null && a.categoria.equals(b.categoria)
                        && a.valoracionMedia != null && a.valoracionMedia.equals(b.valoracionMedia)
                        && a.totalComentarios != null && a.totalComentarios.equals(b.totalComentarios);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopLugarBinding b = ItemTopLugarBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTopLugarBinding binding;

        ViewHolder(ItemTopLugarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LugarResponse lugar, int position) {
            var contexto = binding.getRoot().getContext();
            binding.textoPuesto.setText(contexto.getString(R.string.top_zonas_puesto, position + 1));
            binding.textoNombre.setText(lugar.nombre);
            binding.textoCategoria.setText(lugar.categoria != null
                    ? lugar.categoria.toLowerCase(Locale.getDefault())
                    : contexto.getString(R.string.top_zonas_sin_categoria));

            long total = lugar.totalComentarios != null ? lugar.totalComentarios : 0;
            if (total > 0 && lugar.valoracionMedia != null) {
                binding.textoValoracion.setText(contexto.getResources().getQuantityString(
                        R.plurals.top_zonas_valoracion, (int) total,
                        lugar.valoracionMedia, total));
            } else {
                binding.textoValoracion.setText(R.string.top_zonas_sin_valoraciones);
            }
        }
    }
}
