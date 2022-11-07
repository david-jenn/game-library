package edu.ranken.david_jenn.game_library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.data.GameConsole;
import edu.ranken.david_jenn.game_library.data.GameList;
import edu.ranken.david_jenn.game_library.ui.game.GameListAdapter;
import edu.ranken.david_jenn.game_library.ui.game.GameListViewModel;
import edu.ranken.david_jenn.game_library.ui.utils.SpinnerOption;

public class GameListActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    // Grab my views
    private TextView mainErrorMessage;
    private RecyclerView mainRecyclerView;
    private Spinner mainListSpinner;
    private Spinner mainConsoleSpinner;

    // Initialize state variables
    private GameListViewModel model;
    private GameListAdapter mainListAdapter;
    private ArrayAdapter<SpinnerOption<String>> consoleAdapter;
    private ArrayAdapter<SpinnerOption<GameList>> listFilterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_list);

        //find the views
        mainErrorMessage = findViewById(R.id.mainErrorMessage);
        mainRecyclerView = findViewById(R.id.mainGameList);
        mainConsoleSpinner = findViewById(R.id.mainConsoleSpinner);
        mainListSpinner = findViewById(R.id.mainGameSpinner);

        //create the adapter
        model = new ViewModelProvider(this).get(GameListViewModel.class);
        mainListAdapter = new GameListAdapter(this, model);

        //set up the recycler
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(mainListAdapter);

        SpinnerOption<GameList>[] gameListOptions = new SpinnerOption[]{
            new SpinnerOption(getString(R.string.allGameSpinnerOption), GameList.ALL_GAMES),
            new SpinnerOption(getString(R.string.libraryGamesSpinnerOption), GameList.MY_LIBRARY),
            new SpinnerOption(getString(R.string.wishListSpinnerOption), GameList.MY_WISH_LIST),
        };

        listFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gameListOptions);
        mainListSpinner.setAdapter(listFilterAdapter);

        //set up the observers
        model.getGames().observe(this, (games) -> {
            mainListAdapter.setGames(games);
        });
        model.getLibraryGameKeys().observe(this, (gameKeys) -> {
            mainListAdapter.setLibraryGameKeys(gameKeys);
        });
        model.getWishListGameKeys().observe(this, (gameKeys) -> {
            mainListAdapter.setWishListGameKeys(gameKeys);
        });
        model.getConsoles().observe(this, (consoles) -> {
            if (consoles != null) {
                int selectedPostion = 0;
                String selectedId = model.getFilterConsoleId();

                ArrayList<SpinnerOption<String>> consoleNames = new ArrayList<>(consoles.size());
                consoleNames.add(new SpinnerOption<>(getString(R.string.allConsolesSpinnerOption), null));

                Log.i(LOG_TAG, "size : " + consoles.size());

                for (int i = 0; i < consoles.size(); ++i) {
                    GameConsole console = consoles.get(i);
                    if (console.id != null && console.name != null) {
                        consoleNames.add(new SpinnerOption<>(console.name, console.id));
                        if (Objects.equals(console.id, selectedId)) {
                            selectedPostion = i - 1;
                        }
                    }
                }
                consoleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, consoleNames);
                mainConsoleSpinner.setAdapter(consoleAdapter);
                mainConsoleSpinner.setSelection(selectedPostion, false);
            }
        });
        model.getErrorMessage().observe(this, (errorMessage) -> {
            if (errorMessage != null) {
                mainErrorMessage.setText(errorMessage);
                mainErrorMessage.setVisibility(View.VISIBLE);
            } else {
                mainErrorMessage.setText(null);
                mainErrorMessage.setVisibility(View.GONE);
            }

        });
        model.getSnackbarMessage().observe(this, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(mainRecyclerView, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackBar();
            }

        });

        mainConsoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerOption<String> option = (SpinnerOption<String>) parent.getItemAtPosition(position);
                model.filterGamesByGenre(option.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });

        mainListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerOption<GameList> option = (SpinnerOption<GameList>) parent.getItemAtPosition(position);
                model.filterGamesByList(option.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as temporal navigation
            onBackPressed();
            return true;
        } else if (itemId == R.id.actionSignOut) {
            onSignOut();
            return true;
        } else if (itemId == R.id.actionProfileEdit) {
            displayProfileEditor();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //Do nothing, prevent default behavior
        //super.onBackPressed();
    }

    public void onSignOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener((result) -> {
                Log.i(LOG_TAG, "Signed out!");
                finish();
            });
    }

    public void displayProfileEditor() {
        Intent intent = new Intent(this, MyProfileActivity.class);
        this.startActivity(intent);
    }


}