package com.franciscodokkan.woofmap.ui.adapter;

import android.view.LayoutInflater;
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

    private final int anchoItemPx;

    public FotoAdapter(int anchoItemPx) {
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFotoBinding binding;

        ViewHolder(ItemFotoBinding b) {
            super(b.getRoot());
            this.binding = b;
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
