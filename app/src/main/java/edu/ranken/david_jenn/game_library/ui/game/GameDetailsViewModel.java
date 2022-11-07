package edu.ranken.david_jenn.game_library.ui.game;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.AuthAPI;
import edu.ranken.david_jenn.game_library.data.AuthEnvironment;
import edu.ranken.david_jenn.game_library.data.EbayBrowseAPI;
import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.data.Review;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetailsViewModel extends ViewModel {
    private static final String LOG_TAG = GameDetailsViewModel.class.getSimpleName();

    // firebase
    private final FirebaseFirestore db;
    private ListenerRegistration gameRegistration;
    private ListenerRegistration reviewRegistration;
    private String gameId;

    //ebay
    private final AuthAPI authAPI;
    private final EbayBrowseAPI browseApi;
    private String authToken;

    //live data
    private final MutableLiveData<FirebaseUser> user;
    private final MutableLiveData<Game> game;
    private final MutableLiveData<Integer> gameError;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<List<Review>> reviews;
    private final MutableLiveData<Integer> reviewError;
    private final MutableLiveData<Boolean> authorized;
    private MutableLiveData<Double> avgPrice;

    private Thread calculateTask;

    public GameDetailsViewModel() {
        db = FirebaseFirestore.getInstance();


        authAPI = new AuthAPI(AuthEnvironment.PRODUCTION, "PaulSmit-explorer-PRD-dba622b8c-288c89fc", "PRD-ba622b8c7a63-7c21-4cef-8a9f-55df");
        browseApi = new EbayBrowseAPI(AuthEnvironment.PRODUCTION);
        authorized = new MutableLiveData<>(false);


        user = new MutableLiveData<>(null);
        game = new MutableLiveData<>(null);
        gameError = new MutableLiveData<>(R.string.noGameSelected);
        snackbarMessage = new MutableLiveData<>(null);
        reviews = new MutableLiveData<>(null);
        reviewError = new MutableLiveData<>(null);
        avgPrice = new MutableLiveData<>(null);
        getAuthToken();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        user.postValue(currentUser);

    }

    @Override
    protected void onCleared() {
        if (gameRegistration != null) {
            gameRegistration.remove();
        }

        if(calculateTask != null) {
            calculateTask.interrupt();
        }

        super.onCleared();
    }

    public String getGameId() {
        return gameId;
    }

    public LiveData<Game> getGame() {
        return game;
    }

    public LiveData<Integer> getGameError() {
        return gameError;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    public LiveData<Integer> getReviewError() {
        return reviewError;
    }

    public LiveData<Boolean> getAuthorized() {
        return authorized;
    }

    public LiveData<Double> getAvgPrice() {
        return avgPrice;
    }

    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    public void clearSnackbar() {
        snackbarMessage.postValue(null);
    }

    public void fetchGame(String gameId) {

        if (Objects.equals(gameId, this.gameId)) { return; }

        this.gameId = gameId;
        this.game.postValue(null);
        this.gameError.postValue(null);
        this.reviews.postValue(null);
        this.reviewError.postValue(null);

        if (gameRegistration != null) {
            gameRegistration.remove();
        }

        if (reviewRegistration != null) {
            reviewRegistration.remove();
        }

        if (gameId == null) {
            this.gameError.postValue(R.string.noGameSelected);
        } else {
            gameRegistration =
                db.collection("games")
                    .document(gameId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) {
                            Log.e(LOG_TAG, "Error getting game", error);
                            this.gameError.postValue(R.string.errorGettingGame);
                            this.snackbarMessage.postValue(R.string.errorGettingGame);
                        } else if (document != null && document.exists()) {
                            Game game = document.toObject(Game.class);
                            this.game.postValue(game);
                            this.gameError.postValue(null);
                            this.snackbarMessage.postValue(R.string.gameUpdated);
                        } else {
                            this.game.postValue(null);
                            this.gameError.postValue(R.string.gameDoesNotExist);
                            this.snackbarMessage.postValue(R.string.gameDoesNotExist);
                        }
                    });

            reviewRegistration =
                db.collection("reviews")
                    .whereEqualTo("gameId", gameId)
                    .orderBy("reviewedOn", Query.Direction.DESCENDING)
                    .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                        if (error != null) {
                            Log.e(LOG_TAG, "Error getting reviews", error);
                            reviewError.postValue(R.string.errorLoadingReviews);
                            snackbarMessage.postValue(R.string.errorLoadingReviews);
                        } else {
                            Log.i(LOG_TAG, "Reviews updated");
                            List<Review> newReviews = querySnapshot.toObjects(Review.class);
                            reviews.postValue(newReviews);
                            reviewError.postValue(null);
                        }
                    });
        }
    }

    private void getAuthToken() {

        authAPI.authenticateAsync(
            (authResponse) -> {
                authToken = authResponse.access_token;
                authorized.postValue(Boolean.TRUE);
            },
            (ex) -> {
                Log.e(LOG_TAG, "Auth Error.", ex);
                snackbarMessage.postValue(R.string.authError);
                gameError.postValue(R.string.authError);
            }
        );
    }

    public void fetchResults(String queryString) {

        String filter = "";
        if (game.getValue().maxPrice != null || game.getValue().minPrice != null) {
            filter = "price:[" + game.getValue().minPrice + ".." + game.getValue().maxPrice + "],priceCurrency:USD";
        }

        if (authToken != null) {
            Log.i(LOG_TAG, "AuthToken: " + authToken);
            browseApi.searchAsync(
                authToken, queryString, 100, "", "", filter,
                new Callback<EbayBrowseAPI.SearchResponse>() {
                    @Override
                    public void onResponse(Call<EbayBrowseAPI.SearchResponse> call, Response<EbayBrowseAPI.SearchResponse> response) {
                        if (response.isSuccessful()) {
                            Log.i(LOG_TAG, "Success!");
                            EbayBrowseAPI.SearchResponse thisResponse = response.body();

                            calculateTask = new Thread(() -> CalculateAvg(thisResponse));
                            calculateTask.start();
                            //CalculateAvg(thisResponse);

                            if (thisResponse == null) {
                                Log.i(LOG_TAG, "Null response");
                                gameError.postValue(R.string.ebayListingError);
                            } else {
                                gameError.postValue(null);
                            }
                        } else {
                            Log.e(LOG_TAG, "Error getting Games");
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


    private void CalculateAvg(EbayBrowseAPI.SearchResponse result) {

        if (result != null && result.itemSummaries != null) {
            int length = result.itemSummaries.size();
            Log.i(LOG_TAG, "Length of ebay summaries: " + length);
            List<EbayBrowseAPI.ItemSummary> summaries = result.itemSummaries;
            double totalPrice = 0;

            // FIXME: calculated cost shipping is being handled

            int validPrices = 0;
            for (int i = 0; i < length; ++i) {
                try {
                    EbayBrowseAPI.ItemSummary item = summaries.get(i);

                    double price = Double.parseDouble(item.price.value);
                    double shippingCost = Double.parseDouble(item.shippingOptions.get(0).shippingCost.value);

                    if(item.shippingOptions != null && item.shippingOptions.size() > 0) {
                        if(item.shippingOptions.get(0) != null && !item.shippingOptions.get(0).shippingCostType.equals("FIXED")) {
                            throw new IllegalStateException("Calculated shipping");
                        }
                    }
                    validPrices++;
                    totalPrice += price + shippingCost;
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "invalid item", ex);
                }
            }
            Log.i(LOG_TAG, "Valid items : " + validPrices);
            double avgPrice = totalPrice / validPrices;
            this.avgPrice.postValue(avgPrice);

        } else {
            Log.i(LOG_TAG, "Ebay Results is null");
            this.avgPrice.postValue(null);
        }

    }

    public void deleteReview(Review review) {
        db.collection("reviews")
            .document(review.reviewId)
            .delete()
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    snackbarMessage.postValue(R.string.errorDeletingReview);
                    Log.e(LOG_TAG, "Error deleting review", task.getException());
                } else {
                    snackbarMessage.postValue(R.string.reviewDeleted);
                }
            });
    }


}
