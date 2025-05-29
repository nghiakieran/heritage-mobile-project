package hcmute.edu.vn.heritageproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.adapters.FavoritesAdapter;
import hcmute.edu.vn.heritageproject.models.Favorites;
import hcmute.edu.vn.heritageproject.services.FavoritesService;
import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteItemClickListener {
    private static final String TAG = "FavoritesActivity";

    private RecyclerView recyclerViewFavorites;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private Button buttonAddFavorites;
    private FavoritesAdapter adapter;
    private FavoritesService favoritesService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        buttonAddFavorites = findViewById(R.id.buttonAddFavorites);

        // Initialize service
        favoritesService = FavoritesService.getInstance();

        // Setup RecyclerView
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoritesAdapter(this, new ArrayList<>(), this);
        recyclerViewFavorites.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadFavorites);

        // Setup button click listener
        buttonAddFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            intent.putExtra("SELECT_TAB", "heritage"); // Add extra to indicate selecting Heritage tab
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close FavoritesActivity
        });

        // Load data
        loadFavorites();
    }

    private void loadFavorites() {
        showLoading(true);
        favoritesService.getCurrentUserFavorites(new FavoritesService.FavoritesServiceCallback() {
            @Override
            public void onSuccess(Favorites favorites) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (favorites != null && favorites.getItems() != null && !favorites.getItems().isEmpty()) {
                    adapter.updateData(favorites.getItems());
                    recyclerViewFavorites.setVisibility(View.VISIBLE);
                    textViewEmpty.setVisibility(View.GONE);
                    buttonAddFavorites.setVisibility(View.GONE);
                } else {
                    adapter.updateData(new ArrayList<>()); // Clear the list
                    recyclerViewFavorites.setVisibility(View.GONE);
                    textViewEmpty.setVisibility(View.VISIBLE);
                    buttonAddFavorites.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(FavoritesActivity.this, error, Toast.LENGTH_SHORT).show();
                // In case of error, hide the list and show the empty state with the button
                adapter.updateData(new ArrayList<>()); // Clear the list
                recyclerViewFavorites.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
                buttonAddFavorites.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerViewFavorites.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.GONE);
            buttonAddFavorites.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(Favorites.FavoriteItem item) {
        Intent intent = new Intent(this, HeritageDetailActivity.class);
        intent.putExtra("heritageId", item.getHeritageId());
        startActivity(intent);
    }

    @Override
    public void onRemoveClick(Favorites.FavoriteItem item) {
        favoritesService.removeHeritageFromFavorites(item.getHeritageId(),
                new FavoritesService.FavoritesServiceCallback() {
                    @Override
                    public void onSuccess(Favorites favorites) {
                        Toast.makeText(FavoritesActivity.this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT)
                                .show();
                        loadFavorites(); // Reload the list
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(FavoritesActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}