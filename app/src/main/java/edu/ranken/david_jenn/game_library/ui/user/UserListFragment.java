package edu.ranken.david_jenn.game_library.ui.user;

import android.os.Bundle;
import android.view.View;
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

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.ui.game.GameListFragment;

public class UserListFragment extends Fragment {

    private static final String LOG_TAG = GameListFragment.class.getSimpleName();

    // views
    private TextView userListError;
    private RecyclerView userListRecycler;

    //state
    private UserListViewModel model;
    private UserListAdapter listAdapter;

    public UserListFragment() { super(R.layout.user_list); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userListRecycler = view.findViewById(R.id.userListRecycler);
        userListError = view.findViewById(R.id.userListError);

        //activity / lifecycle owner
        FragmentActivity activity = getActivity();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        model = new ViewModelProvider(activity).get(UserListViewModel.class);
        listAdapter = new UserListAdapter(activity, model);

        int cols = getResources().getInteger(R.integer.userListColumns);
        userListRecycler.setLayoutManager(new GridLayoutManager(activity, cols));
        userListRecycler.setAdapter(listAdapter);

        model.getUsers().observe(lifecycleOwner, (users) -> {
            listAdapter.setUsers(users);
        });
        model.getErrorMessage().observe(lifecycleOwner, (message) -> {
            if (message != null) {
                userListError.setText(message);
                userListError.setVisibility(View.VISIBLE);
            } else {
                userListError.setText(null);
                userListError.setVisibility(View.GONE);
            }
        });
        model.getSnackbarMessage().observe(lifecycleOwner, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(userListRecycler, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackBar();
            }
        });



    }
}
