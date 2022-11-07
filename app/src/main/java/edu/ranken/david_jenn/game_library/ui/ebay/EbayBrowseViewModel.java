package edu.ranken.david_jenn.game_library.ui.ebay;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.AuthAPI;
import edu.ranken.david_jenn.game_library.data.AuthEnvironment;
import edu.ranken.david_jenn.game_library.data.EbayBrowseAPI;
import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.data.GameConsole;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EbayBrowseViewModel extends ViewModel {

    private static final String LOG_TAG = EbayBrowseViewModel.class.getSimpleName();
    private static final String GAME_CATEGORY = "139973";

    private final FirebaseFirestore db;

    private final AuthAPI authAPI;
    private final EbayBrowseAPI browseApi;
    private final ListenerRegistration consoleRegistration;
    private String authToken;
    private String filterConsoleId = null;
    private String sortGameId = null;

    private final MutableLiveData<Boolean> authorized;
    private final MutableLiveData<EbayBrowseAPI.SearchResponse> result;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Game> game;
    private final MutableLiveData<List<GameConsole>> consoles;

    public EbayBrowseViewModel() {
        db = FirebaseFirestore.getInstance();
        authAPI = new AuthAPI(AuthEnvironment.PRODUCTION, "PaulSmit-explorer-PRD-dba622b8c-288c89fc", "PRD-ba622b8c7a63-7c21-4cef-8a9f-55df");
        browseApi = new EbayBrowseAPI(AuthEnvironment.PRODUCTION);

        authorized = new MutableLiveData<>(false);
        snackbarMessage = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        result = new MutableLiveData<>(null);
        game = new MutableLiveData<>(null);
        consoles = new MutableLiveData<>(null);

        getAuthToken();

        consoleRegistration =
            db.collection("consoles")
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting consoles");
                        snackbarMessage.postValue(R.string.errorGettingConsoles);
                    } else {
                        Log.i(LOG_TAG, "Consoles updated");
                        List<GameConsole> newConsoles = querySnapshot.toObjects(GameConsole.class);
                        consoles.postValue(newConsoles);
                    }
                });
    }

    public LiveData<Integer> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<EbayBrowseAPI.SearchResponse> getResult() {
        return result;
    }

    public LiveData<Boolean> getAuthorized() { return authorized; }

    public LiveData<Game> getGame() { return game; }

    public LiveData<List<GameConsole>> getConsoles() {
        return consoles;
    }

    public String getFilterConsoleId() {
        return filterConsoleId;
    }

    public void clearSnackbar() {
        snackbarMessage.postValue(null);
    }

    private void getAuthToken() {

        authAPI.authenticateAsync(
            (authResponse) -> {
                Log.i(LOG_TAG, "Authenticated.");
                authToken = authResponse.access_token;
                authorized.postValue(Boolean.TRUE);
                Log.i(LOG_TAG, "TOKEN: " + authResponse.access_token);

            },
            (ex) -> {
                Log.e(LOG_TAG, "Auth Error.", ex);
                snackbarMessage.postValue(R.string.authError);
                errorMessage.postValue(R.string.authError);
            }
        );
    }

    public void fetchResults(String queryString) {
        if(filterConsoleId != null) {
            queryString = queryString + " " + filterConsoleId;
        }
        String sort = "";
        if(sortGameId != null) {
            sort = sortGameId;
        }
        String filter = "";
        if(game.getValue().maxPrice != null || game.getValue().minPrice != null) {
            filter = "price:[" + game.getValue().minPrice + ".." + game.getValue().maxPrice + "],priceCurrency:USD";
        }

        if (authToken != null) {
            Log.i(LOG_TAG, "AuthToken: " + authToken );
            browseApi.searchAsync(
                authToken, queryString, 10, GAME_CATEGORY, sort, filter,
                new Callback<EbayBrowseAPI.SearchResponse>() {
                    @Override
                    public void onResponse(Call<EbayBrowseAPI.SearchResponse> call, Response<EbayBrowseAPI.SearchResponse> response) {
                        if(response.isSuccessful()) {

                            Log.i(LOG_TAG, "Success!");
                            result.postValue(response.body());
                            EbayBrowseAPI.SearchResponse thisResponse;
                            thisResponse = response.body();
                            if (thisResponse == null) {
                                Log.i(LOG_TAG, "Null response");
                                errorMessage.postValue(R.string.ebayListingError);
                            } else {
                                errorMessage.postValue(null);
                            }
                        } else {
                            Log.e(LOG_TAG, "Error getting Ebay Listings");

                            int statusCode = response.code();
                            Integer messageId = getErrorMessageForStatusCode(statusCode);
                            errorMessage.postValue(messageId);
                            snackbarMessage.postValue(messageId);
                        }
                    }
                    @Override
                    public void onFailure(Call<EbayBrowseAPI.SearchResponse> call, Throwable t) {
                        Log.e(LOG_TAG, "Failed", t);
                    }
                });
        } else {
            Log.i(LOG_TAG, "Invalid Auth");
        }
    }

    public void fetchGame(String gameId) {
        db.collection("games")
            .document(gameId)
            .addSnapshotListener((document, error) -> {
                if(error != null) {
                    Log.i(LOG_TAG, "Error getting game", error);
                    snackbarMessage.postValue(R.string.errorFindingGame);
                } else if(document != null && document.exists()){
                    Game game = document.toObject(Game.class);
                    this.game.postValue(game);
                } else {
                    this.game.postValue(null);
                    errorMessage.postValue(R.string.errorFindingGame);
                    snackbarMessage.postValue(R.string.errorFindingGame);
                }
            });
    }

    public void filterGamesByConsole(String consoleId) {
        this.filterConsoleId = consoleId;
        Log.i(LOG_TAG, "ConsoleId = " + consoleId);
        fetchResults(this.game.getValue().queryString);
    }

    public void sortGamesByField(String sort) {
        this.sortGameId = sort;
        fetchResults(this.game.getValue().queryString);
    }

    @NonNull
    public Integer getErrorMessageForStatusCode(int statusCode) {
        switch(statusCode) {
            case 400: return R.string.ebay400Error;
            case 401: return R.string.ebay401Error;
            case 403: return R.string.ebay403Error;
            case 404: return R.string.ebay404Error;
            case 429: return R.string.ebay429Error;
            case 500: return R.string.ebay500Error;
            default: return R.string.ebayUnknownError;


        }

    }



}
