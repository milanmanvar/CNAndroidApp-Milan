package com.thecn.app.activities.course;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.course.Task;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.network.Downloader;
import com.thecn.app.tools.text.InternalURLSpan;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shows tasks from a Course.  This is old and needs to be updated.  Current method for
 * showing tasks is to manually parse content, which is not good.  A better approach would
 * be to use JSoup (already in the libs folder).  A WebView could also be used to format
 * the content automatically.  Task related actions could be intercepted and handled using a WebInterface
 * (but don't enable Javascript, because the user could possible write in something malicious).
 */

public class CourseTasksFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = CourseTasksFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";
    private final Object mTaskLock = new Object();
    protected SwipeRefreshLayout swipeRefreshLayout;
    private Course mCourse;
    //prevents async changes to task data
    private ArrayList<Task> mTasks;
    private int tasksIndex;
    private int numTasks;
    private TaskLinkPatterns tlps;

    private CallbackManager<CourseTasksFragment> callbackManager;

    private Button leftTaskButton;
    private Button rightTaskButton;
    private TextView taskTitle;
    private TextView taskContent;

    /**
     * @param mCourse must have course object to put in arguments
     * @return new instance
     */
    public static CourseTasksFragment newInstance(Course mCourse) {
        CourseTasksFragment fragment = new CourseTasksFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Initialize data
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
        mTasks = mCourse.getTasks();
        numTasks = mTasks != null ? mTasks.size() : 0;
        tasksIndex = 0;

        callbackManager = new CallbackManager<>();

        tlps = new TaskLinkPatterns();
    }

    /**
     * Gets references to task title field and left right buttons.
     * Also sets up swipe refresh layout.
     * A ViewPager would be much nicer here for scrolling through subtasks...
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_tasks, container, false);

        taskTitle = (TextView) view.findViewById(R.id.task_title);
        taskContent = (TextView) view.findViewById(R.id.task_content);
        taskContent.setMovementMethod(LinkMovementMethod.getInstance());

        leftTaskButton = (Button) view.findViewById(R.id.left_task_button);
        leftTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLeftTaskButtonClick();
            }
        });
        leftTaskButton.setVisibility(View.INVISIBLE);

        rightTaskButton = (Button) view.findViewById(R.id.right_task_button);
        rightTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightTaskButtonClick();
            }
        });
        rightTaskButton.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = new SwipeRefreshLayout(getActivity());
        swipeRefreshLayout.addView(view);
        swipeRefreshLayout.setOnRefreshListener(this);

        return swipeRefreshLayout;
    }

    /**
     * Begins loading tasks if there are some
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (numTasks > 0) {
            setTask();
            loadTasks();
        } else {
            taskTitle.setText("");
            taskContent.setText("No tasks available for this course.");
        }
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

    /**
     * Set view to show that a task is loading.
     */
    private void setTextFieldsLoading() {
        taskTitle.setText("Loading...");
        taskContent.setText("");
    }

    /**
     * Gets indices of tasks to load based on taskIndex and then loads those tasks
     * if they are not already loaded or loading.
     */
    private void loadTasks() {

        int[] indices = getIndicesOfTasksToLoad();

        synchronized (mTaskLock) {
            for (final int i : indices) {
                Task localTask = mTasks.get(i);

                if (localTask.getLoadingState() == Task.LoadingState.NOT_SET) {
                    localTask.setLoadingState(Task.LoadingState.LOADING);

                    CourseStore.getTaskDetails(localTask.getId(), new TaskDetailsCallback(i, callbackManager));
                }
            }
        }
    }

    /**
     * Begin formatting the task content that was returned from the network call.
     *
     * @param response data from server
     * @param index    index of this task in the list of tasks
     */
    public void onTaskDetailSuccess(JSONObject response, int index) {
        Task task = CourseStore.getTaskData(response);

        if (task != null) {
            new FormatTaskTask(index, task.getTitle(), task.getDisplayText(), task.getId()).execute();
        }
    }

    /**
     * Set up data to show an error for this task.
     *
     * @param index index of this task in the list of tasks
     */
    public void onTaskDetailError(int index) {
        synchronized (mTaskLock) {
            if (mTasks != null) {
                Task task = mTasks.get(index);
                task.setFormattedContent("Could not load task content");
                task.setLoadingState(Task.LoadingState.DONE_LOADING);
            }
        }
    }

    /**
     * Set the data for this task after formatting.  Refresh the view to reflect this.
     *
     * @param group formatted content of the task
     * @param index index of the task that was just processed
     */
    public void onTaskFinishProcess(TitleContentGroup group, int index) {
        synchronized (mTaskLock) {
            if (mTasks != null) {
                Task task = mTasks.get(index);

                task.setFormattedTitle(group.title);
                task.setFormattedContent(group.content);

                task.setLoadingState(Task.LoadingState.DONE_LOADING);

                setTask();
            }
        }
    }

    /**
     * Load this task and up to four tasks surrounding it
     * example: index is 5,
     * load 3, 4, 5, 6, 8
     *
     * @return array of indices of the tasks to load
     */
    private int[] getIndicesOfTasksToLoad() {

        ArrayList<Integer> indices = new ArrayList<>();

        indices.add(tasksIndex);

        int holder;

        holder = tasksIndex - 1;
        if (holder >= 0) {
            indices.add(holder);

            holder--;

            if (holder >= 0) indices.add(holder);
        }

        holder = tasksIndex + 1;
        if (holder < numTasks) {
            indices.add(holder);

            holder++;

            if (holder < numTasks) indices.add(holder);
        }

        int[] returnIndices = new int[indices.size()];
        for (int i = 0; i < returnIndices.length; i++) {
            returnIndices[i] = indices.get(i);
        }

        return returnIndices;

    }

    /**
     * Sets the view depending on the task's state (see Task.LoadingState in models)
     * Also sets visibility of buttons that navigate back and forwards
     * from tasks.
     * e.g., don't show the left button if it is the first task in the list.
     */
    private void setTask() {

        synchronized (mTaskLock) {

            Task task = mTasks.get(tasksIndex);

            if (task.getLoadingState() == Task.LoadingState.DONE_LOADING) {
                taskTitle.setText(task.getFormattedTitle());
                taskContent.setText(task.getFormattedContent());
            } else {
                setTextFieldsLoading();
            }
        }

        if (tasksIndex != 0) {
            leftTaskButton.setEnabled(true);
            leftTaskButton.setVisibility(View.VISIBLE);
        } else {
            leftTaskButton.setEnabled(false);
            leftTaskButton.setVisibility(View.INVISIBLE);
        }

        if (tasksIndex < numTasks - 1) {
            rightTaskButton.setEnabled(true);
            rightTaskButton.setVisibility(View.VISIBLE);
        } else {
            rightTaskButton.setEnabled(false);
            rightTaskButton.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Could be replaced with Html.fromHtml(taskTitle).
     * This is a stupid method.
     *
     * @param taskTitle title to format
     * @return formatted title
     */
    private CharSequence formatTaskTitle(String taskTitle) {
        return Html.fromHtml(taskTitle);
    }

    /**
     * Manually formats HTML content to display in Android.
     * <p/>
     * This needs to be changed...use JSoup instead with a WebView
     * Manually goes through HTML and parses out the anchors that pertain
     * to possible Task Actions
     *
     * @param taskContent content to be formatted
     * @return formatted content
     */
    private CharSequence formatTaskContent(String taskContent, final String taskId) {

        if (taskContent != null) {
            ArrayList<LinkAssociator> linkAssociations = new ArrayList<>();

            //used by matchers to mark areas to change
            //todo this is stupid
            String head = "#@%!&";
            String tail = "&!%@#";

            int linkIndex = 0;
            //go through text checking for each possible pattern
            for (int typeIndex = 0; typeIndex < tlps.allPatterns.length; typeIndex++) {
                Pattern[] currentPatternSet = tlps.allPatterns[typeIndex];

                for (int patternIndex = 0; patternIndex < currentPatternSet.length; patternIndex++) {
                    Pattern pattern = currentPatternSet[patternIndex];

                    boolean keepGoing = true;
                    int matcherIndex = 0;
                    while (keepGoing) {

                        Matcher matcher = pattern.matcher(taskContent);
                        if (matcher.find(matcherIndex)) {

                            //match anchors
                            Matcher anchorCloseMatcher = tlps.anchorClosePattern.matcher(taskContent);
                            if (anchorCloseMatcher.find(matcher.end())) {
                                String replacement = head + Integer.toString(linkIndex) + tail;

                                final String linkContent = taskContent.substring(matcher.end(), anchorCloseMatcher.start());

                                taskContent = taskContent.substring(0, matcher.start())
                                        + replacement + taskContent.substring(anchorCloseMatcher.end(), taskContent.length());
                                matcherIndex = matcher.start() + replacement.length();

                                SpannableString finalSpan = new SpannableString(Html.fromHtml(linkContent));
                                //supported link
                                if (typeIndex == 0) {
                                    //possible location of the id is either in group 1 or 2 depending on how
                                    //regex was matched
                                    String contentID = matcher.group(1);
                                    final String finalContentID = contentID != null ? contentID : matcher.group(2);

                                    //set a clickable span
                                    final InternalURLSpan ius = new InternalURLSpan();
                                    if (patternIndex < 2) {
                                        //this is a download link
                                        final Attachment attachment = new Attachment(finalContentID);
                                        ius.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (!ius.isClickActionWorking()) {
                                                    ius.setClickActionWorking(true);
                                                    beginDownload(attachment);
                                                    ius.setClickActionWorking(false);

                                                    //increment number of downloads
                                                    MyVolley.act_log("download_attachment", taskId, attachment.getId());
                                                }
                                            }
                                        });
                                    } else {
                                        //this is a "view post" link
                                        ius.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (!ius.isClickActionWorking()) {
                                                    ius.setClickActionWorking(true);
                                                    PostStore.getPostById(finalContentID, new LinkClickCallback(ius, callbackManager));
                                                }
                                            }
                                        });
                                    }
                                    finalSpan.setSpan(ius, 0, finalSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                } else {
                                    //this is a currently unsupported link on Android
                                    final InternalURLSpan ius = new InternalURLSpan();
                                    ius.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AppSession.showLongToast("Link not currently supported on Android.");
                                        }
                                    });
                                    finalSpan.setSpan(ius, 0, finalSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }

                                linkAssociations.add(new LinkAssociator(replacement, finalSpan));
                                linkIndex++;
                            }
                        } else {
                            keepGoing = false;
                        }
                    }
                }
            }

            CharSequence finalText = Html.fromHtml(taskContent);

            String[] stringIDs = new String[linkAssociations.size()];
            SpannableString[] links = new SpannableString[linkAssociations.size()];

            //create a list of all patterns to replace in text (string ids) with InternalURLSpans
            for (int i = 0; i < linkAssociations.size(); i++) {
                LinkAssociator linkAssociation = linkAssociations.get(i);
                stringIDs[i] = linkAssociation.stringID;
                links[i] = linkAssociation.span;
            }

            return TextUtils.replace(finalText, stringIDs, links);
        }

        return "(No task content)";
    }

    public void onLinkClickSuccess(JSONObject response) {
        Post post = PostStore.getData(response);

        if (post != null) {
            getNavigationActivity().openPostPage(post, false);
        }
    }

    public NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    /**
     * Uses {@link com.thecn.app.tools.network.Downloader} to download something in a task link
     *
     * @param attachment an attachment to download
     */
    private void beginDownload(Attachment attachment) {
        String title = "CN File Download";
        Downloader.downloadAttachment(attachment, title, getActivity());
    }

    /**
     * Changes the view to reflect the task before the last in the list.
     * Loads other tasks surrounding the new one.
     */
    private void onLeftTaskButtonClick() {
        if (tasksIndex != 0) {
            tasksIndex--;
            loadTasks();
            setTask();
        }
    }

    /**
     * Changes the view to reflect the task after the last in the list.
     * Loads other tasks surrounding the new one.
     */
    private void onRightTaskButtonClick() {
        if (tasksIndex < numTasks - 1) {
            tasksIndex++;
            loadTasks();
            setTask();
        }
    }

    /**
     * Simply sets all tasks to not set so they will automatically reload.
     */
    @Override
    public void onRefresh() {
        synchronized (mTaskLock) {
            for (Task t : mTasks) {
                t.setLoadingState(Task.LoadingState.NOT_SET);
            }
        }

        loadTasks();

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Used when a network call to get task data returns.
     */
    private static class TaskDetailsCallback extends CallbackManager.NetworkCallback<CourseTasksFragment> {

        private int index;

        public TaskDetailsCallback(int index, CallbackManager<CourseTasksFragment> manager) {
            super(manager);
            this.index = index;
        }

        @Override
        public void onResumeWithResponse(CourseTasksFragment object) {
            if (wasSuccessful()) {
                object.onTaskDetailSuccess(response, index);
            } else {
                object.onTaskDetailError(index);
            }
        }

        @Override
        public void onResumeWithError(CourseTasksFragment object) {
            object.onTaskDetailError(index);
        }
    }

    /**
     * Associates a link with a task data ID
     */
    private static class LinkAssociator {
        public String stringID;
        public SpannableString span;

        public LinkAssociator(final String stringID, SpannableString span) {
            this.stringID = stringID;
            this.span = span;
        }
    }

    /**
     * When a user clicks a link in the task content and that link starts a network request, this class
     * is used as the callback for that request.
     */
    private static class LinkClickCallback extends CallbackManager.NetworkCallback<CourseTasksFragment> {
        private InternalURLSpan span;

        public LinkClickCallback(InternalURLSpan span, CallbackManager<CourseTasksFragment> manager) {
            super(manager);
            this.span = span;
        }

        @Override
        public void onResumeWithResponse(CourseTasksFragment object) {
            if (wasSuccessful()) {
                object.onLinkClickSuccess(response);
            } else {
                AppSession.showLongToast("Content does not exist");
            }
        }

        @Override
        public void onResumeWithError(CourseTasksFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(CourseTasksFragment object) {
            span.setClickActionWorking(false);
        }
    }

    /**
     * Contains patterns for possible task action links (anchor tags) in HTML.
     * JSoup should be used instead.
     */
    private static class TaskLinkPatterns {

        //two different patterns for different orders of attributes
        public Pattern downloadAttachment;
        public Pattern downloadAttachment2;
        public Pattern uploadAttachment;
        public Pattern uploadAttachment2;
        public Pattern viewPost;
        public Pattern viewPost2;
        public Pattern viewPoll;
        public Pattern viewPoll2;
        public Pattern viewEvent;
        public Pattern viewEvent2;
        public Pattern viewQuiz;
        public Pattern viewQuiz2;
        public Pattern createPost;
        public Pattern createPost2;
        public Pattern createPoll;
        public Pattern createPoll2;

        public Pattern catchAllUnsupported;

        public Pattern anchorClosePattern;

        public Pattern[] postPatterns;
        public Pattern[] unsupportedPatterns;
        public Pattern[][] allPatterns;

        public TaskLinkPatterns() {
            String anchorStart = "<[^>]*a[^>]*";
            String typeHTML = "data-taskactionlink-type[\\s]*=[\\s]*\"";
            String quoteTail = "\"[^>]*";
            String id = "data-taskactionlink-data-id[\\s]*=[\\s]*\"([a-zA-Z0-9]*)\"[^>]*";
            String id2 = "data-taskactionlink-id[\\s]*=[\\s]*\"([a-zA-Z0-9]*)\"[^>]*";

            String head = anchorStart + typeHTML;
            String middle = quoteTail + id + ">|" + anchorStart + id + typeHTML;
            String middle2 = quoteTail + id2 + ">|" + anchorStart + id2 + typeHTML;
            String tail = quoteTail + ">";

            String type;

            type = "download_attachment";
            downloadAttachment = Pattern.compile(head + type + middle + type + tail);
            downloadAttachment2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "upload_attachment";
            uploadAttachment = Pattern.compile(head + type + middle + type + tail);
            uploadAttachment2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_post";
            viewPost = Pattern.compile(head + type + middle + type + tail);
            viewPost2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_survey";
            viewPoll = Pattern.compile(head + type + middle + type + tail);
            viewPoll2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_event";
            viewEvent = Pattern.compile(head + type + middle + type + tail);
            viewEvent2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_quiz";
            viewQuiz = Pattern.compile(head + type + middle + type + tail);
            viewQuiz2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "create_post";
            createPost = Pattern.compile(head + type + middle + type + tail);
            createPost2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "create_survey";
            createPoll = Pattern.compile(head + type + middle + type + tail);
            createPoll2 = Pattern.compile(head + type + middle2 + type + tail);

            catchAllUnsupported = Pattern.compile(anchorStart + "href[\\s]*=[\\s]*\"javascript:;\"[^>]*>");

            anchorClosePattern = Pattern.compile("<[\\s]*/[\\s]*a[\\s]*>");

            postPatterns = new Pattern[]{
                    downloadAttachment,
                    downloadAttachment2,
                    viewPost,
                    viewPoll,
                    viewEvent,
                    viewQuiz,
                    viewPost2,
                    viewPoll2,
                    viewEvent2,
                    viewQuiz2
            };

            unsupportedPatterns = new Pattern[]{
                    catchAllUnsupported
            };

            allPatterns = new Pattern[][]{
                    postPatterns,
                    unsupportedPatterns
            };
        }
    }

    /**
     * Formats task content once it is loaded from network
     */
    private class FormatTaskTask extends AsyncTask<Void, Void, Void> {

        int mIndex;
        TitleContentGroup mGroup;

        public FormatTaskTask(int index, String title, String content, String taskId) {
            mIndex = index;
            mGroup = new TitleContentGroup(title, content, taskId);
        }

        @Override
        protected Void doInBackground(Void... params) {

            mGroup.title = formatTaskTitle(mGroup.title.toString());
            mGroup.content = formatTaskContent(mGroup.content.toString(), mGroup.taskId.toString());

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            callbackManager.addCallback(new CallbackManager.Callback<CourseTasksFragment>() {
                @Override
                public void execute(CourseTasksFragment object) {
                    object.onTaskFinishProcess(mGroup, mIndex);
                }
            });
        }
    }

    /**
     * Used to pass formatted task data back to the fragment after processing (formatting).
     */
    private class TitleContentGroup {
        CharSequence title;
        CharSequence content;
        CharSequence taskId;

        public TitleContentGroup(CharSequence title, CharSequence content, CharSequence taskId) {
            this.title = title;
            this.content = content;
            this.taskId = taskId;
        }
    }

}
