package edu.ranken.david_jenn.game_library.ui.user;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.UserProfileActivity;
import edu.ranken.david_jenn.game_library.data.User;

public class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private static final String LOG_TAG = UserListAdapter.class.getSimpleName();
    private final FragmentActivity context;
    private final LayoutInflater layoutInflater;
    private final Picasso picasso;
    private final UserListViewModel model;
    private List<User> users;

    public UserListAdapter(FragmentActivity context, UserListViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        } else {
            return 0;
        }
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.user_item, parent, false);
        UserViewHolder vh = new UserViewHolder(itemView);

        vh.displayName = itemView.findViewById(R.id.userDisplayName);
        vh.profilePicture = itemView.findViewById(R.id.userProfilePicture);

        vh.itemView.setOnClickListener((view) -> {
            User user = users.get(vh.getAdapterPosition());
            Log.i(LOG_TAG, "User id: " + user.userId);
            model.setSelectedUser(user);
//            Intent intent = new Intent(context, UserProfileActivity.class);
//            intent.putExtra(UserProfileActivity.EXTRA_USER_ID, user.userId);
//            context.startActivity(intent);

        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder vh, int position) {
        User user = users.get(position);


        vh.displayName.setText(user.displayName != null ? user.displayName : "Display name not found");

        if (user.profilePhoto == null) {
            vh.profilePicture.setImageResource(R.drawable.ic_broken_image);
        } else {
            vh.profilePicture.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(user.profilePhoto)
                .error(R.drawable.ic_error)
                .resizeDimen(R.dimen.userListPictureResize, R.dimen.userListPictureResize)
                .into(vh.profilePicture);
        }

    }


}
