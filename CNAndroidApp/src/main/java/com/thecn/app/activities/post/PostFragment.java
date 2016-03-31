package com.thecn.app.activities.post;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.ReflectionsAdapter;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.content.Reflection;
import com.thecn.app.stores.ReflectionStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.text.CNNumberLinker;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;
import com.thecn.app.tools.controllers.PostChangeController;
import com.thecn.app.tools.controllers.PostViewController;
import com.thecn.app.views.text.MyEditText;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Displays a CN post and its reflections.
 * todo currently only shows first-level reflections.  Need to add second level.
 * todo use {@link android.widget.ExpandableListView} for sub levels?
 */
public class PostFragment extends ListFragment {

    public static final String TAG = PostFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_POST_KEY = "post";

    private PostChangeController postChangeController;
    private BroadcastReceiver mUserUpdater;

    private ReflectionsAdapter mReflectionsAdapter;
    private View headerView;
    private ListFooterController mFooter;

    private Post mPost;
    private PostViewController postViewController;

    private int limit;
    private int offset;
    private boolean loading, noMore;

    private boolean hasReceivedData;

    private TextView contentTextView;
    private MyEditText reflectionText;
    private RelativeLayout showReflectionsRelativeLayout;
    private TextView showReflectionsTextView;
    private Button sendReflection;
    private ProgressBar showReflectionsProgressBar;

    private boolean textFocus;

    private static final String NO_REFLECTIONS = "No one has made a reflection yet.";

    private CallbackManager<PostFragment> callbackManager;

