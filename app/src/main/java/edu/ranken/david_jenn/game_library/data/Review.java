package edu.ranken.david_jenn.game_library.data;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Review {
    @DocumentId
    public String reviewId;
    public String gameId;
    public String userId;
    public String displayName;
    public String email;
    public String profilePicture;
    public String reviewText;


    @ServerTimestamp
    public Date reviewedOn;
}
