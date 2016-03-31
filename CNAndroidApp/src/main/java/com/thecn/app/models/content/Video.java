package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Model for video construct in server json.
*/
public class Video implements Serializable {

    @SerializedName("view_url")
    private String viewURL;

    public String getViewURL() {
        return viewURL;
    }

    public void setViewURL(String viewURL) {
        this.viewURL = viewURL;
    }

    public Video(String viewURL) {
        setViewURL(viewURL);
    }

    //possible youtube video patterns
    private static Pattern[] videoIDPatterns = new Pattern[] {
            Pattern.compile("youtube\\.com/v/([\\w0-9_-]{11})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("youtube\\.com/watch\\?*v=([\\w0-9_-]{11})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("youtu\\.be/([\\w0-9_-]{11})", Pattern.CASE_INSENSITIVE),
    };

    /**
     * Gets the youtube video id from a youtube video url
     * @return youtube video id
     */
    public String getVideoID() {
        if (viewURL == null) return null;

        for (Pattern p : videoIDPatterns) {
            Matcher matcher = p.matcher(viewURL);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }
}
