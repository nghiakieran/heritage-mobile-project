package hcmute.edu.vn.heritageproject.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.views.LoginActivity;

public class ProfileFragment extends Fragment {

    private LinearLayout layoutLoggedIn;
    private LinearLayout layoutLoggedOut;
    private Button loginButton;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private Button signOutButton;
    private FirebaseAuth mAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views
        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn);
        layoutLoggedOut = view.findViewById(R.id.layoutLoggedOut);
        loginButton = view.findViewById(R.id.loginButton);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        signOutButton = view.findViewById(R.id.signOutButton);

        // Set click listeners
        loginButton.setOnClickListener(v -> startLoginActivity());
        signOutButton.setOnClickListener(v -> signOut());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            layoutLoggedOut.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);
            userNameTextView.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            userEmailTextView.setText(user.getEmail());
        } else {
            // User is not signed in
            layoutLoggedOut.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        updateUI(null);
    }
}
