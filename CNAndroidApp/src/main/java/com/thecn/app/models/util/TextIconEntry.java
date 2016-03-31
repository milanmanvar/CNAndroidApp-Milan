package com.thecn.app.models.util;


/**
 * In addition to fields in {@link com.thecn.app.models.util.TextEntry},
 * also associates a resource id for images
 */
public class TextIconEntry extends TextEntry {

    public int resourceID;

    public TextIconEntry(int id, String text, int resourceID) {
        super(id, text);

        this.resourceID = resourceID;
    }
}
