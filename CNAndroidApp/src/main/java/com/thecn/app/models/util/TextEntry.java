package com.thecn.app.models.util;

/**
 * Associates an int id, a string, and a boolean variable together.
 */
public class TextEntry {
    public int id;
    public String text;
    public boolean selected;

    public TextEntry(int id, String text) {
        this.id = id;
        this.text = text;
        selected = false;
    }
}
