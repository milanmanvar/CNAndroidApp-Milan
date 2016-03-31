package com.thecn.app.adapters.ThumbnailAdapters;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.views.SquareView;

import java.util.ArrayList;

/**
 * Used to show a grid of images.  User can tap images to select them and
 * this will show a white check mark on the image.  Up to 15 images can be checked.
 */
public class MultiplePickThumbnailAdapter extends BaseThumbnailAdapter {

    private ArrayList<GalleryEntry> mEntries;
    private TextView checkedItemDisplay;

    /**
     * Associates file path with boolean checked variable.
     */
    private static class GalleryEntry {
        String filePath;
        boolean checked;

        public GalleryEntry(String filePath, boolean checked) {
            this.filePath = filePath;
            this.checked = checked;
        }
    }

    /**
     * Call super and set up array list.
     */
    public MultiplePickThumbnailAdapter(Fragment fragment) {
        super(fragment);

        mEntries = new ArrayList<>();
    }

    /**
     * Set the text view to be used to show the number of checked images.
     * @param view text view to show number checked images
     */
    public void setCheckedItemDisplay(TextView view) {
        checkedItemDisplay = view;
    }

    /**
     * Adds a new {@link com.thecn.app.adapters.ThumbnailAdapters.MultiplePickThumbnailAdapter.GalleryEntry}
     * Constructs from parameters
     * @param filePath file path of image
     * @param checked whether image should be checked
     */
    public void add(String filePath, boolean checked) {
        mEntries.add(new GalleryEntry(filePath, checked));
        notifyDataSetChanged();
    }

    /**
     * Empty array list
     */
    public void clear() {
        mEntries.clear();
    }

    @Override
    public GalleryEntry getItem(int position) {
        return mEntries.get(position);
    }

    @Override
    public int getCount() {
        return mEntries.size();
    }

    /**
     * Get the number of currently checked items.
     * @return number of checked items.
     */
    public int getNumCheckedItems() {
        int numCheckedItems = 0;
        for (GalleryEntry entry : mEntries)
            if (entry.checked) numCheckedItems++;

        return numCheckedItems;
    }

    /**
     * Unchecks all items in adapter
     */
    public void uncheckAll() {
        for (GalleryEntry entry : mEntries)
            entry.checked = false;
        notifyDataSetChanged();
        setCheckedItemDisplayText();
    }

    /**
     * Gets the filepaths of all checked items.
     * @return new array list of strings
     */
    public ArrayList<String> getCheckedItemFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (GalleryEntry entry : mEntries)
            if (entry.checked)
                filePaths.add(entry.filePath);

        return filePaths;
    }

    /**
     * Holds view references
     */
    public static class ViewHolder {
        ImageView imageView;
        CheckBox checkbox;
    }

    /**
     * Gets references, loads image into image view, sets checked display,
     * sets on click listener.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = getLayoutInflater().inflate(R.layout.gallery_image_checkbox, parent, false);

            holder.imageView = (SquareView.SquareImageView) convertView.findViewById(R.id.image);
            holder.checkbox = (SquareView.SquareCheckbox) convertView.findViewById(R.id.checkbox);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final GalleryEntry entry = getItem(position);
        final CheckBox mCheckBox = holder.checkbox;

        loadBitmap(entry.filePath, holder.imageView);

        holder.checkbox.setChecked(getItem(position).checked);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (entry.checked) {
                    mCheckBox.setChecked(false);
                    entry.checked = false;
                } else if (getNumCheckedItems() < 15) {
                    mCheckBox.setChecked(true);
                    entry.checked = true;
                } else
                    AppSession.showLongToast("Cannot add more than 15 photos");
                setCheckedItemDisplayText();
            }
        });

        return convertView;
    }

    /**
     * Sets {@link #checkedItemDisplay} to show a message
     * about how many items are checked.
     */
    public void setCheckedItemDisplayText() {
        if (checkedItemDisplay != null) {
            String middle = " Photo";
            String end = " Selected";

            int numCheckedItems = getNumCheckedItems();
            if (numCheckedItems != 1)
                middle += "s";

            checkedItemDisplay.setText(Integer.toString(numCheckedItems)
                    + middle + end);
        }
    }
}
