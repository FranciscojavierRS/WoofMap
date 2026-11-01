package com.franciscodokkan.woofmap.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.remote.model.PerroResponse;
import com.franciscodokkan.woofmap.databinding.ItemPerroRankingBinding;
import com.franciscodokkan.woofmap.ui.ImagenUrl;

public class PerroRankingAdapter extends ListAdapter<PerroResponse, PerroRankingAdapter.ViewHolder> {

    public PerroRankingAdapter() {
        super(new DiffUtil.ItemCallback<PerroResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull PerroResponse a, @NonNull PerroResponse b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PerroResponse a, @NonNull PerroResponse b) {
                return a.id != null && a.id.equals(b.id)
                        && a.likes == b.likes
                        && a.dislikes == b.dislikes
                        && a.nombre != null && a.nombre.equals(b.nombre);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPerroRankingBinding b = ItemPerroRankingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPerroRankingBinding binding;

        ViewHolder(ItemPerroRankingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PerroResponse perro, int position) {
            var contexto = binding.getRoot().getContext();
            binding.textoPuesto.setText(contexto.getString(R.string.top_zonas_puesto, position + 1));
            binding.textoNombre.setText(perro.nombre);
            binding.textoDueno.setText(contexto.getString(
                    R.string.perro_mes_de,
                    perro.usuarioNombre != null ? perro.usuarioNombre : ""));
            binding.textoLikes.setText(contexto.getString(R.string.perro_mes_likes, perro.likes));
            binding.textoDislikes.setText(contexto.getString(R.string.perro_mes_dislikes, perro.dislikes));

            Glide.with(contexto)
                    .load(ImagenUrl.completa(perro.url))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(binding.imagenPerro);
        }
    }
}
