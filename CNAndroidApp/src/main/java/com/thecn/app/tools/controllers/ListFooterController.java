package com.thecn.app.tools.controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.thecn.app.R;

/**
 * Used to control a list footer view in a ListView.
 */
public class ListFooterController {

    private ListView mListView;
    private View mView;

    private View mLoadingView; //indeterminate progress bar
    private TextView mMessageView;

    private boolean isAdded = false;

    public static final String RETRY = "Error loading data.  Touch to retry.";
    public static final String NONE = "There is nothing here...";
    public static final String END = "End of list";

    /**
     * New instance of controller
     * @param listView listview to attach footer to
     * @param inflater used to inflate layouts
     */
    public ListFooterController(ListView listView, LayoutInflater inflater) {
        mListView = listView;

        mView = inflater.inflate(R.layout.loading_view, listView, false);
        mLoadingView = mView.findViewById(R.id.progressBar);
        mMessageView = (TextView) mView.findViewById(R.id.message);

        add();
    }

//    public ListFooterController(ExpandableListView listView, LayoutInflater inflater) {
//        mListView = listView;
//
//        mView = inflater.inflate(R.layout.loading_view, listView, false);
//        mLoadingView = mView.findViewById(R.id.progressBar);
//        mMessageView = (TextView) mView.findViewById(R.id.message);
//
//        add();
//    }

    /**
     * Gets the root view of the footer
     * @return root view of footer
     */
    public View getView() {
        return mView;
    }

    /**
     * Sets the view for loading state
     */
    public void setLoading() {
        mView.setVisibility(View.VISIBLE);

        mLoadingView.setVisibility(View.VISIBLE);
        mMessageView.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows message in footer
     */
    public void showMessage() {
        mView.setVisibility(View.VISIBLE);

        mLoadingView.setVisibility(View.INVISIBLE);
        mMessageView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows custom message in footer
     * @param message custom message
     */
    public void showMessage(String message) {
        mMessageView.setText(message);
        showMessage();
    }

    /**
     * Adds the footer to the list view, sets flag.
     */
    public void add() {
        if (isAdded || mListView == null) return;

        mListView.addFooterView(mView);
        isAdded = true;
    }

    /**
     * Removes footer from list view, sets flag
     */
    public void remove() {
        if (!isAdded || mListView == null) return;

        mListView.removeFooterView(mView);
        isAdded = false;
    }

    /**
     * Sets root footer view visibility to gone
     */
    public void clear() {
        mView.setVisibility(View.GONE);
    }
}
