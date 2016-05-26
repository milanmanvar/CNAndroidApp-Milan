package com.thecn.app.activities.course;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.createpost.CreatePostActivity;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.course.CourseTaskLinkDetail;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.network.Downloader;
import com.thecn.app.tools.text.InternalURLSpan;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by utsav.k on 11-12-2015.
 */
public class TaskFullScreen1 extends NavigationActivity {
    WebView txtContent;
    private String id;
    private TaskLinkPatterns tlps;
    private CallbackManager<TaskFullScreen> callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_full);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        txtContent = (WebView) findViewById(R.id.task_content);
        callbackManager = new CallbackManager<>();
        txtContent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        txtContent.getSettings().setBuiltInZoomControls(true);
        txtContent.getSettings().setDisplayZoomControls(false);
        txtContent.getSettings().setLoadsImagesAutomatically(true);

        id = this.getIntent().getStringExtra("id");
        tlps = new TaskLinkPatterns();
        //txtContent.setMovementMethod(LinkMovementMethod.getInstance());
        if (this.getIntent().hasExtra("content")) {
            //txtContent.setText(formatTaskContent(this.getIntent().getCharSequenceExtra("content").toString(), this.getIntent().getStringExtra("id")));
            txtContent.getSettings().setJavaScriptEnabled(true);
            txtContent.getSettings().setDomStorageEnabled(true);

            txtContent.setWebViewClient(new WebViewClient() {


                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.e("Url:", "data-taskactionlink-type called...." + "=" + url);
                    String[] temp = url.split("&");
                    String dataId = "";
                    for (int i = 0; i < temp.length; i++) {
                        if (temp[i].contains("data-taskactionlink-data-id")) {
                            dataId = temp[i];
                            break;
                        }
                    }
                    if (dataId.contains("=")) {
                        String tempData[] = dataId.split("=");
                        dataId = tempData.length > 1 ? tempData[1] : "";
                    }
                    Log.e("url id:", "" + dataId);
                    if (url.contains("data-taskactionlink-type=create_post")) {

                        if (txtContent.canGoBack()) {
                            txtContent.goBack();
                        }

                        Intent intent = new Intent(TaskFullScreen1.this, CreatePostActivity.class);
                        startActivity(intent);

                    } else if (url.contains("data-taskactionlink-type=view_content_page")) {

                        CourseStore.getCourseContentPage(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("content page:", "" + response);
                                Intent iDetail = new Intent(TaskFullScreen1.this, TaskLinkDetail.class);
                                iDetail.putExtra("data", parseLinkResponse(response));
                                startActivity(iDetail);
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });


                    } else if (url.contains("data-taskactionlink-type=view_content_post")) {

                        CourseStore.getCourseContentPost(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("content post:", "" + response);
                                Intent iDetail = new Intent(TaskFullScreen1.this, TaskLinkDetail.class);
                                iDetail.putExtra("data", parseLinkResponse(response));
                                startActivity(iDetail);
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });

                    } else if (url.contains("data-taskactionlink-type=view_post")) {
                        PostStore.getPostById(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Post post = PostStore.getData(response);

                                if (post != null) {
                                    ((NavigationActivity) TaskFullScreen1.this).openPostPage(post, false);
                                }
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });
                    } else if (url.contains("data-taskactionlink-type=download_attachment")) {
                        final Attachment attachment = new Attachment(dataId);
                        beginDownload(attachment);
                        MyVolley.act_log("download_attachment", id, attachment.getId());
                    } else if (url.startsWith("www.") || url.startsWith("http")) {
                        Intent iView = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(iView);
                    } else if (url.contains("data-taskactionlink-type=view_survey")) {
                        PostStore.getPostById(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Post post = PostStore.getData(response);

                                if (post != null) {
                                    ((NavigationActivity) TaskFullScreen1.this).openPostPage(post, false);
                                }
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });
                    } else if (url.contains("data-taskactionlink-type=view_event")) {
                        PostStore.getPostById(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Post post = PostStore.getData(response);

                                if (post != null) {
                                    ((NavigationActivity) TaskFullScreen1.this).openPostPage(post, false);
                                }
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });
                    } else if (url.contains("data-taskactionlink-type=view_quiz")) {
                        PostStore.getPostById(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Post post = PostStore.getData(response);

                                if (post != null) {
                                    ((NavigationActivity) TaskFullScreen1.this).openPostPage(post, false);
                                }
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });
                    }else if (url.contains("data-taskactionlink-type=view_classcast")) {
                        PostStore.getPostById(dataId, new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Post post = PostStore.getData(response);

                                if (post != null) {
                                    ((NavigationActivity) TaskFullScreen1.this).openPostPage(post, false);
                                }
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });
                    }


                    else {
                        Toast.makeText(TaskFullScreen1.this, "This type of SmartLink is not currently supported", Toast.LENGTH_LONG).show();
                    }

                    return true;
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    super.onLoadResource(view, url);


                }
            });

            txtContent.loadData(getHtmlString(this.getIntent().getCharSequenceExtra("content").toString()), "text/html; charset=UTF-8", null);
        }
        setActionBarAndTitle("Task");
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle("Task");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private CourseTaskLinkDetail parseLinkResponse(JSONObject response) {
        CourseTaskLinkDetail task = new CourseTaskLinkDetail();
        JSONObject jsonMain = null;
        try {
            jsonMain = response;
            if (jsonMain.getBoolean("result")) {
                JSONObject jsData = jsonMain.getJSONObject("data");
                task.setId(jsData.getString("id").toString().trim());
                task.setCourseId(jsData.getString("course_id").toString().trim());
                task.setText(jsData.getString("text").toString().trim());
                task.setTitle(jsData.getString("title").toString().trim());
                task.setcTime(jsData.getDouble("ctime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return task;
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

            CharSequence finalText = Html.fromHtml(taskContent, new URLImageParser(txtContent, this), null);

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

    public TaskFullScreen1 getNavigationActivity() {
        return (TaskFullScreen1) this;
    }

    /**
     * Uses {@link Downloader} to download something in a task link
     *
     * @param attachment an attachment to download
     */
    private void beginDownload(Attachment attachment) {
        String title = "CN File Download";
        Downloader.downloadAttachment(attachment, title, this);
    }

    private String getHtmlString(CharSequence formattedHtml) {
        String script = "<script type='text/javascript'>" +
                "window.onload = function() {" +
                "var anchors = document.getElementsByTagName('a');" +
                "for (var i = 0; i < anchors.length ; i++) {" +
                "anchors[i].addEventListener('click',clicked,false);" +
                "}" +
                "function clicked(e) {" +
                "if (e.currentTarget.tagName.toLowerCase() == 'a') {" +
                "var elem = e.currentTarget;" +
                "e.preventDefault();" +
                "var attributes = {};" +
                "for (var i = 0; i < elem.attributes.length; i++) {" +
                "var name = elem.attributes.item(i).nodeName;" +
                "var value = elem.attributes.item(i).nodeValue;" +
                "attributes[ name ] = value;" +
                "}" +
                "var link;" +
                "if (attributes['data-type'] == 'taskactionlink') {" +
                "link = 'cnapp://?';" +
                "for (var key in attributes) {" +
                "link = link+key+'='+attributes[key]+'&';" +
                "}" +
                "link = link.replace('#', '');" +
                "} else {" +
                "link = elem.href;" +
                "}" +
                "window.location.href = link;" +
                "return false;" +
                "}" +
                "}" +
                "}" +
                "</script>";
        String htmlString = "<html><head><title></title></head><body style=\"background:transparent; margin:10px; \"><b></b><br /><br /> <b>" + formattedHtml + "</b><br /> " + script + "</body></html>";
        Log.e("test", htmlString);
        return htmlString;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (txtContent.canGoBack()) {
            txtContent.goBack();
        } else {
            finish();
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
    private static class LinkClickCallback extends CallbackManager.NetworkCallback<TaskFullScreen> {
        private InternalURLSpan span;

        public LinkClickCallback(InternalURLSpan span, CallbackManager<TaskFullScreen> manager) {
            super(manager);
            this.span = span;
        }

        @Override
        public void onResumeWithResponse(TaskFullScreen object) {
            if (wasSuccessful()) {
                object.onLinkClickSuccess(response);
            } else {
                AppSession.showLongToast("Content does not exist");
            }
        }

        @Override
        public void onResumeWithError(TaskFullScreen object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(TaskFullScreen object) {
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


    @Override
    public void onDestroy() {
        super.onDestroy();

        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

    }
}
