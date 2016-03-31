package com.thecn.app.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.filter.FilterGlobalPostActivity;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.adapters.GlobalPostsAdapter;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;
import com.thecn.app.tools.controllers.PostChangeController;
import com.thecn.app.tools.controllers.SlidingViewController;
import com.thecn.app.tools.network.GobalPostLoader;
import com.thecn.app.tools.network.PostLoader;
import com.thecn.app.tools.volley.MyVolley;
import com.thecn.app.views.list.ObservableListView;

import java.util.ArrayList;

/**
 * Fragment used as base for all fragments that show lists of posts.  Handles a lot of stuff that is common among them all.
 */
public abstract class BaseGlobalPostListFragment extends SwipeRefreshListFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int FILTER_REQUEST = 0;
    private static final String NO_POSTS = "There are no posts here.";
    public GobalPostLoader.Params postLoaderParams; //params for loading posts
    //list of methods that can be used to load posts.  This list can be
    //used to allow the user to use a filter to load posts in different ways.
    public GobalPostLoader.MethodList postLoaderMethodList;
    protected GlobalPostsAdapter postsAdapter;
    protected PostChangeController postChangeController;
    View mPostButton, mTwinPostButton;
    private CallbackManager<BaseGlobalPostListFragment> callbackManager;
    private BroadcastReceiver userUpdater; //notified when logged in user changed.
    private int limit = 10;
    private int offset = 0;
    private boolean noMore = false;
    private boolean loading = false;
    private long lastRefreshTime = 0;
    private boolean swipeRefreshing;
    //if true, load more posts when list gets to bottom.
    private boolean autoLoadMore = true;
    //flag that indicates when FilterActivity returns
    private boolean filterResult = false;
    private PostCallback postCallback;
    private ListFooterController footer;
    private RequestQueue imageRequestQueue;

    /**
     * Called hen there was a problem loading the posts
     */
    public void onLoadingFailure() {
        AppSession.showDataLoadError("post");
        onLoadingComplete();
    }

    /**
     * Retain instance.
     * Get reference to volley image request queue.
     * Get post loader method list, set post loader params using first in list.
     * Set up callback manager and adapter.
     * Check if post loader uses "global" (as in global classmate) or
     * "public" visibility.  If public, set to show public posts before getting data.
     * Register broadcast receivers.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        imageRequestQueue = MyVolley.getRequestQueue();

        setHasOptionsMenu(true);
        postLoaderMethodList = getPostLoaderMethodList();
        postLoaderParams = new GobalPostLoader.Params(
                postLoaderMethodList.get(0),
                postLoaderMethodList.getContentType()
        );

        callbackManager = new CallbackManager<>(getActivity());
        postsAdapter = new GlobalPostsAdapter(callbackManager);

        if (postLoaderMethodList.interpretAsGlobalPosts()) {
            postLoaderParams.interpretAsGlobal = true;
            postLoaderParams.showGlobalPosts = postLoaderMethodList.showGlobalPosts();
        } else {
            AppSession.getInstance().getUser().setHidePublicContents(false);
            refreshWithHidePublicUpdate();
        }

        registerReceivers();
    }

    /**
     * Unregister receivers.
     */
    @Override
    public void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }

    /**
     * Get callback manager
     *
     * @return callback manager
     */
    public CallbackManager<BaseGlobalPostListFragment> getCallbackManager() {
        return callbackManager;
    }

    /**
     * Give callback manager reference to activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (callbackManager != null) callbackManager.setActivity(activity);
    }

    /**
     * Take away callback manager activity reference.
     */
    @Override
    public void onDetach() {
        if (callbackManager != null) callbackManager.setActivity(null);
        super.onDetach();
    }

    /**
     * Set up post change controller.
     * Set up user updater.
     * Register both with local broadcast manager.
     */
    private void registerReceivers() {

        postChangeController = new PostChangeController();
        PostChangeListener listener = new PostChangeListener(callbackManager);
        postChangeController.registerReceivers(listener, getActivity(), toString());

        userUpdater = new UserUpdater(callbackManager);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                userUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );
    }

    /**
     * Unregister post change controller receivers and user updater.
     */
    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(userUpdater);
        postChangeController.unregisterReceivers(getActivity());
    }

    /**
     * Set up list view and footer, set the loading display, set the adapter to the list view,
     * set on refresh listener.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();

        listView.setBackgroundColor(getResources().getColor(R.color.background_color));
        listView.setFooterDividersEnabled(false);
        listView.setDivider(null);

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        footer = new ListFooterController(listView, inflater);

        setLoadingDisplay();

        setListAdapter(postsAdapter);

        setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        swipeRefreshing = true;
        refresh();
    }

    /**
     * If interpreted as "public" visibility, refresh and make sure
     * visibility is consistent with local flag.  Else refresh normally.
     */
    public void refresh() {
        if (!postLoaderMethodList.interpretAsGlobalPosts()) {
            refreshWithHidePublicUpdate();
        } else {
            refreshNoHidePublicUpdate();
        }
    }

    /**
     * Empty list, set the flag on the server side to match it on this side.
     * Uses {@link PostStore#setHidePublicPosts(boolean, com.thecn.app.stores.ResponseCallback)}
     */
    public void refreshWithHidePublicUpdate() {
        empty();

        loading = true;
        boolean hidePublicContents = AppSession.getInstance().getUser().isHidePublicContents();
        PostStore.setHidePublicPosts(
                hidePublicContents,
                new UpdatePubliContentsCallback(callbackManager, hidePublicContents)
        );
    }

    /**
     * Empty list and reload.
     */
    public void refreshNoHidePublicUpdate() {
        empty();
        loadPosts();
    }

    /**
     * Cancel callback if it exists, reset data and clear adapter.
     */
    public void empty() {
        if (postCallback != null) postCallback.cancel();

        offset = 0;
        noMore = false;
        loading = false;
        autoLoadMore = false;
        postsAdapter.clear();
    }

    /**
     * Used to load posts and make sure that more posts won't be automatically
     * loaded at the same time this method is loading more posts.
     */
    public void autoLoadPosts() {
        if (!autoLoadMore || noMore) return;

        autoLoadMore = false;
        loadPosts();
    }

    /**
     * Set loading flag and update loading display,
     * set last refresh time and use {@link GobalPostLoader#loadPosts(BaseGlobalPostListFragment, GobalPostLoader.Params)}
     */
    public void loadPosts() {
        loading = true;
        setLoadingDisplay();

        lastRefreshTime = System.currentTimeMillis();
        postCallback = GobalPostLoader.loadPosts(this, postLoaderParams);
    }

    /**
     * Get list of post loading methods.
     *
     * @return list of methods that can be used to load posts.
     */
    public abstract GobalPostLoader.MethodList getPostLoaderMethodList();

    /**
     * Creates new post loader params from post loader method list selection and
     * content type selection (posts, polls, quizzes, etc.).
     * Sets flags, refreshes fragment, refreshes options menu.
     */
    public void refreshForDifferentPostParams() {
        GobalPostLoader.Params newParams = new GobalPostLoader.Params(
                postLoaderMethodList.getSelectedMethod(),
                postLoaderMethodList.getContentType()
        );
        newParams.interpretAsGlobal = postLoaderMethodList.interpretAsGlobalPosts();
        newParams.showGlobalPosts = postLoaderMethodList.showGlobalPosts();

        postLoaderParams = newParams;

        if (!newParams.interpretAsGlobal) {
            refreshWithHidePublicUpdate();
        } else {
            refreshNoHidePublicUpdate();
        }

        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * Tells whether current filter is something other than the default filter (the first one
     * in post loader method list).  Will also return true if visibility is different from default.
     * Also returns true if content (posts, polls, quizzes, etc.) is being filtered.
     *
     * @return true if different filter from default
     */
    public boolean isFilterSet() {
        boolean visibilityChanged =
                postLoaderMethodList.interpretAsGlobalPosts() ?
                        postLoaderMethodList.showGlobalPosts() : AppSession.getInstance().getUser().isHidePublicContents();

        return postLoaderMethodList.getSelectedIndex() != 0 ||
                postLoaderMethodList.getContentType() != PostLoader.CONTENT_ALL ||
                visibilityChanged;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        NavigationActivity activity = getNavigationActivity();

        if (!activity.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            inflater.inflate(R.menu.base_post_fragment, menu);
            menu.findItem(R.id.action_filter).setIcon(
                    isFilterSet() ?
                            R.drawable.ic_action_filter_active : R.drawable.ic_action_filter
            );
        }
    }

    /**
     * Open filter activity when option is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getNavigationActivity().isDrawerVisible()) return super.onOptionsItemSelected(item);

        int id = item.getItemId();

        if (id == R.id.action_filter) {
            Intent intent = new Intent(getActivity(), FilterGlobalPostActivity.class);
            intent.putExtra(FilterGlobalPostActivity.POST_METHODS_KEY, postLoaderMethodList);
            startActivityForResult(intent, FILTER_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * When {@link FilterGlobalPostActivity} returns result,
     * set flag, set post loader method list from intent.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != FILTER_REQUEST || resultCode != Activity.RESULT_OK || data == null)
            return;

        filterResult = true;

        postLoaderMethodList = data.getParcelableExtra(FilterGlobalPostActivity.POST_METHODS_KEY);
    }

    /**
     * Cast activity
     *
     * @return cast activity
     */
    public NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    /**
     * Get limit of new posts to load each time.
     *
     * @return limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets limit of new posts to load each time.
     *
     * @param limit limit of posts.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Get current list offset
     *
     * @return list offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Show the post button all the way on the screen.
     */
    public void showPostButton() {
        mPostButton.post(new Runnable() {
            @Override
            public void run() {
                mPostButton.setTranslationY(0);
            }
        });
    }

    /**
     * Add post button to the layout.
     *
     * @param hoverButtonID id of view in the layout that will be used
     *                      as the post button.
     */
    protected void addPostButton(Integer hoverButtonID) {
        addPostButton(hoverButtonID, null);
    }

    /**
     * Add post button to the layout.  Allows possibility of twin post
     * button, which should be a duplicate, and is used to show instead
     * once the list view has scrolled up past a certain point.
     * Uses {@link SlidingViewController} to control displaying these
     * buttons.  It should be used with {@link ObservableListView} so the amount
     * of scrolling of the list view can be measured.
     *
     * @param hoverButtonID id of view in the layout that will be used as the post button.
     * @param twinButtonID  id of view in layout that will be used as twin post button.
     *                      If null, then no twin post button used.
     */
    protected void addPostButton(Integer hoverButtonID, Integer twinButtonID) {
        ObservableListView lv = (ObservableListView) getListView();

        SlidingViewController controller = new SlidingViewController(lv);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushCreatePostActivity();
            }
        };

        if (hoverButtonID != null) {
            mPostButton = getView().findViewById(hoverButtonID);
            if (mPostButton != null) {
                mPostButton.findViewById(R.id.actual_post_button).setOnClickListener(listener);
                controller.setSlidingView(mPostButton);
            }
        }

        if (twinButtonID != null) {
            mTwinPostButton = getView().findViewById(twinButtonID);
            if (mTwinPostButton != null) {
                mTwinPostButton.findViewById(R.id.actual_post_button).setOnClickListener(listener);
                controller.setTwinView(mTwinPostButton);
            }
        }

        controller.setOnScrollListener(getScrollListener());
    }

    /**
     * Sets the list view's on scroll listener.
     */
    protected void setListViewScrollListener() {
        getListView().setOnScrollListener(getScrollListener());
    }

    /**
     * Get actions to perform for different states of list view.
     *
     * @return on scroll listener.
     */
    private AbsListView.OnScrollListener getScrollListener() {
        return new AbsListView.OnScrollListener() {
            /**
             * Stop image request queue when list is being flung.  No point in
             * loading images unless user stops to look at them.
             */
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // ensures smoother scrolling
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    imageRequestQueue.stop();
                } else {
                    imageRequestQueue.start();
                }
            }

            /**
             * Load more posts automatically when user scrolls to bottom.
             */
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount - visibleItemCount <= firstVisibleItem) {
                    autoLoadPosts();
                }
            }
        };
    }

    /**
     * Set the loading display.  Refresh options menu.
     * If there was a filter result, refresh for the new filter.
     * Else, if another "web page" activity was viewed after this, reset filter and refresh.
     * If no refreshing occurred so far, check how long it's been since last refresh.
     * If too long of a time has passed, refresh.
     */
    @Override
    public void onResume() {
        super.onResume();

        setLoadingDisplay();

        getActivity().supportInvalidateOptionsMenu();

        boolean refreshed;

        if (filterResult) {
            filterResult = false;

            refreshForDifferentPostParams();
            refreshed = true;

        } else if (getNavigationActivity().isWholePageResult()) {

            //if activity was navigated away from, reset filter and refresh if different
            resetFilterAndRefresh();
            refreshed = true;
        } else {
            refreshed = false;
        }

        if (!refreshed) {
            boolean longTimeLapse = System.currentTimeMillis() - lastRefreshTime > 1800000;

            if (longTimeLapse) refresh();
        }

        callbackManager.resume(this);
    }

    /**
     * Set default filter and refresh.
     */
    public void resetFilterAndRefresh() {
        postLoaderMethodList.setSelectedIndex(0);
        postLoaderMethodList.setContentType(PostLoader.CONTENT_ALL);
        postLoaderMethodList.setShowGlobalPosts(false);
        AppSession.getInstance().getUser().setHidePublicContents(false);

        refreshForDifferentPostParams();
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * If swipe refreshing, show swipe refresh thingy.
     * Else show footer loading thingy.
     */
    private void setLoadingDisplay() {
        if (swipeRefreshing) {
            footer.clear();
            setRefreshing(true);
        } else if (loading) {
            footer.setLoading();
        }
    }

    /**
     * Set flags, allow auto loading of more posts again.
     * Set footer view state.
     */
    public void onLoadingComplete() {
        swipeRefreshing = loading = false;
        autoLoadMore = true;

        setRefreshing(false);

        if (postsAdapter.getCount() == 0) footer.showMessage(NO_POSTS);
        else footer.clear();
    }

    /**
     * Open post page on list item click.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (postsAdapter.getCount() < 1) {
            return;
        }

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);

        Post post = postsAdapter.getItem(position);
        getNavigationActivity().openPostPage(post, false);
    }

    /**
     * Used when the fragment is finished loading posts.
     * Actions to take when this happens.
     */
    public static class PostCallback extends CallbackManager.NetworkCallback<BaseGlobalPostListFragment> {

        public PostCallback(CallbackManager<BaseGlobalPostListFragment> manager) {
            super(manager);
        }

        /**
         * Creates a thread to process the posts in the list before displaying.
         */
        @Override
        public void onResumeWithResponse(BaseGlobalPostListFragment fragment) {
            if (!wasSuccessful()) {
                fragment.onLoadingFailure();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<Post> posts = PostStore.getListData(response);
                    if (posts == null) {
                        manager.addCallback(new CallbackManager.Callback<BaseGlobalPostListFragment>() {
                            @Override
                            public void execute(BaseGlobalPostListFragment object) {
                                //no more posts
                                object.noMore = true;
                                object.onLoadingComplete();
                            }
                        });
                        //nothing to do here
                        return;
                    }

                    final int nextOffset = StoreUtil.getNextOffset(response);

                    //process all post data
                    for (Post post : posts) {
                        post.setFullView(false);
                        post.processData();
                        post.setCallbackManager(manager);
                    }

                    //update fragment
                    manager.addCallback(new CallbackManager.Callback<BaseGlobalPostListFragment>() {
                        @Override
                        public void execute(BaseGlobalPostListFragment object) {
                            object.postsAdapter.addAll(posts);
                            if (nextOffset != -1) object.offset = nextOffset;
                            object.onLoadingComplete();
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onResumeWithError(BaseGlobalPostListFragment fragment) {
            fragment.onLoadingFailure();
        }
    }

    /**
     * Used to notify adapter (which notifies view) when the logged in user has been updated.
     */
    private static class UserUpdater extends BroadcastReceiver {

        private CallbackManager<BaseGlobalPostListFragment> callbackManager;

        public UserUpdater(CallbackManager<BaseGlobalPostListFragment> callbackManager) {
            this.callbackManager = callbackManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            callbackManager.addCallback(new CallbackManager.Callback<BaseGlobalPostListFragment>() {
                @Override
                public void execute(BaseGlobalPostListFragment object) {
                    object.postsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * Methods called when a post has been changed.
     */
    private static class PostChangeListener implements PostChangeController.Listener {

        private CallbackManager<BaseGlobalPostListFragment> manager;

        /**
         * New instance
         *
         * @param manager for async operations.
         */
        public PostChangeListener(CallbackManager<BaseGlobalPostListFragment> manager) {
            this.manager = manager;
        }

        /**
         * Process the post's data, add the post when finished.
         */
        @Override
        public void onPostAdded(final Post post) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    post.setFullView(false);
                    post.processData();
                    post.setCallbackManager(manager);

                    manager.addCallback(new CallbackManager.Callback<BaseGlobalPostListFragment>() {
                        @Override
                        public void execute(BaseGlobalPostListFragment object) {
                            object.postsAdapter.add(0, post);
                            object.offset++;
                        }
                    });
                }
            }).start();
        }

        /**
         * Remove post from adapter.
         */
        @Override
        public void onPostDeleted(final Post post) {
            manager.addCallback(new CallbackManager.Callback<BaseGlobalPostListFragment>() {
                @Override
                public void execute(BaseGlobalPostListFragment object) {
                    if (object.postsAdapter.removePost(post)) {
                        object.offset--;
                    }
                }
            });
        }

        /**
         * Replace post in adapter.
         */
        @Override
        public void onPostUpdated(final Post post) {
            manager.addCallback(new CallbackManager.Callback<BaseGlobalPostListFragment>() {
                @Override
                public void execute(BaseGlobalPostListFragment object) {
                    object.postsAdapter.replacePost(post);
                }
            });
        }
    }

    /**
     * Actions to perform when network call to change public content flag returns.
     */
    private static class UpdatePubliContentsCallback extends CallbackManager.NetworkCallback<BaseGlobalPostListFragment> {

        private static final String ERROR = "Server error";
        boolean hiding;

        public UpdatePubliContentsCallback(CallbackManager<BaseGlobalPostListFragment> manager, boolean hiding) {
            super(manager);
            this.hiding = hiding;
        }

        /**
         * If successful, set the local flag to reflect update and refresh
         */
        @Override
        public void onResumeWithResponse(BaseGlobalPostListFragment fragment) {
            if (StoreUtil.success(response)) {
                AppSession session = AppSession.getInstance();
                synchronized (session.userLock) {
                    User user = session.getUser();
                    user.setHidePublicContents(hiding);
                }

                fragment.refreshNoHidePublicUpdate();
            } else {
                AppSession.showShortToast(ERROR);
                fragment.onLoadingComplete();
            }
        }

        @Override
        public void onResumeWithError(BaseGlobalPostListFragment object) {
            StoreUtil.showExceptionMessage(error);
            object.onLoadingComplete();
        }
    }
}