    /**
     * Get a fragment instance with arguments set.
     * @param mPost the post to show.
     * @param textFocus whether to bring up the soft keyboard for reflection input
     * @return a new instance of this class
     */
    public static PostFragment newInstance(Post mPost, boolean textFocus) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_POST_KEY, mPost);
        args.putBoolean("TEXT_FOCUS", textFocus);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Set up data and adapters.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        callbackManager = new CallbackManager<>(getActivity());
        registerReceivers();

        limit = 10;
        offset = 0;
        noMore = false;

        hasReceivedData = false;

        mPost = (Post) getArguments().getSerializable(FRAGMENT_BUNDLE_POST_KEY);
        mPost.setFullView(true);
        mPost.processData();
        mPost.setCallbackManager(callbackManager);
        mReflectionsAdapter = new ReflectionsAdapter(getActivity().getApplicationContext(), callbackManager);

        textFocus = getArguments().getBoolean("TEXT_FOCUS");

    }

    /**
     * Make sure callback manager has reference to an activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (callbackManager != null) callbackManager.setActivity(activity);
    }

    /**
     * Remove callback manager's activity reference.
     */
    @Override
    public void onDetach() {
        if (callbackManager != null) callbackManager.setActivity(null);
        super.onDetach();
    }

    /**
     * Register receivers (implied with {@link com.thecn.app.tools.controllers.PostChangeController})
     */
    private void registerReceivers() {
        postChangeController = new PostChangeController();
        PostChangeListener listener = new PostChangeListener(callbackManager);
        postChangeController.registerReceivers(listener, getActivity(), toString());

        mUserUpdater = new UserUpdater(callbackManager);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );
    }

    /**
     * Use callback manager to prevent references to dead activities.
     */
    private static class PostChangeListener implements PostChangeController.Listener {

        private CallbackManager<PostFragment> callbackManager;

        public PostChangeListener(CallbackManager<PostFragment> manager) {
            callbackManager = manager;
        }

        @Override
        public void onPostAdded(Post post) {
            //do nothing
        }

        @Override
        public void onPostUpdated(final Post post) {
            callbackManager.addCallback(new CallbackManager.Callback<PostFragment>() {
                @Override
                public void execute(PostFragment object) {
                    if (object.mPost.getIntegerID().equals(post.getIntegerID())) {
                        object.postViewController.setUpView(post);
                    }
                }
            });
        }

        @Override
        public void onPostDeleted(final Post post) {
            callbackManager.addCallback(new CallbackManager.Callback<PostFragment>() {
                @Override
                public void execute(PostFragment object) {
                    if (post.getId().equals(object.mPost.getId())) {
                        object.getActivity().finish();
                    }
                }
            });
        }
    }

    /**
     * Use callback manager to prevent reference to dead activities.
     */
    private static class UserUpdater extends BroadcastReceiver {
        private CallbackManager<PostFragment> callbackManager;

        public UserUpdater(CallbackManager<PostFragment> manager) {
            callbackManager = manager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            callbackManager.addCallback(new CallbackManager.Callback<PostFragment>() {
                @Override
                public void execute(PostFragment object) {
                    object.onUserUpdate();
                }
            });
        }
    }

    /**
     * If the logged in user is the owner of the post, refresh the post view.
     * Also refresh the reflections adapter.
     */
    private void onUserUpdate() {
        if (mPost.getUser().isMe()) {
            mPost.setUser(AppSession.getInstance().getUser());

            if (postViewController != null) {
                postViewController.setUpView(mPost);
            }
        }

        mReflectionsAdapter.notifyDataSetChanged();
    }

    /**
     * Unregister receivers from LocalBroadcastManager
     */
    private void unregisterReceivers() {
        postChangeController.unregisterReceivers(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUserUpdater);
    }

    /**
     * Provides an id for this fragment.
     * @return a unique id for this post and fragment.
     */
    @Override
    public String toString() {
        Post post = (Post) getArguments().getSerializable(FRAGMENT_BUNDLE_POST_KEY);
        return TAG + post.getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        headerView = inflater.inflate(R.layout.post_reflections_header, null, false);
        return inflater.inflate(R.layout.fragment_post_reflections, null);
    }

    /**
     * Passed to the PostViewController to handle delete actions.
     */
    private static class DeleteCallback implements PostViewController.DeleteCallback {
        @Override
        public void onRequest(Fragment fragment) {
            PostFragment postFragment = (PostFragment) fragment;

            //don't allow actions on the post while request is being made
            postFragment.postViewController.allowInteraction(false);
            postFragment.sendReflection.setEnabled(false);
        }

        @Override
        public void onResponse(Fragment fragment) {
            PostFragment postFragment = (PostFragment) fragment;
            postFragment.onDeleteResponse();
        }

        @Override
        public void onConfirm(Fragment fragment) {
            fragment.getActivity().finish();
        }
    }

    /**
     * Set up PostViewController, show reflections layout.
     *
     * Show reflections layout is a button above the reflections that when clicked
     * loads previous reflections.  It also shows a progress bar while loading.
     *
     * Set up reflection button to bring up soft keyboard and scroll list to bottom.
     * Set up list view and footer and send reflection button.
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postViewController = new PostViewController(callbackManager, headerView);
        postViewController.setFullView(true);
        postViewController.setUpView(mPost);
        postViewController.registerDeleteCallback(new DeleteCallback());

        showReflectionsRelativeLayout = (RelativeLayout)headerView.findViewById(R.id.showReflectionsRelativeLayout);
        showReflectionsTextView = (TextView)headerView.findViewById(R.id.showReflectionsTextView);
        showReflectionsProgressBar = (ProgressBar)headerView.findViewById(R.id.showReflectionsProgressBar);

        showReflectionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getReflections();
            }
        });

        contentTextView = (TextView) headerView.findViewById(R.id.post_content);
        contentTextView.setMaxLines(Integer.MAX_VALUE);
        contentTextView.setEllipsize(null);

        //on touch listener scrolls list view to bottom when the reflection edit text is focused.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        reflectionText = (MyEditText) view.findViewById(R.id.submit_reflection_text);
        reflectionText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    //don't use focusReflectionTextBox, soft input will already be opening
                    scrollToBottomDelayed(800);
                }

                return false;
            }
        });

        if (AppSession.needsVerification()) {
            setSubmitLayoutVisibility(View.GONE);
        }

        setListAdapter(null);

        ListView listView = getListView();
        listView.setDivider(null);
        listView.addHeaderView(headerView, null, false);
        mFooter = new ListFooterController(listView, getLayoutInflater(savedInstanceState));
        listView.setFooterDividersEnabled(false);

        setListAdapter(mReflectionsAdapter);

        sendReflection = (Button) view.findViewById(R.id.submit_reflection);
        sendReflection.setOnClickListener(mSendReflectionButtonClickListener);
    }

    /**
     * Check the user's verification status
     */
    @Override
    public void onResume() {
        super.onResume();
        if (AppSession.needsVerification()) {
            setSubmitLayoutVisibility(View.GONE);
        } else {
            setSubmitLayoutVisibility(View.VISIBLE);
            if (textFocus) showSoftInput(reflectionText, 0);
        }

        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Sets the visibility of the submit layout (for submitting reflections)
     * @param visibility whether to show the submit layout
     */
    private void setSubmitLayoutVisibility(int visibility) {
        View view = getView();
        if (view == null) return;
        view = view.findViewById(R.id.submit_layout);
        if (view == null) return;
        view.setVisibility(visibility);
    }

    /**
     * Action to take when send reflection button is clicked.  Check content and
     * if valid, send to server.
     */
    private View.OnClickListener mSendReflectionButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String text = reflectionText.getText().toString();
            if (text.length() < 1) {
                return;
            }

            sendReflection.setEnabled(false);

            ReflectionStore.makeReflection(mPost, text, new SubmissionCallback(callbackManager));
        }
    };

    /**
     * Used when a network request to submit a reflection returns.  Makes decisions based on
     * data returned.
     */
    private static class SubmissionCallback extends CallbackManager.NetworkCallback<PostFragment> {
        private CallbackManager<PostFragment> callbackManager;
        private Reflection reflection;

        public SubmissionCallback(CallbackManager<PostFragment> manager) {
            super(manager);
            callbackManager = manager;
        }

        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //properly format the reflection in separate thread
                        reflection = ReflectionStore.getData(SubmissionCallback.this.response);
                        if (reflection != null) {
                            //link cn numbers to profiles.
                            CNNumberLinker numberLinker = new CNNumberLinker();
                            numberLinker.setCallbackManager(callbackManager);
                            String text = reflection.getText();
                            if (text != null) {
                                CharSequence processedText = numberLinker.linkify(text);
                                reflection.setProcessedText(processedText);
                            } else {
                                reflection = null;
                            }
                        }

                        if (reflection != null) {
                            AppSession.showLongToast("Reflection submitted");
                            //update the view
                            callbackManager.addCallback(new CallbackManager.Callback<PostFragment>() {
                                @Override
                                public void execute(PostFragment object) {
                                    object.onSubmissionFinishedProcessing(reflection);
                                }
                            });
                        } else {
                            AppSession.showLongToast("Reflection submitted, but could not get data");
                        }

                        //enable submit button
                        callbackManager.addCallback(new CallbackManager.Callback<PostFragment>() {
                            @Override
                            public void execute(PostFragment object) {
                                object.enableSubmitButton();
                            }
                        });
                    }
                }).start();

            } else {
                AppSession.showLongToast("Could not post reflection to server");
            }
        }

        @Override
        public void onImmediateError(VolleyError error) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeWithResponse(PostFragment object) {
            //enable submit button if not successful
            if (!wasSuccessful()) {
                object.enableSubmitButton();
            }
        }

        @Override
        public void onResumeWithError(PostFragment object) {
            object.enableSubmitButton();
        }
    }

    /**
     * Enables the submit reflection button
     */
    public void enableSubmitButton() {
        sendReflection.setEnabled(true);
    }

    /**
     * Called when reflection data returned from the server after its submission has been processed
     * and is ready to display.  Hides soft keyboard, updates adapter, post model, and views.
     * @param reflection the reflection to be added to the view.
     */
    public void onSubmissionFinishedProcessing(Reflection reflection) {
        focusReflectionTextBox(false);

        offset++;
        mReflectionsAdapter.add(reflection);
        getListView().setSelection(getListView().getCount() - 1);
        reflectionText.setText("");
        mLoadingCallback.onLoadingComplete();

        mPost.getCount().setReflections(mPost.getCount().getReflections() + 1);
        //reflect post change across all activities
        PostChangeController.sendUpdatedBroadcast(mPost, toString());
        postViewController.updateReflectionText();

        sendReflection.setEnabled(true);
    }

    /**
     * When a request for deletion of this post returns
     * if the fragment is resumed, no matter what happened reenable interaction with the post.
     * The activity will finish if the deletion was successful.
     */
    public void onDeleteResponse() {
        postViewController.allowInteraction(true);
        sendReflection.setEnabled(true);
    }

    /**
     * Get reflections if the fragment has not loaded any.
     * If we've tried to get data and there is none, show a message
     * to the user.
     */
    @Override
    public void onStart() {
        super.onStart();

        if (!hasReceivedData) {
            getReflectionsFirstTime();
        } else if (mReflectionsAdapter.getCount() == 0) {
            mFooter.showMessage();
        }
    }

    /**
     * todo allow user to do things with reflections
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO
    }

    /**
     * Take a different action the first time loading reflections is attempted.
     * Afterwards, set the regular action to happen when the network call returns.
     */
    public void getReflectionsFirstTime() {
        mFooter.setLoading();

        mLoadingCallback = new LoadingCallback() {
            @Override
            public void onLoadingComplete() {
                loading = false;
                //we have queried the server for reflections at least once.
                hasReceivedData = true;

                //tell user if there are no reflections
                if (mReflectionsAdapter.getCount() == 0) {
                    mFooter.showMessage(NO_REFLECTIONS);
                } else mFooter.clear();

                setHiddenReflectionLoader(true);

                if (textFocus) {
                    focusReflectionTextBox(true);
                    textFocus = false;
                }

                //set the action to take on subsequent callbacks.
                mLoadingCallback = new LoadingCallback() {
                    @Override
                    public void onLoadingComplete() {
                        loading = false;

                        mFooter.clear();

                        setHiddenReflectionLoader(true);
                    }
                };
            }
        };

        getReflections();
    }

    /**
     * Called when no more reflections were returned from the server.
     */
    private void onNoMoreReflections() {
        noMore = true;
        showReflectionsRelativeLayout.setVisibility(View.GONE);
    }

    /**
     * Called when there was a problem with the response from the server.
     */
    public void onListFailure() {
        AppSession.showLongToast("Could not load reflection data.");
        mLoadingCallback.onLoadingComplete();
    }

    /**
     * Called when unable to connect to the server for some reason.
     * @param error
     */
    public void onListError(VolleyError error) {
        StoreUtil.showExceptionMessage(error);
        mLoadingCallback.onLoadingComplete();
    }

    /**
     * Called when all reflections returned from the server have had their
     * content processed.
     * @param reflections the array list of reflections to add to the adapter (and list view).
     */
    public void onListFinishedProcessing(ArrayList<Reflection> reflections) {
        if (reflections != null) {
            mReflectionsAdapter.addAll(reflections);
        }
        mLoadingCallback.onLoadingComplete();
    }

    /**
     * Get reflections from the server IF there could be more to get and we aren't already getting more.
     */
    public void getReflections() {
        if (!loading && !noMore) {
            loading = true;

            ReflectionStore.getPostReflections(mPost.getId(), limit, offset, new GetReflectionsCallback(callbackManager));
        } else {
            mLoadingCallback.onLoadingComplete();
        }
    }

    /**
     * Called when a network call to get reflections returns.  Makes decisions based on the response.
     */
    private static class GetReflectionsCallback extends CallbackManager.NetworkCallback<PostFragment> {
        private CallbackManager<PostFragment> callbackManager;
        private ArrayList<Reflection> reflections;

        public GetReflectionsCallback(CallbackManager<PostFragment> manager) {
            super(manager);
            callbackManager = manager;
        }

        @Override
        public void onResumeWithResponse(PostFragment object) {
            if (!wasSuccessful()) {
                object.onListFailure();
                return;
            }

            reflections = ReflectionStore.getListData(response);

            if (reflections == null) {
                object.onNoMoreReflections();
                object.mLoadingCallback.onLoadingComplete();
                return;
            }

            if (object.mReflectionsAdapter.getCount() == 0) {
                object.showReflectionsRelativeLayout.setVisibility(View.VISIBLE);
            }

            if (reflections.size() < object.limit) {
                object.onNoMoreReflections();
            }

            int nextOffset = StoreUtil.getNextOffset(response);
            if (nextOffset != -1) object.offset = nextOffset;

            //process the reflections before posting to list view
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //link cn numbers to profiles.
                    CNNumberLinker numberLinker = new CNNumberLinker();
                    numberLinker.setCallbackManager(callbackManager);

                    for (Reflection reflection : reflections) {
                        try {
                            String text = reflection.getText();
                            CharSequence processedText = numberLinker.linkify(text);
                            reflection.setProcessedText(processedText);
                        } catch (NullPointerException e) {
                            //do nothing
                        }
                    }

                    callbackManager.addCallback(new CallbackManager.Callback<PostFragment>() {
                        @Override
                        public void execute(PostFragment object) {
                            object.onListFinishedProcessing(reflections);
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onResumeWithError(PostFragment object) {
            object.onListError(error);
        }
    }

    private LoadingCallback mLoadingCallback;

    /**
     * Used to perform appropriate actions when a network call for
     * listed reflections returns.
     */
    private interface LoadingCallback {
        public void onLoadingComplete();
    }

    /**
     * Shows a progress bar if not hidden, shows a text view if hidden
     * @param hidden flag for hiding or showing intermediate progress view.
     */
    public void setHiddenReflectionLoader(boolean hidden) {
        if (hidden) {
            showReflectionsProgressBar.setVisibility(View.GONE);
            showReflectionsTextView.setVisibility(View.VISIBLE);
        } else {
            showReflectionsProgressBar.setVisibility(View.VISIBLE);
            showReflectionsTextView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Shows the soft keyboard
     * @param view currently focused view which would like to receive soft input
     * @param flags Provides additional operating flags. Currently may be 0 or have the SHOW_IMPLICIT bit set.
     */
    private void showSoftInput(View view, int flags) {
        InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.showSoftInput(view, flags);
    }

    /**
     * Either focuses the send reflection edit text view and scrolls to the bottom of the
     * reflection list OR hides the soft keyboard.
     * @param focus whether to set up for the user to input a reflection
     */
    public void focusReflectionTextBox(boolean focus) {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (focus) {
            reflectionText.requestFocus();
            //scrolls to the bottom once the soft keyboard has been shown
            imm.showSoftInput(reflectionText, InputMethodManager.SHOW_IMPLICIT, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {

                    if (resultCode == InputMethodManager.RESULT_SHOWN) {
                        scrollToBottomDelayed(500);
                    } else if (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN) {
                        scrollToBottom();
                    }
                }
            });
        } else {
            imm.hideSoftInputFromWindow(reflectionText.getWindowToken(), 0);
        }
    }

    /**
     * Scroll to the bottom, but don't do it until a certain delay has passed.
     * @param delay amount of time to wait before scrolling
     */
    private void scrollToBottomDelayed(int delay) {
        getListView().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollToBottom();
            }
        }, delay);
    }

    /**
     * Scroll the list view to the bottom (makes most recent reflection visible)
     */
    private void scrollToBottom() {
        //account for header and footer
        ListView listView = getListView();
        int lastPos = listView.getCount();
        //if no items in adapter, scroll to footer
        lastPos = lastPos == 0 ? 1 : lastPos;
        listView.smoothScrollToPosition(lastPos);
    }

    /**
     * Unregister receivers
     */
    @Override
    public void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }
}
