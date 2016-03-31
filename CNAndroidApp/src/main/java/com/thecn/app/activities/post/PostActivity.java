package com.thecn.app.activities.post;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

/**
 * Shows a single post and its reflections (through {@link com.thecn.app.activities.post.PostFragment}
 */
public class PostActivity extends NavigationActivity {

    private Post mPost;

    //fragment manager keys
    private static final String mLoadPostFragmentTag = "load_post";
    public static final String FRAGMENT_BUNDLE_KEY = "pr_fragment";

    private DataGrabber mDataGrabber;

    /**
     * Begin loading if savedInstanceState is null, otherwise get instance of DataGrabber.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarAndTitle("Post");

        if (savedInstanceState == null) {
            String postId;
            try {
                mPost = (Post) getIntent().getSerializableExtra("post");
                postId = mPost.getId();
                if (postId == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            mDataGrabber = DataGrabber.getInstance(postId);
            getSupportFragmentManager().beginTransaction()
                    .add(mDataGrabber, mLoadPostFragmentTag)
                    .commit();
        } else {
            mDataGrabber =
                    (DataGrabber) getSupportFragmentManager().findFragmentByTag(mLoadPostFragmentTag);

            if (!mDataGrabber.loading) {
                mPost = (Post) savedInstanceState.getSerializable("post");
            }

            hideProgressBar();
        }
    }

    /**
     * Loads post data for the PostActivity
     */
    public static class DataGrabber extends Fragment {

        public boolean loading = false;
        private CallbackManager<DataGrabber> manager;

        //tag for the id of the post
        public static final String ID_KEY = "id_key";

        /**
         * Sets up this fragment with its arguments.
         * @param id id of the post to load
         * @return new instance of this class
         */
        public static DataGrabber getInstance(String id) {
            Bundle args = new Bundle();
            args.putString(ID_KEY, id);

            DataGrabber grabber = new DataGrabber();
            grabber.setArguments(args);
            return grabber;
        }

        /**
         * Set up and begin loading
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            manager = new CallbackManager<>();

            loading = true;
            String id = getArguments().getString(ID_KEY);
            PostStore.getPostById(id, new Callback(manager));
        }

        @Override
        public void onResume() {
            super.onResume();
            manager.resume(this);
        }

        @Override
        public void onPause() {
            manager.pause();
            super.onPause();
        }

        /**
         * Used to make decisions when a network call returns.
         */
        private static class Callback extends CallbackManager.NetworkCallback<DataGrabber> {
            public Callback(CallbackManager<DataGrabber> grabber) {
                super(grabber);
            }

            @Override
            public void onResumeWithResponse(DataGrabber object) {
                Post post = PostStore.getData(response);
                PostActivity a = (PostActivity) object.getActivity();

                if (post != null) {
                    a.onSuccess(post);
                } else {
                    a.onLoadingError();
                }
            }

            @Override
            public void onResumeWithError(DataGrabber object) {
                StoreUtil.showExceptionMessage(error);
                object.getActivity().finish();
            }
        }
    }

    public DataGrabber getDataGrabber() {
        return mDataGrabber;
    }

    /**
     * Called when a network call returns successfully
     * @param post the post to be displayed
     */
    private void onSuccess(Post post) {
        if (post == null) {
            onLoadingError();
            return;
        }

        setPost(post);
        hideProgressBar();

        //whether or not to show the soft keyboard for reflection input
        boolean textFocus = getIntent().getBooleanExtra("textFocus", false);

        //add a new PostFragment
        Fragment fragment = PostFragment.newInstance(post, textFocus);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PostActivity.FRAGMENT_BUNDLE_KEY)
                .commit();

        getDataGrabber().loading = false;
    }

    /**
     * Finish activity if there is a problem loading.
     */
    private void onLoadingError() {
        AppSession.showDataLoadError("post");
        finish();
    }

    /**
     * Gets the post of this activity
     * @return the post associated with this activity
     */
    public Post getPost() {
        return mPost;
    }

    /**
     * Sets the post for this activity
     * @param post the post to associate with this activity
     */
    public void setPost(Post post) {
        mPost = post;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("post", mPost);
    }

    /**
     * Make sure that duplicate post activities are not opened.
     * If the same post, bring up the soft keyboard if requested.
     * @param post the post to check
     * @param textFocus whether to bring up the soft keyboard for reflection entry.
     */
    @Override
    public void openPostPage(Post post, boolean textFocus) {
        // override to prevent another reflections page from opening
        if (post.getId().equals(mPost.getId())) {
            if (textFocus) {
                try {
                    PostFragment fragment =
                            (PostFragment) getSupportFragmentManager()
                                    .findFragmentByTag(FRAGMENT_BUNDLE_KEY);

                    fragment.focusReflectionTextBox(true);
                } catch (NullPointerException e) {
                    // something went wrong
                } catch (ClassCastException e) {
                    // something went wrong
                }
            } else {
                closeNotificationDrawer();
            }
        } else {
            super.openPostPage(post, textFocus);
        }
    }
}
