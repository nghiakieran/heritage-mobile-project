package hcmute.edu.vn.heritageproject.views;

import android.os.Bundle;
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
        setContentView(R.layout.activity_main);

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