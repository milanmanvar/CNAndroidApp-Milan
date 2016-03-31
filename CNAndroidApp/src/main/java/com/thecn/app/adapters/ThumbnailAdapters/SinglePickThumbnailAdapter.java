package com.thecn.app.adapters.ThumbnailAdapters;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.thecn.app.R;

import java.util.ArrayList;

/**
 * Image thumbnail adapter used to display images in a grid.  Allows user to
 * only pick one image.
 */
public class SinglePickThumbnailAdapter extends BaseThumbnailAdapter {

    private ArrayList<String> filePaths;

    /**
     * Call super and init arraylist
     * @param fragment
     */
    public SinglePickThumbnailAdapter(Fragment fragment) {
        super(fragment);

        filePaths = new ArrayList<>();
    }

    @Override
    public String getItem(int index) {
        return filePaths.get(index);
    }

    @Override
    public int getCount() {
        return filePaths.size();
    }

    /**
     * Add all items to array list, update view
     * @param filePaths file paths of images to add
     */
    public void addAll(ArrayList<String> filePaths) {
        this.filePaths.addAll(filePaths);
        notifyDataSetChanged();
    }

    /**
     * Removes all items from array list
     */
    public void clear() {
        filePaths.clear();
    }

    /**
     * Loads image into image view from file.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.gallery_image, parent, false);
        }

        ImageView imageView = (ImageView) convertView;
        String filePath = getItem(position);
        loadBitmap(filePath, imageView);

        return convertView;
    }
}
