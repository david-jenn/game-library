package edu.ranken.david_jenn.game_library;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.ui.profile.ProfileGameListAdapter;
import edu.ranken.david_jenn.game_library.ui.user.UserProfileFragment;
import edu.ranken.david_jenn.game_library.ui.user.UserProfileViewModel;

public class UserProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = UserProfileActivity.class.getSimpleName();
    public static final String EXTRA_USER_ID = "userId";


    private String userId;

    private FragmentContainerView fragmentContainer;
    private UserProfileFragment fragment;
    private UserProfileViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        fragmentContainer = findViewById(R.id.fragmentContainer);
        fragment = new UserProfileFragment();
        model = new ViewModelProvider(this).get(UserProfileViewModel.class);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();


            Intent intent = getIntent();
            String intentAction = intent.getAction();
            Uri intentData = intent.getData();

            if (intentAction == null) {
                userId = intent.getStringExtra(EXTRA_USER_ID);
                Log.i(LOG_TAG, "userId " + userId);
                model.fetchUser(userId);
            } else if (Objects.equals(intentAction, Intent.ACTION_VIEW) && intentData != null) {
                handleWebLink(intent);
            }


    }

    private void handleWebLink(Intent intent) {
        Uri uri = intent.getData();
        String path = uri.getPath();
        String prefix = "/user/";
        if(path.startsWith(prefix)) {
            Log.i(LOG_TAG, "prefix is correct");
            int userIdEnd = path.indexOf("/", prefix.length());
            if(userIdEnd < 0) {
                userId = path.substring(prefix.length());
            } else {
                userId = path.substring(prefix.length(), userIdEnd);
            }
            model.fetchUser(userId);
        } else {
            userId = null;
            Log.i(LOG_TAG, "Id is null");
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getAction() != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(new Intent(this, LoginActivity.class));
            stackBuilder.addNextIntent(new Intent(this, HomeActivity.class));
            stackBuilder.startActivities();
        } else {
            super.onBackPressed();
        }
    }
}