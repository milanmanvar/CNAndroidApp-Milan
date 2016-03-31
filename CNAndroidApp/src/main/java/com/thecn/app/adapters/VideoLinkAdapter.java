package com.thecn.app.adapters;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;

import java.util.ArrayList;

/**
 * Adapter for showing links to videos the user has added.  This is used in {@link com.thecn.app.activities.createpost.CreatePostActivity}
 */
public class VideoLinkAdapter extends BaseAdapter {
    public static final String TAG = VideoLinkAdapter.class.getSimpleName();

    private ArrayList<String> mLinks;
    private Button removeAllVideosButton;
    private EditText linkEditText;
    private Fragment mFragment;

    private static final String linkRegex = "(https?://)?(www\\.)?" +
            "(youtube(-nocookie)?\\.com/(((e(mbed)?|v|user)/.*?)|((watch)?(\\?feature=player_embedded)?[\\?&]v=))" +
            "|(youtu\\.be/))" +
            "[A-Za-z0-9-_]{11}.*";

    /**
     * New instance
     * @param fragment fragment to keep for resources, etc.
     */
    public VideoLinkAdapter(Fragment fragment) {
        super();
        mFragment = fragment;
        mLinks = new ArrayList<>();
    }

    /**
     * Keep references to views.
     */
    static class ViewHolder {
        TextView linkText;
        ImageButton removeButton;
    }

    @Override
    public String getItem(int position) {
        return mLinks.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    /**
     * Get all link string data.
     * @return array of strings
     */
    public String[] getAllItems() {
        String[] links = new String[mLinks.size()];
        return mLinks.toArray(links);
    }

    @Override
    public int getCount() {
        return mLinks.size();
    }

    /**
     * Add a link.  Check to make sure link is valid youtube link.
     * Enable remove all links button
     * @param link link to add
     */
    public void add(String link) {
        if (link.matches(linkRegex)) {
            mLinks.add(link);
            setButtonEnabled();
            notifyDataSetChanged();
            linkEditText.setText("");
        }
        else
            AppSession.showLongToast("Not a valid YouTube link.");
    }

    /**
     * Remove link at position.
     * Set state of remove all links button
     * @param position position of link to remove
     */
    public void remove(int position) {
        mLinks.remove(position);
        setButtonEnabled();
        notifyDataSetChanged();
    }

    /**
     * Remove all items in list, set state of remove all links button,
     * update view.
     */
    public void removeAll() {
        mLinks.clear();
        setButtonEnabled();
        notifyDataSetChanged();
    }

    /**
     * Set the view used to remove all links when clicked.
     * @param button button to remove all links
     */
    public void setRemoveAllVideosButton(Button button) {
        removeAllVideosButton = button;
    }

    /**
     * Edit text view used to enter new links.
     * @param text edit text used to add links.
     */
    public void setLinkEditText(EditText text) {
        linkEditText = text;
    }

    /**
     * Set text view to show link address.  Set remove button to remove link when tapped.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = mFragment.getActivity().getLayoutInflater();

            convertView = inflater.inflate(R.layout.youtube_link_view, parent, false);

            holder = new ViewHolder();
            holder.linkText = (TextView) convertView.findViewById(R.id.link_text);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.remove_link);

            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();

        holder.linkText.setText(mLinks.get(position));

        final int pos = position;
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(pos);
            }
        });

        return convertView;
    }

    public void setButtonEnabled() {
        removeAllVideosButton.setEnabled(getCount() > 0);
    }
}
