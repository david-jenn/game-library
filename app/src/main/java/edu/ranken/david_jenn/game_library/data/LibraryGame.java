package edu.ranken.david_jenn.game_library.data;

import com.google.firebase.firestore.DocumentId;

import java.util.Map;

public class LibraryGame {
    @DocumentId
    public String id;
    public String gameId;
    public String userId;

    public GameSummary game;
    public Map<String, Boolean> selectedConsoles;

}
