package edu.ranken.david_jenn.game_library.ui.game;

import android.media.Image;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

public class GameViewHolder extends RecyclerView.ViewHolder {

    public ImageView image;
    public TextView title;
    public TextView description;
    public TextView developer;
    public TextView publisher;
    public TextView console;
    public ImageView[] consoleIcons;
    public ImageButton library;
    public ImageButton wishlist;

    public boolean inLibrary;
    public boolean inWishlist;


    public GameViewHolder(View itemView) {
        super(itemView);
    }
}
