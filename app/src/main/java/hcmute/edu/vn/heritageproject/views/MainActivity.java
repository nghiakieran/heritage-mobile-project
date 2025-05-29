package hcmute.edu.vn.heritageproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.views.fragments.ChatFragment;
import hcmute.edu.vn.heritageproject.views.fragments.HeritageFragment;
import hcmute.edu.vn.heritageproject.views.fragments.HomeFragment;
import hcmute.edu.vn.heritageproject.views.fragments.MapFragment;
import hcmute.edu.vn.heritageproject.views.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Set default fragment or select tab based on Intent extra
        if (savedInstanceState == null) {
            String selectTab = getIntent().getStringExtra("SELECT_TAB");
            if ("heritage".equals(selectTab)) {
                loadFragment(new HeritageFragment());
                bottomNavigationView.setSelectedItemId(R.id.navigation_heritage);
            } else {
                loadFragment(new HomeFragment());
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_heritage) {
            fragment = new HeritageFragment();
        } else if (itemId == R.id.navigation_map) {
            fragment = new MapFragment();
        } else if (itemId == R.id.navigation_chat) {
            fragment = new ChatFragment();
        } else if (itemId == R.id.navigation_profile) {
            fragment = new ProfileFragment();
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}