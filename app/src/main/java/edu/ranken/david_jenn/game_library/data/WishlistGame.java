package edu.ranken.david_jenn.game_library.data;

import com.google.firebase.firestore.DocumentId;

import java.util.Map;

public class WishlistGame {
    @DocumentId
    public String id;
    public String gameId;
    public String userId;

    public Map<String, Boolean> selectedConsoles;
    public GameSummary game;

}
