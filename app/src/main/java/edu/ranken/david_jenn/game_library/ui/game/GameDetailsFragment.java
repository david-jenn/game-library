package edu.ranken.david_jenn.game_library.ui.game;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.ComposeReviewActivity;
import edu.ranken.david_jenn.game_library.EbayBrowseActivity;
import edu.ranken.david_jenn.game_library.GameDetailsActivity;
import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.Game;
import edu.ranken.david_jenn.game_library.ui.review.ReviewListAdapter;

public class GameDetailsFragment extends Fragment {

    //constants
    private static final String LOG_TAG = GameDetailsFragment.class.getSimpleName();

    //state

    private Game game;
    private GameDetailsViewModel model;

    private TextView detailsError;
    private TextView detailsTitle;
    private TextView detailsDescription;
    private TextView detailsMultiplayerSupport;
    private TextView detailsControllerSupport;
    private TextView detailsTags;
    private TextView detailsGenres;
    private TextView detailsScreenShotLabels;
    private ImageView detailsMainImage;
    private ImageView[] consoleIcons;
    private ImageView[] screenshots;
    private RecyclerView detailsReviewList;
    private TextView detailsReviewError;
    private TextView detailsLink;
    private TextView detailsLinkLabel;
    private TextView detailsAvgPrice;
    private TextView detailsAvgPriceLabel;
    private TextView detailsReviewCount;
    private TextView detailsReviewLabel;

    private ReviewListAdapter reviewAdapter;
    private FloatingActionButton composeReviewButton;
    private FloatingActionButton shareGameButton;
    private FloatingActionButton ebayListButton;

    public GameDetailsFragment() { super(R.layout.game_details_scroll); }

    @Override
    public void onViewCreated(@NonNull View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);

        detailsError = contentView.findViewById(R.id.detailsError);
        detailsTitle = contentView.findViewById(R.id.detailsTitle);
        detailsDescription = contentView.findViewById(R.id.detailsDescription);
        detailsMainImage = contentView.findViewById(R.id.detailsMainImage);
        detailsControllerSupport = contentView.findViewById(R.id.detailsControllerSupport);
        detailsMultiplayerSupport = contentView.findViewById(R.id.detailsMultiplayerSupport);
        detailsTags = contentView.findViewById(R.id.detailsTags);
        detailsGenres = contentView.findViewById(R.id.detailsGenres);
        detailsScreenShotLabels = contentView.findViewById(R.id.detailsScreenShotsLabel);
        composeReviewButton = contentView.findViewById(R.id.composeReviewButton);
        detailsReviewError = contentView.findViewById(R.id.detailsReviewError);
        shareGameButton = contentView.findViewById(R.id.shareGameButton);
        ebayListButton = contentView.findViewById(R.id.ebayListButton);
        detailsLink = contentView.findViewById(R.id.detailsLink);
        detailsLinkLabel = contentView.findViewById(R.id.detailsLinkLabel);
        detailsAvgPrice = contentView.findViewById(R.id.detailsAvgPrice);
        detailsAvgPriceLabel = contentView.findViewById(R.id.detailsAvgPriceLabel);
        detailsReviewCount = contentView.findViewById(R.id.detailsReviewCount);
        detailsReviewLabel = contentView.findViewById(R.id.detailsReviewLabel);


        detailsReviewList = contentView.findViewById(R.id.detailsReviewList);

        consoleIcons = new ImageView[]{
            contentView.findViewById(R.id.detailsConsoleOne),
            contentView.findViewById(R.id.detailsConsoleTwo),
            contentView.findViewById(R.id.detailsConsoleThree),
            contentView.findViewById(R.id.detailsConsoleFour),
        };

        screenshots = new ImageView[]{
            contentView.findViewById(R.id.detailsScreenShotOne),
            contentView.findViewById(R.id.detailsScreenShotTwo),
            contentView.findViewById(R.id.detailsScreenShotThree),
            contentView.findViewById(R.id.detailsScreenShotFour),
            contentView.findViewById(R.id.detailsScreenShotFive)
        };
        //Get lifecycleOwner and activity
        FragmentActivity activity = getActivity();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();


