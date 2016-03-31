package com.thecn.app.views.text;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Edit text made to be a password edit text.
 */
public class PasswordEditText extends EditText {

    public PasswordEditText(Context context) {
        super(context);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Set typeface and transformation method ({@link android.text.method.PasswordTransformationMethod})
     */
    private void init() {
        setTypeface(Typeface.DEFAULT);
        setTransformationMethod(new PasswordTransformationMethod());
    }
}
