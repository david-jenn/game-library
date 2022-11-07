package edu.ranken.david_jenn.game_library;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.ranken.david_jenn.game_library.ui.game.GameDetailsFragment;
import edu.ranken.david_jenn.game_library.ui.game.GameDetailsViewModel;
import edu.ranken.david_jenn.game_library.ui.game.GameListViewModel;
import edu.ranken.david_jenn.game_library.ui.home.HomePageAdapter;
import edu.ranken.david_jenn.game_library.ui.user.UserListViewModel;
import edu.ranken.david_jenn.game_library.ui.user.UserProfileFragment;
import edu.ranken.david_jenn.game_library.ui.user.UserProfileViewModel;
import edu.ranken.david_jenn.game_library.ui.utils.ConfirmDialog;

public class HomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    // views
    private ViewPager2 pager;
    private BottomNavigationView bottomNav;
    private FragmentContainerView detailsContainer;

    // state
    private HomePageAdapter adapter;

    private GameListViewModel gameListModel;
    private GameDetailsViewModel gameDetailsModel;
    private UserListViewModel userListModel;
    private UserProfileViewModel userProfileModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        pager = findViewById(R.id.homePager);
        bottomNav = findViewById(R.id.homeBottomNav);
        detailsContainer = findViewById(R.id.homeDetailsContainer);

        adapter = new HomePageAdapter(this);
        pager.setAdapter(adapter);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNav.getMenu().getItem(position).setChecked(true);

                if(position == 0) {
                    if (detailsContainer != null) {
                        getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.homeDetailsContainer, GameDetailsFragment.class, null)
                            .commit();
                    }
                }
                else if (position == 1) {
                    if (detailsContainer != null) {
                        getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.homeDetailsContainer, UserProfileFragment.class, null)
                            .commit();
                    }
                }
            }
        });

        bottomNav.setOnItemSelectedListener((MenuItem item) -> {
            int itemId = item.getItemId();
            if (itemId == R.id.actionGameList) {

                pager.setCurrentItem(0);
                if(detailsContainer != null) {
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.homeDetailsContainer, GameDetailsFragment.class, null)
                        .commit();
                }
                return true;
            } else if (itemId == R.id.actionUserList) {
                pager.setCurrentItem(1);
                if(detailsContainer != null) {
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.homeDetailsContainer, UserProfileFragment.class, null)
                        .commit();
                }

                return true;
            } else {
                return false;
            }
        });

        //get models
        gameListModel = new ViewModelProvider(this).get(GameListViewModel.class);
        gameDetailsModel = new ViewModelProvider(this).get(GameDetailsViewModel.class);
        userListModel = new ViewModelProvider(this).get(UserListViewModel.class);
        userProfileModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        //observe models
        gameListModel.getSelectedGame().observe(this, (game) -> {
            if (detailsContainer == null) {
                if (game != null) {
                    gameListModel.setSelectedGame(null);
                    Intent intent = new Intent(this, GameDetailsActivity.class);
                    intent.putExtra(GameDetailsActivity.EXTRA_GAME_ID, game.id);
                    this.startActivity(intent);
                }
            } else {
                if (game != null) {
                    gameDetailsModel.fetchGame(game.id);
                } else {
                    gameDetailsModel.fetchGame(null);
                }
            }
        });

        userListModel.getSelectedUser().observe(this, (user) -> {
            if(detailsContainer == null) {
                if(user != null) {
                    userListModel.setSelectedUser(null);
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.EXTRA_USER_ID, user.userId);
                    this.startActivity(intent);
                }
            } else {
                if(user != null) {
                    userProfileModel.fetchUser(user.userId);
                } else {
                    userProfileModel.fetchUser(null);
                }

            }

        });


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as temporal navigation
            onBackPressed();
            return true;
        } else if (itemId == R.id.actionSignOut) {
            showConfirmDialog();
            return true;
        } else if (itemId == R.id.actionProfileEdit) {
            displayProfileEditor();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //Do nothing, prevent default behavior
        //super.onBackPressed();
    }

    public void onSignOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener((result) -> {
                Log.i(LOG_TAG, "Signed out!");
                finish();
            });
    }

    public void displayProfileEditor() {
        Intent intent = new Intent(this, MyProfileActivity.class);
        this.startActivity(intent);
    }

    public void showConfirmDialog() {
        Context context = this;
        ConfirmDialog dialog = new ConfirmDialog(
            context,
            getString(R.string.logoutConfirmation),
            (which) -> {
                onSignOut();
            },
            null
        );
        dialog.show();
    }
}