package com.thecn.app.tools.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.course.FullScreenWebDetail;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.activities.poll.PollActivity;
import com.thecn.app.adapters.AttachmentAdapter;
import com.thecn.app.activities.post.PostFragment;
import com.thecn.app.adapters.LinkAdapter;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.models.content.Link;
import com.thecn.app.models.content.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.text.TextUtil;
import com.thecn.app.tools.images.PictureScrollViewLayouter;
import com.thecn.app.tools.volley.MyVolley;
import com.thecn.app.views.list.MyDialogListView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to control the views involved in displaying post informatino.
 * Acts as a view holder for a list view adapter and contains methods
 * for performing changes after initialization.
 */
public class PostViewController {

    private Post mPost;

    private boolean isFullView;

    private View mRootView;

    private TextView postTitleTextView;
    private TextView postContentTextView;

    private TextView usernameTextView;
    private TextView cnNumberTextView;
    private TextView postTimeTextView;
    private TextView postFromTextView;
    private TextView userPositionTextView;
    private TextView attachmentTextView;
    private TextView linkTextView;

    private LinearLayout likeButton;
    private ImageView thumbsUpImg;
    private TextView likeText;
    private LinearLayout reflectButton;
    private TextView reflectionText;
    private ImageButton moreOptionsBtn;

    private ImageView userAvatar;
    private ImageView userFlag;

    private View pollButton;

    private HorizontalScrollView picturesScrollView;
    private LinearLayout picturesScrollViewLayout;
    private PictureScrollViewLayouter mPictureScrollViewLayouter;

    private int userIconWidth, flagIconHeight, flagIconWidth;

    private boolean viewSetUp;

    private CallbackManager<? extends Fragment> callbackManager;

    /**
     * New instance of controller.  Inflates the post view and adds to parent
     * @param manager for networking
     * @param parent parent view to contain post view
     */
    public PostViewController(CallbackManager<? extends Fragment> manager, ViewGroup parent) {
        callbackManager = manager;
        View rootView = callbackManager.getActivity().getLayoutInflater().inflate(R.layout.post_view, parent, false);

        init(rootView);
    }

    /**
     * New instance of controller.  Uses root view provided as post view.
     * @param manager for networking
     * @param rootView view to be used as post view
     */
    public PostViewController(CallbackManager<? extends Fragment> manager, View rootView) {
        callbackManager = manager;

        init(rootView);
    }

    /**
     * Get view references from root view and set up flags and dimensions.
     * @param rootView view to be used as post view
     */
    private void init(View rootView) {
        mRootView = rootView;

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        isFullView = false;
        viewSetUp = false;

        postTitleTextView = (TextView) mRootView.findViewById(R.id.post_title);
        postContentTextView = (TextView) mRootView.findViewById(R.id.post_content);

        usernameTextView = (TextView) mRootView.findViewById(R.id.content_text);
        usernameTextView.setTypeface(typeface);
        cnNumberTextView = (TextView) mRootView.findViewById(R.id.headline);
        postTimeTextView = (TextView) mRootView.findViewById(R.id.post_time);
        postFromTextView = (TextView) mRootView.findViewById(R.id.post_from_field);
        userPositionTextView = (TextView) mRootView.findViewById(R.id.user_position_text);
        attachmentTextView = (TextView) mRootView.findViewById(R.id.attachments_text);
        linkTextView = (TextView) mRootView.findViewById(R.id.links_text);

        userAvatar = (ImageView) mRootView.findViewById(R.id.user_avatar);
        userFlag = (ImageView) mRootView.findViewById(R.id.user_flag);

        pollButton = mRootView.findViewById(R.id.poll_button);

        likeButton = (LinearLayout) mRootView.findViewById(R.id.like_operate_btn);
        thumbsUpImg = (ImageView) likeButton.findViewById(R.id.thumbs_up_img);
        likeText = (TextView) likeButton.findViewById(R.id.like_text);
        reflectButton = (LinearLayout) mRootView.findViewById(R.id.reflect_operate_btn);
        reflectionText = (TextView) reflectButton.findViewById(R.id.reflect_text);
        moreOptionsBtn = (ImageButton) mRootView.findViewById(R.id.more_options_btn);

        picturesScrollView = (HorizontalScrollView) mRootView.findViewById(R.id.picturesScrollView);
        picturesScrollViewLayout = (LinearLayout) mRootView.findViewById(R.id.picturesScrollViewLayout);
        Resources r = getActivity().getResources();

        //add fifteen image views as a starting point so that there are usually enough views
        //to display a post's pictures/videos
        for (int i = 0; i < 15; i++) {
            addPictureLayout(picturesScrollViewLayout, callbackManager.getActivity());
        }

        userIconWidth = (int) r.getDimension(R.dimen.user_icon_width);
        flagIconHeight = (int) r.getDimension(R.dimen.user_flag_height);
        flagIconWidth = (int) r.getDimension(R.dimen.user_flag_width);
    }

