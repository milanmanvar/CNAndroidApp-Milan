package com.thecn.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.tools.network.Downloader;
import com.thecn.app.tools.text.TextUtil;

import java.util.ArrayList;

/**
 * Adapter for showing attachments to content (Post, poll, etc.)
 */
public class AttachmentAdapter extends BaseAdapter {
    private ArrayList<Attachment> mAttachments = new ArrayList<Attachment>();
    private int padding; //padding for text view
    private Context mContext;

    /**
     * Net instance
     * @param activity for resources
     * @param attachments list of attachments
     */
    public AttachmentAdapter(Activity activity, ArrayList<Attachment> attachments) {
        mContext = activity.getApplicationContext(); //Alex Lockwood's idea (Google engineer)

        mAttachments = attachments;
        padding = (int) mContext.getResources().getDimension(R.dimen.dialog_padding);
    }

    /**
     * Clears all items from array list, updates view
     */
    public void clear() {
        mAttachments.clear();
        notifyDataSetChanged();
    }

    /**
     * Add all items to list, update view
     * @param attachments attachments to add
     */
    public void addAll(ArrayList<Attachment> attachments) {
        mAttachments.addAll(attachments);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAttachments.size();
    }

    @Override
    public Attachment getItem(int position) {
        return mAttachments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Set up text view to display attachment, on click listener to start download.
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

        final Attachment attachment = getItem(position);
        if (attachment != null && attachment.getId() != null) {
            final String title = "CN File Download";

            String name = attachment.getNameWithExtension();
            SpannableString str =
                    TextUtil.getLinkStyleSpannableString(name, mContext);
            textView.setText(str);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Downloader.downloadAttachment(attachment, title, mContext);
                }
            });
        } else {
            textView.setText("(could not get attachment ID)");
            textView.setOnClickListener(null);
        }

        return textView;
    }
}
