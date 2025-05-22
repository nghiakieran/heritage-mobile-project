package hcmute.edu.vn.heritageproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.PopularMonument;

public class PopularMonumentAdapter extends RecyclerView.Adapter<PopularMonumentAdapter.ViewHolder> {
    private final List<PopularMonument> monuments;

    public PopularMonumentAdapter(List<PopularMonument> monuments) {
        this.monuments = monuments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_popular_monument, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopularMonument monument = monuments.get(position);
        holder.titleTextView.setText(monument.getTitle());
        holder.descriptionTextView.setText(monument.getDescription());
        holder.imageView.setImageResource(monument.getImageResId());
    }

    @Override
    public int getItemCount() {
        return monuments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.monumentImage);
            titleTextView = itemView.findViewById(R.id.monumentTitle);
            descriptionTextView = itemView.findViewById(R.id.monumentDescription);
        }
    }
}
