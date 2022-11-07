package edu.ranken.david_jenn.game_library.ui.review;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.data.User;

public class ComposeReviewViewModel extends ViewModel {
    private static final String LOG_TAG = ComposeReviewViewModel.class.getSimpleName();

    private final FirebaseFirestore db;
    private ListenerRegistration gameRegistration;
    private String gameId;


    private final MutableLiveData<FirebaseUser> user;
    private final MutableLiveData<String> gameName;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<Boolean> finished;

    public ComposeReviewViewModel() {
        db = FirebaseFirestore.getInstance();

        user = new MutableLiveData<>(null);
        gameName = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        finished = new MutableLiveData<>(Boolean.FALSE);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        user.postValue(currentUser);
    }

    @Override
    protected void onCleared() {
        if(gameRegistration != null) {
            gameRegistration.remove();
        }
        super.onCleared();
    }

    public String getGameId() { return gameId; }
    public LiveData<String> getGameName() { return gameName; }
    public LiveData<Integer> getErrorMessage() { return errorMessage; }
    public LiveData<Integer> getSnackbarMessage() { return snackbarMessage; }
    public LiveData<Boolean> getFinished() { return finished; }

    public void clearSnackbar() { snackbarMessage.postValue(null); }

    public void fetchGame(String gameId) {
        this.gameId = gameId;

        if (gameRegistration != null) {
            gameRegistration.remove();
        }

        if (gameId == null) {
            this.gameName.postValue(null);
            this.errorMessage.postValue(R.string.noGameSelected);
            this.snackbarMessage.postValue(R.string.noGameSelected);
        } else {
            gameRegistration =
                db.collection("games")
                    .document(gameId)
                    .addSnapshotListener((document, error) -> {
                        if(error != null) {
                            Log.e(LOG_TAG,"Error getting game", error);
                            this.errorMessage.postValue(R.string.errorGettingGame);
                            this.snackbarMessage.postValue(R.string.errorGettingGame);
                        } else if(document != null && document.exists()) {
                            Game game = document.toObject(Game.class);
                            this.gameName.postValue(game.title);
                            this.errorMessage.postValue(null);
                            this.snackbarMessage.postValue(R.string.gameUpdated);
                        } else {
                            this.gameName.postValue(null);
                            this.errorMessage.postValue(R.string.gameDoesNotExist);
                            this.snackbarMessage.postValue(R.string.gameDoesNotExist);
                        }
                    });
        }



    }
    public void publishReview(String gameId, String reviewText) {
        if(reviewText.equals("")) {
            errorMessage.postValue(R.string.emptyReviewError);
            snackbarMessage.postValue(R.string.emptyReviewError);
            return;
        }

        // FIXME: get user once
        String userId = user.getValue().getUid();

        db.collection("users")
            .document(userId)
            .get()
            .addOnCompleteListener((task) -> {
                if(!task.isSuccessful()) {
                    Log.e(LOG_TAG,"Error getting User", task.getException());
                } else {
                    DocumentSnapshot document = task.getResult();
                    if(!document.exists()) {
                        Log.e(LOG_TAG, "User document " + userId + " does not exist");
                    } else {
                        User databaseUser = document.toObject(User.class);

                        HashMap<String, Object> review = new HashMap<>();
                        review.put("gameId", gameId);
                        review.put("userId", databaseUser.userId);
                        review.put("displayName", databaseUser.displayName);
                        review.put("profilePicture", databaseUser.profilePhoto);
                        review.put("reviewText", reviewText);
                        review.put("reviewedOn", FieldValue.serverTimestamp());

                        db.collection("reviews")
                            .document(userId + ";" + gameId)
                            .set(review)
                            .addOnCompleteListener((Task<Void> reviewTask) -> {
                                if (!reviewTask.isSuccessful()) {
                                    Log.e(LOG_TAG, "Error posting review", reviewTask.getException());
                                    snackbarMessage.postValue(R.string.errorPostingReviews);
                                } else {
                                    Log.i(LOG_TAG, "Review added");
                                    snackbarMessage.postValue(R.string.reviewAdded);
                                    finished.postValue(Boolean.TRUE);
                                }
                            });


                    }
                }
            });




    }



}
