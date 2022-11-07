package edu.ranken.david_jenn.game_library.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.UserProfileActivity;
import edu.ranken.david_jenn.game_library.data.User;
import edu.ranken.david_jenn.game_library.ui.profile.ProfileGameListAdapter;

public class UserProfileFragment extends Fragment {

    private static final String LOG_TAG = UserProfileActivity.class.getSimpleName();
    public static final String EXTRA_USER_ID = "userId";

    private User user;

    private TextView userProfileDisplayName;
    private TextView userProfileId;
    private TextView userProfileLibraryPlaceholder;
    private TextView userProfileWishListPlaceholder;
    private TextView userProfileLastLogin;
    private TextView userProfileErrorMessage;
    private TextView userConsolePlaceholder;
    private TextView libraryGameLabel;
    private TextView wishListGameLabel;
    private TextView preferredConsoleLabel;
    private ImageView userProfilePicture;
    private ImageView[] consoleIcons;

    private RecyclerView userProfileLibraryList;
    private RecyclerView userProfileWishList;

    private UserProfileViewModel model;
    private ProfileGameListAdapter libraryAdapter;
    private ProfileGameListAdapter wishListAdapter;
    private String userId;

    public UserProfileFragment() { super(R.layout.activity_user_profile_scroll); }

    @Override
    public void onViewCreated(@NonNull View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);

        userProfileDisplayName = contentView.findViewById(R.id.userProfileDisplayName);
        userProfileId = contentView.findViewById(R.id.userProfileUserId);
        userProfileLibraryPlaceholder = contentView.findViewById(R.id.userProfileLibraryPlaceholder);
        userProfileWishListPlaceholder = contentView.findViewById(R.id.userProfileWishListPlaceholder);
        userProfileErrorMessage = contentView.findViewById(R.id.userProfileErrorMessage);
        userConsolePlaceholder = contentView.findViewById(R.id.userConsolePlaceholder);
        userProfilePicture = contentView.findViewById(R.id.userProfileProfilePicture);
        userProfileLibraryList = contentView.findViewById(R.id.userProfileLibraryList);
        userProfileWishList = contentView.findViewById(R.id.userProfileWishList);
        userProfileLastLogin = contentView.findViewById(R.id.userProfileLastLogin);
        libraryGameLabel = contentView.findViewById(R.id.userProfileLibraryGamesLabel);
        wishListGameLabel = contentView.findViewById(R.id.userProfileWishListGameLabel);
        preferredConsoleLabel = contentView.findViewById(R.id.userPreferredConsoleLabel);

        consoleIcons = new ImageView[] {
            contentView.findViewById(R.id.userConsoleOne),
            contentView.findViewById(R.id.userConsoleTwo),
            contentView.findViewById(R.id.userConsoleThree),
            contentView.findViewById(R.id.userConsoleFour),
        };

        //Get lifecycleOwner and activity
        FragmentActivity activity = getActivity();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();


        model = new ViewModelProvider(activity).get(UserProfileViewModel.class);

        libraryAdapter = new ProfileGameListAdapter(activity, model);
        wishListAdapter = new ProfileGameListAdapter(activity, model);

        userProfileLibraryList.setAdapter(libraryAdapter);
        userProfileLibraryList.setLayoutManager(new LinearLayoutManager(activity));

        userProfileWishList.setAdapter(wishListAdapter);
        userProfileWishList.setLayoutManager(new LinearLayoutManager(activity));


