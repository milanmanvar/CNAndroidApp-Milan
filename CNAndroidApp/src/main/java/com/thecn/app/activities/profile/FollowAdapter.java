package com.thecn.app.activities.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.broadcastreceivers.FollowChangeReceiver;
import com.thecn.app.models.profile.Avatar;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONObject;

import java.util.ArrayList;

/**
* Adapter for showing Follow users in a list.
*/
class FollowAdapter extends BaseAdapter {

    private static final String FOLLOWING = "Following";
    private static final String FOLLOW = "Follow";

    private ArrayList<User> users = new ArrayList<>();

    private CallbackManager<FollowActivity.FollowFragment> callbackManager;

    private int iconDimen;

    /**
     * Holds view objects for recycling
     */
    private static class ViewHolder {
        public ImageView imageView;
        public TextView userName, cnNumber;

        public View followButton;
        public ImageView followButtonImage;
        public TextView followButtonText;
    }

    /**
     * Clears data, notifies change
     */
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    /**
     * Adds users, notifies change
     * @param users users to add
     */
    public void addAll(ArrayList<User> users) {
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    /**
     * Gets users
     * @return list of users.
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * Set up instance.
     * Gets proper icon width for user icons.
     * @param manager callback manager for handling network requests
     */
    public FollowAdapter(CallbackManager<FollowActivity.FollowFragment> manager) {
        callbackManager = manager;
        iconDimen = (int) callbackManager.getActivity().getResources().getDimension(R.dimen.user_icon_width);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Set up the view and return it.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final User user = getItem(position);

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.follower_list_item, parent, false);

            holder = new ViewHolder();

            holder.imageView = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.cnNumber = (TextView) convertView.findViewById(R.id.headline);

            holder.followButton = convertView.findViewById(R.id.follow_button);
            holder.followButtonImage = (ImageView) holder.followButton.findViewById(R.id.follow_img);
            holder.followButtonText = (TextView) holder.followButton.findViewById(R.id.follow_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //open profile when the parent view is clicked
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowActivity a = callbackManager.getObject().getFollowActivity();
                a.openProfileByID(user.getId());
            }
        });

        String stringHolder;

        //load user image
        stringHolder = getAvatarURL(user);
        if (stringHolder != null) {
            MyVolley.loadIndexedUserImage(stringHolder, holder.imageView, position, iconDimen);
        }

        //set user's name
        stringHolder = user.getDisplayName();
        if (stringHolder != null) {
            holder.userName.setText(stringHolder);
        }

        //set user's cn number
        stringHolder = user.getCNNumber();
        if (stringHolder != null) {
            holder.cnNumber.setText(stringHolder);
        }

        if (!user.isMe()) {
            User.Relations relations = user.getRelations();

            if (relations != null) {
                //as long as user is not me, show follow button
                holder.followButton.setVisibility(View.VISIBLE);

                int bgResourceID, imgResourceID;
                String text;

                //set disabled if request pending
                if (relations.isPendingFollowing()) {
                    holder.followButton.setEnabled(false);
                } else {
                    holder.followButton.setEnabled(true);
                }

                //set green for following, blue for not following
                if (relations.isFollowing()) {
                    bgResourceID = R.drawable.standard_green_button;
                    imgResourceID = R.drawable.ic_accept;
                    text = FOLLOWING;
                } else {
                    bgResourceID = R.drawable.standard_blue_button;
                    imgResourceID = R.drawable.ic_plus;
                    text = FOLLOW;
                }

                holder.followButton.setBackgroundResource(bgResourceID);
                holder.followButtonImage.setImageResource(imgResourceID);
                holder.followButtonText.setText(text);
                holder.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FollowActivity a = callbackManager.getObject().getFollowActivity();
                        if (AppSession.checkVerification(a)) {
                            return;
                        }

                        User.Relations r = user.getRelations();
                        r.setPendingFollowing(true);

                        //update view
                        notifyDataSetChanged();

                        boolean following = r.isFollowing();
                        FollowCallback callback = new FollowCallback(user, !following, callbackManager);

                        //send request to server
                        if (following) {
                            UserStore.stopFollowingUser(user.getId(), callback);
                        } else {
                            UserStore.followUser(user.getId(), callback);
                        }
                    }
                });

            } else {
                //no data to work with
                holder.followButton.setVisibility(View.INVISIBLE);
            }
        } else {
            //user is me
            holder.followButton.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    /**
     * Used to make decisions when a request to follow or unfollow a user returns.
     */
    private static class FollowCallback extends CallbackManager.NetworkCallback<FollowActivity.FollowFragment> {

        private User user;
        //the new state
        private boolean state;

        public FollowCallback(User user, boolean state, CallbackManager<FollowActivity.FollowFragment> manager) {
            super(manager);
            this.user = user;
            this.state = state;
        }

        @Override
        public void onImmediateResponse(JSONObject response) {
            user.getRelations().setPendingFollowing(false);

            if (wasSuccessful()) {
                //broadcast change
                FollowChangeReceiver.sendFollowChangeBroadcast(
                        user.getId(),
                        state,
                        AppSession.getInstance().getApplicationContext()
                );
            }
        }

        @Override
        public void onImmediateError(VolleyError error) {
            user.getRelations().setPendingFollowing(false);
        }

        @Override
        public void onResumeAfter(FollowActivity.FollowFragment object) {
            if (!wasSuccessful()) {
                object.getFollowAdapter().notifyDataSetChanged();
            }
        }
    }

    /**
     * Method used for handling nullptr exceptions
     * @param user User to get data from
     * @return avatar url or null
     */
    private String getAvatarURL(User user) {
        if (user == null) return null;
        Avatar a = user.getAvatar();
        if (a == null) return null;
        return a.getView_url();
    }
}
