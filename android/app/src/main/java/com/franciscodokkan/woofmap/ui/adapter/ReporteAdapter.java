package com.franciscodokkan.woofmap.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.franciscodokkan.woofmap.R;
import com.franciscodokkan.woofmap.data.remote.model.ReporteResponse;
import com.franciscodokkan.woofmap.databinding.ItemReporteBinding;

public class ReporteAdapter extends ListAdapter<ReporteResponse, ReporteAdapter.ViewHolder> {

    public interface OnAccionListener {
        void onResolver(ReporteResponse reporte);
        void onEliminar(ReporteResponse reporte);
    }

    private final OnAccionListener listener;

    public ReporteAdapter(OnAccionListener listener) {
        super(new DiffUtil.ItemCallback<ReporteResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull ReporteResponse a, @NonNull ReporteResponse b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ReporteResponse a, @NonNull ReporteResponse b) {
                return a.id != null && a.id.equals(b.id) && a.resuelto == b.resuelto;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReporteBinding b = ItemReporteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemReporteBinding binding;

        ViewHolder(ItemReporteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReporteResponse reporte) {
            binding.textoTipo.setText(binding.getRoot().getContext().getString(
                    R.string.admin_reporte_tipo, reporte.tipo, reporte.referenciaId));
            binding.textoMotivo.setText(reporte.motivo);
            binding.textoAutor.setText(binding.getRoot().getContext().getString(
                    R.string.admin_reporte_autor,
                    reporte.usuarioNombre != null ? reporte.usuarioNombre : ""));

            boolean resuelto = reporte.resuelto;
            binding.botonResolver.setEnabled(!resuelto);
            binding.botonEliminar.setEnabled(!resuelto);
            binding.botonResolver.setText(resuelto ? R.string.admin_resuelto : R.string.admin_resolver);

            binding.botonResolver.setOnClickListener(v -> {
                if (listener != null && !reporte.resuelto) {
                    listener.onResolver(reporte);
                }
            });
            binding.botonEliminar.setOnClickListener(v -> {
                if (listener != null && !reporte.resuelto) {
                    listener.onEliminar(reporte);
                }
            });
        }
    }
}
