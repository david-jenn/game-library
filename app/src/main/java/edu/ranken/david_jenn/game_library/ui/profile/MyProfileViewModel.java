package edu.ranken.david_jenn.game_library.ui.profile;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.GameSummary;
import edu.ranken.david_jenn.game_library.data.LibraryGame;
import edu.ranken.david_jenn.game_library.data.User;
import edu.ranken.david_jenn.game_library.data.WishlistGame;

public class MyProfileViewModel extends ViewModel {

    private static final String LOG_TAG = MyProfileViewModel.class.getSimpleName();

    private final MutableLiveData<FirebaseUser> firebaseUser;
    private final MutableLiveData<User> databaseUser;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<Integer> consolePlaceholder;
    private final MutableLiveData<List<GameSummary>> libraryGames;
    private final MutableLiveData<List<GameSummary>> wishListGames;
    private final MutableLiveData<Uri> imageDownloadUrl;


    private final ListenerRegistration gameLibraryRegistration;
    private final ListenerRegistration gameWishListRegistration;
    private final ListenerRegistration userRegistration;

    private final FirebaseFirestore db;

    private final FirebaseStorage storage;

    public MyProfileViewModel() {

        firebaseUser = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        libraryGames = new MutableLiveData<>(null);
        wishListGames = new MutableLiveData<>(null);
        imageDownloadUrl = new MutableLiveData<>(null);
        databaseUser = new MutableLiveData<>(null);
        consolePlaceholder = new MutableLiveData<>(null);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseUser.postValue(currentUser);

        String userId = currentUser.getUid();

        gameLibraryRegistration =
            db.collection("userLibrary")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((@NonNull QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting library games", error);
                        snackbarMessage.postValue(R.string.errorGettingLibrary);
                    } else {
                        Log.i(LOG_TAG, "getting library games");
                        List<LibraryGame> newLibraryGames = querySnapshot.toObjects(LibraryGame.class);
                        List<GameSummary> newLibrarySummaries = new ArrayList<>();
                        for (LibraryGame libraryGame : newLibraryGames) {
                            libraryGame.game.selectedConsoles = libraryGame.selectedConsoles;
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
                        //String errorMessage = R.string.errorLoadingLibraryGames;
                        Log.e(LOG_TAG, "Error getting wish list games", error);
                        snackbarMessage.postValue(R.string.errorGettingWishList);
                    } else {
                        Log.i(LOG_TAG, "getting wish list games");
                        List<WishlistGame> newWishListGames = querySnapshot.toObjects(WishlistGame.class);
                        List<GameSummary> newWishListSummaries = new ArrayList<>();
                        for (WishlistGame wishListGame : newWishListGames) {
                            wishListGame.game.selectedConsoles = wishListGame.selectedConsoles;
                            newWishListSummaries.add(wishListGame.game);
                        }
                        wishListGames.postValue(newWishListSummaries);
                    }
                });

        userRegistration =
            db.collection("users")
                .document(userId)
                .addSnapshotListener((document, error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting user", error);
                        this.errorMessage.postValue(R.string.userNotFound);
                        this.snackbarMessage.postValue(R.string.userNotFound);
                    } else if (document != null && document.exists()) {
                        User user = document.toObject(User.class);
                        this.databaseUser.postValue(user);
                        this.errorMessage.postValue(null);

                        boolean consoleSelected = false;
                        Map<String, Boolean> selectedConsoles = user.preferredConsoles;
                        if (selectedConsoles != null) {
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

                        this.snackbarMessage.postValue(R.string.userUpdated);
                    } else {
                        databaseUser.postValue(null);
                        this.errorMessage.postValue(R.string.userDoesNotExist);
                        this.snackbarMessage.postValue(R.string.userDoesNotExist);
                    }
                });


    }

    @Override
    protected void onCleared() {
        if (gameLibraryRegistration != null) {
            super.onCleared();
        }
        if (gameWishListRegistration != null) {
            super.onCleared();
        }

    }

