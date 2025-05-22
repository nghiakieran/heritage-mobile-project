package hcmute.edu.vn.heritageproject.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.PopularMonument;
import java.util.List;

public class PopularMonumentAdapter extends RecyclerView.Adapter<PopularMonumentAdapter.MonumentViewHolder> {
    private List<PopularMonument> monuments;

    public PopularMonumentAdapter(List<PopularMonument> monuments) {
        this.monuments = monuments;
    }

    @NonNull
    @Override
    public MonumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_monument, parent, false);
        return new MonumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonumentViewHolder holder, int position) {
        PopularMonument monument = monuments.get(position);
        holder.titleTextView.setText(monument.getTitle());
        holder.descriptionTextView.setText(monument.getDescription());
        holder.imageView.setImageResource(monument.getImageResId());
    }

    @Override
    public int getItemCount() {
        return monuments.size();
    }

    static class MonumentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, descriptionTextView;

        MonumentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.monumentImage);
            titleTextView = itemView.findViewById(R.id.monumentTitle);
            descriptionTextView = itemView.findViewById(R.id.monumentDescription);
        }
    }
}