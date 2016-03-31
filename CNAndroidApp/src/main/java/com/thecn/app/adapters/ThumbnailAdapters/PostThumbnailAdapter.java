package com.thecn.app.adapters.ThumbnailAdapters;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.thecn.app.R;
import com.thecn.app.views.SquareView;

import java.util.ArrayList;

/**
 * Used in {@link com.thecn.app.activities.createpost.CreatePostActivity} to
 * display images that are selected to be uploaded with the post.
 */
public class PostThumbnailAdapter extends BaseThumbnailAdapter {

    private ArrayList<String> mFilepaths; //file paths of images.

    private Button removeAllPhotosButton;

    /**
     * Holds references to views
     */
    private static class ViewHolder {
        ImageView imageView;
        ImageView cancelView;
    }

    /**
     * Call super, set up array list
     */
    public PostThumbnailAdapter(Fragment fragment) {
        super(fragment);

        mFilepaths = new ArrayList<>();
    }

    /**
     * Set the button that when clicked should remove all photos.
     * @param button a button
     */
    public void setRemoveAllPhotosButton(Button button) {
        removeAllPhotosButton = button;
    }

    /**
     * Adds filepath to list, enables remove all button
     * @param filePath file path to add
     */
    public void add(String filePath) {
        mFilepaths.add(filePath);
        setButtonEnabled();
        notifyDataSetChanged();
    }

    @Override
    public String getItem(int position) {
        return mFilepaths.get(position);
    }

    @Override
    public int getCount() {
        return mFilepaths.size();
    }

    /**
     * Remove file path at position
     * @param position position of file path to remove
     */
    public void remove(int position) {
        mFilepaths.remove(position);
        setButtonEnabled();
        notifyDataSetChanged();
    }

    /**
     * Removes all file paths (no images shown)
     * Sets button disabled (see {@link #setButtonEnabled()}
     */
    public void removeAll() {
        mFilepaths.clear();
        setButtonEnabled();
        notifyDataSetChanged();
    }

    /**
     * Get all file paths as string array
     * @return string array
     */
    public String[] getFilePaths() {
        String[] filePaths = new String[getCount()];
        for (int i = 0; i < getCount(); i++) {
            filePaths[i] = mFilepaths.get(i);
        }
        return filePaths;
    }

    /**
     * load image, set on click listener to cancel button.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = getLayoutInflater().inflate(R.layout.post_image_thumbnail, parent, false);

            holder = new ViewHolder();

            holder.imageView = (SquareView.SquareImageView) convertView.findViewById(R.id.image);
            holder.cancelView = (ImageView) convertView.findViewById(R.id.cancel);

            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();

        loadBitmap(getItem(position), holder.imageView);

        final int pos = position;

        holder.cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(pos);
            }
        });

        return convertView;
    }

    /**
     * Sets the remove all photos button enabled IF there is an item in the adapter.
     * Otherwise, sets disabled.
     */
    public void setButtonEnabled() {
        removeAllPhotosButton.setEnabled(getCount() > 0);
    }
}
