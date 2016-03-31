package com.thecn.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.thecn.app.R;

/**
 * Used to cover moving anar bar rectangle and other members with a mask so it looks elliptical
 */
public class BarMask extends View {

    private RectF rect;

    private Paint whitePaint;
    private Paint borderPaint;

    private Path maskPath;

    public BarMask(Context context) {
        super(context);
        init();
    }

    public BarMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarMask(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialize rect, paints, and mask path
     */
    private void init() {
        rect = new RectF();

        whitePaint = new Paint();
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setColor(Color.WHITE);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        int borderColor = getResources().getColor(R.color.anar_bar_border);
        borderPaint.setColor(borderColor);

        maskPath = new Path();
    }

    /**
     * Draw a white mask to place over the anar bar
     * so that it looks elliptical.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int radius;

        if (width >= height) {
            radius = height / 2;

            rect.set(0, 0, height, height);
            maskPath.arcTo(rect, 90, 180);
            maskPath.lineTo(0, 0);
            maskPath.lineTo(0, height);
            maskPath.lineTo(radius, height);
            maskPath.close();
            canvas.drawPath(maskPath, whitePaint);

            rect.set(width - height, 0, width, height);
            maskPath.arcTo(rect, 270, 180);
            maskPath.lineTo(width, height);
            maskPath.lineTo(width, 0);
            maskPath.lineTo(width - radius, 0);
            maskPath.close();
            canvas.drawPath(maskPath, whitePaint);

            rect.set(0, 0, width, height);
        } else {
            radius = width / 2;

            rect.set(0, 0, width, width);
            maskPath.arcTo(rect, 180, 180);
            maskPath.lineTo(width, 0);
            maskPath.lineTo(0, 0);
            maskPath.lineTo(0, radius);
            maskPath.close();
            canvas.drawPath(maskPath, whitePaint);

            rect.set(0, height - width, width, height);
            maskPath.arcTo(rect, 0, 180);
            maskPath.lineTo(0, height);
            maskPath.lineTo(width, height);
            maskPath.lineTo(width, height - radius);
            maskPath.close();
            canvas.drawPath(maskPath, whitePaint);
        }

        rect.set(0, 0, width, height);
        canvas.drawRoundRect(rect, radius, radius, borderPaint);
    }

}