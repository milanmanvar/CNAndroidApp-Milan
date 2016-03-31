package com.thecn.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.content.Link;
import com.thecn.app.tools.text.TextUtil;

import java.util.ArrayList;

/**
 * Used to show list of links the user can click on in order to open an external web app.
 */
public class LinkAdapter extends BaseAdapter {
    private ArrayList<Link> mLinks = new ArrayList<>();
    private int padding;
    private Context mContext;

    private OnLinkClickListener listener;

    /**
     * Custom on click listener class
     */
    public interface OnLinkClickListener {
        public void onLinkClick(String url);
    }

    /**
     * New instance
     * @param activity for app context
     * @param links data
     */
    public LinkAdapter(Activity activity, ArrayList<Link> links) {
        mContext = activity.getApplicationContext();
        mLinks = links;
        padding = (int) mContext.getResources().getDimension(R.dimen.dialog_padding);
    }

    /**
     * Remove all items from list, update view
     */
    public void clear() {
        mLinks.clear();
        notifyDataSetChanged();
    }

    /**
     * Add all items to list, update view
     * @param links data list
     */
    public void addAll(ArrayList<Link> links) {
        mLinks.addAll(links);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLinks.size();
    }

    @Override
    public Link getItem(int position) {
        return mLinks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Set click listener for when link clicked.
     * @param listener listener
     */
    public void setOnLinkClickListener(OnLinkClickListener listener) {
        this.listener = listener;
    }

    /**
     * Sets padding of textview, sets on click listener to call link click listener.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(mContext);
            textView.setPadding(padding, padding, padding, padding);
            textView.setBackgroundResource(R.drawable.link_selector);
        } else {
            textView = (TextView) convertView;
        }

        Link link = getItem(position);
        if (link != null && link.getViewUrl() != null && !link.getViewUrl().isEmpty()) {
            String text = link.getDisplayUrl();
            if (text == null || text.isEmpty()) text = link.getViewUrl();

            final String url = TextUtil.checkURL(link.getViewUrl());
            SpannableString str =
                    TextUtil.getLinkStyleSpannableString(text, mContext);
            textView.setText(str);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onLinkClick(url);
                }
            });
        } else {
            textView.setOnClickListener(null);
        }

        return textView;
    }
}
