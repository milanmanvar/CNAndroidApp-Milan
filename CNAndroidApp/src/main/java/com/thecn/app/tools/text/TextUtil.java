package com.thecn.app.tools.text;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;

import com.thecn.app.R;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Common routines used with text in the app.
 */
public class TextUtil {

    /**
     * Get a string that is styled to look like a web link.
     * @param text text to use in span
     * @param context for resource
     * @return a styled spannable string
     */
    public static SpannableString getLinkStyleSpannableString(String text, Context context) {
        int linkColor = context.getResources().getColor(R.color.link_color);
        SpannableString str = new SpannableString(Html.fromHtml("<u>" + text + "</u>"));
        str.setSpan(new ForegroundColorSpan(linkColor), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }

    /**
     * Adds scheme if there is no scheme present.  There is a better way to do this...
     * @param url url to check
     * @return edited or same url
     */
    public static String checkURL(String url) {
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            url = "http://" + url;
        }

        return url;
    }

    /**
     * Adds an s to a string if the number is greater than 1.
     * @param number count of nouns
     * @param noun word to check
     * @return singular/plural string
     */
    public static String getPluralityString(int number, String noun) {
        String head = Integer.toString(number) + " ";
        String tail = "";
        if (number > 1) tail += "s";
        return (head + noun + tail);
    }

    /**
     * Get time stamp of the current time using {@link java.text.SimpleDateFormat} and {@link java.util.Date
     * @return time stamp string
     */
    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
    }

    /**
     * Checks if string is null or empty.
     * @param string string to check
     * @return true if null or empty
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Gets a display string for an integer count.
     * Displays a count using "k" for kilo, "M" for mega, etc.
     * @param count the count to check
     * @return a display string for integer count.
     */
    public static String getCountString(int count) {
        BigDecimal bd = new BigDecimal(count);
        bd = bd.round(new MathContext(3));
        int roundCount = bd.intValue();

        String string = Integer.toString(roundCount);
        int length = string.length();

        if (length < 4) {
            return string;
        }

        float retVal = roundCount;
        String tail;
        if (string.length() > 9) {
            retVal /= 1000000000;
            tail = " G";
        } else if (length > 6) {
            retVal /= 1000000;
            tail = " M";
        } else if (length > 3) {
            retVal /= 1000;
            tail = " k";
        } else {
            return "";
        }

        //get rid of trailing numbers
        string = Float.toString(retVal);
        int truncatePoint = string.length();
        boolean keepGoing = true;
        for (int i = truncatePoint - 1; keepGoing; i--) {
            char charAt = string.charAt(i);
            if (charAt == '0') {
                truncatePoint = i;
            } else {
                if (charAt == '.') {
                    truncatePoint = i;
                }

                keepGoing = false;
            }
        }

        string = string.substring(0, truncatePoint);

        return string + tail;
    }

    /**
     * Check if valid email
     * @param email email to check
     * @return true if email is valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
