package com.thecn.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
* Grid view that will grow in size to match its contents (no scrolling).
 *
 * todo this is hacky, a set of linear layouts would be better.
*/
public class ExpandableGridView extends GridView {
    private boolean expanded = true;

    public ExpandableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExpandableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableGridView(Context context) {
        super(context);
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Match height of children
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (expanded) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
