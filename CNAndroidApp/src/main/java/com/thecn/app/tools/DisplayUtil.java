package com.thecn.app.tools;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;

/**
 * Methods commonly used to gain information about the display.
 */
public class DisplayUtil {

    /**
     * Get the width of the display
     * @param activity used to get {@code WindowManager}
     * @return width of display
     */
    public static int getDisplayWidth(Activity activity) {
        if (activity == null) return 0;

        Display display = activity.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        } else {
            return display.getWidth();
        }
    }

    /**
     * Get the height of the display
     * @param activity used to get {@code WindowManager}
     * @return height of display
     */
    public static int getDisplayHeight(Activity activity) {
        if (activity == null) return 0;

        Display display = activity.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            return size.y;
        } else {
            return display.getHeight();
        }
    }

    /**
     * Get the display width minus a given margin.
     * @param marginInDIP margin to subtract
     * @param activity for resources, etc.
     * @return max width
     */
    public static float getMaxWidth(float marginInDIP, Activity activity) {
        float dispWidth = (float) DisplayUtil.getDisplayWidth(activity);
        float margin =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInDIP, activity.getResources().getDisplayMetrics());

        return dispWidth - margin;
    }
}
