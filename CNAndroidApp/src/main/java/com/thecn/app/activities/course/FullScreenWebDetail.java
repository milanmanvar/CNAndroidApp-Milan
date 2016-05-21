package com.thecn.app.activities.course;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Post;
import com.thecn.app.tools.CallbackManager;

/**
 * Created by milanmanvar on 15/05/16.
 */
public class FullScreenWebDetail extends NavigationActivity{


    private WebView txtContent;

    private ProgressBar progressBar;
    private Post mPost; //(the post data)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_full);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        txtContent = (WebView) findViewById(R.id.task_content);
        progressBar = (ProgressBar) findViewById(R.id.progressLoding);

        txtContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);


        txtContent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        txtContent.getSettings().setBuiltInZoomControls(true);
        txtContent.getSettings().setDisplayZoomControls(false);
        txtContent.getSettings().setLoadsImagesAutomatically(true);


        txtContent.getSettings().setJavaScriptEnabled(true);
        txtContent.getSettings().setDomStorageEnabled(true);



        txtContent.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                txtContent.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);


            }
        });


        mPost = (Post) getIntent().getSerializableExtra("post");
        String url = "https://www.thecn.com/mini/content/view/"+mPost.getId();
        //String url = "https://www.google.com/";
        txtContent.loadUrl(url);


        setActionBarAndTitle("Post");


    }


}
