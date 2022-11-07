package edu.ranken.david_jenn.game_library.data;

import android.net.Uri;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class User {
    @DocumentId
    public String userId;
    public Boolean emailVerificationStatus;
    public String displayName;
    public String profilePhoto;
    public Map<String, Boolean> preferredConsoles;

    @ServerTimestamp
    public Date lastLogin;



}
