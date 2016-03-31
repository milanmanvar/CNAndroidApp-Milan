package com.thecn.app.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.content.PollItem;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Adapter for showing user's short answer responses in a list.
 */
public class PollSubmissionAdapter extends BaseAdapter {

    private ArrayList<PollItem.Submission> mSubmissions = new ArrayList<PollItem.Submission>();

    private LayoutInflater layoutInflater;

    private int iconDimen;
    private int blackID;

    /**
     * Used to specify proper get view method depending on {@link #mDisplayType}
     */
    private interface GetViewMethod {
        public View getView(int position, View convertView, ViewGroup parent);
    }

    private GetViewMethod mGetViewMethod;
    private PollItem.SubmissionDisplayType mDisplayType;

    /**
     * Create new instance
     * @param context for resources
     * @param displayType tells whether to show user information alongside their response.
     */
    public PollSubmissionAdapter(Context context, PollItem.SubmissionDisplayType displayType) {
        layoutInflater = LayoutInflater.from(context);
        iconDimen = (int) context.getResources().getDimension(R.dimen.user_icon_width);
        blackID = context.getResources().getColor(R.color.black);
        setDisplayType(displayType);
    }

    /**
     * Sets {@link #mDisplayType} and sets {@link #mGetViewMethod} to specify proper
     * method for getting item view.
     * @param displayType tells whether to show user information alongside their response.
     */
    public void setDisplayType(PollItem.SubmissionDisplayType displayType) {
        switch (displayType) {
            case USER_ANSWER:
                mGetViewMethod = new GetViewMethod() {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return getAllView(position, convertView, parent);
                    }
                };
                break;
            case ANSWER:
                mGetViewMethod = new GetViewMethod() {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return getAnswerOnlyView(position, convertView, parent);
                    }
                };
                break;
            case NOTHING:
                mGetViewMethod = new GetViewMethod() {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return convertView;
                    }
                };
        }

        mDisplayType = displayType;
    }

    /**
     * Remove all entries from list, update view.
     */
    public void clear() {
        mSubmissions.clear();
        notifyDataSetChanged();
    }

    /**
     * Add all entries to list, update view.
     * @param responses list entries
     */
    public void addAll(ArrayList<PollItem.Submission> responses) {
        mSubmissions.addAll(responses);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSubmissions.size();
    }

    @Override
    public PollItem.Submission getItem(int position) {
        return mSubmissions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * References to views.
     */
    static class ViewHolder {
        ImageView userAvatar;
        TextView userName, response;
    }

    /**
     * Calls assigned {@link #mGetViewMethod}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mGetViewMethod.getView(position, convertView, parent);
    }

    /**
     * Get a view that will display the user's response as well as info about the user.
     * Links user profile pic to their profile.
     */
    private View getAllView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.poll_response_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.userName.setTextColor(blackID);
            holder.response = (TextView) convertView.findViewById(R.id.response);
            holder.response.setTextColor(blackID);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        loadImage(holder.userAvatar, position);
        setUserName(holder.userName, position);

        setResponseText(holder.response, position);

        return convertView;
    }

    /**
     * Get a view that will only show user's response (no user info).
     */
    private View getAnswerOnlyView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.poll_response_item_no_user, parent, false);
        }

        TextView responseText = (TextView) convertView.findViewById(R.id.response);
        responseText.setTextColor(blackID);
        setResponseText(responseText, position);

        return convertView;
    }

    /**
     * Attempts to set text.  Catches null pointer exceptions
     * @param textView text view to set text of
     * @param position position of item to get text from.
     */
    private void setResponseText(TextView textView, int position) {
        try {
            textView.setText(getItem(position).getAnswers().get(0));
        } catch (Exception e) {
            //data not present
            textView.setText("(Could not get response)");
        }
    }

    /**
     * Attempts to set user name, handles nullptr
     * @param textView text view to set text of
     * @param position position of item to get text from.
     */
    private void setUserName(TextView textView, int position) {
        try {
            textView.setText(getSubmissionUser(position).getDisplayName());
        } catch (Exception e) {
            //data not present
            textView.setText("(Could not load user)");
        }
    }

    /**
     * Attempts to load user image, handles exceptions
     * @param imageView view to set image into
     * @param position position of item to get url from.
     */
    private void loadImage(ImageView imageView, int position) {
        try {
            String avatarUrl = getSubmissionUser(position).getAvatar().getView_url() + ".w160.jpg";
            MyVolley.loadIndexedUserImage(avatarUrl, imageView, position, iconDimen);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.default_user_icon);
        }
    }

    /**
     * Get user at this position.  If user is me,
     * get the AppSession user.
     * @param position location of user
     * @return user
     */
    private User getSubmissionUser(int position) {
        if (getItem(position).getUser().isMe()) {
            return AppSession.getInstance().getUser();
        } else {
            return getItem(position).getUser();
        }
    }
}
