package com.thecn.app.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.thecn.app.R;

/**
 * {@code ActionProvider} used for the notification button in a navigation activity's menu.
 */
public class NotificationActionProvider extends ActionProvider implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private View rootView;
    private View indicator; //background of indicator (red circle)
    private TextView indicatorText; //shows count of new notifications

    private View.OnClickListener onClickListener;

    private String TITLE;

    private int initialCount = 0;

    public NotificationActionProvider(Context context) {
        super(context);
        this.context = context;
        TITLE = context.getResources().getString(R.string.notifications);
    }

    /**
     * Gets custom view instead of regular menu item.
     * Sets on click listeners (short and long click)
     * @return custom view.
     */
    @Override
    public View onCreateActionView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.notification_button, null);
        rootView.setOnClickListener(this);
        rootView.setOnLongClickListener(this);

        indicator = rootView.findViewById(R.id.total_notification_indicator);
        indicatorText = (TextView) rootView.findViewById(R.id.total_notification_indicator_text);
        setCount(initialCount);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (onClickListener == null) return;
        onClickListener.onClick(v);
    }

    //Taken and modified from android support source code (ActionMenuItemView)

    /**
     * Taken and modified from android support source code (ActionMenuItemView)
     * @param v view that was clicked
     * @return true
     */
    @Override
    public boolean onLongClick(View v) {
        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        rootView.getLocationOnScreen(screenPos);
        rootView.getWindowVisibleDisplayFrame(displayFrame);

        final Context context = getContext();
        final int width = rootView.getWidth();
        final int height = rootView.getHeight();
        final int midy = screenPos[1] + height / 2;
        int referenceX = screenPos[0] + width / 2;
        if (ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_LTR) {
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            referenceX = screenWidth - referenceX; // mirror
        }

        //show a toast as a "hint" that displays menu item's title.
        //menu buttons normally do this, but a custom action provider doesn't do this automatically.
        Toast cheatSheet = Toast.makeText(context, TITLE, Toast.LENGTH_SHORT);
        if (midy < displayFrame.height()) {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP | GravityCompat.END, referenceX, height);
        } else {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
        }
        cheatSheet.show();
        return true;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setInitialCount(int count) {
        this.initialCount = count;
    }

    /**
     * Sets the views appropriately for the count given.
     * If count <= 0, don't show indicator.
     * If less than 100, show the count.
     * If over 100, show 99+
     * @param count count to display.
     */
    public void setCount(int count) {
        if (count <= 0) {
            indicator.setVisibility(View.INVISIBLE);
            return;
        }

        String display;

        if (count < 100) {
            display = Integer.toString(count);
        } else {
            display = context.getResources().getString(R.string.ninety_nine_plus);
        }

        indicatorText.setText(display);
        indicator.setVisibility(View.VISIBLE);
    }

}