    /**
     * Inflate a picture layout view and add it to given parent
     * @param parent parent to add view to
     * @param activity used to get inflater
     */
    public static void addPictureLayout(LinearLayout parent, Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View child = inflater.inflate(R.layout.picture_layout, parent, false);

        parent.addView(child);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
        params.setMargins(0, 0, 10, 0);
    }

    /**
     * Get fragment from callback manager
     * @return fragment reference
     */
    private Fragment getFragment() {
        return callbackManager.getObject();
    }

    /**
     * Get activity from callback manager
     * @return activity reference
     */
    private Activity getActivity() {
        return callbackManager.getActivity();
    }

    /**
     * Get cast activity from callback manager
     * @return cast activity
     */
    private NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    /**
     * Set the data source for the controller
     * @param post post to use as data source
     */
    public void setPost(Post post) {
        mPost = post;
    }

    /**
     * Set whether truncate post or display all of its information.
     * @param isFullView true to display all information.
     */
    public void setFullView(boolean isFullView) {
        this.isFullView = isFullView;
    }

    /**
     * Get the post view's root view.
     * @return root view
     */
    public View getRootView() {
        return mRootView;
    }

    /**
     * Set up view without specifying index
     * @param post post to use as data source
     */
    public void setUpView(Post post) {
        setUpView(post, 0);
    }

