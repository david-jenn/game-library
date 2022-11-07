package edu.ranken.david_jenn.game_library.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.GameConsole;
import edu.ranken.david_jenn.game_library.data.GameList;
import edu.ranken.david_jenn.game_library.ui.utils.SpinnerOption;

public class GameListFragment extends Fragment {

    private static final String LOG_TAG = GameListFragment.class.getSimpleName();
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

    public GameListFragment() { super(R.layout.game_list); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //find the views
        mainErrorMessage = view.findViewById(R.id.mainErrorMessage);
        mainRecyclerView = view.findViewById(R.id.mainGameList);
        mainConsoleSpinner = view.findViewById(R.id.mainConsoleSpinner);
        mainListSpinner = view.findViewById(R.id.mainGameSpinner);

        //get activity and lifecycle owner
        FragmentActivity activity = getActivity();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        //create the adapter
        model = new ViewModelProvider(activity).get(GameListViewModel.class);
        mainListAdapter = new GameListAdapter(activity, model);

        //set up the recycler

        int cols = this.getResources().getInteger(R.integer.gameListColumns);
        mainRecyclerView.setLayoutManager(new GridLayoutManager(activity, cols));
        mainRecyclerView.setAdapter(mainListAdapter);

        SpinnerOption<GameList>[] gameListOptions = new SpinnerOption[]{
            new SpinnerOption(getString(R.string.allGameSpinnerOption), GameList.ALL_GAMES),
            new SpinnerOption(getString(R.string.libraryGamesSpinnerOption), GameList.MY_LIBRARY),
            new SpinnerOption(getString(R.string.wishListSpinnerOption), GameList.MY_WISH_LIST),
        };

        listFilterAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, gameListOptions);
        mainListSpinner.setAdapter(listFilterAdapter);

        if(savedInstanceState != null) {
           int listSelectedIndex = savedInstanceState.getInt("listSelectedIndex");
           int consoleSelectedIndex = savedInstanceState.getInt("consoleSelectedIndex");
           mainListSpinner.setSelection(listSelectedIndex);
           mainConsoleSpinner.setSelection(consoleSelectedIndex);
        }

        //set up the observers
        model.getGames().observe(lifecycleOwner, (games) -> {
            mainListAdapter.setGames(games);
        });
        model.getLibraryGameKeys().observe(lifecycleOwner, (gameKeys) -> {
            mainListAdapter.setLibraryGameKeys(gameKeys);
        });
        model.getWishListGameKeys().observe(lifecycleOwner, (gameKeys) -> {
            mainListAdapter.setWishListGameKeys(gameKeys);
        });
        model.getConsoles().observe(lifecycleOwner, (consoles) -> {
            if (consoles != null) {
                int selectedPostion = 0;
                String selectedId = model.getFilterConsoleId();

                ArrayList<SpinnerOption<String>> consoleNames = new ArrayList<>(consoles.size());
                consoleNames.add(new SpinnerOption<>(getString(R.string.allConsolesSpinnerOption), null));

                for (int i = 0; i < consoles.size(); ++i) {
                    GameConsole console = consoles.get(i);
                    if (console.id != null && console.name != null) {
                        consoleNames.add(new SpinnerOption<>(console.name, console.id));
                        if (Objects.equals(console.id, selectedId)) {
                            selectedPostion = consoleNames.size() - 1;
                        }
                    }
                }
                consoleAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, consoleNames);
                mainConsoleSpinner.setAdapter(consoleAdapter);
                mainConsoleSpinner.setSelection(selectedPostion, false);
            }
        });
        model.getErrorMessage().observe(lifecycleOwner, (errorMessage) -> {
            if (errorMessage != null) {
                mainErrorMessage.setText(errorMessage);
                mainErrorMessage.setVisibility(View.VISIBLE);
            } else {
                mainErrorMessage.setText(null);
                mainErrorMessage.setVisibility(View.GONE);
            }
        });
        model.getSnackbarMessage().observe(lifecycleOwner, (snackbarMessage) -> {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putInt("consoleSelectedIndex", mainConsoleSpinner.getSelectedItemPosition());
        outState.putInt("listSelectedIndex", mainListSpinner.getSelectedItemPosition());

    }
}
