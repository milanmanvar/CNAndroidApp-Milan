package com.thecn.app.tools.images;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Picture;
import com.thecn.app.models.content.Video;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.DisplayUtil;
import com.thecn.app.tools.controllers.PostViewController;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Used to layout a horizontal scroll view to show pictures/videos.
 * Uses {@link com.thecn.app.models.content.Picture} and {@link com.thecn.app.models.content.Video}
 * Shows either an image (Picture) or an image with a play icon over it (Video).
 * On click, pictures open {@link com.thecn.app.activities.PhotoGalleryViewerActivity} and videos open a web browser (or youtube app).
 */
public class PictureScrollViewLayouter {

    private HorizontalScrollView mHorizontalScrollView; //holds linear layout
    private LinearLayout mLayout; //holds image views
    private ArrayList<Picture> mPictures;
    private ArrayList<Video> mVideos;
    private String postId;
    private int thumbSize; //in pixels
    private boolean openPhotoGalleryOnClick = true;
    private int index; //index, e.g. in list

    private CallbackManager callbackManager;

    /**
     * New instance.  Gets references.
     * @param horizontalScrollView the scroll view
     * @param layout layout that holds the image views
     * @param pictures list of pictures
     * @param videos list of videos
     * @param manager callback manager
     */
    public PictureScrollViewLayouter(
            HorizontalScrollView horizontalScrollView,
            LinearLayout layout,
            ArrayList<Picture> pictures,
            ArrayList<Video> videos,
            CallbackManager manager
    ) {
        mHorizontalScrollView = horizontalScrollView;
        mLayout = layout;
        mPictures = pictures;
        mVideos = videos;
        callbackManager = manager;

        index = -1;
    }

    /**
     * Get index
     * @return index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set index
     * @param index index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Layout for use inside an adapter view used in a list view.
     * @param marginTotal max width of scroll view
     */
    public void doLayoutForListView(float marginTotal) {
        final NavigationActivity activity = privateDoLayout();
        if (activity == null) return;

        float maxScrollViewWidth = DisplayUtil.getMaxWidth(marginTotal, activity);
        float layoutWidth = mLayout.getChildCount() * (thumbSize + 10);

        //if view can scroll, add to sliding menu ignored view so that user won't open sliding menu
        //while scrolling through pictures
        if (layoutWidth < maxScrollViewWidth) {
            activity.getSlidingMenu().removeIgnoredView(mHorizontalScrollView);
        } else {
            activity.getSlidingMenu().addIgnoredView(mHorizontalScrollView);
        }
    }

    /**
     * Do regular layout.
     * @param marginTotal max width of scroll view
     */
    public void doLayout(float marginTotal) {
        final NavigationActivity activity = privateDoLayout();
        if (activity == null) return;

        float maxScrollViewWidth = DisplayUtil.getMaxWidth(marginTotal, activity);
        float layoutWidth = mLayout.getChildCount() * (thumbSize + 10);

        //if layout non scrollable, remove HSV and insert layout where it was
        //todo needs refinement.  Sometimes will not remove scrollview when it isn't necessary
        if (layoutWidth < maxScrollViewWidth) {
            if (mLayout.getChildCount() == 0) return;

            ViewGroup vGroup = (ViewGroup) mHorizontalScrollView.getParent();
            int index = vGroup.indexOfChild(mHorizontalScrollView);
            mHorizontalScrollView.removeView(mLayout);
            vGroup.removeView(mHorizontalScrollView);
            vGroup.addView(mLayout, index);
        }
    }

    /**
     * Layout pictures and videos.
     * @return
     */
    private NavigationActivity privateDoLayout() {
        final NavigationActivity activity = (NavigationActivity) callbackManager.getActivity();

        boolean displayPictures = (mPictures != null && mPictures.size() > 0)
                || (mVideos != null && mVideos.size() > 0);

        //check that there is proper data
        if (!displayPictures) {
            mHorizontalScrollView.setVisibility(View.GONE);
            return null;
        }

        //check that there are enough image views to show data.
        //if not, add more
        int childCount = mLayout.getChildCount();
        int numDataItems = getNumberOfDataItems();
        int difference = numDataItems - childCount;
        if (difference > 0) {
            for (int i = 0; i < difference; i++) {
                PostViewController.addPictureLayout(mLayout, activity);
            }
        }

        thumbSize = (int) activity.getResources().getDimension(R.dimen.post_view_pictures_height);
        mHorizontalScrollView.setVisibility(View.VISIBLE);
        mHorizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_BACKWARD);

        int viewPos = 0;

