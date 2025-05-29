package hcmute.edu.vn.heritageproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.models.Favorites;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONException;

public class FavoritesAdapter
        extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    private final Context context;
    private List<Favorites.FavoriteItem> favoriteItemList;
    private final OnFavoriteItemClickListener listener;

    public interface OnFavoriteItemClickListener {
        void onItemClick(Favorites.FavoriteItem item);

        void onRemoveClick(Favorites.FavoriteItem item);
    }

    public FavoritesAdapter(
            Context context,
            List<Favorites.FavoriteItem> favoriteItemList,
            OnFavoriteItemClickListener listener) {
        this.context = context;
        this.favoriteItemList = favoriteItemList;
        this.listener = listener;
    }

    public void updateData(List<Favorites.FavoriteItem> newFavoriteItemList) {
        this.favoriteItemList = newFavoriteItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorites.FavoriteItem currentItem = favoriteItemList.get(position);

        // Use correct TextView IDs from layout
        holder.textViewHeritageName.setText("Đang tải..."); // Placeholder text
        holder.textViewLocation.setText(""); // Clear previous data
        holder.textViewAddedDate.setText("Đã thêm vào: " + currentItem.getAddedAt().toDate().toString());

        // Fetch heritage details using OkHttpClient via HeritageApiService
        HeritageApiService.getInstance().getHeritageById(currentItem.getHeritageId(),
                new HeritageApiService.ApiCallback<HeritageResponse>() {
                    @Override
                    public void onSuccess(HeritageResponse result) {
                        // Run UI updates on the main thread
                        if (context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                if (result != null && result.getHeritages() != null
                                        && !result.getHeritages().isEmpty()) {
                                    Heritage heritage = result.getHeritages().get(0);
                                    holder.textViewHeritageName.setText(heritage.getName());
                                    holder.textViewLocation.setText(heritage.getLocation());

                                    // Load image using Glide
                                    if (heritage.getImages() != null && !heritage.getImages().isEmpty()) {
                                        Glide.with(context).load(heritage.getImages().get(0))
                                                .placeholder(R.drawable.placeholder_image)
                                                .into(holder.imageViewHeritage);
                                    } else {
                                        holder.imageViewHeritage.setImageResource(R.drawable.placeholder_image);
                                    }

                                } else {
                                    holder.textViewHeritageName.setText("Không tìm thấy thông tin");
                                    holder.textViewLocation.setText("");
                                    holder.textViewAddedDate.setText(""); // Clear added date on failure
                                    holder.imageViewHeritage.setImageResource(R.drawable.error_image);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // Run UI updates on the main thread
                        if (context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                holder.textViewHeritageName.setText("Lỗi tải dữ liệu");
                                holder.textViewLocation.setText("Vui lòng thử lại");
                                holder.textViewAddedDate.setText(""); // Clear added date on error
                                holder.imageViewHeritage.setImageResource(R.drawable.error_image);
                            });
                        }
                    }
                });

        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onItemClick(currentItem));
        holder.buttonRemove.setOnClickListener(v -> listener.onRemoveClick(currentItem));
    }

    @Override
    public int getItemCount() {
        return favoriteItemList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewHeritage;
        TextView textViewHeritageName;
        TextView textViewLocation; // Corrected ID
        TextView textViewAddedDate; // Corrected ID
        ImageButton buttonRemove;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewHeritage = itemView.findViewById(R.id.imageViewHeritage);
            textViewHeritageName = itemView.findViewById(R.id.textViewHeritageName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation); // Corrected ID
            textViewAddedDate = itemView.findViewById(R.id.textViewAddedDate); // Corrected ID
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}