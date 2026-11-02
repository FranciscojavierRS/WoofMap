package com.franciscodokkan.woofmap.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.franciscodokkan.woofmap.BuildConfig;
import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.remote.model.FotoResponse;
import com.franciscodokkan.woofmap.databinding.ItemFotoBinding;

public class FotoAdapter extends ListAdapter<FotoResponse, FotoAdapter.ViewHolder> {

    public interface OnMenuListener {
        void onMenu(FotoResponse foto, View ancla);
    }

    private final int anchoItemPx;
    private final OnMenuListener listener;

    public FotoAdapter(int anchoItemPx, OnMenuListener listener) {
        super(new DiffUtil.ItemCallback<FotoResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull FotoResponse a, @NonNull FotoResponse b) {
                return a.id != null && a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull FotoResponse a, @NonNull FotoResponse b) {
                return a.id != null && a.id.equals(b.id) && a.url != null && a.url.equals(b.url);
            }
        });
        this.anchoItemPx = anchoItemPx;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFotoBinding b = ItemFotoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        b.getRoot().getLayoutParams().width = anchoItemPx;
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFotoBinding binding;

        ViewHolder(ItemFotoBinding b) {
            super(b.getRoot());
            this.binding = b;
            binding.botonMenuFoto.setOnClickListener(v -> {
                int posicion = getBindingAdapterPosition();
                if (posicion != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMenu(getItem(posicion), v);
                }
            });
        }

        void bind(FotoResponse foto) {
            String fullUrl = foto.url;
            if (fullUrl != null && !fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
                if (fullUrl.startsWith("/")) {
                    fullUrl = fullUrl.substring(1);
                }
                fullUrl = BuildConfig.API_BASE_URL + fullUrl;
            }
            Glide.with(binding.imagen.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(binding.imagen);
        }
    }
}
