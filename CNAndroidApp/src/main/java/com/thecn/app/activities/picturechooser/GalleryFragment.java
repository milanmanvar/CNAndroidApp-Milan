package com.thecn.app.activities.picturechooser;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.ThumbnailAdapters.SinglePickThumbnailAdapter;
import com.thecn.app.tools.images.ImageUtil;
import com.thecn.app.tools.ServiceChecker;
import com.thecn.app.tools.text.TextUtil;

import java.util.ArrayList;

/**
* Used to pick a picture from the user's public files.
*/
public class GalleryFragment extends BaseFragment {

    public static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final String CAMERA = "Take picture";
    private static final String ERROR = "Error getting image from file";

    private static final String TITLE = "Choose a picture";
    private SinglePickThumbnailAdapter singlePickThumbnailAdapter;

    //uri to picture
    private Uri uri;

    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Create root view and grid view inside it.  Set the number of columns of the
     * grid view and set its adapter ({@link com.thecn.app.adapters.ThumbnailAdapters.SinglePickThumbnailAdapter}).
     * When a picture is clicked, push {@link com.thecn.app.activities.picturechooser.CropImageFragment}.
     * When TAKE PICTURE is clicked, try to open a camera activity.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        GridView view = (GridView) inflater.inflate(R.layout.gallery_grid, container, false);

        singlePickThumbnailAdapter = new SinglePickThumbnailAdapter(this);

        int orientation = getActivity().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            view.setNumColumns(5);
        else view.setNumColumns(3);

        view.setAdapter(singlePickThumbnailAdapter);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String filePath = singlePickThumbnailAdapter.getItem(position);
                pushCropImageFragment(filePath);
            }
        });

        layout.addView(view);
        LinearLayout.LayoutParams gridParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        //grid layout fills up the rest of the unused layout.
        gridParams.weight = 1;
        gridParams.height = 0;

        Button button = (Button) getActivity().getLayoutInflater().inflate(R.layout.profile_pic_activity_button, null, false);
        button.setText(CAMERA);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushTakePhotoActivity();
            }
        });

        RelativeLayout buttonLayout = new RelativeLayout(getActivity());
        buttonLayout.setBackgroundResource(R.color.nav_bar);
        buttonLayout.addView(button);
        ViewGroup.LayoutParams buttonParams = button.getLayoutParams();
        buttonParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        buttonParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        layout.addView(buttonLayout);
        LinearLayout.LayoutParams bLayoutParams = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
        bLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        bLayoutParams.height = (int) getResources().getDimension(R.dimen.fifty_dip);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ServiceChecker.isStorageAvailable()) getImages();
    }

    /**
     * Get filepaths of images from external storage.
     */
    private void getImages() {
        ArrayList<String> filePaths = ImageUtil.getImagesFromExternalStorage(getActivity());
        singlePickThumbnailAdapter.clear();
        singlePickThumbnailAdapter.addAll(filePaths);
    }

    /**
     * If the camera activity returned a URI, open {@link com.thecn.app.activities.picturechooser.CropImageFragment}
     * with it.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || requestCode != REQUEST_IMAGE_CAPTURE) return;
        if (data == null || data.getData() == null) return;
        try {
            String filePath = getPath(uri);
            if (filePath != null) {
                pushCropImageFragment(filePath);
            }
        } catch (Exception e) {
            AppSession.showShortToast(ERROR);
        }
    }

    /**
     * Gets a timestamped filepath to use for the camera's captured photo.
     * @return timestamped URI
     */
    private Uri getTimestampUri() {
        String timeStamp = TextUtil.getTimeStamp();
        String imageFileName = "THECN_IMG_CAPTURE_" + timeStamp + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, imageFileName);

        return getActivity().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * Push a valid camera activity if one exists.  Pass in the URI to use to save
     * the photo.
     */
    private void pushTakePhotoActivity() {
        PackageManager pkgManager = getActivity().getPackageManager();

        if (pkgManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(pkgManager) != null) {
                uri = getTimestampUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            AppSession.showLongToast("Unable to use the camera.  Permission must be granted.");
        }
    }

    /**
     * Get String file path from a URI
     * @param uri the uri that stores the filename
     * @return file path
     */
    private String getPath(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }

        return null;
    }

    /**
     * Pushes {@link com.thecn.app.activities.picturechooser.CropImageFragment}
     * @param filePath file path for {@link com.thecn.app.activities.picturechooser.CropImageFragment}
     *                 to use to get the bitmap.
     */
    private void pushCropImageFragment(String filePath) {
        Fragment fragment = CropImageFragment.getInstance(filePath);
        getPictureChooseActivity().replace(fragment);
    }
}
