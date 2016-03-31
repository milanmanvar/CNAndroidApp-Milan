package com.thecn.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.adapters.ThumbnailAdapters.MultiplePickThumbnailAdapter;
import com.thecn.app.tools.images.ImageUtil;
import com.thecn.app.tools.ServiceChecker;

import java.util.ArrayList;

/**
 * Activity for picking multiple pictures from a gallery.  Uses check marks to show
 * the user that he/she has selected certain pictures.
 * todo show directories?
 */
public class MultiplePickGalleryActivity extends FragmentActivity {

    /**
     * Init and add fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_container);

        if (savedInstanceState == null) {

            Fragment fragment = new GalleryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    /**
     * Persists data and shows a grid view, buttons, and number of selected images indicator.
     * Used to pick pictures.  Up to 15 can be selected.
     */
    public static class GalleryFragment extends Fragment {
        private MultiplePickThumbnailAdapter multiplePickThumbnailAdapter;
        private ArrayList<String> savedFilePaths;//on creation, set to
        //an intent extra.  On pause, set to currently checked image file paths

        /**
         * Set up, get passed in file paths.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            savedFilePaths = getActivity().getIntent().getStringArrayListExtra("FILE_PATHS");
        }

        /**
         * Get view references, set up view, set up grid with adapter.  Deselect all button
         * will deselect all images.  Submit button finished activity with result.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_gallery_multiple, container, false);

            Button resultButton = (Button) view.findViewById(R.id.return_button);
            resultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    returnWithResult();
                }
            });

            multiplePickThumbnailAdapter = new MultiplePickThumbnailAdapter(this);

            TextView checkedItemDisplay = (TextView) view.findViewById(R.id.selected_text);
            multiplePickThumbnailAdapter.setCheckedItemDisplay(checkedItemDisplay);

            Button deselectAllButton = (Button) view.findViewById(R.id.deselect_all_button);
            deselectAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    multiplePickThumbnailAdapter.uncheckAll();
                }
            });

            GridView imageGrid = (GridView) view.findViewById(R.id.IMAGE_GRID);

            //set number of columns in grid depending on orientation
            int orientation = getActivity().getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                imageGrid.setNumColumns(5);
            else imageGrid.setNumColumns(3);

            imageGrid.setAdapter(multiplePickThumbnailAdapter);

            return view;
        }

        /**
         * Get images from files, set checked items
         */
        @Override
        public void onResume() {
            super.onResume();
            if (ServiceChecker.isStorageAvailable()) getImages();
            multiplePickThumbnailAdapter.setCheckedItemDisplayText();
        }

        /**
         * Save the file paths
         */
        @Override
        public void onPause() {
            super.onPause();
            savedFilePaths = multiplePickThumbnailAdapter.getCheckedItemFilePaths();
        }

        /**
         * Set result (list of file paths chosen) and finish activity.
         */
        private void returnWithResult() {
            Intent returnIntent = new Intent();

            ArrayList<String> filePaths = multiplePickThumbnailAdapter.getCheckedItemFilePaths();
            returnIntent.putStringArrayListExtra("FILE_PATHS", filePaths);

            Activity activity = getActivity();
            activity.setResult(RESULT_OK, returnIntent);
            activity.finish();
        }

        /**
         * Gets list of filepaths of all images available on device.  Uses savedFilePaths
         * to check the images that have been selected.
         * todo why is this not in a separate thread?
         */
        private void getImages() {
            ArrayList<String> filePaths = ImageUtil.getImagesFromExternalStorage(getActivity());
            multiplePickThumbnailAdapter.clear();

            if (savedFilePaths != null) {
                for (String filePath : filePaths) {
                    boolean checked = isAlreadyAdded(filePath);
                    multiplePickThumbnailAdapter.add(filePath, checked);
                }
            } else {
                for (String filePath : filePaths) {
                    multiplePickThumbnailAdapter.add(filePath, false);
                }
            }
        }

        /**
         * Checks if filePath is in {@link #savedFilePaths}
         * @param filePath filepath to check
         * @return true if is in {@link #savedFilePaths}
         */
        private boolean isAlreadyAdded(String filePath) {
            for (String addedPath : savedFilePaths) {
                if (filePath.equals(addedPath)) {
                    return true;
                }
            }

            return false;
        }
    }
}
