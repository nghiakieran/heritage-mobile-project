package hcmute.edu.vn.heritageproject.views;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.PopularMonument;
import hcmute.edu.vn.heritageproject.repository.MonumentRepository;

import java.util.List;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup RecyclerView for popular monuments
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPopularMonuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Load data from repository
        MonumentRepository repository = new MonumentRepository();
        List<PopularMonument> monuments = repository.getPopularMonuments();
        PopularMonumentAdapter adapter = new PopularMonumentAdapter(monuments);
        recyclerView.setAdapter(adapter);
    }
}