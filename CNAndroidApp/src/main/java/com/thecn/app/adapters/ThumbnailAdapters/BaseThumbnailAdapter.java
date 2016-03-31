package com.thecn.app.adapters.ThumbnailAdapters;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.thecn.app.tools.images.ImageUtil;
import com.thecn.app.tools.images.MyPicasso;

import java.io.File;

/**
 * Base adapter class for showing grids of images from files
 */
public abstract class BaseThumbnailAdapter extends BaseAdapter {

    private int dimension; //dimension of image desired
    private Fragment mFragment;

    private boolean noFade; //flag for turning off fade

    /**
     * New instance
     * @param fragment fragment for getting resources
     */
    public BaseThumbnailAdapter(Fragment fragment) {
        mFragment = fragment;
        dimension = ImageUtil.getGalleryItemDimension();
    }

    public Fragment getFragment() {
        return mFragment;
    }

    /**
     * Gets layout inflater of fragment's activity
     * @return layout inflater
     */
    public LayoutInflater getLayoutInflater() {
        return mFragment.getActivity().getLayoutInflater();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Loads bitmap from filepath into image view using Picasso
     * @param filePath location of image
     * @param imageView imageview where image should be loaded.
     */
    protected void loadBitmap(String filePath, ImageView imageView) {
        File file = new File(filePath);

        Picasso picasso = MyPicasso.getPicasso();
        RequestCreator creator = picasso.load(file);
        if (noFade) {
            creator.noFade();
        }

        creator.resize(dimension, dimension).centerCrop().into(imageView);
    }

    /**
     * Set no fade flag
     * @param noFade whether or not to fade images
     */
    public void setNoFade(boolean noFade) {
        this.noFade = noFade;
    }

    /**
     * Get dimension
     * @return dimension of pictures in adapter
     */
    public int getDimension() {
        return dimension;
    }
}
