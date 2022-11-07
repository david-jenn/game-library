package edu.ranken.david_jenn.game_library;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import edu.ranken.david_jenn.game_library.ui.review.ComposeReviewViewModel;

public class ComposeReviewActivity extends BaseActivity {
    
    //constants
    private static final String LOG_TAG = ComposeReviewActivity.class.getSimpleName();
    public static final String EXTRA_GAME_ID = "gameId";

    //state
    private String gameId;
    private ComposeReviewViewModel model;

    private TextView reviewGameTitle;
    private EditText reviewInput;
    private Button reviewPublishButton;
    private TextView reviewErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_review);

        reviewGameTitle = findViewById(R.id.reviewGameTitle);
        reviewInput = findViewById(R.id.reviewInput);
        reviewPublishButton = findViewById(R.id.reviewPublishButton);
        reviewErrorMessage = findViewById(R.id.reviewErrorMessage);

        model = new ViewModelProvider(this).get(ComposeReviewViewModel.class);

        model.getGameName().observe(this, (gameName) -> {
            if (gameName == null) {
                reviewGameTitle.setText(R.string.titleNotFound);
            } else {
                reviewGameTitle.setText(gameName);
            }
        });

        model.getErrorMessage().observe(this, (error) -> {
            if(error != null) {
                reviewErrorMessage.setVisibility(View.VISIBLE);
                reviewErrorMessage.setText(error);
            } else {
                reviewErrorMessage.setVisibility(View.GONE);
                reviewErrorMessage.setText(null);
            }
        });

        model.getFinished().observe(this, (finished) -> {
            if(Objects.equals(finished, Boolean.TRUE)) {
                finish();
            }
        });

        model.getSnackbarMessage().observe(this, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(reviewGameTitle, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackbar();
            }

        });

        reviewPublishButton.setOnClickListener((view) -> {
            hideKeyboard(this, reviewPublishButton);
            String reviewText = reviewInput.getText().toString();
            model.publishReview(gameId, reviewText );
        });

        if(savedInstanceState == null) {
            Intent intent = getIntent();
            gameId = intent.getStringExtra(EXTRA_GAME_ID);
            model.fetchGame(gameId);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putString("gameId", gameId);
        outState.putString("text", reviewInput.getText().toString());
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm =
            (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}