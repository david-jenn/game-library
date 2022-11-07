package edu.ranken.david_jenn.game_library.data;

public class ConsoleFilter {
    public final String consoleId;
    public final String consoleName;

    public ConsoleFilter(String consoleId, String consoleName) {
        this.consoleId = consoleId;
        this.consoleName = consoleName;
    }

    @Override
    public String toString() { return consoleName; }
}
