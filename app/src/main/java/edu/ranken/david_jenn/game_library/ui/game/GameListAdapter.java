package edu.ranken.david_jenn.game_library.ui.game;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.GameDetailsActivity;
import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.GameSummary;
import edu.ranken.david_jenn.game_library.ui.utils.ConsoleChooserDialog;

public class GameListAdapter extends RecyclerView.Adapter<GameViewHolder> {

    private static final String LOG_TAG = GameListAdapter.class.getSimpleName();

    private final FragmentActivity context;
    private final LayoutInflater layoutInflater;
    private final Picasso picasso;
    private final GameListViewModel model;
    private List<GameSummary> games;
    private List<String> libraryGameKeys;
    private List<String> wishListGameKeys;

    public GameListAdapter(FragmentActivity context, GameListViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    @Override
    public int getItemCount() {
        if (games != null) {
            return games.size();
        } else {
            return 0;
        }
    }

    public void setGames(List<GameSummary> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    public void setLibraryGameKeys(List<String> libraryGameKeys) {
        this.libraryGameKeys = libraryGameKeys;
        notifyDataSetChanged();
    }

    public void setWishListGameKeys(List<String> wishListGameKeys) {
        this.wishListGameKeys = wishListGameKeys;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.game_item, parent, false);

        GameViewHolder vh = new GameViewHolder(itemView);
        vh.title = itemView.findViewById(R.id.gameTitle);
        vh.description = itemView.findViewById(R.id.gameDescription);
        vh.developer = itemView.findViewById(R.id.gameDeveloper);
        vh.publisher = itemView.findViewById(R.id.gamePublisher);
        vh.image = itemView.findViewById(R.id.gameImage);
        vh.wishlist = itemView.findViewById(R.id.gameWishlistButton);
        vh.library = itemView.findViewById(R.id.gameLibraryButton);

        vh.consoleIcons = new ImageView[] {
            itemView.findViewById(R.id.gameConsoleOne),
            itemView.findViewById(R.id.gameConsoleTwo),
            itemView.findViewById(R.id.gameConsoleThree),
            itemView.findViewById(R.id.gameConsoleFour),
        };

        vh.library.setOnClickListener((view) -> {

            GameSummary game = games.get(vh.getAdapterPosition());

            if(!vh.inLibrary) {
                ConsoleChooserDialog dialog = new ConsoleChooserDialog(
                    context,
                    context.getString(R.string.libraryConsoleChooser),
                    game.console,
                    null,
                    (selectedConsoles) -> {
                        model.addToLibrary(game, selectedConsoles);
                        if (vh.inWishlist) {
                            model.removeFromUserWishList(game.id);
                        }
                    });
                dialog.show();
            } else {
                model.removeFromLibrary(game.id);
            }

        });

        vh.wishlist.setOnClickListener((view) -> {
            GameSummary game = games.get(vh.getAdapterPosition());

            if(!vh.inWishlist) {
                ConsoleChooserDialog dialog = new ConsoleChooserDialog(
                    context,
                    context.getString(R.string.wishListConsoleChooser),
                    game.console,
                    null,
                    (selectedConsoles) -> {
                        model.addToWishList(game, selectedConsoles);
                        if (vh.inLibrary) {
                            model.removeFromLibrary(game.id);
                        }
                    });
                dialog.show();
            } else {
                model.removeFromUserWishList(game.id);
            }
        });

        vh.itemView.setOnClickListener((view) -> {
            GameSummary game = games.get(vh.getAdapterPosition());

            model.setSelectedGame(game);

        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder vh, int position) {

        GameSummary game = games.get(position);

        if (game.title != null) {
            String heading = game.title + " (" + game.releaseYear + ")";
            vh.title.setText(heading);
        } else {
            vh.title.setText(R.string.gameTitleNotFound);
        }
        if (game.shortDescription != null) {
            vh.description.setText(game.shortDescription);
        } else {
            vh.description.setText(R.string.gameDescriptionNotFound);
        }
        if (game.publisher != null) {
            String publisher = context.getString(R.string.gamePublisherLabel, game.publisher);
            vh.publisher.setText(publisher);
        } else {
            vh.publisher.setText(R.string.gamePublisherNotFound);
        }
        if (game.developer != null) {
            String developer = context.getString(R.string.gameDeveloperLabel, game.developer);
            vh.developer.setText(developer);
        } else {
            vh.developer.setText(R.string.gameDeveloperNotFound);
        }
        if(game.imageUrl == null || game.imageUrl.length() == 0) {
            vh.image.setImageResource(R.drawable.ic_broken_image);
        } else {

            vh.image.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(game.thumbnailUrl)
                .error(R.drawable.ic_error)
                .resizeDimen(R.dimen.thumbnailWidthResize, R.dimen.thumbnailHeightResize)
                .centerInside()
                .into(vh.image);
        }

        vh.wishlist.setImageResource(R.drawable.ic_wishlist_outline);
        vh.library.setImageResource(R.drawable.ic_library_outline);
        vh.inLibrary = false;
        vh.inWishlist = false;

        if (libraryGameKeys != null) {
            for (String key : libraryGameKeys) {
                if (key.equals(game.id)) {
                    vh.library.setImageResource(R.drawable.ic_library_solid);
                    vh.inLibrary = true;
                }
            }
        }

        if (wishListGameKeys != null) {
            for (String key : wishListGameKeys) {
                if (key.equals(game.id)) {
                    vh.wishlist.setImageResource(R.drawable.ic_wishlist_solid);
                    vh.inWishlist = true;
                }
            }
        }
        Map<String, Boolean> consoleList;
        if(game.selectedConsoles != null) {
            consoleList = game.selectedConsoles;
        } else {
            consoleList = game.console;
        }

        if(consoleList == null) {
            for (int i = 0; i < vh.consoleIcons.length; ++i) {
               vh.consoleIcons[i].setImageResource(0);
               vh.consoleIcons[i].setVisibility(View.GONE);
            }
        } else {
            int iconIndex = 0;

            for(Map.Entry<String, Boolean> entry : consoleList.entrySet()) {

                if(Objects.equals(entry.getValue(), Boolean.TRUE)) {
                    vh.consoleIcons[0].setVisibility(View.VISIBLE);

                    switch (entry.getKey()) {
                        default:
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_error);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.consoleIcons[iconIndex].setContentDescription(context.getString(R.string.unknownConsole));
                            break;
                        case "computer":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_computer_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.consoleIcons[iconIndex].setContentDescription(context.getString(R.string.computer));
                            break;
                        case "nintendo-switch":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_switch_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.consoleIcons[iconIndex].setContentDescription(context.getString(R.string.nintendoSwitch));
                            break;
                        case "playstation-4":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_ps4_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.consoleIcons[iconIndex].setContentDescription(context.getString(R.string.playstation4));
                            break;
                        case "xbox-one":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_xbox_one_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.consoleIcons[iconIndex].setContentDescription(context.getString(R.string.xboxOne));
                            break;
                    }
                    iconIndex++;
                    if(iconIndex >= vh.consoleIcons.length) {
                        break;
                    }
                }
            }
            for(; iconIndex < vh.consoleIcons.length; ++iconIndex) {
                vh.consoleIcons[iconIndex].setImageResource(0);
                vh.consoleIcons[iconIndex].setVisibility(View.GONE);
            }
        }
    }
}
