package edu.ranken.david_jenn.game_library;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.ui.game.GameDetailsFragment;
import edu.ranken.david_jenn.game_library.ui.game.GameDetailsViewModel;
import edu.ranken.david_jenn.game_library.ui.review.ReviewListAdapter;

public class GameDetailsActivity extends BaseActivity {

    //constants
    private static final String LOG_TAG = GameDetailsActivity.class.getSimpleName();
    public static final String EXTRA_GAME_ID = "gameId";

    //state
    private String gameId;

    //views
    private FragmentContainerView fragmentContainer;
    private GameDetailsFragment fragment;
    private GameDetailsViewModel model;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        fragmentContainer = findViewById(R.id.fragmentContainer);
        fragment = new GameDetailsFragment();
        model = new ViewModelProvider(this).get(GameDetailsViewModel.class);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();

        if(savedInstanceState == null) {

            Intent intent = getIntent();
            String intentAction = intent.getAction();
            Uri intentData = intent.getData();

            if (intentAction == null) {
                gameId = intent.getStringExtra(EXTRA_GAME_ID);
                model.fetchGame(gameId);
            } else if (Objects.equals(intentAction, Intent.ACTION_VIEW) && intentData != null) {
                handleWebLink(intent);
            }
        } else {
            Log.i(LOG_TAG, "gameId: " + gameId);
            gameId = savedInstanceState.getString("gameId");

        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("gameId", gameId);

    }

    private void handleWebLink(Intent intent) {
        Uri uri = intent.getData();
        String path = uri.getPath();
        String prefix = "/game/";

        // parse uri path
        if (path.startsWith(prefix)) {
            int movieIdEnd = path.indexOf("/", prefix.length());
            if (movieIdEnd < 0) {
                gameId = path.substring(prefix.length());
            } else {
                gameId = path.substring(prefix.length(), movieIdEnd);
            }
        } else {
            gameId = null;
        }
        model.fetchGame(gameId);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getAction() != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(new Intent(this, LoginActivity.class));
            stackBuilder.addNextIntent(new Intent(this, HomeActivity.class));
            stackBuilder.startActivities();
        } else {
            super.onBackPressed();
        }
    }

}