        model.getUser().observe(lifecycleOwner, (user) -> {
            this.user = user;
            if (user == null) {
                userProfileErrorMessage.setVisibility(View.VISIBLE);
                userProfileErrorMessage.setText(R.string.noUserSelected);
                userProfilePicture.setImageResource(R.drawable.ic_broken_image);
                userProfileId.setVisibility(View.GONE);
                userProfilePicture.setVisibility(View.GONE);
                libraryGameLabel.setVisibility(View.GONE);
                wishListGameLabel.setVisibility(View.GONE);
                userProfileLibraryPlaceholder.setVisibility(View.GONE);
                userProfileWishListPlaceholder.setVisibility(View.GONE);
                preferredConsoleLabel.setVisibility(View.GONE);
                for(int i = 0; i < consoleIcons.length; ++i) {
                    consoleIcons[i].setVisibility(View.GONE);
                }
            } else {
                userProfileErrorMessage.setVisibility(View.GONE);
                userProfilePicture.setVisibility(View.VISIBLE);
                userProfileId.setVisibility(View.VISIBLE);
                libraryGameLabel.setVisibility(View.VISIBLE);
                wishListGameLabel.setVisibility(View.VISIBLE);
                preferredConsoleLabel.setVisibility(View.VISIBLE);
                if(user.displayName != null) {
                    userProfileDisplayName.setText(user.displayName);
                } else {
                    userProfileDisplayName.setText(R.string.displayNameNotFound);
                }
                if(user.userId != null) {
                    userProfileId.setText(user.userId);
                } else {
                    userProfileId.setText(R.string.userIdNotFound);
                }


                if (user.profilePhoto != null) {
                    userProfilePicture.setImageResource(R.drawable.ic_downloading);
                    Picasso.get()
                        .load(user.profilePhoto)
                        .noPlaceholder()
                        .error(R.drawable.ic_error)
                        .resizeDimen(R.dimen.profilePictureResize, R.dimen.profilePictureResize)
                        .centerCrop()
                        .into(userProfilePicture);
                } else {
                    userProfilePicture.setImageResource(R.drawable.ic_broken_image);
                }

                if (user.lastLogin != null) {
                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                    String dateString = dateFormat.format(user.lastLogin);
                    String lastLoginLabel = getString(R.string.lastLoginLabel);
                    String lastLoginFormatted = lastLoginLabel + " " + dateString;
                    userProfileLastLogin.setText(lastLoginFormatted);
                } else {
                    userProfileLastLogin.setText(R.string.userProfileLastLoginUnknown);
                }
                Map<String, Boolean> consoleList = user.preferredConsoles;
                if (consoleList == null) {
                    for (int i = 0; i < consoleIcons.length; ++i) {
                        consoleIcons[i].setImageResource(0);
                        consoleIcons[i].setVisibility(View.GONE);
                    }
                } else {
                    int iconIndex = 0;

                    for(Map.Entry<String, Boolean> entry : consoleList.entrySet()) {

                        if(Objects.equals(entry.getValue(), Boolean.TRUE)) {
                            consoleIcons[0].setVisibility(View.VISIBLE);

                            switch (entry.getKey()) {
                                default:
                                    consoleIcons[iconIndex].setImageResource(R.drawable.ic_error);
                                    consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                                    break;
                                case "computer":
                                    consoleIcons[iconIndex].setImageResource(R.drawable.ic_computer_sm);
                                    consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                                    break;
                                case "nintendo-switch":
                                    consoleIcons[iconIndex].setImageResource(R.drawable.ic_switch_sm);
                                    consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                                    break;
                                case "playstation-4":
                                    consoleIcons[iconIndex].setImageResource(R.drawable.ic_ps4_sm);
                                    consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                                    break;
                                case "xbox-one":
                                    consoleIcons[iconIndex].setImageResource(R.drawable.ic_xbox_one_sm);
                                    consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                                    break;
                            }
                            iconIndex++;
                            if(iconIndex >= consoleIcons.length) {
                                break;
                            }
                        }
                    }
                    for(; iconIndex < consoleIcons.length; ++iconIndex) {
                        consoleIcons[iconIndex].setImageResource(0);
                        consoleIcons[iconIndex].setVisibility(View.GONE);
                    }
                }


            }
        });

        model.getLibraryGames().observe(lifecycleOwner, (games) -> {

            if (games != null && games.size() != 0) {
                libraryAdapter.setGames(games);
                userProfileLibraryPlaceholder.setText(null);
                userProfileLibraryPlaceholder.setVisibility(View.GONE);

            } else {
                libraryAdapter.setGames(games);
                userProfileLibraryPlaceholder.setText(R.string.noLibraryGamesPlaceholder);
                userProfileLibraryPlaceholder.setVisibility(View.VISIBLE);
            }
            if(this.user == null) {
                userProfileLibraryPlaceholder.setVisibility(View.GONE);
            }
        });

        model.getWishListGames().observe(lifecycleOwner, (games) -> {
            if (games != null && games.size() != 0) {
                wishListAdapter.setGames(games);
                userProfileWishListPlaceholder.setText(null);
                userProfileWishListPlaceholder.setVisibility(View.GONE);
            } else {
                wishListAdapter.setGames(games);
                userProfileWishListPlaceholder.setText(R.string.noWishListGamesPlaceholder);
                userProfileWishListPlaceholder.setVisibility(View.VISIBLE);
            }

            if(this.user == null) {
                userProfileWishListPlaceholder.setVisibility(View.GONE);
            }

        });

        model.getConsolePlaceholder().observe(lifecycleOwner, (message) -> {
            if(message != null) {
                userConsolePlaceholder.setText(message);
                userConsolePlaceholder.setVisibility(View.VISIBLE);
            } else {
                userConsolePlaceholder.setText(null);
                userConsolePlaceholder.setVisibility(View.GONE);
            }
        });


        model.getErrorMessage().observe(lifecycleOwner, (message) -> {
            if (message != null) {
                userProfileErrorMessage.setText(message);
                userProfileErrorMessage.setVisibility(View.VISIBLE);
            } else {
                userProfileErrorMessage.setText(null);
                userProfileErrorMessage.setVisibility(View.GONE);
            }
        });

        model.getSnackbarMessage().observe(lifecycleOwner, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(userProfileLibraryList, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackBar();
            }

        });

//        Intent intent = getIntent();
//        String intentAction = intent.getAction();
//        Uri intentData = intent.getData();
//
//        if(intentAction == null) {
//            userId = intent.getStringExtra(EXTRA_USER_ID);
//            model.fetchUser(userId);
//        } else if (Objects.equals(intentAction, Intent.ACTION_VIEW) && intentData != null) {
//            handleWebLink(intent);
//        }
    }
}
