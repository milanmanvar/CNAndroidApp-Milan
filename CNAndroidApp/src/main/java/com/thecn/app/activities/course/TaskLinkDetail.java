package com.thecn.app.activities.course;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.course.CourseTaskLinkDetail;
import com.thecn.app.stores.BaseStore;

/**
 * Created by utsav.k on 03-02-2016.
 */
public class TaskLinkDetail extends NavigationActivity {
    WebView txtContent;
    private CourseTaskLinkDetail taskLinkDetail;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_link_detail);
        txtContent = (WebView) findViewById(R.id.task_content);
        txtTitle = (TextView) findViewById(R.id.title);
        txtContent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        txtContent.getSettings().setLoadWithOverviewMode(true);
        txtContent.getSettings().setUseWideViewPort(true);
        txtContent.getSettings().setBuiltInZoomControls(true);
        txtContent.getSettings().setDisplayZoomControls(false);
        txtContent.getSettings().setJavaScriptEnabled(true);
        txtContent.getSettings().setDomStorageEnabled(true);
        txtContent.getSettings().setLoadsImagesAutomatically(true);
        setActionBarAndTitle("Task");
        if (this.getIntent().hasExtra("data")) {
            taskLinkDetail = (CourseTaskLinkDetail) this.getIntent().getSerializableExtra("data");
            txtTitle.setText(taskLinkDetail.getTitle());
            if (BaseStore.isOnline(this)) {
                txtContent.loadData(getHtmlString(taskLinkDetail.getText()), "text/html; charset=UTF-8", null);
            } else {
                AppSession.showLongToast("No internet connection.  Try again later.");
            }
        }

    }

    private String getHtmlString(String formattedHtml) {
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
        String htmlString = "<html><head><title></title></head><body style=\"background:transparent; margin:10px; font-size:20px; \"><b></b><br /><br /> <b>" + formattedHtml + "</b><br /> " + script + "</body></html>";
        Log.e("test", htmlString);
        return htmlString;
    }
}
