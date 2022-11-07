package edu.ranken.david_jenn.game_library.ui.review;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.Review;
import edu.ranken.david_jenn.game_library.ui.game.GameDetailsViewModel;
import edu.ranken.david_jenn.game_library.ui.utils.ConfirmDialog;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewViewHolder> {

    private static final String LOG_TAG = ReviewListAdapter.class.getSimpleName();

    private final Activity context;
    private final LayoutInflater layoutInflater;
    private final Picasso picasso;
    private final GameDetailsViewModel model;
    private List<Review> reviews;

    public ReviewListAdapter(Activity context, GameDetailsViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.model = model;
        this.picasso = Picasso.get();
    }

    @Override
    public int getItemCount() {
        if (reviews != null) {
            return reviews.size();
        } else {
            return 0;
        }
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = layoutInflater.inflate(R.layout.review_item, parent, false);
        ReviewViewHolder vh = new ReviewViewHolder(itemView);

        vh.reviewUsername = itemView.findViewById(R.id.reviewUsername);
        vh.reviewText = itemView.findViewById(R.id.reviewText);
        vh.reviewTimeStamp = itemView.findViewById(R.id.reviewTimeStamp);
        vh.reviewProfilePicture = itemView.findViewById(R.id.reviewProfilePicture);
        vh.deleteReviewButton = itemView.findViewById(R.id.deleteReviewButton);


        vh.deleteReviewButton.setOnClickListener((view) -> {
            Review review = reviews.get(vh.getAdapterPosition());
            showConfirmReviewDelete(review);
        });

        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder vh, int position) {
        Review review = reviews.get(position);
        vh.deleteReviewButton.setVisibility(View.GONE);
        String userId = model.getUser().getValue().getUid();

        if(review.userId.equals(userId)) {
            vh.deleteReviewButton.setVisibility(View.VISIBLE);
        }

        if (review.userId != null) {
            vh.reviewUsername.setText(review.displayName);
        } else {
            vh.reviewUsername.setText(R.string.reviewUserNotFound);
        }

        if (review.reviewText != null) {
            vh.reviewText.setText(review.reviewText);
        } else {
            vh.reviewText.setText(R.string.reviewTextNotFound);
        }

        if (review.reviewedOn != null) {
            vh.reviewTimeStamp.setText(formatDateTime(review.reviewedOn));
        } else {
            vh.reviewTimeStamp.setText(R.string.reviewDateNotFound);
        }

        if (review.profilePicture == null || review.profilePicture.length() == 0) {
            vh.reviewProfilePicture.setImageResource(R.drawable.ic_broken_image);
        } else {
            vh.reviewProfilePicture.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(review.profilePicture)
                .error(R.drawable.ic_error)
                .resizeDimen(R.dimen.reviewPictureResize, R.dimen.reviewPictureResize)
                .centerInside()
                .into(vh.reviewProfilePicture);
        }
    }

    private String formatDateTime(Date timestamp) {
        try {
            if (timestamp != null) {
                DateFormat outputFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                return outputFormat.format(timestamp);
            } else {
                return "Invalid Date";
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Failed to format: " + timestamp, ex);
            return "Invalid Date";
        }
    }

    public void showConfirmReviewDelete(Review review) {
        ConfirmDialog dialog = new ConfirmDialog(
            context,
            context.getString(R.string.confirmDeleteReview),
            (which) -> { model.deleteReview(review); },
            null
        );
        dialog.show();
    }
}
