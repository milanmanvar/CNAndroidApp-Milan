package com.thecn.app.views.text;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by philjay on 8/12/14.
 */
public class MyEditText extends EditText {

    private KeyEventCallback mKeyEventCallback;

    public void setKeyEventCallback(KeyEventCallback callback) {
        mKeyEventCallback = callback;
    }

    public interface KeyEventCallback {
        public void onKeyEvent(KeyEvent event);
    }

    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        boolean retVal = super.dispatchKeyEventPreIme(event);

        if (mKeyEventCallback != null) mKeyEventCallback.onKeyEvent(event);

        return retVal;
    }
}
