package edu.ranken.david_jenn.game_library.ui.user;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.User;

public class UserListViewModel extends ViewModel {

    private static final String LOG_TAG = UserListViewModel.class.getSimpleName();
    private final ListenerRegistration userRegistration;

    private final MutableLiveData<List<User>> users;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<User> selectedUser;

    public UserListViewModel() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        users = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        selectedUser = new MutableLiveData<>(null);

        userRegistration =
            db.collection("users")
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                   if(error != null) {
                       Log.e(LOG_TAG, "Error getting users");
                       snackbarMessage.postValue(R.string.errorGettingUsers);
                       errorMessage.postValue(R.string.errorGettingUsers);
                   } else {
                       Log.i(LOG_TAG, "users updated");
                       List<User> newUsers = querySnapshot.toObjects(User.class);
                       users.postValue(newUsers);
                    }
                });

    }

    public LiveData<List<User>> getUsers() { return users; }
    public LiveData<Integer> getErrorMessage() { return errorMessage; }
    public LiveData<Integer> getSnackbarMessage() { return snackbarMessage; }
    public LiveData<User> getSelectedUser() { return selectedUser; }
    public void setSelectedUser(User user) { this.selectedUser.postValue(user); }

    public void clearSnackBar() {
        snackbarMessage.postValue(null);
    }

}