        //videos (first)
        if (mVideos != null) {
            for (int i = 0; i < mVideos.size(); i++) {
                final Video video = mVideos.get(i);
                View view = mLayout.getChildAt(viewPos);
                viewPos++;

                if (video == null || video.getVideoID() == null) {
                    //don't use this view, data was corrupt
                    view.setVisibility(View.GONE);
                    continue;
                }

                //show with play button
                view.setVisibility(View.VISIBLE);
                view.findViewById(R.id.play_button).setVisibility(View.VISIBLE);

                final String videoID = video.getVideoID();
                ImageView picView = (ImageView) view.findViewById(R.id.image);
                picView.setImageResource(R.color.black);

                //get loading progress view, set visible.
                View loadingView = view.findViewById(R.id.loading_image);
                loadingView.clearAnimation();
                loadingView.setVisibility(View.VISIBLE);
                loadingView.setAlpha(1f);

                //get video thumbnail from network
                MyVolley.ImageParams ilParams = new MyVolley.ImageParams(
                        "https://img.youtube.com/vi/" + videoID + "/default.jpg",
                        picView
                );
                ilParams.placeHolderID = ilParams.errorImageResourceID = R.color.black;
                ilParams.fade = false;
                ilParams.listener = getImageListener(ilParams, loadingView);

                MyVolley.loadImage(ilParams);

                picView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //increment the video view

                        MyVolley.act_log("view_content_video", postId, video.getViewURL());

                        //start web activity
                        Intent webIntent = new Intent(Intent.ACTION_VIEW);
                        webIntent.setData(Uri.parse(video.getViewURL()));
                        activity.startActivity(webIntent);
                    }
                });
            }
        }

        //layout pictures, after videos
        if (mPictures != null) {
            for (int i = 0; i < mPictures.size(); i++) {
                Picture picture = mPictures.get(i);
                View view = mLayout.getChildAt(viewPos);
                viewPos++;

                if (picture == null || picture.getPictureURL() == null) {
                    view.setVisibility(View.GONE);
                    continue;
                }

                view.setVisibility(View.VISIBLE);
                //don't show play button
                view.findViewById(R.id.play_button).setVisibility(View.GONE);

                ImageView picView = (ImageView) view.findViewById(R.id.image);
                picView.setImageResource(R.color.black);

                //get loading view and set loading
                View loadingView = view.findViewById(R.id.loading_image);
                loadingView.clearAnimation();
                loadingView.setVisibility(View.VISIBLE);
                loadingView.setAlpha(1f);

                //get picture thumbnail from network
                MyVolley.ImageParams params = new MyVolley.ImageParams(
                        picture.getPictureURL() + ".w320.jpg",
                        picView
                );
                params.placeHolderID = R.color.black;
                params.fade = false;
                params.listener = getImageListener(params, loadingView);

                MyVolley.loadImage(params);

                final int index = i;
                picView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //perform some action
                        onPictureAttachmentClick(mPictures, index, activity);
                    }
                });
            }
        }

        //for all unused views, set to GONE
        while (viewPos < childCount) {
            View view = mLayout.getChildAt(viewPos);
            view.setVisibility(View.GONE);
            viewPos++;
        }

        return activity;
    }

    /**
     * Gets an indexed image listener if the image params is indexed.  Otherwise gets a regular listener.
     * @param params image params to check index with
     * @param loadingView loading view to change visibility of after an image is set
     * @return either indexed or regular image listener
     */
    private MyVolley.MyImageListener getImageListener(MyVolley.ImageParams params, final View loadingView) {

        //valid index if greater than 0
        if (index >= 0) {
            params.index = index;
            return new MyVolley.MyIndexedImageListener(params) {
                @Override
                protected void setImage(Drawable drawable, boolean isImmediate) {
                    super.setImage(drawable, isImmediate);
                    onSetImage(isImmediate, loadingView);
                }
            };
        } else {
            return new MyVolley.MyImageListener(params) {
                @Override
                protected void setImage(Drawable drawable, boolean isImmediate) {
                    super.setImage(drawable, isImmediate);
                    onSetImage(isImmediate, loadingView);
                }
            };
        }
    }

    /**
     * Hide the loading view.  If immediate, set visibility.  Else, use an animation to fade it away.
     * @param isImmediate whether visibility should be set immediately
     * @param loadingView view to set visibility of
     */
    private void onSetImage(boolean isImmediate, View loadingView) {
        if (isImmediate) {
            loadingView.setVisibility(View.INVISIBLE);
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(loadingView, "alpha", 1f, 0f);
            animator.setDuration(MyVolley.MyImageListener.DEFAULT_FADE_TIME);
            animator.start();
        }
    }

    /**
     * Get the total count of data items for layouter
     * @return total count of data items
     */
    private int getNumberOfDataItems() {
        int count = 0;

        if (mPictures != null) count += mPictures.size();
        if (mVideos != null) count += mVideos.size();

        return count;
    }

    /**
     * Set whether {@link com.thecn.app.activities.PhotoGalleryViewerActivity} should
     * be opened when a picture is clicked.
     * @param shouldOpen true if should open activity
     */
    public void setOpenPhotoGalleryOnClick(boolean shouldOpen) {
        openPhotoGalleryOnClick = shouldOpen;
    }

    /**
     * Opens {@link com.thecn.app.activities.PhotoGalleryViewerActivity} if {@link #openPhotoGalleryOnClick} is true
     * @param pics pics to pass into activity
     * @param currentIndex current index to start pictures at
     * @param activity activity used to open {@link com.thecn.app.activities.PhotoGalleryViewerActivity}
     */
    private void onPictureAttachmentClick(ArrayList<Picture> pics, int currentIndex, NavigationActivity activity) {
        if (openPhotoGalleryOnClick) {
            activity.openPhotoGalleryViewerActivity(pics, currentIndex);
        }
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
