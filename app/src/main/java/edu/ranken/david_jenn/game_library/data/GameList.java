package edu.ranken.david_jenn.game_library.data;

public enum GameList {

    ALL_GAMES("All Games"),
    MY_LIBRARY("Your Library"),
    MY_WISH_LIST("Your Wish List");

    private final String name;

    private GameList(String name) { this.name = name; }

    public String toString() { return name; }


}
