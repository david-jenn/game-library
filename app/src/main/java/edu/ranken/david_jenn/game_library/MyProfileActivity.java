package edu.ranken.david_jenn.game_library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.ui.profile.MyProfileViewModel;
import edu.ranken.david_jenn.game_library.ui.profile.ProfileGameListAdapter;
import edu.ranken.david_jenn.game_library.ui.utils.ConsoleChooserDialog;

public class MyProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = MyProfileActivity.class.getSimpleName();
    private TextView profileDisplayName;
    private TextView profileEmail;
    private TextView profileId;
    private TextView profileLibraryPlaceholder;
    private TextView profileWishListPlaceholder;
    private TextView profileVerificationStatus;
    private TextView profileErrorMessage;
    private ImageView profilePicture;
    private TextView preferredConsolePlaceholder;
    private ImageButton galleryButton;
    private ImageButton cameraButton;
    private FloatingActionButton shareProfileButton;
    private Button preferredConsoleButton;
    private ImageView[] consoleIcons;

    private RecyclerView profileLibraryList;
    private RecyclerView profileWishList;

    private ProfileGameListAdapter profileLibraryListAdapter;
    private ProfileGameListAdapter profileWishListAdapter;

    private MyProfileViewModel model;
    private FirebaseUser user;

    private File outputImageFile;
    private Uri outputImageUri;

    private final ActivityResultLauncher<String> getContentLauncher =
        registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            (Uri uri) -> {
                if (uri != null) {
                    uploadImage(uri);
                }
            }
        );

    private final ActivityResultLauncher<Uri> takePictureLauncher =
        registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            (Boolean result) -> {
                Log.i(LOG_TAG, "take picture result: " + result);
                if (Objects.equals(result, Boolean.TRUE)) {
                    uploadImage(outputImageUri);
                } else {
                    Log.e(LOG_TAG, "failed to return picture");
                }
            }
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_scroll);

        profileDisplayName = findViewById(R.id.userProfileDisplayName);
        profileEmail = findViewById(R.id.profileEmail);
        profileId = findViewById(R.id.userProfileUserId);
        profileVerificationStatus = findViewById(R.id.userProfileLastLogin);
        profilePicture = findViewById(R.id.userProfileProfilePicture);
        profileErrorMessage = findViewById(R.id.userProfileErrorMessage);
        profileLibraryList = findViewById(R.id.userProfileLibraryList);
        profileWishList = findViewById(R.id.userProfileWishList);
        profileWishListPlaceholder = findViewById(R.id.userProfileWishListPlaceholder);
        profileLibraryPlaceholder = findViewById(R.id.userProfileLibraryPlaceholder);
        galleryButton = findViewById(R.id.galleryButton);
        cameraButton = findViewById(R.id.cameraButton);
        shareProfileButton = findViewById(R.id.shareProfileButton);
        preferredConsoleButton = findViewById(R.id.preferredConsoleButton);
        preferredConsolePlaceholder = findViewById(R.id.preferredConsolePlaceholder);

        consoleIcons = new ImageView[]{
            findViewById(R.id.preferredConsoleOne),
            findViewById(R.id.preferredConsoleTwo),
            findViewById(R.id.preferredConsoleThree),
            findViewById(R.id.preferredConsoleFour),
        };

        model = new ViewModelProvider(this).get(MyProfileViewModel.class);
        profileLibraryListAdapter = new ProfileGameListAdapter(this, model);
        profileWishListAdapter = new ProfileGameListAdapter(this, model);

        profileLibraryList.setAdapter(profileLibraryListAdapter);
        profileLibraryList.setLayoutManager(new LinearLayoutManager(this));
        profileWishList.setAdapter(profileWishListAdapter);
        profileWishList.setLayoutManager(new LinearLayoutManager(this));

        galleryButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "Gallery button fired!");
            getContentLauncher.launch("image/*");
        });

        cameraButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "Camera button fired!");
            try {
                outputImageFile = createImageFile();
                Log.i(LOG_TAG, "outputImageFile = " + outputImageFile);
                outputImageUri = fileToUri(outputImageFile);
                Log.i(LOG_TAG, "outputImageUri = " + outputImageUri);
                takePictureLauncher.launch(outputImageUri);
            } catch (Exception ex) {
                Log.e(LOG_TAG, "take picture failed", ex);
            }

        });

        shareProfileButton.setOnClickListener((view) -> {
            if (user == null) {
                Snackbar.make(view, getString(R.string.userNotFound), Snackbar.LENGTH_SHORT).show();
            } else if (user.getDisplayName() == null) {
                Snackbar.make(view, getString(R.string.userDisplayNameNotFound), Snackbar.LENGTH_SHORT).show();
            } else {
                String username = user.getDisplayName();

                String message = getString(R.string.shareProfileText, username, user.getUid());


                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(sendIntent, "Share profile"));
            }
        });

        preferredConsoleButton.setOnClickListener((view) -> {

            Map<String, Boolean> userPreferredConsoles = model.getDatabaseUser().getValue().preferredConsoles;
            ConsoleChooserDialog dialog = new ConsoleChooserDialog(
                this,
                getString(R.string.prefferedConsoleChooser),
                null,
                userPreferredConsoles,
                (selectedConsoles) -> {

                    model.savePreferredConsoles(selectedConsoles);
                });
            dialog.show();
        });

        model.getFirebaseUser().observe(this, (user) -> {
            this.user = user;
            if (user == null) {
                profileErrorMessage.setVisibility(View.VISIBLE);
                profileErrorMessage.setText(R.string.errorLoadingProfilePage);
                profilePicture.setImageResource(R.drawable.ic_broken_image);
                profileId.setText(R.string.profileIdNotFound);
                profileEmail.setText(R.string.profileEmailNotFound);
                profileVerificationStatus.setText(R.string.profileVerificationNotFound);
            } else {
                profilePicture.setImageResource(R.drawable.ic_downloading);
                profileErrorMessage.setVisibility(View.GONE);
                profileErrorMessage.setText(null);
                profileDisplayName.setText(user.getDisplayName());
                profileEmail.setText(user.getEmail());
                profileId.setText(user.getUid());
                if (user.isEmailVerified()) {
                    profileVerificationStatus.setText(R.string.emailVerified);
                } else {
                    profileVerificationStatus.setText(R.string.emailNotVerified);
                }

                if (user.getPhotoUrl() != null) {

                    Picasso.get()
                        .load(user.getPhotoUrl())
                        .resizeDimen(R.dimen.profilePictureResize, R.dimen.profilePictureResize)
                        .centerCrop()
                        .into(profilePicture);
                } else {
                    profilePicture.setImageResource(R.drawable.ic_broken_image);
                }
            }
        });

        model.getDatabaseUser().observe(this, (user) -> {

            if(user == null) {
                for (int i = 0; i < consoleIcons.length; ++i) {
                    consoleIcons[i].setImageResource(0);
                    consoleIcons[i].setVisibility(View.GONE);
                }
                return;
            } else {
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


        model.getLibraryGames().observe(this, (games) -> {
            if (games != null && games.size() != 0) {
                Log.i(LOG_TAG, "library games length : " + games.size());
                profileLibraryListAdapter.setGames(games);
                profileLibraryPlaceholder.setText(null);
                profileLibraryPlaceholder.setVisibility(View.GONE);
            } else {
                profileLibraryPlaceholder.setText(R.string.emptyGameLibrary);
                profileLibraryPlaceholder.setVisibility(View.VISIBLE);
            }
        });

        model.getWishListGames().observe(this, (games) -> {

            if (games != null && games.size() != 0) {
                profileWishListAdapter.setGames(games);
                profileWishListPlaceholder.setText(null);
                profileWishListPlaceholder.setVisibility(View.GONE);
            } else {
                profileWishListPlaceholder.setText(R.string.emptyWishList);
                profileWishListPlaceholder.setVisibility(View.VISIBLE);
            }
        });

        model.getConsolePlaceholder().observe(this, (message) -> {
            if(message != null) {
                preferredConsolePlaceholder.setText(message);
                preferredConsolePlaceholder.setVisibility(View.VISIBLE);
            } else {
                preferredConsolePlaceholder.setText(null);
                preferredConsolePlaceholder.setVisibility(View.GONE);
            }
        });

        model.getErrorMessage().observe(this, (message) -> {
            if (message != null) {
                profileErrorMessage.setText(message);
                profileErrorMessage.setVisibility(View.VISIBLE);
            } else {
                profileErrorMessage.setText(null);
                profileErrorMessage.setVisibility(View.GONE);
            }
        });

        model.getSnackbarMessage().observe(this, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(profileLibraryList, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackBar();
            }

            //disable camera if not available
//            boolean hasCamera =
//                this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
//            cameraButton.setVisibility(hasCamera ? View.VISIBLE : View.GONE);


        });


    }

    public void uploadImage(Uri uri) {
        profilePicture.setImageResource(R.drawable.ic_downloading);
        Picasso
            .get()
            .load(uri)
            .resize(400, 400)
            .centerCrop()
            .into(profilePicture);
        model.uploadProfileImage(uri);

    }

    private File createImageFile() throws IOException {
        // create file name
        Calendar now = Calendar.getInstance();
        String fileName = String.format(Locale.US, "image_%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS.jpg", now);

        // create paths
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, fileName);

        // return File object
        return imageFile;
    }

    private static final String FILE_PROVIDER_AUTHORITY = "edu.ranken.david_jenn.game_library.fileprovider";

    private Uri fileToUri(File file) {
        return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
    }


}