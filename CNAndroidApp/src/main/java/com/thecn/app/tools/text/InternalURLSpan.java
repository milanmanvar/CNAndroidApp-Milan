package com.thecn.app.tools.text;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Custom span used to perform an action on click.
 * source:
 * http://blog.elsdoerfer.name/2009/10/29/clickable-urls-in-android-textviews/
*/
public class InternalURLSpan extends ClickableSpan {
    View.OnClickListener mListener;
    private boolean clickActionWorking; //used to prevent same action if already clicked.

    public InternalURLSpan() {
        clickActionWorking = false;
    }

    /**
     * New instance
     * @param listener on click listener to call on click
     */
    public InternalURLSpan(View.OnClickListener listener) {
        mListener = listener;
        clickActionWorking = false;
    }

    public boolean isClickActionWorking() {
        return clickActionWorking;
    }

    public void setClickActionWorking(boolean working) {
        clickActionWorking = working;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public View.OnClickListener getOnClickListener() {
        return mListener;
    }

    /**
     * Must have on click listener set
     * @throws NullPointerException if no on click listener set
     */
    @Override
    public void onClick(View widget) throws NullPointerException {
        if (mListener != null)
            mListener.onClick(widget);
        else
            throw new NullPointerException("OnClickListener not instantiated");
    }
}
