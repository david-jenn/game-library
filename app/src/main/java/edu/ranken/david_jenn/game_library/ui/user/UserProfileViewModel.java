package edu.ranken.david_jenn.game_library.ui.user;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.GameSummary;
import edu.ranken.david_jenn.game_library.data.LibraryGame;
import edu.ranken.david_jenn.game_library.data.User;
import edu.ranken.david_jenn.game_library.data.WishlistGame;

public class UserProfileViewModel extends ViewModel {

    private static final String LOG_TAG = UserProfileViewModel.class.getSimpleName();

    private final MutableLiveData<User> user;
    private final MutableLiveData<FirebaseUser> fireUser;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<Integer> consolePlaceholder;
    private final MutableLiveData<List<GameSummary>> libraryGames;
    private final MutableLiveData<List<GameSummary>> wishListGames;


    private final FirebaseFirestore db;
    private ListenerRegistration userRegistration;
    private ListenerRegistration libraryRegistration;
    private ListenerRegistration gameWishListRegistration;
    private String userId;

    public UserProfileViewModel() {
        user = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(R.string.noUserSelected);
        snackbarMessage = new MutableLiveData<>(null);
        consolePlaceholder = new MutableLiveData<>(null);
        libraryGames = new MutableLiveData<>(null);
        wishListGames = new MutableLiveData<>(null);
        fireUser = new MutableLiveData<>(null);
        db = FirebaseFirestore.getInstance();
    }
    @Override
    protected void onCleared() {
        if(libraryRegistration != null) {
            super.onCleared();
        }
        if(gameWishListRegistration != null) {
            super.onCleared();
        }
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Integer> getConsolePlaceholder() { return consolePlaceholder; }

    public LiveData<Integer> getErrorMessage() { return errorMessage; }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<List<GameSummary>> getLibraryGames() {
        return libraryGames;
    }

    public LiveData<List<GameSummary>> getWishListGames() {
        return wishListGames;
    }

    public void clearSnackBar() {
        snackbarMessage.postValue(null);
    }

    public void fetchUser(String userId) {

        if(Objects.equals(userId, this.userId)) {
            return;
        }
        this.userId = userId;

        if(userId == null) {
            this.user.postValue(null);
            this.errorMessage.postValue(R.string.noUserSelected);
        } else {

            libraryRegistration =
                db.collection("userLibrary")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((@NonNull QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                        if (error != null) {
                            //String errorMessage = R.string.errorLoadingLibraryGames;
                            Log.e(LOG_TAG, "Error getting library games", error);
                            snackbarMessage.postValue(R.string.errorGettingLibrary);
                        } else {
                            Log.i(LOG_TAG, "getting library games");
                            List<LibraryGame> newLibraryGames = querySnapshot.toObjects(LibraryGame.class);
                            List<GameSummary> newLibrarySummaries = new ArrayList<>();
                            Log.i(LOG_TAG, "Size " + newLibrarySummaries.size());
                            for (LibraryGame libraryGame : newLibraryGames) {
                                newLibrarySummaries.add(libraryGame.game);
                            }
                            libraryGames.postValue(newLibrarySummaries);
                        }
                    });

            gameWishListRegistration =
                db.collection("userWishList")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((@NonNull QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                        if (error != null) {
                            Log.e(LOG_TAG, "Error getting wish list games", error);
                            snackbarMessage.postValue(R.string.errorGettingWishList);
                        } else {
                            Log.i(LOG_TAG, "getting wish list games");
                            List<WishlistGame> newWishListGames = querySnapshot.toObjects(WishlistGame.class);
                            List<GameSummary> newWishListSummaries = new ArrayList<>();
                            for (WishlistGame wishListGame : newWishListGames) {
                                newWishListSummaries.add(wishListGame.game);
                            }
                            wishListGames.postValue(newWishListSummaries);
                        }
                    });
            userRegistration =
                db.collection("users")
                    .document(userId)
                    .addSnapshotListener((document, error) -> {
                       if(error != null) {
                           Log.e(LOG_TAG, "Error getting user", error);
                           this.errorMessage.postValue(R.string.userNotFound);
                           this.snackbarMessage.postValue(R.string.userNotFound);
                       } else if(document != null && document.exists()){
                           User user = document.toObject(User.class);
                           this.user.postValue(user);
                           this.errorMessage.postValue(null);
                           this.snackbarMessage.postValue(R.string.userUpdated);

                           boolean consoleSelected = false;
                           Map<String, Boolean> selectedConsoles = user.preferredConsoles;
                           if(selectedConsoles != null) {
                               for (Boolean selected : selectedConsoles.values()) {
                                   if (Objects.equals(Boolean.TRUE, selected)) {
                                       consoleSelected = true;
                                   }
                               }
                           }

                           if (!consoleSelected) {
                               consolePlaceholder.postValue(R.string.noConsoleSelected);
                           } else {
                               consolePlaceholder.postValue(null);
                           }

                       } else {
                           this.user.postValue(null);
                           this.errorMessage.postValue(R.string.userDoesNotExist);
                           this.snackbarMessage.postValue(R.string.userDoesNotExist);
                       }
                    });
        }
    }

}
