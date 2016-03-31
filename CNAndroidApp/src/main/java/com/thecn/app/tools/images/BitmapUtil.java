package com.thecn.app.tools.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

/**
 * Utility methods for working with bitmaps.
 * The term "Scaling" here refers to scaling a bitmap, but keeping the original aspect ratio.
 */
public class BitmapUtil {

    /**
     * Extension of RectF that adds fields to allow specification of
     * a section of another original RectF.
     */
    public static class SectionRectF extends RectF implements Serializable {
        public int originalWidth;
        public int originalHeight;

        public int maxOutputWidth;
        public int maxOutputHeight;

        public SectionRectF(RectF rectF) {
            left = rectF.left;
            top = rectF.top;
            right = rectF.right;
            bottom = rectF.bottom;
        }
    }

    /**
     * Get a bitmap from file that will be as large as will fit inside the given dimensions
     * @param filePath filepath to get bitmap from
     * @param widthLimit limit on width
     * @param heightLimit limit on height
     * @return scaled bitmap
     */
    public static Bitmap insideFitBitmapFromFile(String filePath, int widthLimit, int heightLimit) {

        Bitmap bitmap = getSampledBitmapFromFile(filePath, widthLimit, heightLimit);
        if (bitmap == null) return null;

        bitmap = rotateBitmap(bitmap, getRotation(filePath));
        return getInsideFitBitmap(bitmap, widthLimit, heightLimit);
    }

    /**
     * Scale a bitmap gotten from file.  Fit the given dimensions exactly.
     * @param filePath filepath to get bitmap from
     * @param width width of resulting bitmap
     * @param height height of resulting bitmap
     * @return scaled bitmap
     */
    public static Bitmap scaledBitmapFromFile(String filePath, int width, int height) {
        Bitmap bitmap = getSampledBitmapFromFile(filePath, width, height);
        if (bitmap == null) return null;

        bitmap = rotateBitmap(bitmap, getRotation(filePath));
        return getScaledBitmap(bitmap, width, height);
    }

    /**
     * Get a section of a bitmap from file and then scale it using given rect object
     * @param filePath filepath to get bitmap from
     * @param sectionRect dimensions specifying the section of the image
     * @return scaled section of original image as bitmap
     */
    public static Bitmap scaledBitmapSectionFromFile(String filePath, SectionRectF sectionRect) {
        return getScaledBitmap(
                getBitmapSectionFromFile(filePath, sectionRect),
                sectionRect.maxOutputWidth,
                sectionRect.maxOutputHeight
        );
    }

    /**
     * Get section of a bitmap from file.
     * @param filePath filepath to get bitmap from
     * @param sectionRect dimensions specifying the section of the image
     * @return section of original image as bitmap
     */
    public static Bitmap getBitmapSectionFromFile(String filePath, SectionRectF sectionRect) {
        float sectionWidth = sectionRect.right - sectionRect.left;
        float sectionHeight = sectionRect.bottom - sectionRect.top;

        //get proportion of size of bitmap section in comparison to the original
        float widthRatio = sectionWidth / (float) sectionRect.originalWidth;
        float heightRatio = sectionHeight / (float) sectionRect.originalHeight;

        Bitmap bitmap = getSampledBitmapFromFile(filePath, widthRatio, heightRatio,
                sectionRect.maxOutputWidth, sectionRect.maxOutputHeight);
        if (bitmap == null) return null;

        bitmap = rotateBitmap(bitmap, getRotation(filePath));
        if (bitmap == null) return null;

        float widthFactor = (float) bitmap.getWidth() / (float) sectionRect.originalWidth;
        float heightFactor = (float) bitmap.getHeight() / (float) sectionRect.originalHeight;

        int startX = (int) (sectionRect.left * widthFactor);
        int newWidth = (int) (sectionWidth * widthFactor);
        int startY = (int) (sectionRect.top * heightFactor);
        int newHeight = (int) (sectionHeight * heightFactor);

        if (!dimensionsWithinBounds(startX, startY, newWidth, newHeight, bitmap)) {
            return null;
        }

        return Bitmap.createBitmap(bitmap, startX, startY, newWidth, newHeight);
    }

