package hcmute.edu.vn.heritageproject.views.adapters;

import android.util.Log;
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
    private static final String TAG = "HeritageAdapter";
    private final List<Heritage> heritages;
    private OnHeritageClickListener clickListener;

    public interface OnHeritageClickListener {
        void onHeritageClick(Heritage heritage);
    }

    public HeritageAdapter(List<Heritage> heritages) {
        this.heritages = heritages;
        Log.d(TAG, "HeritageAdapter created with " + heritages.size() + " items");
    }

    public void setOnHeritageClickListener(OnHeritageClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_popular_monument, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Heritage heritage = heritages.get(position);
        Log.d(TAG, "onBindViewHolder: Binding item " + position + " - " + heritage.getName());

        // Set heritage name
        if (heritage.getName() != null) {
            holder.titleTextView.setText(heritage.getName());
        } else {
            holder.titleTextView.setText("Tên không xác định");
        }

        // Set heritage location
        if (heritage.getLocation() != null) {
            holder.descriptionTextView.setText(heritage.getLocation());
        } else {
            holder.descriptionTextView.setText("Vị trí không xác định");
        }

        // Load image using Glide if available
        if (heritage.getImages() != null && !heritage.getImages().isEmpty()) {
            String imageUrl = heritage.getImages().get(0);
            Log.d(TAG, "Loading image: " + imageUrl);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(8));

            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .apply(requestOptions)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
        } else {
            Log.d(TAG, "No images available, using placeholder");
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Item clicked: " + heritage.getName());
            if (clickListener != null) {
                clickListener.onHeritageClick(heritage);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = heritages.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
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

            // Check if views are found
            if (imageView == null) {
                Log.e("HeritageAdapter", "monumentImage not found in layout!");
            }
            if (titleTextView == null) {
                Log.e("HeritageAdapter", "monumentTitle not found in layout!");
            }
            if (descriptionTextView == null) {
                Log.e("HeritageAdapter", "monumentDescription not found in layout!");
            }
        }
    }
}