package hcmute.edu.vn.heritageproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.models.Heritage;

public class HeritageAdapter extends RecyclerView.Adapter<HeritageAdapter.ViewHolder> {
    private final List<Heritage> heritages;
    private OnHeritageClickListener clickListener;

    public interface OnHeritageClickListener {
        void onHeritageClick(Heritage heritage);
    }

    public HeritageAdapter(List<Heritage> heritages) {
        this.heritages = heritages;
    }

    public void setOnHeritageClickListener(OnHeritageClickListener listener) {
        this.clickListener = listener;
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
        Heritage heritage = heritages.get(position);
        holder.titleTextView.setText(heritage.getName());
        holder.descriptionTextView.setText(heritage.getLocation());            // Load image using Glide if available
            if (heritage.getImages() != null && !heritage.getImages().isEmpty()) {
                String imageUrl = heritage.getImages().get(0);
                
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(8));
                
                Glide.with(holder.imageView.getContext())
                        .load(imageUrl)
                        .apply(requestOptions)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.imageView);
            } else {
                // Set a default image if no images are available
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }
        
        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onHeritageClick(heritage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return heritages.size();
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