    /**
     * Set up view, specifying its index in list
     * @param post post to use as data source
     * @param index index in list
     */
    public void setUpView(Post post, int index) {
        mPost = post;

        adaptViewForPost();

        //set onclick selector if in list
        if (!isFullView) {
            mRootView.setBackgroundResource(R.drawable.post_onclick_selector);
        }

        //set user name
        try {
            usernameTextView.setText(mPost.getUser().getDisplayName());
        } catch (NullPointerException e) {
            usernameTextView.setText("");
        }

        //set post time
        try {
            postTimeTextView.setText(mPost.getTimeText());
        } catch (NullPointerException e) {
            postTimeTextView.setText("");
        }

        //set cn number of user.
        try {
            cnNumberTextView.setText(mPost.getUser().getCNNumber());
        } catch (NullPointerException e) {
            cnNumberTextView.setText("");
        }

        //set where post came from
        postFromTextView.setText(mPost.getPostFromText());

        //set user position (instructor, admin, etc.) in colored text view below avatar
        boolean hideUserPosition;
        try {
            String userPosition = mPost.getUserPosition();

            hideUserPosition = userPosition.length() == 0;
            if (!hideUserPosition) {
                userPositionTextView.setText(userPosition.toUpperCase().replace("_", " "));

                //change drawable depending on type of position
                if (userPosition.equalsIgnoreCase("instructor")) {
                    userPositionTextView.setBackgroundResource(R.drawable.user_position_instructor_display);
                } else if (userPosition.equalsIgnoreCase("cn admin")) {
                    userPositionTextView.setBackgroundResource(R.drawable.user_position_admin_display);
                } else {
                    userPositionTextView.setBackgroundResource(R.drawable.user_position_display);
                }
            }
        } catch (NullPointerException e) {
            hideUserPosition = true;
        }
        if (hideUserPosition) {
            userPositionTextView.setVisibility(View.GONE);
        } else {
            userPositionTextView.setVisibility(View.VISIBLE);
        }

        //set up like button
        updatePostViewLikes(true);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeButtonClick();
            }
        });

        //set up reflection button
        updateReflectionText();
        reflectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReflectButtonClicked(mPost);
            }
        });

        //set up more options button
        moreOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMoreOptionsBtnClicked(mPost);
            }
        });

        //get user's avatar from server
        try {
            String avatarUrl = mPost.getUser().getAvatar().getView_url() + ".w160.jpg";
            MyVolley.loadIndexedUserImage(avatarUrl, userAvatar, index, userIconWidth);

        } catch (NullPointerException e) {
            userAvatar.setImageResource(R.drawable.default_user_icon);
        }

        //set avatar to open profile on click if user is not admin
        if (!mPost.getUser().isSystemUser()) {
            userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationActivity().openProfileByID(mPost.getUser().getId());
                }
            });
        } else {
            userAvatar.setOnClickListener(null);
        }

        //get user's flag image
        try {
            MyVolley.ImageParams params = new MyVolley.ImageParams(
                    mPost.getUser().getCountry().getFlagURL(),
                    userFlag
            );
            params.placeHolderID = params.errorImageResourceID = R.color.white;
            params.maxWidth = flagIconWidth;
            params.maxHeight = flagIconHeight;
            params.index = index;

            MyVolley.loadImage(params);

        } catch (NullPointerException e) {
            // no country flag
        }

        setUpPictures(index);
        setUpAttachments();
        setUpLinks();

        viewSetUp = true;
    }

    /**
     * Set up a text view to show the number of attachments.
     * When clicked, it will show a dialog with clickable links
     * to download attachments.
     */
    private void setUpAttachments() {
        ArrayList<Attachment> attachments = mPost.getAttachments();
        if (attachments == null || attachments.size() < 1) {
            attachmentTextView.setVisibility(View.GONE);
            return;
        }

        String text = TextUtil.getPluralityString(attachments.size(), "attachment");
        SpannableString str = TextUtil.getLinkStyleSpannableString(text, getActivity());
        attachmentTextView.setText(str);
        attachmentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAttachments();
            }
        });
        attachmentTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Set up a text view to show number of links on this post.
     * When clicked, it shows a dialog that allows user to click on links.
     */
    private void setUpLinks() {
        ArrayList<Link> links = mPost.getLinks();
        if (links == null || links.size() < 1) {
            linkTextView.setVisibility(View.GONE);
            return;
        }

        String text = TextUtil.getPluralityString(links.size(), "link");
        SpannableString str = TextUtil.getLinkStyleSpannableString(text, getActivity());
        linkTextView.setText(str);
        linkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinks();
            }
        });
        linkTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Uses {@link com.thecn.app.tools.images.PictureScrollViewLayouter} to
     * set up picture scroll view layout.
     * @param index used to properly load images for different indices
     */
    private void setUpPictures(int index) {

        mPictureScrollViewLayouter =
                new PictureScrollViewLayouter(
                        picturesScrollView, picturesScrollViewLayout,
                        mPost.getPictures(), mPost.getVideos(), callbackManager
                );

        mPictureScrollViewLayouter.setIndex(index);
        mPictureScrollViewLayouter.doLayoutForListView(48f);
        mPictureScrollViewLayouter.setPostId(mPost.getId());
    }

    /**
     * Shows a dialog with clickable attachments listed for download.
     */
    private void showAttachments() {
        if (checkVerification()) {
            return;
        }

        AttachmentFragment fragment = AttachmentFragment.getInstance(mPost);
        FragmentActivity activity = (FragmentActivity) getActivity();
        fragment.show(activity.getSupportFragmentManager(), "attachment_dialog");
    }

    /**
     * Dialog fragment that shows clickable attachments
     */
    public static class AttachmentFragment extends DialogFragment {
        private static final String KEY = "post";

        private static final String TITLE = "Post Attachments";

        /**
         * Get new instance with arguments
         * @param post post with attachments
         * @return new instance of this class
         */
        public static AttachmentFragment getInstance(Post post) {
            Bundle args = new Bundle();
            args.putSerializable(KEY, post);

            AttachmentFragment fragment = new AttachmentFragment();
            fragment.setArguments(args);
            return fragment;
        }

        /**
         * Create dialog with list view to show attachments and handle click events.
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Post post = (Post) getArguments().getSerializable(KEY);
            ArrayList<Attachment> attachments = post.getAttachments();
            ListView listView = MyDialogListView.getListViewForDialog(getActivity());
            AttachmentAdapter adapter = new AttachmentAdapter(getActivity(), attachments);
            listView.setAdapter(adapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(TITLE)
                    .setView(listView);

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);

            return dialog;
        }
    }

    /**
     * Show a dialog with clickable links (urls)
     */
    private void showLinks() {
        if (checkVerification()) {
            return;
        }

        LinkFragment fragment = LinkFragment.getInstance(mPost);
        FragmentActivity activity = (FragmentActivity) getActivity();
        fragment.show(activity.getSupportFragmentManager(), "link_dialog");
    }

    /**
     * Dialog fragment used to show a list of clickable links associated with a post.
     */
    public static class LinkFragment extends DialogFragment {
        private static final String KEY = LinkFragment.class.getName() + ".post";

        private static final String TITLE = "Post Links";

        private static final String INSTALL = "Please install a web browser";
        private static final String INVALID = "Invalid URL";
        private static final String SCHEME = "http://";

        /**
         * Get new instance with arguments
         * @param post post with links
         * @return new instance of this class
         */
        public static LinkFragment getInstance(Post post) {
            Bundle args = new Bundle();
            args.putSerializable(KEY, post);
            LinkFragment f = new LinkFragment();
            f.setArguments(args);
            return f;
        }

        /**
         * Create a dialog with a list view that will show links associated with
         * this post and will handle on click events for each of those links (opens a
         * browser app).
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Post post = (Post) getArguments().getSerializable(KEY);
            ArrayList<Link> links = post.getLinks();
            ListView listView = MyDialogListView.getListViewForDialog(getActivity());
            LinkAdapter adapter = new LinkAdapter(getActivity(), links);
            adapter.setOnLinkClickListener(new LinkAdapter.OnLinkClickListener() {
                @Override
                public void onLinkClick(String url) {
                    Uri uri = Uri.parse(url);
                    String scheme = uri.getScheme();
                    if (scheme == null || scheme.isEmpty()) {
                        //add scheme if not present
                        uri = Uri.parse(SCHEME + url);
                    }

                    try {
                        //send browser intent
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        getActivity().startActivity(intent);

                    } catch (ActivityNotFoundException e) {
                        //show error message
                        String message =
                                Patterns.WEB_URL.matcher(uri.toString()).matches() ?
                                        INSTALL : INVALID;

                        AppSession.showLongToast(message);
                    }
                }
            });
            listView.setAdapter(adapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(TITLE)
                    .setView(listView);

            Dialog d = builder.create();
            d.setCanceledOnTouchOutside(true);

            return d;
        }
    }

    /**
     * Enable or disable the like button
     * @param enabled whether button enabled or disabled
     */
    public void enableLikeButton(final boolean enabled) {
        likeButton.post(new Runnable() {
            @Override
            public void run() {
                likeButton.setEnabled(enabled);
            }
        });
    }

    /**
     * Enable or disable reflect button
     * @param enabled whether button enabled or disabled
     */
    public void enableReflectButton(boolean enabled) {
        reflectButton.setEnabled(enabled);
    }

    /**
     * Change the view for the type of post (post, poll, event, etc.)
     */
    private void adaptViewForPost() {

        Post.Type type = mPost.getEnumType();

        postContentTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if (type == Post.Type.POLL) {
            pollButton.setVisibility(View.VISIBLE);

            View.OnClickListener listener;

            if (!isFullView) {
                //open poll activity if this post is part of a list
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //getNavigationActivity().openPollPage(mPost, 1);
                        final Intent intent = new Intent(getActivity(), FullScreenWebDetail.class);
                        intent.putExtra("post", mPost);
                        getActivity().startActivity(intent);
                    }
                };
            } else if (getActivity() instanceof PollActivity) {
                //set view pager index to first item if this is a poll activity
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //PollActivity activity = (PollActivity) getActivity();
                        //activity.setViewPagerIndex(1);

                        final Intent intent = new Intent(getActivity(), FullScreenWebDetail.class);
                        intent.putExtra("post", mPost);
                        getActivity().startActivity(intent);
                    }
                };
            } else {
                listener = null;
            }

            pollButton.setOnClickListener(listener);

        }else if(type == Post.Type.CLASSCAST){

            pollButton.setVisibility(View.VISIBLE);
            TextView tvButton = (TextView) pollButton.findViewById(R.id.txtGotoPoll);
            ImageView ivGraph = (ImageView) pollButton.findViewById(R.id.ivGraph);
            ivGraph.setVisibility(View.GONE);
            tvButton.setText("Enter ClassCast");

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //PollActivity activity = (PollActivity) getActivity();
                    //activity.setViewPagerIndex(1);
                    //Toast.makeText(getActivity(),mPost.getId(),Toast.LENGTH_LONG).show();

                    //getNavigationActivity().openClassCastPage(mPost);

                    final Intent intent = new Intent(getActivity(), FullScreenWebDetail.class);
                    intent.putExtra("post", mPost);
                    getActivity().startActivity(intent);

                }
            };


            pollButton.setOnClickListener(listener);

        }
        else {
            //this is not a poll
            pollButton.setVisibility(View.GONE);
        }

        showTitleIfExists();

        postContentTextView.setText(mPost.getContentText());
    }

    /**
     * Shows title if there is a title and the post is not a quiz.
     * Set height of title to zero instead of
     * GONE, so that content of post will still know where
     * to insert itself if there is no title.
     */
    private void showTitleIfExists() {

        if (mPost.getEnumType() != Post.Type.QUIZ && mPost.getProcessedTitle() != null) {
            postTitleTextView.setText(mPost.getProcessedTitle());
            setViewHeight(postTitleTextView, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            setViewHeight(postTitleTextView, 0);
        }
    }

    /**
     * Sets the height of a given view
     * @param view view to set height of
     * @param height height to set
     */
    private void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
    }

    /**
     * Sends a network request to server to either like or
     * unlike a post.  Sets necessary view states as well.
     */
    public void onLikeButtonClick() {
        if (checkVerification()) {
            return;
        }

        enableLikeButton(false);

        if (mPost.isLiked()) {
            //show immediate change
            setLiked(false);
            updatePostViewLikes(false);

            //send request to unlike post
            PostStore.unlikePost(mPost, new ResponseCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    if (!StoreUtil.success(response)) {
                        AppSession.showLongToast("Server error");
                        reset();
                    }

                    enableLikeButton(true);
                }

                @Override
                public void onError(VolleyError error) {
                    StoreUtil.showExceptionMessage(error);
                    reset();

                    enableLikeButton(true);
                }

                /**
                 * Reset button to what it was before last click
                 */
                private void reset() {
                    setLiked(true);
                    updatePostViewLikes(false);
                }
            });
        } else {
            //show immediate change
            setLiked(true);
            updatePostViewLikes(false);

            //send like request to server
            PostStore.likePost(mPost, new ResponseCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    if (!StoreUtil.success(response)) {
                        AppSession.showLongToast("Server error");
                        reset();
                    }

                    enableLikeButton(true);
                }

                @Override
                public void onError(VolleyError error) {
                    StoreUtil.showExceptionMessage(error);
                    reset();

                    enableLikeButton(true);
                }

                /**
                 * Set button to what it was before
                 */
                private void reset() {
                    setLiked(false);
                    updatePostViewLikes(false);
                }
            });
        }
    }

    /**
     * Set a post as either liked or unliked.  Update associated views.
     * @param liked whether post is liked or unliked.
     */
    public void setLiked(boolean liked) {
        Fragment fragment = getFragment();

        try {
            mPost.setLiked(liked);

            int numLikes = mPost.getCount().getLikes();
            if (liked) numLikes++;
            else numLikes--;
            mPost.getCount().setLikes(numLikes);

            String id = fragment == null ? "NULL" : fragment.toString();
            //broadcast change
            PostChangeController.sendUpdatedBroadcast(mPost, id);
        } catch (NullPointerException e) {
            //data not there...
        }
    }

    /**
     * Update the view to show change in reflection data
     */
    public void updateReflectionText() {
        int count = mPost.getCount().getReflections();

        String head = TextUtil.getCountString(count);
        String tail = " Reflection";
        if (count != 1) {
            tail += "s";
        }

        String text = head + tail;

        reflectionText.setText(text);
    }

    /**
     * Update the view to reflect liked status of post
     * @param immediate whether to change the view immediately
     *                  or post an animation to the view
     */
    public void updatePostViewLikes(boolean immediate) {
        try {
            final boolean liked = mPost.isLiked();

            if (immediate) {
                likeButton.setSelected(liked);
                setLikeButtonContents(liked);
            } else {
                mRootView.post(new Runnable() {
                    @Override
                    public void run() {
                        likeButton.setSelected(liked);
                        setLikeButtonContents(liked);
                        playScaleAnimation(thumbsUpImg);
                    }
                });
            }

        } catch (NullPointerException e) {
            // data not there...
        }
    }

    /**
     * Set the contents of the like button.
     * This sets the thumbs up graphic and the color and content of the text.
     * @param liked whether to show view as liked or unliked.
     */
    private void setLikeButtonContents(boolean liked) {
        //only check for null fragment if view hasn't been set up
        if (viewSetUp && getFragment() == null) return;
        Activity a = getActivity();
        if (a == null) return;

        Resources r = a.getResources();
        int src, color;

        //set icon and color
        if (liked) {
            src = R.drawable.thumb_up_icon_selected;
            color = r.getColor(R.color.link_color);
        } else {
            src = R.drawable.thumb_up_icon;
            color = r.getColor(R.color.lesser_post_button_text);
        }

        thumbsUpImg.setImageResource(src);

        //set count of likes
        int count = mPost.getCount().getLikes();
        if (count > 0) {
            String countString = TextUtil.getCountString(count);
            likeText.setText(countString);
            likeText.setTextColor(color);

            likeText.setVisibility(View.VISIBLE);
        } else {
            likeText.setVisibility(View.GONE);
        }
    }

    /**
     * Play an animation that makes the thumbs up graphic expand and then contract briefly
     * @param view view to animate.
     */
    private void playScaleAnimation(View view) {

        AnimatorSet one = getScaleAnimatorSet(view, 1.3f);
        final AnimatorSet two = getScaleAnimatorSet(view, 1f);
        one.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //start scale back down
                two.start();
            }
        });

        one.start();
    }

    /**
     * Get animator set that changes the scale x and scale y attributes of the view
     * to the relative value given
     * @param target view to animate
     * @param val relative scale value
     * @return animator set that will scale the target view
     */
    private AnimatorSet getScaleAnimatorSet(Object target, float val) {
        Resources r = getActivity().getResources();
        int shortAnimTime = r.getInteger(android.R.integer.config_shortAnimTime);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, "scaleX", val);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, "scaleY", val);
        scaleX.setDuration(shortAnimTime);
        scaleY.setDuration(shortAnimTime);

        //fixes a strange bug..
        ValueAnimator.AnimatorUpdateListener invalidator = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                View parent = (View) thumbsUpImg.getParent();
                parent.invalidate();
            }
        };

        scaleX.addUpdateListener(invalidator);
        scaleY.addUpdateListener(invalidator);

        AnimatorSet set = new AnimatorSet();
        set.play(scaleX).with(scaleY);

        return set;
    }

    /**
     * If full view, focus the reflection text box.
     * If in list view, open post page.
     * @param post data to operate with
     */
    public void onReflectButtonClicked(Post post) {
        if (isFullView && getFragment() instanceof PostFragment) {
            //open edit text
            if (checkVerification()) {
                return;
            }

            ((PostFragment) getFragment()).focusReflectionTextBox(true);
        } else {
            //open post page
            boolean focusText = !AppSession.needsVerification();

            getNavigationActivity().openPostPage(post, focusText);
        }
    }

    /**
     * Show a dialog that allows user to select from more options
     * possible for a given post.
     * @param post the post to use.
     */
    public void onMoreOptionsBtnClicked(final Post post) {
        if (checkVerification()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] options;
        if (post.isDeletable()) {
            //show delete option
            if (isFullView) {
                options = new String[]{"Show Likes", "Delete Post"};
            } else {
                options = new String[]{"Show Likes", "Show Reflections", "Delete Post"};
            }
        } else {
            //show delete option
            if (isFullView) {
                options = new String[]{"Show Likes"};
            } else {
                options = new String[]{"Show Likes", "Show Reflections"};
            }
        }
        builder.setTitle(null)
                .setItems(options, new DialogInterface.OnClickListener() {
                    /**
                     * perform relevant action
                     */
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                getNavigationActivity().openPostLikesActivity(post);
                                break;
                            case 1:
                                if (isFullView && post.isDeletable())
                                    deletePost(post);
                                else
                                    getNavigationActivity().openPostPage(post, false);
                                break;
                            case 2:
                                deletePost(post);
                                break;
                            default:
                                break;
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Calls {@link com.thecn.app.AppSession#checkVerification(android.support.v4.app.FragmentActivity)}
     * @return true if needs verification
     */
    private boolean checkVerification() {
        return AppSession.checkVerification((FragmentActivity) getActivity());
    }

    private boolean mAllowInteraction = true; //whether post can be interacted with

    /**
     * Allow or disallow interaction with post view's views.
     * @param allow whether to allow interaction
     */
    public void allowInteraction(boolean allow) {
        mAllowInteraction = allow;

        //enable/disable like button
        likeButton.post(new Runnable() {
            @Override
            public void run() {
                likeButton.setEnabled(mAllowInteraction);
            }
        });

        //enable/disable reflect button
        reflectButton.post(new Runnable() {
            @Override
            public void run() {
                reflectButton.setEnabled(mAllowInteraction);
            }
        });

        //enable/disable more options button
        moreOptionsBtn.post(new Runnable() {
            @Override
            public void run() {
                moreOptionsBtn.setEnabled(mAllowInteraction);
            }
        });

        //enable/disable looking at photos.
        if (mPictureScrollViewLayouter != null) {
            mPictureScrollViewLayouter.setOpenPhotoGalleryOnClick(mAllowInteraction);
        }
    }

    /**
     * For updates when certain parts of post deletion operation are performed
     */
    public interface DeleteCallback {
        public void onRequest(Fragment fragment);
        public void onConfirm(Fragment fragment);
        public void onResponse(Fragment fragment);
    }

    private DeleteCallback mDeleteCallback;

    /**
     * Register delete callback to be called when delete operations are performed
     * @param callback callback to register
     */
    public void registerDeleteCallback(DeleteCallback callback) {
        mDeleteCallback = callback;
    }

    /**
     * Show a dialog to confirm that user wants to delete the post.  If so,
     * send a network request to delete this post.  Call delete callbacks if they
     * are set.
     * @param post post to delete.
     */
    public void deletePost(final Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (mDeleteCallback != null) {
                            mDeleteCallback.onRequest(getFragment());
                        }

                        CallbackManager<Fragment> manager = (CallbackManager<Fragment>) callbackManager;

                        DeleteResponseCallback callback = new DeleteResponseCallback(
                                manager, mDeleteCallback, post, getFragment().toString()
                        );

                        //send request
                        PostStore.deletePost(post.getId(), callback);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Used when a network request to delete a post returns from the server.
     * Actions to take when this happens.
     */
    private static class DeleteResponseCallback extends CallbackManager.NetworkCallback<Fragment> {
        private DeleteCallback callback;
        private Post post;
        private String fragmentID;

        public DeleteResponseCallback(CallbackManager<Fragment> manager, DeleteCallback callback, Post post, String fragmentID) {
            super(manager);
            this.callback = callback;
            this.post = post;
            this.fragmentID = fragmentID;
        }

        /**
         * Show immediate message to user.
         */
        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                PostChangeController.sendDeletedBroadcast(post, fragmentID);
                AppSession.showLongToast("Post deleted");
            } else {
                AppSession.showLongToast("Could not delete post.");
            }
        }

        /**
         * Show immediate message to user.
         */
        @Override
        public void onImmediateError(VolleyError error) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeWithResponse(Fragment object) {
            if (callback == null) return;

            callback.onResponse(object);

            if (wasSuccessful()) {
                callback.onConfirm(object);
            }
        }

        @Override
        public void onResumeWithError(Fragment object) {
            if (callback == null) return;
            callback.onResponse(object);
        }
    }
}
