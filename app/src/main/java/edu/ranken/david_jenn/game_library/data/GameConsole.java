package edu.ranken.david_jenn.game_library.data;

import com.google.firebase.firestore.DocumentId;

public class GameConsole {
    @DocumentId
    public String id;
    public String name;
    public String icon;
    public String releaseYear;
    @Override
    public String toString() { return "Genre {" + id + ", " + name + "}"; }
}
