package edu.ranken.david_jenn.game_library.ui.profile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileGameViewHolder extends RecyclerView.ViewHolder {

    public TextView profileGameTitle;
    public TextView releaseYear;
    public ImageView[] consoleIcons;


    public ProfileGameViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
