package edu.ranken.david_jenn.game_library.data;

import com.google.firebase.firestore.DocumentId;

import java.util.List;
import java.util.Map;

public class Game {
    @DocumentId
    public String id;
    public String title;
    public String shortDescription;
    public List<String> longDescriptionParagraphs;
    public String developer;
    public String publisher;
    public String releaseYear;
    public String imageUrl;
    public String thumbnailUrl;
    public String controllerSupport;
    public String multiplayerSupport;
    public String website;
    public String queryString;
    public Double minPrice;
    public Double maxPrice;
    public Boolean inLibrary;
    public Boolean inWishList;
    public Map<String, Boolean> console;

    public List<String> screenShots;
    public List<String> tags;
    public List<String> genres;


}
