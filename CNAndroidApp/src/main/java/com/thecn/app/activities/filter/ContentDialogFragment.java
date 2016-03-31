package com.thecn.app.activities.filter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.thecn.app.adapters.SimpleChoiceTextIconAdapter;
import com.thecn.app.models.util.TextEntry;
import com.thecn.app.models.util.TextIconEntry;
import com.thecn.app.tools.network.PostLoader;

import java.util.ArrayList;

/**
 * Shows a list of different content filters that can be applied
 * (All, Post, Poll, Quiz, Event, Classcast)
*/
public class ContentDialogFragment extends DialogFragment {
    public static final String TAG = "content_fragment";

    /**
     * Constructs instance of fragment with arguments
     * @param currentSelection currently selected content filter
     * @param contentChoice flags used to construct the choices in this dialog
     * @return new instance of this fragment
     */
    public static ContentDialogFragment getInstance(int currentSelection, int contentChoice) {
        Bundle args = new Bundle();
        args.putInt(FilterActivity.CONTENT_TYPE_KEY, currentSelection);
        args.putInt(FilterActivity.CONTENT_CHOICE_KEY, contentChoice);

        ContentDialogFragment f = new ContentDialogFragment();
        f.setArguments(args);
        return f;
    }

    /**
     * Constructs a ListView and adapter to show the content choices.
     * Uses {@link com.thecn.app.tools.network.PostLoader.MethodList#getContentChoiceList(int)}
     * @return a new dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int contentChoice = getArguments().getInt(FilterActivity.CONTENT_CHOICE_KEY);

        ArrayList<TextIconEntry> entries = PostLoader.MethodList.getContentChoiceList(contentChoice);

        final SimpleChoiceTextIconAdapter adapter = new SimpleChoiceTextIconAdapter(
                getActivity(),
                entries
        );

        ListView listView = new ListView(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dismiss();
                //set the content filter
                getFilterActivity().setContentFilter(
                        ((TextEntry) adapterView.getAdapter().getItem(i)).id
                );
            }
        });

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(listView)
                .create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    /**
     * Casts activity to filter activity
     * @return FilterActivity
     */
    public FilterActivity getFilterActivity() {
        return (FilterActivity) getActivity();
    }
}
