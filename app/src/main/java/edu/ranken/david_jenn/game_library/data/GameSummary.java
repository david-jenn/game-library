package edu.ranken.david_jenn.game_library.data;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class GameSummary {

    public String id;
    public String title;
    public String shortDescription;
    public String developer;
    public String publisher;
    public String releaseYear;
    public String imageUrl;
    public String thumbnailUrl;
    public Map<String, Boolean> console;
    public Map<String, Boolean> selectedConsoles;


    public GameSummary() {}

    public GameSummary(@NonNull Game game) {
        this.id = game.id;
        this.title = game.title;
        this.shortDescription = game.shortDescription;
        this.developer = game.developer;
        this.publisher = game.publisher;
        this.releaseYear = game.releaseYear;
        this.imageUrl = game.imageUrl;
        this.thumbnailUrl = game.thumbnailUrl;
        this.console = game.console;
        this.selectedConsoles = null;

    }

    @NonNull
    @Override
    public String toString() { return "GameSummary {" + id + ", " + title + "}"; }

}
