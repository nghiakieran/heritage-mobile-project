package hcmute.edu.vn.heritageproject.views.adapters;

import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import android.view.View;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
    private List<String> imageUrls;
    public ImageSliderAdapter(List<String> imageUrls) { this.imageUrls = imageUrls; }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ImageViewHolder(imageView);
    }
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Glide.with(holder.imageView.getContext())
            .load(imageUrls.get(position))
            .placeholder(hcmute.edu.vn.heritageproject.R.drawable.placeholder_image)
            .error(hcmute.edu.vn.heritageproject.R.drawable.error_image)
            .into(holder.imageView);
    }
    @Override
    public int getItemCount() { return imageUrls.size(); }
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
} 