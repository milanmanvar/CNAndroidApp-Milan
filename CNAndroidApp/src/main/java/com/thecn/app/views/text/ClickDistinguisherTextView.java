package com.thecn.app.views.text;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Thanks to weakwire for original code
 * http://stackoverflow.com/questions/7236840/android-textview-linkify-intercepts-with-parent-view-gestures
 * This view does not use LinkMovementMethod, instead only absorbs touch event if a link was actually clicked
 */
public class ClickDistinguisherTextView extends TextView {
    public ClickDistinguisherTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Object text = getText();

        if (text instanceof Spannable) {
            Spannable buffer = (Spannable) text;

            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();

                x += getScrollX();
                y += getScrollY();

                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int offset = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] links = buffer.getSpans(offset, offset, ClickableSpan.class);

                if (links.length > 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        links[0].onClick(this);
                    } else {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(links[0]),
                                buffer.getSpanEnd(links[0]));
                    }
                    return true;
                }
            }
        }

        return false;
    }
}
