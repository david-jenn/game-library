package edu.ranken.david_jenn.game_library.ui.review;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewViewHolder extends RecyclerView.ViewHolder {

    public TextView reviewTimeStamp;
    public TextView reviewUsername;
    public TextView reviewText;
    public ImageView reviewProfilePicture;
    public ImageButton deleteReviewButton;


    public ReviewViewHolder(View itemView) {
        super(itemView);
    }
}
