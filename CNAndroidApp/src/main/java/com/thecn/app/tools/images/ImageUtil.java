package com.thecn.app.tools.images;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.thecn.app.AppSession;

import java.io.File;
import java.util.ArrayList;

/**
 * Utility methods for working with images.
 */
public class ImageUtil {

    /**
     * Create a circular bitmap from a given bitmap.
     * Cuts out a circle and replaces cut out with white.
     * @param source source bitmap
     * @return new circular bitmap
     */
    public static Bitmap getCircularBitmap(Bitmap source) {
        if (source == null) return null;

        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        int dimen = Math.max(srcWidth, srcHeight);

        //create canvas that will draw to output bitmap
        Bitmap output = Bitmap.createBitmap(dimen,
                dimen, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int left, top, right, bottom;

        if (srcWidth > srcHeight) {
            top = (dimen - srcHeight) / 2;
            bottom = (dimen + srcHeight) / 2;

            left = 0;
            right = dimen;
        } else {
            left = (dimen - srcWidth) / 2;
            right = (dimen + srcWidth) / 2;

            top = 0;
            bottom = dimen;
        }

        int color = 0xffffffff;
        Paint paint = new Paint();

        dimen /= 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(dimen, dimen, dimen, paint);
        //set porter duff mode to atop
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        Rect srcRect = new Rect(0, 0, srcWidth, srcHeight);
        Rect dstRect = new Rect(left, top, right, bottom);
        canvas.drawBitmap(source, srcRect, dstRect, paint);

        return output;
    }

    /**
     * Get all image filepaths available from external storage.
     * @param context used to get cursor
     * @return array list of file paths to images.
     */
    public static ArrayList<String> getImagesFromExternalStorage(Context context) {
        Cursor cursor = getCursor(context);
        int count = cursor.getCount();

        ArrayList<String> filePaths = new ArrayList<>();
        for (int i = 0; i < count; i++) {

            String path = getFilePathIfValid(cursor, i);
            if (path != null) {
                filePaths.add(path);
            }
        }
        cursor.close();

        return filePaths;
    }

    /**
     * Returns a file path if the path is valid.  Otherwise returns null.
     * @param cursor cursor used to access data
     * @param index current index into cursor
     * @return valid filepath or null
     */
    private static String getFilePathIfValid(Cursor cursor, int index) {
        cursor.moveToPosition(index);
        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        String path = cursor.getString(dataColumnIndex);
        File file = new File(path);
        boolean valid = file.exists() && file.length() > 0;

        return valid ? path : null;
    }

    /**
     * Get a cursor from context that will move through image data.
     * @param context used to get cursor
     * @return new cursor object
     */
    private static Cursor getCursor(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

        return contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
    }

    /**
     * Get dimension of a gallery item on screen based on the dimensions of the display.
     * Returns one fourth of the smallest screen dimension.
     * @return dimension in pixels of a gallery item.
     */
    public static int getGalleryItemDimension() {
        int dimension;

        Resources resources = AppSession.getInstance()
                .getApplicationContext().getResources();

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        int orientation = resources.getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            dimension = displayMetrics.heightPixels;
        else dimension = displayMetrics.widthPixels;

        return dimension / 4;
    }

    /**
     * Check if file path valid
     * @param filePath file path to check
     * @return true if valid, false otherwise
     */
    public static boolean isFilePathValid(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }
}
