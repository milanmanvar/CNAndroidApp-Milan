package com.thecn.app.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.thecn.app.tools.text.TextUtil;

/**
 * Activity for showing a web view.
 */
public class WebViewActivity extends ActionBarActivity {

    public static final String TITLE_KEY = "title_extra";
    public static final String URL_KEY = "url_extra";
    private WebView mWebView;

    /**
     * Set action bar title, init web view, load url if savedInstanceState null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getIntent().getStringExtra(TITLE_KEY);
        if (!TextUtil.isNullOrEmpty(title)) {
            ActionBar bar = getSupportActionBar();
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(title);
        }

        mWebView = new WebView(this);

        //fixes bug in web view code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        } else {
            mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        }

        setContentView(mWebView);

        if (savedInstanceState == null) {

            String url = getIntent().getStringExtra(URL_KEY);
            Log.d("OBS", url);
            if (!TextUtil.isNullOrEmpty(url)) {
                mWebView.loadUrl(url);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }
}
