package edu.ranken.david_jenn.game_library.ui.game;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.data.GameConsole;
import edu.ranken.david_jenn.game_library.data.GameList;
import edu.ranken.david_jenn.game_library.data.GameSummary;
import edu.ranken.david_jenn.game_library.data.LibraryGame;
import edu.ranken.david_jenn.game_library.data.WishlistGame;

public class GameListViewModel extends ViewModel {

    private static final String LOG_TAG = "GameListViewModel";
    private ListenerRegistration gameRegistration;
    private final ListenerRegistration gameLibraryRegistration;
    private final ListenerRegistration gameWishListRegistration;
    private final ListenerRegistration consoleRegistration;
    private final FirebaseFirestore db;


    private String filterConsoleId = null;
    private GameList filterList = GameList.ALL_GAMES;

    private final MutableLiveData<FirebaseUser> user;
    private final MutableLiveData<List<GameSummary>> games;
    private final MutableLiveData<List<String>> libraryGameKeys;
    private final MutableLiveData<List<String>> wishListGameKeys;
    private final MutableLiveData<List<GameConsole>> consoles;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<GameSummary> selectedGame;

    public GameListViewModel() {
        db = FirebaseFirestore.getInstance();

        user = new MutableLiveData<>(null);
        games = new MutableLiveData<>(null);
        consoles = new MutableLiveData<>(null);
        libraryGameKeys = new MutableLiveData<>(null);
        wishListGameKeys = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        selectedGame = new MutableLiveData<>(null);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        user.postValue(currentUser);
        String userId = currentUser.getUid();

        queryGames();

        gameLibraryRegistration =
            db.collection("userLibrary")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((@NonNull QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        //String errorMessage = R.string.errorLoadingLibraryGames;
                        Log.e(LOG_TAG, "Error getting library games", error);
                        errorMessage.postValue(R.string.errorGettingLibrary);
                    } else {
                        Log.i(LOG_TAG, "getting library games");
                        List<String> gameIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String gameId = document.getString("gameId");
                            if (gameId != null) {
                                gameIds.add(gameId);
                            }
                        }
                        libraryGameKeys.postValue(gameIds);
                    }
                });

        gameWishListRegistration =
            db.collection("userWishList")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((@NonNull QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting wish list games", error);
                        errorMessage.postValue(R.string.errorGettingWishList);
                    } else {
                        Log.i(LOG_TAG, "getting wish list games");
                        List<String> gameIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String gameId = document.getString("gameId");
                            if (gameId != null) {
                                gameIds.add(gameId);
                            }
                        }
                        wishListGameKeys.postValue(gameIds);
                    }
                });
        consoleRegistration =
            db.collection("consoles")
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting consoles");
                        errorMessage.postValue(R.string.errorGettingConsoles);
                    } else {
                        Log.i(LOG_TAG, "Consoles updated");
                        List<GameConsole> newConsoles = querySnapshot.toObjects(GameConsole.class);
                        consoles.postValue(newConsoles);
                    }
                });

    }

    @Override
    protected void onCleared() {
        if (gameRegistration != null) {
            gameRegistration.remove();
        }

        if (gameLibraryRegistration != null) {
            gameLibraryRegistration.remove();
        }

        if (gameWishListRegistration != null) {
            gameWishListRegistration.remove();
        }

        if (consoleRegistration != null) {
            super.onCleared();
        }
    }

    public LiveData<List<GameSummary>> getGames() {
        return games;
    }

    public LiveData<List<String>> getLibraryGameKeys() {
        return libraryGameKeys;
    }

    public LiveData<List<String>> getWishListGameKeys() {
        return wishListGameKeys;
    }

    public LiveData<Integer> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<List<GameConsole>> getConsoles() {
        return consoles;
    }

    public String getFilterConsoleId() {
        return filterConsoleId;
    }

    public LiveData<GameSummary> getSelectedGame() { return selectedGame; }

    public void setSelectedGame(GameSummary game) { this.selectedGame.postValue(game);}

    public void clearSnackBar() {
        snackbarMessage.postValue(null);
    }


    public void queryGames() {

        if (gameRegistration != null) {
            gameRegistration.remove();
        }

        Query query;

        switch (filterList) {
            default:
                throw new IllegalStateException("Unsupported Option");
            case ALL_GAMES:
                query = db.collection("games");
                break;
            case MY_LIBRARY:
                query = db.collection("userLibrary")
                    .whereEqualTo("userId", user.getValue().getUid());
                break;
            case MY_WISH_LIST:
                query = db.collection("userWishList")
                    .whereEqualTo("userId", user.getValue().getUid());
                break;
        }

        if (filterConsoleId != null) {
            if (filterList == GameList.ALL_GAMES) {
                query = query.whereEqualTo("console." + filterConsoleId, true);
            } else {
                query = query.whereEqualTo("game.console." + filterConsoleId, true);
            }
        }

        gameRegistration =
            query
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting games.", error);
                        errorMessage.postValue(R.string.errorGettingGames);

                    } else if (querySnapshot != null) {

                        Log.i(LOG_TAG, "Games updated");
                        ArrayList<GameSummary> newGameSummaries = new ArrayList<>();

                        switch (filterList) {
                            default:
                                throw new IllegalStateException("Unsupported Option");

                            case ALL_GAMES:
                                List<Game> newGames = querySnapshot.toObjects(Game.class);
                                for (Game game : newGames) {
                                    newGameSummaries.add(new GameSummary(game));
                                }
                                break;
                            case MY_LIBRARY:
                                List<LibraryGame> newLibraryGames = querySnapshot.toObjects(LibraryGame.class);
                                for (LibraryGame libraryGame : newLibraryGames) {
                                    if (libraryGame.game != null) {
                                        libraryGame.game.id = libraryGame.gameId;
                                        libraryGame.game.selectedConsoles = libraryGame.selectedConsoles;
                                        newGameSummaries.add(libraryGame.game);
                                    }
                                }
                                break;

                            case MY_WISH_LIST:
                                List<WishlistGame> newWishListGames = querySnapshot.toObjects(WishlistGame.class);
                                for (WishlistGame wishListGame : newWishListGames) {
                                    if (wishListGame.game != null) {
                                        wishListGame.game.id = wishListGame.gameId;
                                        wishListGame.game.selectedConsoles = wishListGame.selectedConsoles;
                                        newGameSummaries.add(wishListGame.game);
                                    }
                                }
                                break;

                        }
                        errorMessage.postValue(null);
                        games.postValue(newGameSummaries);

                    }
                });
    }

    public void filterGamesByGenre(String consoleId) {
        this.filterConsoleId = consoleId;
        queryGames();
    }

    public void filterGamesByList(GameList list) {
        this.filterList = list;
        queryGames();
    }

    public void addToLibrary(GameSummary game, Map<String, Boolean> selectedConsoles) {

        boolean consoleSelected = false;
        for (Boolean selected : selectedConsoles.values()) {
            if (Objects.equals(Boolean.TRUE, selected)) {
                consoleSelected = true;
            }
        }
        if (!consoleSelected) {
            errorMessage.postValue(R.string.noConsoleError);
            return;
        }

        String userId = user.getValue().getUid();
        HashMap<String, Object> gameToAdd = new HashMap<>();
        gameToAdd.put("gameId", game.id);
        gameToAdd.put("userId", userId);
        gameToAdd.put("game", game);
        gameToAdd.put("selectedConsoles", selectedConsoles);


        db.collection("userLibrary")
            .document(userId + ";" + game.id)
            .set(gameToAdd)
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Error adding game to library", task.getException());
                    snackbarMessage.postValue(R.string.errorAddingGameToLibrary);
                } else {
                    Log.i(LOG_TAG, "Game added to library");
                    snackbarMessage.postValue(R.string.gameAddedToLibrary);
                }
            });

    }

    public void addToWishList(GameSummary game, Map<String, Boolean> selectedConsoles) {

        boolean consoleSelected = false;
        for (Boolean selected : selectedConsoles.values()) {
            if (Objects.equals(Boolean.TRUE, selected)) {
                consoleSelected = true;
            }
        }
        if (!consoleSelected) {
            snackbarMessage.postValue(R.string.noConsoleError);
            return;
        }

        String userId = user.getValue().getUid();
        HashMap<String, Object> gameToAdd = new HashMap<>();
        gameToAdd.put("gameId", game.id);
        gameToAdd.put("userId", userId);
        gameToAdd.put("game", game);
        gameToAdd.put("selectedConsoles", selectedConsoles);

        db.collection("userWishList")
            .document(userId + ";" + game.id)
            .set(gameToAdd)
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Error adding game to Wish List", task.getException());
                    snackbarMessage.postValue(R.string.errorAddingGameWishList);
                } else {

                    Log.i(LOG_TAG, "Game added to Wish List");
                    snackbarMessage.postValue(R.string.gameAddedToWishList);
                }
            });
    }

    private void removeFromCollection(String gameId, String collection) {
        String userId = user.getValue().getUid();
        String id = userId + ";" + gameId;
            Log.i(LOG_TAG, "in here");

        Log.i(LOG_TAG, "queryId: " + id);
        db.collection(collection)
            .document(id)
            .delete()
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Error removing game " + collection, task.getException());
                    snackbarMessage.postValue(R.string.errorRemovingGame);
                } else {
                    Log.i(LOG_TAG, "Game removed from " + collection);
                }
            });
    }

    public void removeFromLibrary(String gameId) {
        removeFromCollection(gameId, "userLibrary");
    }

    public void removeFromUserWishList(String gameId) {
        removeFromCollection(gameId, "userWishList");
    }
}
