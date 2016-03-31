package com.thecn.app.tools.text;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.activities.profile.ProfileActivity;
import com.thecn.app.tools.CallbackManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used to linkify cn number text so that clicking on it will
 * open a User's profile.
 */
public class CNNumberLinker {

    //make sure surrounding characters are non word characters
    private static final String cnNumberPattern = "([^\\w])([a-zA-Z]{2}\\d{2,})";

    //these pattern is returned from server wherever a cn number
    //can link to a profile
    private static final String cnNumberLinkPattern =
            "<a[^>]*data-type=\"cn_number\"[^>]*data-id=\"([a-zA-Z]{2}\\d{2,})\"[^>]*>[^<]*</a>";

    private static final String cnNumberLinkPattern2 =
            "<a[^>]*data-id=\"([a-zA-Z]{2}\\d{2,})\"[^>]*data-type=\"cn_number\"[^>]*>[^<]*</a>";

    private static final String newLinePattern = "\n";

    private CallbackManager<? extends Fragment> callbackManager;

    /**
     * Set callback manager
     * @param callbackManager the callback manager
     */
    public void setCallbackManager(CallbackManager<? extends Fragment> callbackManager) {
        this.callbackManager = callbackManager;
    }

    /**
     * Create links to user profile pages from any text that matches a cn number.
     * @param text the text to linkify
     * @return string builder containing modified text
     */
    public SpannableStringBuilder linkify(String text) {
        return substituteLinks(Html.fromHtml(substitutePatterns(text)));
    }

    /**
     * Replace certain patterns with a group inside that pattern.  Also replace new line
     * characters with <br>.
     * @param text the text to subsitute patterns in
     * @return modified text
     */
    private String substitutePatterns(String text) {
        return text.replaceAll(cnNumberLinkPattern, "$1")
                .replaceAll(cnNumberLinkPattern2, "$1")
                .replaceAll(newLinePattern, "<br>");
    }

    //pattern used by replaceCNNumbers
    private Pattern cnNumberHTMLPattern = Pattern.compile("<cn_number>[a-zA-Z]{2}\\d{2,}</cn_number>");

    /**
     * Substitute links to profile pages for each cn number found.
     * Uses {@link com.thecn.app.tools.text.InternalURLSpan} to create links.
     * @param charSequence character sequence to find cn numbers in
     * @return builder which contains modified content
     */
    private SpannableStringBuilder substituteLinks(CharSequence charSequence) {

        String text = charSequence.toString();
        text = text.replaceAll(cnNumberPattern, "$1<cn_number>$2</cn_number>");

        Matcher cnNumberMatcher = cnNumberHTMLPattern.matcher(text);

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        int subStringStart = 0;
        int subStringEnd;
        while (cnNumberMatcher.find()) {
            subStringEnd = cnNumberMatcher.start();
            if (subStringEnd > 0) {
                stringBuilder.append(text.substring(subStringStart, subStringEnd));
            }

            final String cnNumberString =
                    text.substring(cnNumberMatcher.start(), cnNumberMatcher.end())
                            .replace("<cn_number>", "").replace("</cn_number>", "");
            final SpannableString cnNumberSpan = new SpannableString(cnNumberString);
            cnNumberSpan.setSpan(new InternalURLSpan(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openProfilePage(cnNumberString);
                }
            }), 0, cnNumberSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            stringBuilder.append(cnNumberSpan);

            subStringStart = cnNumberMatcher.end();
        }
        if (subStringStart < text.length()) {
            stringBuilder.append(text.substring(subStringStart, text.length()));
        }

        return stringBuilder;
    }

    /**
     * Used by {@link com.thecn.app.tools.text.InternalURLSpan}s set by the linker
     * to open a profile page.
     * @param cnNumber the cn number to use to open the profile.
     */
    private void openProfilePage(String cnNumber) {
        if (callbackManager == null) return;
        if (callbackManager.getObject() == null) return;
        Activity activity = callbackManager.getObject().getActivity();
        if (activity == null) return;

        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).openProfileByCNNumber(cnNumber);
        } else {
            Intent intent = new Intent(activity, ProfileActivity.class);
            intent.putExtra("cn_number", cnNumber);
            activity.startActivity(intent);
        }
    }
}
