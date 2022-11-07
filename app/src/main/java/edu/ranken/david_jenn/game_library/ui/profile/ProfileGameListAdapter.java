package edu.ranken.david_jenn.game_library.ui.profile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.GameSummary;

public class ProfileGameListAdapter extends RecyclerView.Adapter<ProfileGameViewHolder> {

    private final Activity context;
    private final LayoutInflater layoutInflater;
    private final ViewModel model;
    private List<GameSummary> games;

    public ProfileGameListAdapter(Activity context, ViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
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


    @NonNull
    @Override
    public ProfileGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.profile_list_game_item, parent, false);
        ProfileGameViewHolder vh = new ProfileGameViewHolder(itemView);

        vh.profileGameTitle = itemView.findViewById(R.id.profileGameTitle);
        vh.releaseYear = itemView.findViewById(R.id.profileGameReleaseYear);

        vh.consoleIcons = new ImageView[]{
            itemView.findViewById(R.id.profileConsoleOne),
            itemView.findViewById(R.id.profileConsoleTwo),
            itemView.findViewById(R.id.profileConsoleThree),
            itemView.findViewById(R.id.profileConsoleFour),
        };
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileGameViewHolder vh, int position) {
        GameSummary game = games.get(position);

        if (game.title == null) {
            vh.profileGameTitle.setText(R.string.titleNotFound);
        } else {
            vh.profileGameTitle.setText(game.title);
        }

        if (game.releaseYear == null) {
            vh.releaseYear.setText(R.string.releaseYearNotFound);
        } else {
            vh.releaseYear.setText(game.releaseYear);
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
                            break;
                        case "computer":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_computer_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            break;
                        case "nintendo-switch":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_switch_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            break;
                        case "playstation-4":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_ps4_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
                            break;
                        case "xbox-one":
                            vh.consoleIcons[iconIndex].setImageResource(R.drawable.ic_xbox_one_sm);
                            vh.consoleIcons[iconIndex].setVisibility(View.VISIBLE);
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
