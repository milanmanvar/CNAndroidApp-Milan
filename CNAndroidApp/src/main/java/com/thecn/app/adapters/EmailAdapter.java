package com.thecn.app.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.activities.email.EmailFragment;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.text.CNNumberLinker;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Used in {@link com.thecn.app.activities.email.EmailActivity} to show list of sub emails
 * of a parent email.
 */
public class EmailAdapter extends BaseAdapter {

    private ArrayList<Email> mEmails;
    private Typeface mTypeface;
    private LayoutInflater inflater;

    private CallbackManager<EmailFragment> callbackManager;

    private CNNumberLinker cnNumberLinker;

    private int iconDimen;

    /**
     * Hold references to views
     */
    static class ViewHolder {
        ImageView userAvatar;
        TextView nameText;
        TextView dateText;
        TextView contentText;
    }

    /**
     * New instance.  Init {@link com.thecn.app.tools.text.CNNumberLinker} and resources
     * @param context for resources
     * @param manager for network callbacks
     * @param emails data
     */
    public EmailAdapter(Context context, CallbackManager<EmailFragment> manager, ArrayList<Email> emails) {
        mEmails = emails;
        cnNumberLinker = new CNNumberLinker();
        cnNumberLinker.setCallbackManager(manager);

        callbackManager = manager;

        inflater = LayoutInflater.from(context);
        iconDimen = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40f, context.getResources().getDisplayMetrics()
        );
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @Override
    public int getCount() {
        return mEmails.size();
    }

    @Override
    public Email getItem(int position) {
        return mEmails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Set view to show information about email in list.  Shows profile picture of user.
     * When user clicked, opens profile page.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (null == convertView) {

            convertView = inflater.inflate(R.layout.email_reply_full, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
            holder.dateText = (TextView) convertView.findViewById(R.id.date_text);
            holder.contentText = (TextView) convertView.findViewById(R.id.email_content_text);
            holder.contentText.setMovementMethod(LinkMovementMethod.getInstance());
            holder.contentText.setTypeface(mTypeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Email email = getItem(position);
        final User sender;
        if (email.getSender().isMe()) {
            sender = AppSession.getInstance().getUser();
        } else {
            sender = email.getSender();
        }

        try {
            String avatarUrl = sender.getAvatar().getView_url();
            MyVolley.loadIndexedUserImage(avatarUrl, holder.userAvatar, position, iconDimen);
        } catch (NullPointerException e) {
            // no user or no avatar data
        }

        if (sender != null) {
            holder.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavigationActivity activity =
                            (NavigationActivity) callbackManager.getObject().getActivity();
                    activity.openProfileByID(sender.getId());
                }
            });
        }

        try {
            holder.nameText.setText(sender.getDisplayName());
        } catch (NullPointerException e) {
            holder.nameText.setText("");
        }

        try {
            holder.dateText.setText(email.getDisplayTime());
        } catch (NullPointerException e) {
            holder.dateText.setText("");
        }

        try {
            CharSequence content = cnNumberLinker.linkify(email.getContent());
            holder.contentText.setText(content);
        } catch (NullPointerException e) {
            holder.contentText.setText("");
        }

        return convertView;

    }

    /**
     * Change array list used for email data.
     * @param newEmails new list
     */
    public void changeDataSource(ArrayList<Email> newEmails) {
        mEmails = newEmails;
        notifyDataSetChanged();
    }
}
