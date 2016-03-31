package com.thecn.app.views.list;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

/**
* List view that can be expanded dynamically.
 *
 * todo this is kind of hacky.  Should use linear layout(s) instead.
*/
public class ExpandableListView extends ListView {
    private boolean expanded = true;

    public ExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableListView(Context context) {
        super(context);
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Set list view to match height of its contents
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
