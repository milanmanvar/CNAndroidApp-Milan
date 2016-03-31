package com.thecn.app.fragments.common;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Displays multiple errors to the user using an array list of strings.
 */
public class MultiErrorDialogFragment extends ErrorDialogFragment{

    public static final String TAG = "error_dialog";
    private static final String KEY = "errors";
    private static final String TITLE = "Errors";

    private ArrayList<String> mErrors;

    /**
     * New instance with arguments
     * @param errors array list of errors
     * @return new instance of this class
     */
    public static MultiErrorDialogFragment getInstance(ArrayList<String> errors) {
        Bundle args = new Bundle();
        args.putStringArrayList(KEY, errors);

        MultiErrorDialogFragment fragment = new MultiErrorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * init array list
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mErrors = getArguments().getStringArrayList(KEY);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Create content of error message by concatenating array list of errors,
     * separating with two carriage returns
     */
    @Override
    public String getMessage() {
        String message = "";

        if (mErrors != null && mErrors.size() > 0) {
            int lastPos = mErrors.size() - 1;
            for (int i = 0; i < lastPos; i++) {
                message += mErrors.get(i) + "\n\n";
            }

            message += mErrors.get(lastPos);
        }

        return message;
    }
}
