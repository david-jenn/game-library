package edu.ranken.david_jenn.game_library;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.data.GameConsole;
import edu.ranken.david_jenn.game_library.ui.ebay.EbayBrowseViewModel;
import edu.ranken.david_jenn.game_library.ui.ebay.EbayListItemAdapter;
import edu.ranken.david_jenn.game_library.ui.utils.SpinnerOption;

public class EbayBrowseActivity extends BaseActivity {

    private static final String LOG_TAG = EbayBrowseActivity.class.getSimpleName();
    public static final String EXTRA_GAME_ID= "gameId";

    private TextView ebayError;
    private RecyclerView ebayRecycler;
    private Spinner ebayConsoleSpinner;
    private Switch ebayRelevanceSwitch;

    //state
    private String gameId;
    private Game game;
    private EbayBrowseViewModel model;
    private EbayListItemAdapter adapter;
    private ArrayAdapter<SpinnerOption<String>> consoleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ebay_browse);

        ebayRecycler = findViewById(R.id.ebayRecycler);
        ebayError = findViewById(R.id.ebayError);
        ebayConsoleSpinner = findViewById(R.id.ebayConsoleSpinner);
        ebayRelevanceSwitch = findViewById(R.id.ebayRelevanceSwitch);

        model = new ViewModelProvider(this).get(EbayBrowseViewModel.class);
        adapter = new EbayListItemAdapter(this, model);
        int cols = getResources().getInteger(R.integer.ebayListColumns);
        ebayRecycler.setLayoutManager(new GridLayoutManager(this, cols));
        ebayRecycler.setAdapter(adapter);

        Intent intent = getIntent();
        gameId = intent.getStringExtra(EXTRA_GAME_ID);
        model.fetchGame(gameId);

        model.getResult().observe(this, (result) -> {
            if(result != null) {
                adapter.setItemSummaries(result.itemSummaries);
            }
        });

        model.getSnackbarMessage().observe(this, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(ebayError, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackbar();
            }
        });

        model.getErrorMessage().observe(this, (errorMessage) -> {
            if(errorMessage != null) {
                ebayError.setText(errorMessage);
                ebayError.setVisibility(View.VISIBLE);
            } else {
                ebayError.setVisibility(View.GONE);
                ebayError.setText(null);
            }
        });

        model.getAuthorized().observe(this, (authorized) -> {
            Game game = model.getGame().getValue();
            if(Objects.equals(Boolean.TRUE, authorized) && game != null) {
                model.fetchResults(game.queryString);
            }
        });

        model.getGame().observe(this, (game) -> {
            Boolean authorized = model.getAuthorized().getValue();
            if (game != null && Objects.equals(Boolean.TRUE, authorized)) {
                model.fetchResults(game.queryString);
            }
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
                ebayConsoleSpinner.setAdapter(consoleAdapter);
                ebayConsoleSpinner.setSelection(selectedPostion, false);

                ebayConsoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SpinnerOption<String> option = (SpinnerOption<String>) parent.getItemAtPosition(position);
                        model.filterGamesByConsole(option.getValue());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        //Do nothing
                    }
                });

                ebayRelevanceSwitch.setOnCheckedChangeListener((view, isChecked) ->  {
                        if(isChecked) {
                            model.sortGamesByField("price");
                            ebayRelevanceSwitch.setText(R.string.spinnerPriceLabel);
                        } else {
                            model.sortGamesByField("");
                            ebayRelevanceSwitch.setText(R.string.spinnerRelevenceOption);
                        }
                });
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("consoleSelectedIndex", ebayConsoleSpinner.getSelectedItemPosition());
        outState.putBoolean("relevanceSwitchChecked", ebayRelevanceSwitch.isChecked());

    }
}