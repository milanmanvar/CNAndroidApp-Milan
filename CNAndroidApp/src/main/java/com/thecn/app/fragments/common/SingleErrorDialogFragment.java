package com.thecn.app.fragments.common;

import android.os.Bundle;

/**
 * Shows a single error to the user in a dialog.
 */
public class SingleErrorDialogFragment extends ErrorDialogFragment {

    public static final String TAG = "error_dialog";
    private static final String KEY = "error";
    private static final String TITLE = "Error";

    private String mError;

    /**
     * New instance with arguments
     * @param error error message
     * @return new instance of this class
     */
    public static SingleErrorDialogFragment getInstance(String error) {
        Bundle args = new Bundle();
        args.putString(KEY, error);

        SingleErrorDialogFragment fragment = new SingleErrorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Init {@link #mError}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mError = getArguments().getString(KEY);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public String getMessage() {
        return mError;
    }
}