    /**
     * Check that the dimensions of a bitmap section are valid
     * @param startX left coordinate of section
     * @param startY top coordinate of section
     * @param width width of section
     * @param height height of section
     * @param bitmap original bitmap
     * @return true if valid, false otherwise
     */
    public static boolean dimensionsWithinBounds(int startX, int startY, int width, int height, Bitmap bitmap) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        return 0 <= startX                     &&
               0 <= startY                     &&
               startX <= bitmapWidth           &&
               startY <= bitmapHeight          &&
               startX + width <= bitmapWidth   &&
               startY + height <= bitmapHeight ;
    }

    /**
     * Sample the bitmap brought from file based on given dimensions.
     * This can save a lot of memory.
     * @param filePath filepath to get bitmap from
     * @param widthLimit limit on width
     * @param heightLimit limit on height
     * @return sampled bitmap from file
     */
    public static Bitmap getSampledBitmapFromFile(String filePath, int widthLimit, int heightLimit) {
        BitmapFactory.Options options = BitmapUtil.justDecodeBounds(filePath);

        // Decode bitmap with inSampleSize set
        options.inSampleSize = BitmapUtil.calculateInSampleSize(options, widthLimit, heightLimit);
        options.inInputShareable = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Sample the bitmap brought from file based on a section of the bitmap, not the original bitmap.
     * @param filePath filepath to get bitmap from
     * @param widthRatio relationship between original bitmap width and section width
     * @param heightRatio relationship between original bitmap height and section height.
     * @param widthLimit limit on width
     * @param heightLimit limit on height
     * @return sampled section of a bitmap from file
     */
    public static Bitmap getSampledBitmapFromFile(
            String filePath, float widthRatio, float heightRatio, int widthLimit, int heightLimit
    ) {
        BitmapFactory.Options options = BitmapUtil.justDecodeBounds(filePath);

        options.inSampleSize =
                calculateInSampleSizeForBitmapSection(options, widthRatio, heightRatio, widthLimit, heightLimit);
        options.inInputShareable = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Same as {@link #getScaledBitmap(android.graphics.Bitmap, float)} but does not scale if bitmap smaller than
     * both limits.
     * @param bitmap original bitmap
     * @param widthLimit limit on width
     * @param heightLimit limit on height
     * @return inside fit bitmap
     */
    public static Bitmap getInsideFitBitmap(Bitmap bitmap, int widthLimit, int heightLimit) {
        if (bitmap == null) return null;

        float ratio = getGreatestRatio(bitmap, widthLimit, heightLimit);
        if (ratio < 1) {
            return bitmap;
        } else {
            return getScaledBitmap(bitmap, ratio);
        }
    }

    /**
     * Scales bitmap so that at least one dimension is equal to its limit.
     * @param bitmap original bitmap
     * @param widthLimit limit on width
     * @param heightLimit limit on height
     * @return scaled bitmap.
     */
    public static Bitmap getScaledBitmap(Bitmap bitmap, int widthLimit, int heightLimit) {
        if (bitmap == null) return null;

        return getScaledBitmap(
                bitmap,
                getGreatestRatio(bitmap, widthLimit, heightLimit)
        );
    }

    /**
     * Calculates width and height ratios between the dimensions of a bitmap and the dimensions
     * of the viewport.  Returns the largest of these, which can be used to scale said bitmap
     * to fit precisely inside a limited space.
     * @param bitmap original bitmap
     * @param width new width
     * @param height new height
     * @return largest ratio
     */
    private static float getGreatestRatio(Bitmap bitmap, int width, int height) {
        float widthRatio = (float) bitmap.getWidth() / (float) width;
        float heightRatio = (float) bitmap.getHeight() / (float) height;

        Log.d("OBS", "widthRatio " + widthRatio);
        Log.d("OBS", "heightRatio " + heightRatio);

        return widthRatio > heightRatio ? widthRatio : heightRatio;
    }

    /**
     * Get a scaled bitmap based on the greatest dimension ratio.
     * Calculates new width and height and gets a scaled bitmap based on these.
     * @param bitmap original bitmap
     * @param greatestDimenRatio greatest ratio of dimensions.
     * @return scaled bitmap
     */
    private static Bitmap getScaledBitmap(Bitmap bitmap, float greatestDimenRatio) {
        int width = (int) (bitmap.getWidth() / greatestDimenRatio);
        int height = (int) (bitmap.getHeight() / greatestDimenRatio);

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    /**
     * Get rotation (in degrees) from image metadata.
     * @param filePath filepath of image
     * @return rotation of the image.
     */
    public static float getRotation(String filePath) {

        int orientation;
        try {
            ExifInterface ei = new ExifInterface(filePath);
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            orientation = ExifInterface.ORIENTATION_NORMAL;
        }

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Rotate a bitmap based on given rotation in degrees.
     * @param bitmap original bitmap
     * @param rotation amount of rotation in degrees
     * @return rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float rotation) {
        if (bitmap == null) return null;

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Calculate the amount of sampling to do from file in order for the bitmap to be at least
     * as large as the specified dimensions.
     * @param options used to get outHeight and outWidth
     * @param reqWidth smallest width allowed
     * @param reqHeight smallest height allowed
     * @return value to use as the sample size
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Calculate in sample size for a section of a bitmap.
     * Divides required width and required height by respective ratios in order
     * to calculate the sample size based on the section, not the original bitmap.
     * @param options bitmap options
     * @param widthRatio ratio between section and original dimensions
     * @param heightRatio ratio between section and original dimensions
     * @param reqWidth smallest width allowed
     * @param reqHeight smallest height allowed
     * @return in sample size for section of a bitmap
     */
    public static int calculateInSampleSizeForBitmapSection(
            BitmapFactory.Options options, float widthRatio, float heightRatio, int reqWidth, int reqHeight
    ) {
        reqWidth /= widthRatio;
        reqHeight /= heightRatio;

        return calculateInSampleSize(options, reqWidth, reqHeight);
    }

    /**
     * Convenience method for getting the bounds of a bitmap from file.
     * @param filePath filepath of bitmap
     * @return options which will contain the width and height of the bitmap on file.
     */
    public static BitmapFactory.Options justDecodeBounds(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFile(filePath, options);

        return options;
    }
}