    public LiveData<FirebaseUser> getFirebaseUser() {
        return firebaseUser;
    }

    public LiveData<Integer> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<Integer> getConsolePlaceholder() { return consolePlaceholder; }

    public LiveData<List<GameSummary>> getLibraryGames() {
        return libraryGames;
    }

    public LiveData<List<GameSummary>> getWishListGames() {
        return wishListGames;
    }

    public LiveData<User> getDatabaseUser() { return databaseUser; }

    public void clearSnackBar() { snackbarMessage.postValue(null); }

    public void uploadProfileImage(Uri profileImageUri) {
        String userId = firebaseUser.getValue().getUid();
        if (userId == null) {
            return;
        }
        StorageReference storageRef =
            storage.getReference("/user/" + userId + "/profilePhoto.png");
        storageRef
            .putFile(profileImageUri)
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "failed to upload image to " + storageRef.getPath(), task.getException());
                    errorMessage.postValue(R.string.failedToUpload);
                    snackbarMessage.postValue(R.string.failedToUpload);
                } else {
                    Log.i(LOG_TAG, "Image uploaded to " + storageRef.getPath());
                    storageRef.getDownloadUrl()
                        .addOnCompleteListener((downloadTask) -> {
                            if (!downloadTask.isSuccessful()) {
                                Log.e(LOG_TAG, "Failed to get download Url " + storageRef.getPath(), task.getException());
                                errorMessage.postValue(R.string.failedToDownload);
                                snackbarMessage.postValue(R.string.failedToDownload);
                            } else {
                                Uri downloadUrl = downloadTask.getResult();
                                Log.i(LOG_TAG, "Download url: " + downloadUrl);
                                this.errorMessage.postValue(null);
                                this.imageDownloadUrl.postValue(downloadUrl);
                                updateAuthProfile(downloadUrl, userId);
                            }
                        });
                }
            });
    }

    private void updateAuthProfile(Uri downloadUrl, String userId) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setPhotoUri(downloadUrl)
            .build();

        firebaseUser.getValue().updateProfile(profileUpdates)
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Error updating Firebase Auth", task.getException());
                    snackbarMessage.postValue(R.string.errorUpdatingProfilePicture);
                } else {
                    Log.i(LOG_TAG, "Firebase Auth user updated");
                    updateDatabaseProfile(downloadUrl, userId);
                }
            });
    }

    private void updateDatabaseProfile(Uri downloadUrl, String userId) {

        HashMap<String, Object> imageUpdate = new HashMap<>();
        imageUpdate.put("profilePhoto", downloadUrl.toString());

        db.collection("users")
            .document(userId)
            .set(imageUpdate, SetOptions.merge())
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Error updating user in database", task.getException());
                    snackbarMessage.postValue(R.string.errorUpdatingProfilePicture);
                    errorMessage.postValue(R.string.errorUpdatingProfilePicture);
                } else {
                    Log.i(LOG_TAG, "User updated in database");
                    snackbarMessage.postValue(R.string.profilePhotoUpdated);
                    errorMessage.postValue(null);
                }
            });
    }

    public void savePreferredConsoles(Map<String, Boolean> preferredConsoles) {

        HashMap<String, Object> update = new HashMap<>();
        update.put("preferredConsoles", preferredConsoles);

        db.collection("users")
            .document(databaseUser.getValue().userId)
            .set(update, SetOptions.merge())
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Error updating user preferred consoles", task.getException());
                    snackbarMessage.postValue(R.string.errorUpdateingConsoles);
                    errorMessage.postValue(R.string.errorUpdateingConsoles);
                } else {
                    Log.i(LOG_TAG, "User preferred consoles updated");
                    snackbarMessage.postValue(R.string.consolesUpdated);
                    errorMessage.postValue(null);
                }
            });
    }

}



