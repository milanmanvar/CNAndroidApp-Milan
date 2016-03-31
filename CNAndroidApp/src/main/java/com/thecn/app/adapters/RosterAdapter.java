package com.thecn.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Adapter for showing list of users.
 * todo can be replaced by {@link com.thecn.app.activities.profile.FollowAdapter} ?
 */
public class RosterAdapter extends BaseAdapter {

    private ArrayList<User> mUsers = new ArrayList<>();

    private Typeface mTypeface;

    private LayoutInflater mInflater;

    private int iconDimen;
    private int flagWidth, flagHeight;

    /**
     * New instance.  Get view inflater, typeface, and image dimensions
     *
     * @param activity for resources
     */
    public RosterAdapter(Activity activity) {
        Context context = activity.getApplicationContext();

        mInflater = LayoutInflater.from(context);
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

        Resources r = activity.getResources();
        iconDimen = (int) r.getDimension(R.dimen.user_icon_width);
        DisplayMetrics dm = r.getDisplayMetrics();
        flagWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, dm);
        flagHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22f, dm);
    }

    /**
     * Remove all items from list, update view
     */
    public void clear() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    /**
     * Add all items to list, update view
     *
     * @param users items to add
     */
    public void addAll(ArrayList<User> users) {
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public User getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Set user name, flag, and profile picture.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final User user;
//        if (getItem(position).isMe()) {
//            user = AppSession.getInstance().getUser();
//            Log.e("Roster scroe is me:", "total:" + user.getScore().getTotal() + ":Seeds:" + user.getScore().getTotalSeeds());
//        } else {
//            user = getItem(position);
//            Log.e("Roster scroe:", "total:" + user.getScore().getTotal() + ":Seeds:" + user.getScore().getTotalSeeds());
//        }
        user = getItem(position);
        ViewHolder holder;

        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.roster_list_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.userFlag = (ImageView) convertView.findViewById(R.id.user_flag);
            holder.userName = (TextView) convertView.findViewById(R.id.content_text);
            holder.txtAnarCount = (TextView) convertView.findViewById(R.id.anar_count);
            holder.userName.setTypeface(mTypeface);
            holder.txtAnarCount.setTypeface(mTypeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        try {
            String avatarUrl = user.getAvatar().getView_url() + ".w160.jpg";
            MyVolley.loadIndexedUserImage(avatarUrl, holder.userAvatar, position, iconDimen);
        } catch (Exception e) {
            //whoops
            e.printStackTrace();
        }

        try {

            holder.userName.setText(user.getDisplayName());
            holder.txtAnarCount.setText(String.valueOf(user.getScore().getSubtotal()));
        } catch (Exception e) {
            //whoops
            e.printStackTrace();
        }

        try {
            MyVolley.ImageParams params = new MyVolley.ImageParams(
                    user.getCountry().getFlagURL(),
                    holder.userFlag
            );
            params.placeHolderID = params.errorImageResourceID = R.color.white;
            params.index = position;
            params.maxWidth = flagWidth;
            params.maxHeight = flagHeight;

            MyVolley.loadImage(params);

        } catch (NullPointerException e) {
            // no country flag
            e.printStackTrace();
        }

        return convertView;

    }

    /**
     * Keep references to views
     */
    static class ViewHolder {
        ImageView userAvatar, userFlag;
        TextView userName, txtAnarCount;
    }
}
