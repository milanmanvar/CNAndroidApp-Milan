package com.thecn.app.activities.poll;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.profile.ProfileActivity;
import com.thecn.app.adapters.AttachmentAdapter;
import com.thecn.app.adapters.LinkAdapter;
import com.thecn.app.adapters.PollSubmissionAdapter;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.models.content.Link;
import com.thecn.app.models.content.PollItem;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.PollStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.DisplayUtil;
import com.thecn.app.tools.controllers.ListFooterController;
import com.thecn.app.tools.images.PictureScrollViewLayouter;
import com.thecn.app.tools.text.TextUtil;
import com.thecn.app.views.list.MyDialogListView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
* Displays a question in a poll (one {@link com.thecn.app.models.content.PollItem}.
 * Used in a PagerAdapter in {@link com.thecn.app.activities.poll.PollActivity}
 * Creates many of the views dynamically based on the content of the poll item.
*/
public class PollFragment extends Fragment {

    private PollItem mPollItem;
    private static final String ARG_POLL_ITEM = "poll_item";

    private final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    private LinearLayout mRootLayout, mInputLayout, mOutputLayout;
    private Button mSubmitButton;
    private EditText mShortAnswerText;

    private ListView mSubmissionList;
    private PollSubmissionAdapter mPollSubmissionAdapter;
    //controls list view in submissions dialog
    private SubmissionController mSubmissionController;

    //pie chart
    private GraphicalView mChartView;
    private LinearLayout mLegendView;

    //flag for finding errors in data from API
    private boolean mHasDataFault;
    //flags for whether or not showing link or attachment dialogs.
    private boolean mShowingLinks, mShowingAttachments;

    private int mViewBottomMargin, mHorizontalMargin;

    private static final long mLayoutTransitionDuration = 600;
    private long mAnimationStartTime;

    private CallbackManager<PollFragment> callbackManager;

    /**
     * Gets a new instance of this class
     * @param pollItem Poll item object to display
     * @return new instance of this class
     */
    public static PollFragment newInstance(PollItem pollItem) {
        PollFragment fragment = new PollFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POLL_ITEM, pollItem);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initialize and check for errors.
     * If no errors, initialize adapter and controller and resources.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mPollItem = (PollItem) getArguments().getSerializable(ARG_POLL_ITEM);
        checkData();
        if (mHasDataFault) return;

        PollItem.SubmissionDisplayType displayType = mPollItem.getSubmissionDisplayType();
        mPollSubmissionAdapter = new PollSubmissionAdapter(getActivity().getApplicationContext(), displayType);
        mSubmissionController = new SubmissionController();

        Resources r = getResources();
        mViewBottomMargin = (int) r.getDimension(R.dimen.poll_item_margin_bottom);
        mHorizontalMargin = (int) r.getDimension(R.dimen.activity_horizontal_margin);

        callbackManager = new CallbackManager<>(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (callbackManager != null) callbackManager.setActivity(activity);
    }

    @Override
    public void onDetach() {
        if (callbackManager != null) callbackManager.setActivity(null);
        super.onDetach();
    }

    private void checkData() {
        checkData(mPollItem);
    }

    /**
     * Checks data integrity.  Sets flag that communicates whether there is an error.
     * @param pollItem data to check
     */
    private void checkData(PollItem pollItem) {
        mHasDataFault =
                pollItem == null
                        || pollItem.getSurveyType() == null
                        || pollItem.getInputType() == null
                        || pollItem.getInputType() != PollItem.InputType.SHORT_ANSWER
                        && hasChoiceDataFault(pollItem);
    }

    /**
     * Checks the choices of the poll item to make sure there is no fault
     * @param pollItem item to check
     * @return true if there is a fault in the data
     */
    private boolean hasChoiceDataFault(PollItem pollItem) {
        ArrayList<PollItem.Choice> choices = pollItem.getChoices();
        if (choices == null || choices.size() == 0) {
            return true;
        } else {
            for (PollItem.Choice choice : pollItem.getChoices()) {
                if (choice == null || choice.getSequenceId() == null || choice.getSubject() == null) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates a view for error if there is an error or creates
     * the question's appropriate view if there is no error.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mHasDataFault) {
            return createViewForError(inflater, container);
        } else {
            return createQuestionView(inflater, container);
        }
    }

    /**
     * Creates a view that displays an error when data has a fault.
     * @param inflater used to inflate an error view
     * @param container used to specify the parent of the view
     * @return the error view
     */
    private View createViewForError(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.message_load_view, container, false);
        TextView errorMessage = (TextView) view.findViewById(R.id.message);
        errorMessage.setText("Error loading question data.");
        errorMessage.setVisibility(View.VISIBLE);
        return view;
    }

    /**
     * Creates the appropriate question view based on the type of poll question.
     * Gets references to views and sets on click listeners.  Sets up the input and output layouts
     * for the question view.  Sets up the question title, attachment, and link texts.
     * Sets up a ScrollView if the answer is multiple choice (for finite scrolling length).
     * Sets up a ListView if the answer is short answer (for indefinite amount of answers).
     * @param inflater used to inflate views.
     * @param container used to specify the parent view
     * @return the root view
     */
    private View createQuestionView(LayoutInflater inflater, ViewGroup container) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_poll, container, false);

        mRootLayout = (LinearLayout) inflater.inflate(R.layout.poll_root_layout, null);

        //set the question title
        TextView questionTitle = (TextView) mRootLayout.findViewById(R.id.question);
        questionTitle.setText(mPollItem.getText());

        mSubmitButton = (Button) mRootLayout.findViewById(R.id.done);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitAnswer();
            }
        });

        //input layout contains either RadioButtons (single choice), Checkboxes(multiple choice),
        //or a single TextView (short answer)
        mInputLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        inputParams.bottomMargin = mViewBottomMargin;
        mInputLayout.setLayoutParams(inputParams);
        mInputLayout.setGravity(Gravity.CENTER);
        mInputLayout.setOrientation(LinearLayout.VERTICAL);

        setUpInputLayout();

        initLinksText();
        initAttachmentsText();

        //output layout shows nothing if answers set not to be shown.
        //shows a pie chart for multiple/single choice
        //shows responses if short answer (may or may not show who made what response)
        mOutputLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams outputParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        mOutputLayout.setLayoutParams(outputParams);
        mOutputLayout.setOrientation(LinearLayout.VERTICAL);

        if (mPollItem.getIsShortAnswer()) {
            //use a list view
            setUpResponseList();
            view.addView(mSubmissionList);
        } else {
            //use a scroll view
            mLegendView = new LinearLayout(getActivity());
            mLegendView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mLegendView.setOrientation(LinearLayout.VERTICAL);
            mLegendView.setGravity(Gravity.CENTER);

            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            scrollView.setFillViewport(true);
            scrollView.addView(mRootLayout);
            view.addView(scrollView);
        }

        //view for pictures added with this poll item.
        HorizontalScrollView scrollView = (HorizontalScrollView) mRootLayout.findViewById(R.id.picturesScrollView);
        LinearLayout scrollViewLayout = (LinearLayout) scrollView.findViewById(R.id.picturesScrollViewLayout);
        PictureScrollViewLayouter layouter = new PictureScrollViewLayouter(
                scrollView, scrollViewLayout, mPollItem.getPictures(), mPollItem.getVideos(), callbackManager
        );
        float margin = getResources().getDimension(R.dimen.activity_horizontal_margin) * 2f;
        layouter.doLayout(margin);

        setViewStates(true);

        mRootLayout.setLayoutTransition(new LayoutTransition());
        return view;
    } //end createQuestionView()

    /**
     * Get duration for layout transition
     * @return duration of a layout transition
     */
    public long getLayoutTransitionDuration() {
        return mLayoutTransitionDuration;
    }

    /**
     * Get the time that the last "show answer" animation was started.
     * @return last animation start time
     */
    public long getAnimationStartTime() {
        return mAnimationStartTime;
    }

    /**
     * Initialize ListView.  If users are shown with their responses, set an on click listener
     * that takes the user to their respective profiles.
     */
    private void setUpResponseList() {

        mSubmissionList = new ListView(getActivity());

        mSubmissionList.setClipToPadding(false);
        int extraPadBottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20f, getResources().getDisplayMetrics()
        );
        mSubmissionList.setPadding(
                mSubmissionList.getPaddingLeft(),
                mSubmissionList.getPaddingTop(),
                mSubmissionList.getPaddingRight(),
                mSubmissionList.getPaddingBottom() + extraPadBottom
        );
        mSubmissionList.setDivider(null);
        mSubmissionList.setFooterDividersEnabled(false);
        mSubmissionList.addHeaderView(mRootLayout);

        mSubmissionList.setAdapter(mPollSubmissionAdapter);

        if (mPollItem.getSubmissionDisplayType() == PollItem.SubmissionDisplayType.USER_ANSWER) {
            //show users with their answers
            mSubmissionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mPollSubmissionAdapter.getCount() < 1) return;

                    i--; //account for header
                    User user = mPollSubmissionAdapter.getItem(i).getUser();
                    if (user == null) return;

                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra("user_id", user.getId());
                    startActivity(intent);
                }
            });
        } else {
            //don't show users with their answers
            mSubmissionList.setOnItemClickListener(null);
            //disables selector
            mSubmissionList.setSelector(new StateListDrawable());
        }
    }

    /**
     * Sets the states of the views depending on various attributes of the PollItem.
     * @param immediate if true, don't animate the views.
     */
    private void setViewStates(boolean immediate) {
        //set the text and click listener of the number of users who have answered this question.
        initRespondentsText();

        //if this is my poll or I have submitted a response, show results
        if (mPollItem.getOwnerIsMe() || mPollItem.getUserHasSubmitted()) {
            //show the correct response if the creator specified one
            showCorrectResponseIfThere();

            String type = mPollItem.getIsShortAnswer() ? "Responses" : "Results";

            //if results displayed, show them
            if (mPollItem.getDisplayResult()) {

                if (mPollItem.getSubmissionCount() > 0) {

                    showResultsText(type);

                    //animation looks choppy if the two views
                    //(results label and output) come in at same time
                    if (immediate) {
                        showOutput();
                    } else {
                        mRootLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showOutput();
                            }
                        }, mLayoutTransitionDuration);
                    }
                }
            } else {
                //communicate that results are not shown
                displayResultsNotShown(type);
            }
        }
    }

    /**
     * Shows user responses if short answer.
     * Shows pie chart if not short answer.
     */
    private void showOutput() {
        if (mPollItem.getIsShortAnswer()) {
            showSubmissions();
        } else {
            showChartAndLegend();
        }
    }

    /**
     * Show that results are not shown.
     * If they are shown later, communicate this.
     * @param type either "Responses" (SA) or "Results" (PIE)
     */
    private void displayResultsNotShown(String type) {
        PollActivity activity = getPollActivity();
        if (activity == null) return; //what...?
        Post post = activity.getPost();
        if (post == null) return; //this shouldn't happen...

        String resultDisplayType = post.getResultDisplayType();
        if (resultDisplayType != null && resultDisplayType.equals("show_after_date")) {
            //results shown later
            String resultDate = post.getResultDate();
            String resultTime = post.getResultTime();

            boolean resultOK = resultDate != null && resultTime != null
                    && resultDate.length() > 0 && resultTime.length() > 0;

            if (resultOK) {
                showResultsText(type + " will be shown\n" + resultDate + " at " + resultTime);
            } else {
                showResultsText(type + " will be shown at a later date");
            }
        } else {
            //results never shown
            showResultsText(type + " not shown");
        }
    }

    /**
     * Set results text visible and set its text.
     * @param text text to set to the TextView
     */
    private void showResultsText(String text) {
        TextView resultText = (TextView) mRootLayout.findViewById(R.id.results_text);
        resultText.setText(text);
        resultText.setVisibility(View.VISIBLE);
    }

    /**
     * Show a clickable TextView that tells how many links there are
     * and opens a dialog that shows the links when clicked.
     */
    private void initLinksText() {
        ArrayList<Link> links = mPollItem.getLinks();
        if (links == null || links.size() < 1) return;

        TextView linksText = (TextView) mRootLayout.findViewById(R.id.links_text);

        String text = TextUtil.getPluralityString(links.size(), "link");
        SpannableString str = TextUtil.getLinkStyleSpannableString(text, getActivity());
        linksText.setText(str);
        linksText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //must be verified
                if (AppSession.checkVerification(getActivity())) {
                    return;
                }
                showLinks();
            }
        });
        linksText.setVisibility(View.VISIBLE);
    }

    /**
     * Show a clickable TextView that tells how many attachments there are
     * and opens a dialog that shows the attachments when clicked.
     */
    private void initAttachmentsText() {
        ArrayList<Attachment> attachments = mPollItem.getAttachments();
        if (attachments == null || attachments.size() < 1) return;

        TextView attachmentsText = (TextView) mRootLayout.findViewById(R.id.attachments_text);

        String text = TextUtil.getPluralityString(attachments.size(), "attachment");
        SpannableString str = TextUtil.getLinkStyleSpannableString(text, getActivity());
        attachmentsText.setText(str);
        attachmentsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //must be verified
                if (AppSession.checkVerification(getActivity())) {
                    return;
                }
                showAttachments();
            }
        });
        attachmentsText.setVisibility(View.VISIBLE);
    }

    /**
     * Show the number of respondents to this question.
     * If users are not shown with their responses and there are less than five respondents and we are not the creator,
     * Don't allow the user to click the text to see the list of respondents.
     * This helps prevent people from figuring out who said what.  todo should this be more than five?
     * If users ARE shown with their responses, there is no point in showing respondents in a separate list.
     */
    private void initRespondentsText() {
        final TextView respondentsText = (TextView) mRootLayout.findViewById(R.id.respondents_text);
        int submissionCount = mPollItem.getSubmissionCount();

        respondentsText.setMovementMethod(LinkMovementMethod.getInstance());

        String linkText = "";
        String verb;
        String tail = " responded to this question";
        linkText += Integer.toString(submissionCount);
        if (submissionCount > 1) {
            linkText += " people";
            verb = " have";
        } else if (submissionCount == 1) {
            linkText += " person";
            verb = " has";
        } else {
            respondentsText.setText("No one has" + tail);
            return;
        }

        boolean noLink;

        if (mPollItem.getDisplayUser()) {
            //if user can see short answer results and the other users are displayed next
            //to their associated answers, there is no point in having a link to
            //show the users who answered the poll in a separate list
            noLink = mPollItem.getIsShortAnswer() &&
                    (mPollItem.getUserHasSubmitted() || mPollItem.getOwnerIsMe());
        } else {
            //if other users are not displayed, we are not the owner, and their are less than 5 submissions,
            //don't show users for this poll (possible identity security issue)
            noLink = !mPollItem.getOwnerIsMe() && submissionCount < 5;
        }

        if (noLink) {
            respondentsText.setText(linkText + verb + tail);
            respondentsText.setOnClickListener(null);
        } else {
            //instead of making link clickable, make entire textview clickable (easier to click)
            SpannableString str = TextUtil.getLinkStyleSpannableString(linkText, getActivity());
            CharSequence text = TextUtils.concat(str, verb, tail);
            respondentsText.setText(text);
            respondentsText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppSession.checkVerification(getActivity())) {
                        return;
                    }
                    showRespondents();
                }
            });
        }
    }

    /**
     * Preserve text in the short answer text box
     * Dismiss dialogs to prevent leaked windows.
     * todo should be using DialogFragments
     */
    @Override
    public void onDestroyView() {
        if (mShortAnswerText != null) {
            Editable shortAnswer = mShortAnswerText.getText();
            if (shortAnswer != null) {
                mPollItem.setShortAnswer(shortAnswer.toString());
            }
        }

        if (mLinkDialog != null) {
            mLinkDialog.dismiss();
            mLinkDialog = null;
        }

        if (mAttachmentDialog != null) {
            mAttachmentDialog.dismiss();
            mAttachmentDialog = null;
        }

        super.onDestroyView();
    }

    /**
     * Casts into PollActivity
     * @return cast activity
     */
    private PollActivity getPollActivity() {
        return (PollActivity) getActivity();
    }

    /**
     * Show dialogs if they were showing when view destroyed
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mShowingLinks) showLinks();
        else if (mShowingAttachments) showAttachments();
    }

    /**
     * Check for errors (including verification).
     * If none, animate removal of the output layout and send the submission to the server.
     * Uses {@link com.thecn.app.stores.PostStore#sendPollSubmission(java.util.ArrayList, String, String, com.thecn.app.stores.ResponseCallback)}
     */
    private void submitAnswer() {
        if (AppSession.checkVerification(getPollActivity())) {
            return;
        }

        ArrayList<String> answers = new ArrayList<>();

        if (mPollItem.getInputType() == PollItem.InputType.SHORT_ANSWER) {
            String answer = mShortAnswerText.getText().toString();

            if (answer == null || answer.length() < 1) {
                AppSession.showLongToast("Answer cannot be blank.");
                return;
            }

            answers.add(answer);
        } else {
            for (PollItem.Choice choice : mPollItem.getChoices()) {
                if (choice.isSelected()) {
                    answers.add(choice.getSequenceId());
                }
            }

            if (answers.size() < 1) {
                AppSession.showLongToast("Please make a selection.");
                return;
            }
        }

        String postId = getPollActivity().getPost().getId();

        mAnimationStartTime = System.currentTimeMillis();
        removeInputLayout(true);
        mSubmitButton.setVisibility(View.GONE);

        PostStore.sendPollSubmission(answers, postId, mPollItem.getId(), new PollSubmissionCallback(callbackManager));
    }

    /**
     * Used when a request for a poll submission returns.
     */
    private static class PollSubmissionCallback extends CallbackManager.NetworkCallback<PollFragment> {

        private PollItem pollItem;

        public PollSubmissionCallback(CallbackManager<PollFragment> manager) {
            super(manager);
        }

        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                pollItem = PollStore.getItemData(response);
                if (pollItem != null) {
                    AppSession.showShortToast("Poll response submitted!");
                } else {
                    AppSession.showLongToast("Poll submitted, but could not get data.");
                }
            } else {
                showPollDataError();
            }
        }

        @Override
        public void onImmediateError(VolleyError error) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeWithResponse(PollFragment object) {
            if (pollItem != null) {
                object.replacePollItemData(pollItem);
            }

            executeWhenReady(object);
        }

        /**
         * If not enough time has elapsed since the layout was changed
         * for the animation to finish, add this method as a callback
         * to be called when the correct amount of time has passed.
         * @param pollFragment fragment to perform operations on
         */
        private void executeWhenReady(PollFragment pollFragment) {
            long duration = pollFragment.getLayoutTransitionDuration();
            long timeElapsed = System.currentTimeMillis() - pollFragment.getAnimationStartTime();
            if (timeElapsed < duration) {
                long delay = duration - timeElapsed + 100;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.addCallback(new CallbackManager.Callback<PollFragment>() {
                            @Override
                            public void execute(PollFragment object) {
                                executeWhenReady(object);
                            }
                        });
                    }
                }, delay);

                return;
            }

            if (wasSuccessful()) {
                pollFragment.setViewStates(false);
            } else {
                pollFragment.bringBackView();
            }
        }

        @Override
        public void onResumeWithError(PollFragment object) {
            object.bringBackView();
        }
    }

    /**
     * Shows error when we could not submit response.
     */
    private static void showPollDataError() {
        AppSession.showLongToast("Error receiving poll data.");
    }

    /**
     * Brings back the output view when there was an error submitting the
     * poll response.
     */
    private void bringBackView() {
        addInputLayout(false);
        mSubmitButton.setVisibility(View.VISIBLE);
    }

    /**
     * Updates the poll item to reflect the data that was just
     * retrieved from the server.
     * @param pollItem data from the server
     */
    private void replacePollItemData(PollItem pollItem) {
        if (mPollItem == null || pollItem == null) return;

        mPollItem.setChartData(pollItem.getChartData());
        mPollItem.setDisplaySubmissionCount(pollItem.getDisplaySubmissionCount());
        mPollItem.setHasSubmissionsCount(pollItem.getHasSubmissionsCount());
        mPollItem.setDisplayResult(pollItem.getDisplayResult());
        mPollItem.setDisplayUser(pollItem.getDisplayUser());
        mPollItem.setEnabled(pollItem.getEnabled());
        mPollItem.setEnded(pollItem.getEnded());
        mPollItem.setUserHasSubmitted(pollItem.getUserHasSubmitted());
        mPollItem.setQuestionCount(pollItem.getQuestionCount());
        mPollItem.setQuestionOrder(pollItem.getQuestionOrder());
        mPollItem.setShowResultMessage(pollItem.getShowResultMessage());
        mPollItem.setSubmissionCount(pollItem.getSubmissionCount());
        mPollItem.setCorrectResponse(pollItem.getCorrectResponse());
    }

    /**
     * Sets up the input layout.
     * If the user is the owner of the poll, don't let them submit an answer!
     * Layout the input either for single choice, multiple choice, or short answer.
     */
    private void setUpInputLayout() {
        if (mPollItem.getOwnerIsMe()) {
            mSubmitButton.setVisibility(View.GONE);

            showCorrectResponseIfThere();

            boolean showOutput = mPollItem.getDisplayResult() && mPollItem.getSubmissionCount() > 0;

            if (showOutput) {
                // if the owner can see the result and people have answered, don't render the input
                // the input will be disabled for the owner anyway, and multiple/single choice questions can
                // be seen in the output
                return;
            }
        }

        if (mPollItem.getUserHasSubmitted()) {
            mSubmitButton.setVisibility(View.GONE);

            showCorrectResponseIfThere();
        } else {
            switch (mPollItem.getInputType()) {
                case SHORT_ANSWER:
                    layoutShortAnswer();
                    break;
                case ONE_CHOICE:
                    layoutOneChoice();
                    break;
                case MULTIPLE_CHOICE:
                    layoutMultipleChoice();
                    break;
            }
        }
    }

    /**
     * Show a text view that contains the correct answer to the question if it exists in the data.
     */
    private void showCorrectResponseIfThere() {
        String correctResponse = mPollItem.getCorrectResponse();
        if (correctResponse != null && correctResponse.length() > 0) {
            TextView correctResponseView = (TextView) mRootLayout.findViewById(R.id.correct_response_text);
            correctResponseView.setText(Html.fromHtml("<u>Correct Answer</u>: " + correctResponse));
            correctResponseView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Adds input layout
     * @param immediate if true, add immediately, else post a runnable to the parent.
     */
    private void addInputLayout(boolean immediate) {
        if (immediate) {
            mRootLayout.addView(mInputLayout, 1);
        } else {
            postAddView(mRootLayout, mInputLayout, 1);
        }
    }

    /**
     * Removes input layout
     * @param immediate if true, remove immediately, else post a runnable to the parent.
     */
    private void removeInputLayout(boolean immediate) {
        if (immediate) {
            mRootLayout.removeView(mInputLayout);
        } else {
            postRemoveView(mRootLayout, mInputLayout);
        }
    }

    /**
     * Adds output layout
     * @param immediate if true, add immediately, else post a runnable to the parent.
     */
    private void addOutputLayout(boolean immediate) {
        if (immediate) {
            mRootLayout.addView(mOutputLayout);
        } else {
            postAddView(mRootLayout, mOutputLayout);
        }
    }

    /**
     * Posts runnable to add a view to a viewgroup
     * @param parent the parent view
     * @param view the child view to add to parent
     */
    private void postAddView(final ViewGroup parent, final View view) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                parent.addView(view);
            }
        });
    }

    /**
     * Posts runnable to add a view to a viewgroup at the specified index
     * @param parent parent view
     * @param view child view to add to parent
     * @param index index of where to add the child view
     */
    private void postAddView(final ViewGroup parent, final View view, final int index) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                parent.addView(view, index);
            }
        });
    }

    /**
     * Posts a runnable to remove a view from a viewgroup
     * @param parent parent veiw
     * @param view child view to remove
     */
    private void postRemoveView(final ViewGroup parent, final View view) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                parent.removeView(view);
            }
        });
    }

    /**
     * Set up output layout to display choices as radio buttons so that
     * the user can only choose one.
     */
    private void layoutOneChoice() {

        mInputLayout.removeAllViews();

        RadioGroup group = new RadioGroup(getActivity());
        group.setLayoutParams(new RadioGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        group.setOrientation(LinearLayout.VERTICAL);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                //set data member of radio button
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    PollRadioButton button = (PollRadioButton) radioGroup.getChildAt(i);
                    button.setChoiceSelected(button.getId() == id);
                }
            }
        });

        //disable buttons if the owner of the poll is me.  Add them all to the parent view.
        for (PollItem.Choice choice : mPollItem.getChoices()) {
            PollRadioButton button = new PollRadioButton(getActivity(), choice);
            button.setEnabled(!mPollItem.getOwnerIsMe());
            group.addView(button, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            if (choice.isSelected()) button.setChecked(true);
        }

        mInputLayout.addView(group);
        addInputLayout(true);
    }

    /**
     * Set up output layout to display choices as checkboxes so that
     * the user can choose multiple.
     */
    private void layoutMultipleChoice() {

        mInputLayout.removeAllViews();

        for (PollItem.Choice choice : mPollItem.getChoices()) {
            PollCheckBox checkBox = new PollCheckBox(getActivity(), choice);
            checkBox.setEnabled(!mPollItem.getOwnerIsMe());
            checkBox.setChecked(choice.isSelected());
            checkBox.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mInputLayout.addView(checkBox);
        }

        addInputLayout(true);
    }

    /**
     * Set up output layout to display a single TextView where the
     * user can enter an answer to the question.
     */
    private void layoutShortAnswer() {

        mInputLayout.removeAllViews();

        mShortAnswerText = new EditText(getActivity());
        mShortAnswerText.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mShortAnswerText.setEnabled(!mPollItem.getOwnerIsMe());
        mShortAnswerText.setHint("Type your answer here");
        mShortAnswerText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        if (mPollItem.getShortAnswer() != null) {
            mShortAnswerText.setText(mPollItem.getShortAnswer());
        }

        mInputLayout.addView(mShortAnswerText);

        addInputLayout(true);
    }

    /**
     * Show a dialog that displays the respondents to this poll question.
     */
    private void showRespondents() {
        PollActivity activity = getPollActivity();
        if (activity == null) return;

        if (mPollItem.getSubmissionCount() == 0) return;

        String pollItemID = mPollItem.getId();
        String postID = activity.getPost().getId();
        RespondentsDialogFragment f = RespondentsDialogFragment.getInstance(pollItemID, postID);
        f.show(getActivity().getSupportFragmentManager(), "RESPONDENTS_DIALOG");
    }

    private Dialog mLinkDialog;

    /**
     * Show a dialog that displays all the links associated with this poll item in a list view.
     */
    private void showLinks() {
        if (mLinkDialog != null) {
            mLinkDialog.cancel();
        }

        Activity activity = getActivity();
        if (activity == null) return;
        mLinkDialog = new LinkDialog(activity);

        ArrayList<Link> links = mPollItem.getLinks();
        if (links == null || links.size() < 1) return;

        mLinkDialog.setTitle("Links");

        ListView listView = MyDialogListView.getListViewForDialog(getActivity());

        LinkAdapter adapter = new LinkAdapter(getActivity(), links);
        adapter.setOnLinkClickListener(new LinkAdapter.OnLinkClickListener() {
            @Override
            public void onLinkClick(String url) {
                getPollActivity().openURL(url);
            }
        });
        listView.setAdapter(adapter);
        mLinkDialog.setContentView(listView);

        mLinkDialog.show();

        mShowingLinks = true;
    }

    Dialog mAttachmentDialog;

    /**
     * Show a dialog that displays all the attachments associated with this poll item in a list view.
     */
    private void showAttachments() {
        if (mAttachmentDialog != null) {
            mAttachmentDialog.cancel();
        }

        Activity activity = getActivity();
        if (activity == null) return;
        mAttachmentDialog = new AttachmentDialog(activity);

        ArrayList<Attachment> attachments = mPollItem.getAttachments();
        if (attachments == null || attachments.size() < 1) return;

        mAttachmentDialog.setTitle("Attachments");

        ListView listView = MyDialogListView.getListViewForDialog(getActivity());

        AttachmentAdapter adapter = new AttachmentAdapter(getPollActivity(), attachments);
        listView.setAdapter(adapter);
        mAttachmentDialog.setContentView(listView);

        mAttachmentDialog.show();

        mShowingAttachments = true;
    }

    /**
     * A dialog for showing links.  Sets a flag when cancelled.
     */
    private class LinkDialog extends Dialog {

        public LinkDialog(Context context) {
            super(context);
        }

        @Override
        public void cancel() {
            super.cancel();
            mShowingLinks = false;
        }
    }

    /**
     * A dialog for showing attachments.  Sets a flag when cancelled.
     */
    private class AttachmentDialog extends Dialog {

        public AttachmentDialog(Context context) {
            super(context);
        }

        @Override
        public void cancel() {
            super.cancel();
            mShowingAttachments = false;
        }
    }

    /**
     * Shows the submissions to this poll item (short answer responses) in
     * the list view.
     */
    private void showSubmissions() {
        if (mSubmissionList == null) {
            AppSession.showLongToast("Could not display responses.");
            return;
        }

        mSubmissionController.setUpListView();
    }

    /**
     * Check the submissions to make sure there are no data faults.
     * If there is a data fault in a submission, remove the submission instead of completely failing.
     * @param submissions list of submissions to this poll item
     * @return true if there was not a data fault.
     */
    private boolean checkSubmissionData(ArrayList<PollItem.Submission> submissions) {
        if (submissions == null) return false;

        for (int i = 0; i < submissions.size(); i++) {
            PollItem.Submission submission = submissions.get(i);
            ArrayList<String> answers = submission.getAnswers();

            if (answers == null) {
                submissions.remove(i);
            } else {
                for (int j = 0; j < answers.size(); j++) {
                    //remove a faulty Submission
                    String answer = answers.get(j);
                    if (answer == null) answers.remove(j);
                }

                if (answers.size() < 1) submissions.remove(i);
            }
        }

        return true;
    }

    /**
     * Totals that counts (votes) for each choice in the question
     * Assigns colors to each slice of the pie chart (the ones with at lease one count)
     * @return true if successful, false on error
     *
     * thanks to Niels Bosma for this idea http://stackoverflow.com/a/5651670
     */
    private boolean prepareChartData() {
        ArrayList<PollItem.ChartMember> chartMembers = mPollItem.getChartData();
        if (chartMembers == null || chartMembers.size() == 0) return false;

        LinkedList<PollItem.ChartMember> membersWithSlice = new LinkedList<PollItem.ChartMember>();

        int countTotal = 0;
        /**
         * count the total counts (or votes) by people who answered this poll
         * add chart members with at least one vote to a list
         * so their color can be added later
         */
        for (PollItem.ChartMember member : chartMembers) {
            //the member and its name should never be null
            if (member == null || member.getName() == null) return false;

            int count = member.getCount();
            countTotal += count;

            if (count > 0) {
                membersWithSlice.add(member);
            }
        }
        //there should always be at least one submission
        if (countTotal < 1) return false;

        mPollItem.setCountTotal(countTotal);

        //int baseColor = Color.rgb(237, 194, 64);
        int baseColor = Color.rgb(222, 179, 49);
        membersWithSlice.getFirst().setColor(baseColor);

        float[] baseHSV = new float[3];
        Color.colorToHSV(baseColor, baseHSV);
        double step = 360.0 / (double) membersWithSlice.size();

        for (int i = 1; i < membersWithSlice.size(); i++) {
            /**
             * derive other pie slice colors from base color
             * "grab" these colors from all around the base color's hue continuum
             */
            float[] newHSV = new float[3];
            newHSV[0] = (float) ((baseHSV[0] + step * ((double) i)) % 360.0);
            newHSV[1] = baseHSV[1];
            newHSV[2] = baseHSV[2];

            int newColor = Color.HSVToColor(newHSV);
            membersWithSlice.get(i).setColor(newColor);
        }

        return true;
    }

    /**
     * Prepare the chart view itself.  Calls {@link #prepareChartData()} before
     * setting up the view.
     * @return false if there is an error
     */
    private boolean prepareChart() {

        if (!prepareChartData()) return false;

        DefaultRenderer renderer = new DefaultRenderer();

        renderer.setChartTitleTextSize(20);
        renderer.setInScroll(true);
        renderer.setPanEnabled(false);
        renderer.setShowLabels(false);
        renderer.setZoomEnabled(false);
        renderer.setExternalZoomEnabled(false);
        renderer.setShowLegend(false); //screw your legend, I'll make my own
        renderer.setStartAngle(270);

        CategorySeries series = new CategorySeries(mPollItem.getText());

        for (PollItem.ChartMember member : mPollItem.getChartData()) {
            //chart data already checked for null members, no need now
            series.add(member.getName(), member.getCount());
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(member.getColor());
            renderer.addSeriesRenderer(seriesRenderer);
        }

        mChartView = ChartFactory.getPieChartView(getActivity(), series, renderer);

        return true;
    }

    /**
     * Prepare and add the pie chart to the output layout
     * @param showError if true, show that there was an error if one occurs
     */
    private void addChart(final boolean showError) {
        //remove the old view if it's there
        if (mChartView != null && mChartView.getParent() != null) {
            mOutputLayout.removeView(mChartView);
        }

        if (!prepareChart()) {
            if (showError) {
                AppSession.showLongToast("Could not show chart data.");
            }

            return;
        }

        mOutputLayout.addView(mChartView);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mChartView.getLayoutParams();
        params.height = (int) getActivity().getResources().getDimension(R.dimen.pie_chart_height);
    }

    /**
     * Should always be called after prepareChartData()
     * Prepares the view for displaying the legend for the pie chart.
     * @return false if there was an error
     */
    private boolean prepareLegend() {

        mLegendView = new LinearLayout(getActivity());
        mLegendView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mLegendView.setOrientation(LinearLayout.VERTICAL);
        mLegendView.setGravity(Gravity.CENTER);

        List<LinearLayout> legendEntries = new ArrayList<LinearLayout>();

        //used to decide which colored text view is the largest so as
        //to set them all to that size
        int largestColorTextWidth = 0;
        for (PollItem.ChartMember member : mPollItem.getChartData()) {

            LinearLayout legendEntry = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.poll_chart_legend_entry, null);

            TextView colorText = (TextView) legendEntry.findViewById(R.id.legend_item_color_text);
            if (member.getCount() == 0) {
                //zero percent answered this choice
                colorText.setBackgroundColor(Color.BLACK);
                colorText.setText("0%");
            } else {
                //someone answered this choice
                colorText.setBackgroundColor(member.getColor());
                int percent = member.getCount() * 100 / mPollItem.getCountTotal();
                colorText.setText(Integer.toString(percent) + "%");
            }

            //update largest view width
            int colorTextWidth = getProjectedWidth(colorText);
            if (colorTextWidth > largestColorTextWidth)
                largestColorTextWidth = colorTextWidth;

            TextView choiceText = (TextView) legendEntry.findViewById(R.id.text_area);
            choiceText.setText(member.getName());

            legendEntries.add(legendEntry);
        }

        //should not still be zero
        if (largestColorTextWidth < 1) return false;

        int largestWidth = 0;
        for (LinearLayout legendEntry : legendEntries) {

            TextView colorText = (TextView) legendEntry.findViewById(R.id.legend_item_color_text);
            int colorTextWidth = colorText.getMeasuredWidth(); //should be set from before
            //set right margin to make up for any difference in width between color area and text
            int additionalWidth = largestColorTextWidth - colorTextWidth;

            colorText.setPadding(colorText.getPaddingLeft() + additionalWidth,
                    colorText.getPaddingTop(),
                    colorText.getPaddingRight(),
                    colorText.getPaddingBottom()
            );

            int width = getProjectedWidth(legendEntry);
            if (width > largestWidth) largestWidth = width;
        }

        //should not still be zero
        if (largestWidth < 1) return false;

        int fragmentRootViewWidth = getFragmentRootViewWidth();
        if (fragmentRootViewWidth < 1) return false; //if true, something's not right

        int columns = fragmentRootViewWidth / largestWidth;

        //generate legend based on the amount of space available to draw it with

        if (columns > 1) {

            int leftOverWidth = columns * largestWidth;
            int leftMargin = leftOverWidth / 2;
            LinearLayout row = getGridRow(leftMargin);

            if (legendEntries.size() > 0) {
                int numEntries = legendEntries.size();
                //add as many entries for each row as was calculated into "columns" value
                for (int i = 0; i < numEntries - 1; i++) {
                    addLegendEntry(legendEntries.get(i), row, largestWidth);

                    if (i % columns == columns - 1) {
                        mLegendView.addView(row);
                        row = getGridRow(leftMargin);
                    }
                }

                addLegendEntry(legendEntries.get(numEntries - 1), row, largestWidth);
                if (mLegendView.getChildCount() > 0) {
                    /*
                    * make dummy views to make up the difference in space between other rows
                    * and this row if the rows number more than 1
                    **/
                    int numLessChildrenInRow = columns - row.getChildCount();
                    for (int i = 0; i < numLessChildrenInRow; i ++) {
                        View view = new View(getActivity());
                        view.setLayoutParams(new ViewGroup.LayoutParams(largestWidth, 0));
                        row.addView(view);
                    }
                }
                mLegendView.addView(row);
            }
        } else {

            if (largestWidth > fragmentRootViewWidth) {
                //width should not be larger than available space
                largestWidth = fragmentRootViewWidth;
            }

            for (LinearLayout entry : legendEntries) {
                addLegendEntry(entry, mLegendView, largestWidth);
            }
        }

        return true;
    }

    /**
     * Get the width of the fragment's root view
     * @return width of fragment root view.
     */
    private int getFragmentRootViewWidth() {
        int padding = mHorizontalMargin * 2;
        return DisplayUtil.getDisplayWidth(getActivity()) - padding;
    }

    /**
     * Get the projected width of a view before it is drawn on the screen
     * @param view view to get the width of
     * @return the projected width of the view
     */
    private int getProjectedWidth(View view) {
        //when using UNSPECIFIED, size input does not matter, view measures itself as large is it wants
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        view.measure(widthSpec, heightSpec);

        //measured width is the projected width of a view based on MeasureSpecs passed in measure()
        //this is not necessarily the same as when the view is actually rendered
        return view.getMeasuredWidth();
    }

    /**
     * Gets a horizontally oriented LinearLayout that can be used to hold legend items.
     * @param leftMargin left margin to set to the linear layout
     * @return a linear layout to be used as a legend row
     */
    private LinearLayout getGridRow(int leftMargin) {
        LinearLayout gridRow = new LinearLayout(getActivity());
        gridRow.setGravity(Gravity.LEFT);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        marginParams.setMargins(leftMargin, 0, 0, 0);
        gridRow.setLayoutParams(marginParams);

        return gridRow;
    }

    /**
     * Adds a legend entry (Colored text view that displays a percent along with a text view that displays the
     * corresponding choice) to a ViewGroup
     * @param entry the view to add to the parent
     * @param parent the parent to add the child view to
     * @param width the width the child should conform to
     */
    private void addLegendEntry(LinearLayout entry, ViewGroup parent, int width) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, WRAP_CONTENT);
        entry.setLayoutParams(params);
        parent.addView(entry);
    }

    /**
     * Remove old legend layout if it exists.  Add new legend to the output layout.
     * @param showError whether to show an error if one occurs while preparing legend view.
     */
    private void addLegend(final boolean showError) {
        if (mLegendView != null && mLegendView.getParent() != null) {
            mOutputLayout.removeView(mLegendView);
        }

        if (!prepareLegend()) {
            if (showError) {
                AppSession.showLongToast("Could not show chart legend data.");
            }
            return;
        }

        mOutputLayout.addView(mLegendView);
    }

    /**
     * Show chart and legend all in one step.
     */
    private void showChartAndLegend() {
        addChart(true);
        addLegend(false);

        addOutputLayout(true);
    }

    /**
     * Controls the display of items in a list view.  These items are
     * responses that have been submitted for this poll item.  They can have
     * a user associated with them OR the user might be hidden.  The submission controller
     * stays in the {@link com.thecn.app.activities.poll.PollFragment}'s memory always, but
     * it may be associated with a number of list views (say, when an orientation change takes
     * place).
     */
    private class SubmissionController {
        private int offset = 0;
        private final int limit = 10;
        private boolean loading = false;
        private boolean noMore = false;

        private ListFooterController mFooter;

        /**
         * Configure the list view to show poll submissions.
         */
        public void setUpListView() {
            mFooter = new ListFooterController(mSubmissionList, LayoutInflater.from(mSubmissionList.getContext()));
            mSubmissionList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                        getSubmissions();
                    }
                }
            });

            if (loading) {
                mFooter.setLoading();
            } else if (mPollSubmissionAdapter.getCount() == 0) {
                getSubmissions();
            } else {
                mFooter.clear();
            }
        }

        /**
         * Get submissions if there are more and we are not already getting submissions.
         */
        public void getSubmissions() {
            if (!loading && !noMore) {
                loading = true;
                mFooter.setLoading();

                String contentID = getPollActivity().getPost().getId();
                String itemID = mPollItem.getId();
                PollStore.getPollSubmissions(contentID, itemID, limit, offset, new SubmissionCallback(callbackManager));
            }
        }

        /**
         * Called when poll submission data has been successfully retrieved from the server
         * @param response json that contains the data
         */
        public void onSuccess(JSONObject response) {
            ArrayList<PollItem.Submission> submissions = PollStore.getSubmissionListData(response);

            //check data for errors
            if (checkSubmissionData(submissions)) {

                mPollSubmissionAdapter.addAll(submissions);

                int nextOffset = StoreUtil.getNextOffset(response);
                if (nextOffset != -1) offset = nextOffset;

            } else {
                //checkSubmissionData() returns false if there are no entries in list
                noMore = true;
            }
        }

        /**
         * Called when the network call has returned.
         */
        public void onLoadingComplete() {
            loading = false;

            mFooter.remove();
        }
    }

    /**
     * Returns this {@link com.thecn.app.activities.poll.PollFragment}'s submission controller
     * @return submission controller
     */
    public SubmissionController getSubmissionController() {
        return mSubmissionController;
    }

    /**
     * Used to make decisions when a network call for poll submissions returns.
     */
    private static class SubmissionCallback extends CallbackManager.NetworkCallback<PollFragment> {
        public SubmissionCallback(CallbackManager<PollFragment> callbackManager) {
            super(callbackManager);
        }

        @Override
        public void onResumeWithResponse(PollFragment object) {
            if (wasSuccessful()) {
                object.getSubmissionController().onSuccess(response);
            } else {
                AppSession.showDataLoadError("submission");
            }
        }

        @Override
        public void onResumeWithError(PollFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(PollFragment object) {
            object.getSubmissionController().onLoadingComplete();
        }
    }
}
