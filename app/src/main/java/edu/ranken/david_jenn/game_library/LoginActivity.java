package edu.ranken.david_jenn.game_library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    //views
    private Button loginButton;

    //state
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginButton = findViewById(R.id.loginButton);

        // Register a callback for when the sign in process is complete
        signInLauncher =
            registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                (result) -> onSignInResult(result)
            );

        loginButton.setOnClickListener((view) -> {

            List<AuthUI.IdpConfig> providers = new ArrayList<>();
            providers.add(new AuthUI.IdpConfig.EmailBuilder().build());
            providers.add(new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create sign-in intent
            Intent signInIntent =
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.Theme_GameLibrary)
                    .setAvailableProviders(providers)
                    .build();

            // Launch sign-in activity
            signInLauncher.launch(signInIntent);

        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            onLoginSuccess(user);
        } else {
            loginButton.performClick();
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.i(LOG_TAG, "sign-in successful: " + user.getUid());
            onLoginSuccess(user);
        } else {
            if(result.getIdpResponse() != null) {
                FirebaseUiException error = result.getIdpResponse().getError();
                Log.e(LOG_TAG, "sign-in failed", error);
            }
        }
    }

    private void onLoginSuccess(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        String displayName = user.getDisplayName();
        Uri photoUrl = user.getPhotoUrl();
        String photoUrlString = photoUrl != null ? photoUrl.toString() : null;

        HashMap<String, Object> update = new HashMap<>();
        update.put("profilePhoto", photoUrlString);
        update.put("lastLogin", FieldValue.serverTimestamp());
        update.put("displayName", displayName);

        db.collection("users")
            .document(userId)
            .set(update, SetOptions.merge())
            .addOnSuccessListener((result) -> {
                Log.i(LOG_TAG, "User profile updated");
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            })
            .addOnFailureListener((error) -> {
               Log.e(LOG_TAG, "Failed to update user profile", error);
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            });
    }
}