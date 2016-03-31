package com.thecn.app.adapters.NotificationAdapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.fragments.NotificationFragments.NotificationFragment;
import com.thecn.app.models.notification.Notification;
import com.thecn.app.models.notification.Notification.NotificationClickCallback;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Adapter for showing notifications that are not email notifications.  (General notifications and
 * new follower notifications)
 */
public class NotificationAdapter extends BaseAdapter {

    private ArrayList<Notification> mNotifications = new ArrayList<Notification>();

    private Typeface mTypeface;

    private final int readColor;
    private final int unreadColor;

    private final int iconDimen;

    private LayoutInflater inflater;

    private CallbackManager<NotificationFragment> callbackManager;

    static class ViewHolder {
        ImageView userAvatar;
        RelativeLayout contentLayout;
        TextView contentText;
    }

    /**
     * New instance
     * @param context for getting necessary resources
     * @param manager for handling network callbacks
     */
    public NotificationAdapter(Context context, CallbackManager<NotificationFragment> manager) {
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

        inflater = LayoutInflater.from(context);

        callbackManager = manager;

        Resources r = context.getResources();
        iconDimen = (int) r.getDimension(R.dimen.user_icon_width);
        readColor = r.getColor(R.color.white);
        unreadColor = r.getColor(R.color.unread_notification_color);
    }

    /**
     * Adds all notifications to internal array list.  Notifies view.
     * @param notifications data to add
     */
    public void addAll(ArrayList<Notification> notifications) {
        mNotifications.addAll(notifications);
        notifyDataSetChanged();
    }

    /**
     * Removes all notifications from array list.  Notifies view.
     */
    public void clear() {
        mNotifications.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mNotifications.size();
    }

    @Override
    public Notification getItem(int position) {
        return mNotifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Set up user picture, content layout, content text.  Set typeface.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (null == convertView) {

            convertView = inflater.inflate(R.layout.roster_list_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.contentText = (TextView) convertView.findViewById(R.id.content_text);
            holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.roster_parent_layout);
            holder.contentText.setTypeface(mTypeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Notification notification = getItem(position);
        final NotificationClickCallback callback = notification.getNewCallback(callbackManager);

        if (notification.getMark().equals("read")) {
            holder.contentLayout.setBackgroundColor(readColor);
        } else {
            holder.contentLayout.setBackgroundColor(unreadColor);
        }

        String avatarUrl = notification.getAvatarUrl();
        MyVolley.loadIndexedUserImage(avatarUrl, holder.userAvatar, position, iconDimen);

        holder.contentText.setText(notification.getNotificationDescription());

        holder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onUserClick();
            }
        });

        return convertView;
    }
}
