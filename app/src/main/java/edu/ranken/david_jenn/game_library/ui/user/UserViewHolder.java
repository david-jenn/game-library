package edu.ranken.david_jenn.game_library.ui.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public TextView displayName;
    public ImageView profilePicture;

    public UserViewHolder(@NonNull View itemView) { super(itemView); }
}