        model = new ViewModelProvider(activity).get(GameDetailsViewModel.class);
        reviewAdapter = new ReviewListAdapter(activity, model);

        detailsReviewList.setLayoutManager(new LinearLayoutManager(activity));
        detailsReviewList.setAdapter(reviewAdapter);


        model.getGame().observe(lifecycleOwner, (game) -> {
            this.game = game;
            composeReviewButton.setVisibility(game != null ? View.VISIBLE : View.GONE);
            ebayListButton.setVisibility(game != null ? View.VISIBLE : View.GONE);
            shareGameButton.setVisibility(game != null ? View.VISIBLE : View.GONE);

            if(game == null) {
                detailsTitle.setVisibility(View.GONE);
            } else if (game.title == null) {
                detailsTitle.setText(R.string.titleNotFound);
                detailsTitle.setVisibility(View.VISIBLE);
            } else {
                detailsTitle.setVisibility(View.VISIBLE);
                detailsTitle.setText(game.title);
            }

            if(game == null) {
                detailsControllerSupport.setVisibility(View.GONE);
            } else if (game.controllerSupport == null) {
                detailsControllerSupport.setText(null);
                detailsControllerSupport.setVisibility(View.VISIBLE);
            } else {
                detailsControllerSupport.setVisibility(View.VISIBLE);
                String controllerSupport = getString(R.string.controllerSupportLabel) + " " + game.controllerSupport;
                detailsControllerSupport.setText(controllerSupport);
            }

            if(game == null) {
                detailsMultiplayerSupport.setVisibility(View.GONE);
            } else if (game.multiplayerSupport == null) {
                detailsMultiplayerSupport.setText(null);
                detailsMultiplayerSupport.setVisibility(View.VISIBLE);
            } else {
                detailsMultiplayerSupport.setVisibility(View.VISIBLE);
                String multiplayerSupport = getString(R.string.multiplayerSupport) + " " + game.multiplayerSupport;
                detailsMultiplayerSupport.setText(multiplayerSupport);
            }

            if(game == null) {
                detailsLink.setVisibility(View.GONE);
                detailsLink.setClickable(false);
                detailsLinkLabel.setVisibility(View.GONE);
            } else if (game.website == null) {
                detailsLink.setVisibility(View.VISIBLE);
                detailsLinkLabel.setVisibility(View.VISIBLE);
                detailsLink.setText(R.string.linkNotFound);
            } else {
                detailsLinkLabel.setVisibility(View.VISIBLE);
                detailsLink.setVisibility(View.VISIBLE);
                detailsLink.setText(game.website);
                detailsLink.setClickable(true);
            }

            if (game == null) {
                detailsDescription.setVisibility(View.GONE);
            } else if (game.longDescriptionParagraphs == null) {
                detailsDescription.setText(R.string.detailsDescriptionNotFound);
                detailsDescription.setVisibility(View.VISIBLE);
            } else {
                detailsDescription.setVisibility(View.VISIBLE);
                StringBuilder longDescription = new StringBuilder();
                int totalParagraphs = game.longDescriptionParagraphs.size();
                for (int i = 0; i < totalParagraphs; ++i) {
                    String paragraph = game.longDescriptionParagraphs.get(i);
                    longDescription.append(paragraph);
                    if (i < totalParagraphs - 1) {
                        longDescription.append("\n\n");
                    }
                }
                detailsDescription.setText(longDescription);
            }

            if (game == null) {
                detailsMainImage.setVisibility(View.GONE);
            } else if (game.imageUrl == null) {
                detailsMainImage.setImageResource(R.drawable.ic_broken_image);
                detailsMainImage.setVisibility(View.VISIBLE);
            } else {
                detailsMainImage.setImageResource(R.drawable.ic_downloading);
                detailsMainImage.setVisibility(View.VISIBLE);
                Picasso.get()
                    .load(game.imageUrl)
                    .noPlaceholder()
                    .error(R.drawable.ic_error)
                    .resizeDimen(R.dimen.detailsBannerWidthResize, R.dimen.detailsBannerHeightResize)
                    .centerInside()
                    .into(detailsMainImage);
            }

            if (game == null || game.genres == null) {
                detailsGenres.setVisibility(View.GONE);
                detailsGenres.setText(null);
            } else {
                detailsGenres.setVisibility(View.VISIBLE);
                StringBuilder genres = new StringBuilder("Genres: ");
                for (String genre : game.genres) {
                    genres.append(genre).append(", ");
                }
                genres.setLength(genres.length() - 2);
                detailsGenres.setText(genres);
            }

            if (game == null || game.tags == null) {
                detailsTags.setVisibility(View.GONE);
            } else {
                StringBuilder tags = new StringBuilder("#");
                for (String tag : game.tags) {
                    tags.append(tag).append(" #");
                }
                tags.setLength(tags.length() - 2);
                detailsTags.setText(tags);
            }

            if (game == null || game.console == null) {
                for (int i = 0; i < consoleIcons.length; ++i) {
                    consoleIcons[i].setImageResource(0);
                    consoleIcons[i].setVisibility(View.GONE);
                }
            } else {
                int iconIndex = 0;
                for (Map.Entry<String, Boolean> entry : game.console.entrySet()) {
                    if (Objects.equals(entry.getValue(), Boolean.TRUE)) {
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
                        if (iconIndex >= consoleIcons.length) {
                            break;
                        }
                    }
                }
                for (; iconIndex < consoleIcons.length; ++iconIndex) {
                    consoleIcons[iconIndex].setImageResource(0);
                    consoleIcons[iconIndex].setVisibility(View.GONE);
                }
            }

            if (game == null || game.screenShots == null) {
                detailsScreenShotLabels.setVisibility(View.GONE);
                screenshots[0].setVisibility(View.GONE);
                screenshots[1].setVisibility(View.GONE);
                screenshots[2].setVisibility(View.GONE);
                screenshots[3].setVisibility(View.GONE);
                screenshots[4].setVisibility(View.GONE);
            } else {
                detailsScreenShotLabels.setVisibility(View.VISIBLE);
                int size;
                if (game.screenShots.size() > 5) {
                    size = 5;
                } else {
                    size = game.screenShots.size();
                }
                for (int i = 0; i < size; ++i) {
                    screenshots[i].setVisibility(View.VISIBLE);
                    Picasso.get()
                        .load(game.screenShots.get(i))
                        .noPlaceholder()
                        .error(R.drawable.ic_error)
                        .resizeDimen(R.dimen.screenShotWidthResize, R.dimen.screenshotHeightResize)
                        .centerInside()
                        .into(screenshots[i]);
                }
                if (size < 5) {
                    for (; size < 5; ++size) {
                        screenshots[size].setVisibility(View.GONE);
                    }
                }
            }
        });

        model.getAuthorized().observe(lifecycleOwner, (authorized) -> {
            Game game = model.getGame().getValue();
            if(Objects.equals(Boolean.TRUE, authorized) && game != null) {
                model.fetchResults(game.queryString);
            }
        });

        model.getGame().observe(lifecycleOwner, (game) -> {
            Boolean authorized = model.getAuthorized().getValue();
            if (game != null && Objects.equals(Boolean.TRUE, authorized)) {
                model.fetchResults(game.queryString);
            }
        });

        model.getAvgPrice().observe(lifecycleOwner, (average) -> {
            if(this.game == null) {
                detailsAvgPrice.setVisibility(View.GONE);
                detailsAvgPriceLabel.setVisibility(View.GONE);
                return;
            }
            if(average != null) {
                detailsAvgPriceLabel.setVisibility(View.VISIBLE);
                detailsAvgPrice.setVisibility(View.VISIBLE);
                String averageFormatted = NumberFormat.getCurrencyInstance().format(average);
                detailsAvgPrice.setText(averageFormatted);
            } else {
                detailsAvgPrice.setText(null);
            }
        });

        model.getReviews().observe(lifecycleOwner, (reviews) -> {

            if (reviews == null) {
                detailsReviewLabel.setVisibility(View.GONE);
                detailsReviewCount.setVisibility(View.GONE);
            } else {
                detailsReviewLabel.setVisibility(View.VISIBLE);
                detailsReviewList.setVisibility(View.VISIBLE);
                reviewAdapter.setReviews(reviews);
                int reviewCount = reviews.size();
                String reviewPlural = this.getResources().getQuantityString(R.plurals.review, reviewCount);
                String message = this.getResources().getString(R.string.reviewCount, reviewCount, reviewPlural);
                detailsReviewCount.setText(message);
            }
            if(this.game == null) {
                detailsReviewList.setVisibility(View.GONE);
                detailsReviewLabel.setVisibility(View.GONE);
                return;
            }
        });
        model.getReviewError().observe(lifecycleOwner, (message) -> {
            if (message != null) {
                detailsReviewError.setVisibility(View.VISIBLE);
                detailsReviewError.setText(message);
            } else {
                detailsReviewError.setVisibility(View.GONE);
                detailsReviewError.setText(null);
            }
        });


        model.getGameError().observe(lifecycleOwner, (errorMessage) -> {
            if(errorMessage != null) {
                detailsError.setVisibility(View.VISIBLE);
                detailsError.setText(errorMessage);
            } else {
                detailsError.setVisibility(View.GONE);
                detailsError.setText(null);
            }

        });
        model.getSnackbarMessage().observe(lifecycleOwner, (message) -> {
            if (message != null) {
                Snackbar.make(detailsTitle, message, Snackbar.LENGTH_SHORT).show();
                model.clearSnackbar();
            }
        });

        composeReviewButton.setOnClickListener((v) -> {

            Intent reviewIntent = new Intent(activity, ComposeReviewActivity.class);
            reviewIntent.putExtra(ComposeReviewActivity.EXTRA_GAME_ID, model.getGameId());
            this.startActivity(reviewIntent);

        });

        ebayListButton.setOnClickListener((v) -> {
            Intent ebayIntent = new Intent(activity, EbayBrowseActivity.class);
            ebayIntent.putExtra(EbayBrowseActivity.EXTRA_GAME_ID, model.getGameId());
            this.startActivity(ebayIntent);
        });

        shareGameButton.setOnClickListener((view) -> {

            if (game == null) {
                Snackbar.make(view, getString(R.string.gameDoesNotExist), Snackbar.LENGTH_SHORT).show();
            } else if (game.title == null) {
                Snackbar.make(view, getString(R.string.gameTitleNotFound), Snackbar.LENGTH_SHORT).show();
            } else {
                String gameName;
                if (game.releaseYear == null) {
                    gameName = game.title;
                } else {
                    gameName = game.title + " (" + game.releaseYear + ").";
                }

                String message = getString(R.string.shareGameMessage) + gameName + "\nhttps://my-game-list.com/game/" + game.id;

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(sendIntent, "Share Game"));
            }
        });

//        if(savedInstanceState == null) {
//
//            Intent intent = getIntent();
//            String intentAction = intent.getAction();
//            Uri intentData = intent.getData();
//
//            if (intentAction == null) {
//                gameId = intent.getStringExtra(EXTRA_GAME_ID);
//                model.fetchGame(gameId);
//            } else if (Objects.equals(intentAction, Intent.ACTION_VIEW) && intentData != null) {
//                handleWebLink(intent);
//            }
//        } else {
//            Log.i(LOG_TAG, "gameId: " + gameId);
//            gameId = savedInstanceState.getString("gameId");
//            game = model.getGame().getValue();
//        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}
