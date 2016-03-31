package com.thecn.app.adapters.NotificationAdapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.fragments.NotificationFragments.EmailNotificationFragment;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Adapter for showing email notifications.  Used with {@link com.thecn.app.fragments.NotificationFragments.EmailNotificationFragment}
 */
public class EmailNotificationAdapter extends BaseAdapter {

    //string for text in the notification
    private static final String COURSE = "course";
    private static final String CONEXUS = "conexus";

    private static final String CONTENT_HEAD = "has invited you to join the";

    private static final String CN_MEMBERS = "CN Members";

    private static final String CONTENT_HEAD_SELF_SENDER = "You have invited";
    private static final String CONTENT_MID_SELF_SENDER = "to join the";
    private static final String CONTENT_MID_SELF_SENDER_DELETED = "to join a";

    private static final String SUCCESS_ACCEPT_HEAD = "Accepted";
    private static final String SUCCESS_IGNORE_HEAD = "Ignored";
    private static final String SUCCESS_TAIL = "invite!";
    private static final String FAILURE_HEAD = "Unable to join";

    private static final String DELETED_CONTENT_HEAD = "has invited you to join a";
    private static final String DELETED_CONTENT_MID =  "but this";
    private static final String DELETED_CONTENT_TAIL = "has been deleted.";

    private static final String DATA_ERROR = "ERROR: Could not load data for this notification.";

    private CallbackManager<EmailNotificationFragment> manager;
    private LayoutInflater inflater;
    private ArrayList<Email> mEmails = new ArrayList<>();
    private Typeface mTypeface;

    private final int readColor; //color for read notifications
    private final int unreadColor; //color for unread notifications

    private final int iconDimen;

    static class ViewHolder {
        ImageView userAvatar;
        TextView nameText;
        TextView dateText;
        TextView contentText;
        RelativeLayout contentLayout;
        Button acceptButton, ignoreButton;
        LinearLayout buttonLayout;
        RelativeLayout acceptIgnoreLayout;
        TextView messageText;

        String emailID;

        /**
         * Enables or disables taps in view.
         * @param enabled whether to enable buttons
         */
        public void setButtonsEnabled(boolean enabled) {
            if (acceptButton != null) {
                acceptButton.setEnabled(enabled);
            }
            if (ignoreButton != null) {
                ignoreButton.setEnabled(enabled);
            }
            if (contentLayout != null) {
                contentLayout.setClickable(!enabled);
                //absorbs parent's clicks so that cannot go to email page when
                //clickable
            }
        }
    }

    /**
     * New instance.  Get layout inflater, colors, icon dimension, and typeface.
     * @param manager callback manger
     * @param context for setting up
     */
    public EmailNotificationAdapter(CallbackManager<EmailNotificationFragment> manager, Context context) {
        this.manager = manager;

        inflater = LayoutInflater.from(context);

        Resources r = context.getResources();
        readColor = r.getColor(R.color.white);
        unreadColor = r.getColor(R.color.unread_notification_color);
        iconDimen = (int) r.getDimension(R.dimen.user_icon_width);

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    /**
     * Get and cast activity from fragment object
     * @return cast activity
     */
    public NavigationActivity getNavigationActivity() {
        Fragment fragment = manager.getObject();
        if (fragment == null) return null;

        return (NavigationActivity) fragment.getActivity();
    }

    /**
     * Add all emails to adapter
     * @param emails list of emails to add
     */
    public void addAll(ArrayList<Email> emails) {
        mEmails.addAll(emails);
        notifyDataSetChanged();
    }

    /**
     * Clear adapter data
     */
    public void clear() {
        mEmails.clear();
        notifyDataSetChanged();
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
     * Set up view for email, using view holder if possible.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.email_list_item, parent, false);

            holder = new ViewHolder();

            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
            holder.dateText = (TextView) convertView.findViewById(R.id.date_text);
            holder.contentText = (TextView) convertView.findViewById(R.id.content_text);
            holder.contentText.setTypeface(mTypeface);
            holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.email_notification_parent_layout);
            holder.acceptIgnoreLayout = (RelativeLayout) convertView.findViewById(R.id.email_list_item_accept_reject);
            holder.buttonLayout = (LinearLayout) convertView.findViewById(R.id.accept_reject_button_layout);
            holder.acceptButton = (Button) convertView.findViewById(R.id.accept_button);
            holder.ignoreButton = (Button) convertView.findViewById(R.id.ignore_button);
            holder.messageText = (TextView) convertView.findViewById(R.id.accept_reject_message);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //get logged in user data if I'm the sender
        final Email email = getItem(position);
        holder.emailID = email.getId();
        final User sender;
        if (email.getSender() != null && email.getSender().isMe()) {
            sender = AppSession.getInstance().getUser();
        } else {
            sender = email.getSender();
        }

        //set background color
        if (email.isUnread()) {
            holder.contentLayout.setBackgroundColor(unreadColor);
        } else {
            holder.contentLayout.setBackgroundColor(readColor);
        }

        //open profile on avatar click
        if (sender != null) {
            holder.userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationActivity().openProfileByID(sender.getId());
                }
            });
        }

        holder.userAvatar.setImageResource(R.drawable.default_user_icon);

        //load user profile pic
        try {
            String avatarUrl = sender.getAvatar().getView_url();
            MyVolley.loadIndexedUserImage(avatarUrl, holder.userAvatar, position, iconDimen);
        } catch (NullPointerException e) {
            // no user or no avatar data
        }

        //set user name text
        try {
            holder.nameText.setText(email.getSender().getDisplayName());
        } catch (NullPointerException e) {
            holder.nameText.setText("");
        }

        //set notification date
        try {
            holder.dateText.setText(email.getDisplayTime());
        } catch (NullPointerException e) {
            holder.dateText.setText("");
        }

        //if not an invite to something, return now.
        if (!email.isInvite()) {
            holder.acceptIgnoreLayout.setVisibility(View.GONE);

            holder.contentText.setMaxLines(3);
            holder.contentText.setEllipsize(TextUtils.TruncateAt.END);
            holder.contentText.setText(Html.fromHtml(email.getContent()));

            return convertView;
        }

        //allow larger text space and show buttons for accepting/ignoring
        holder.contentText.setMaxLines(Integer.MAX_VALUE);
        holder.contentText.setEllipsize(null);
        holder.acceptIgnoreLayout.setVisibility(View.VISIBLE);

        //check for data error
        if (email.getId() == null) {
            holder.acceptIgnoreLayout.setVisibility(View.GONE);
            setContentString(DATA_ERROR, email, holder);
            return convertView;
        }

        //check for error
        if (!setInviteContent(email, holder)) {
            holder.acceptIgnoreLayout.setVisibility(View.GONE);
            return convertView;
        }

        if (email.getInviteState() == Email.INVITE_STATE_ACCEPTED) {
            //if accepted, show message instead of buttons
            holder.buttonLayout.setVisibility(View.GONE);
            holder.messageText.setText(getStatusString(email));
            return convertView;
        } else if (email.getInviteState() == Email.INVITE_STATE_IGNORED) {
            //if ignored, show message instead of buttons
            holder.buttonLayout.setVisibility(View.GONE);
            holder.messageText.setText(getStatusString(email));
            return convertView;
        }

        //if waiting for response, disable buttons
        holder.setButtonsEnabled(email.getInviteState() != Email.INVITE_STATE_WAITING);
        //remove message text
        holder.messageText.setText("");
        holder.acceptIgnoreLayout.setVisibility(View.VISIBLE);
        holder.buttonLayout.setVisibility(View.VISIBLE);

        if (email.getInviteType() == Email.INVITE_COURSE) {
            //take actions on click for Course model type
            final Course course = email.getCourse();

            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAcceptClick(course, email, holder);
                }
            });

            holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onIgnoreClick(email, holder);
                }
            });

        } else if (email.getInviteType() == Email.INVITE_CONEXUS) {
            //take actions on click for Conexus model type
            final Conexus conexus = email.getConexus();

            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAcceptClick(conexus, email, holder);
                }
            });

            holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onIgnoreClick(email, holder);
                }
            });
        }

        return convertView;

    }

    /**
     * Check verification.
     * Attempt to join course.
     * @param course course to join
     * @param email invitation email
     * @param holder view holder corresponding to email data
     */
    private void onAcceptClick(Course course, Email email, ViewHolder holder) {
        if (AppSession.checkVerification(getNavigationActivity())) {
            return;
        }

        String userID = AppSession.getInstance().getUser().getId();
        final String emailID = email.getId();

        //disable buttons, set state to "waiting for response"
        holder.setButtonsEnabled(false);
        email.setInviteState(Email.INVITE_STATE_WAITING);

        CourseStore.joinCourse(course.getId(), userID, emailID, new AcceptCallback(email, manager));
    }

    /**
     * Check verification.
     * Attempt to join conexus.
     * @param conexus conexus to join
     * @param email invitation email
     * @param holder view holder corresponding to email data
     */
    private void onAcceptClick(Conexus conexus, Email email, ViewHolder holder) {
        if (AppSession.checkVerification(getNavigationActivity())) {
            return;
        }

        String userID = AppSession.getInstance().getUser().getId();
        final String emailID = email.getId();

        //disable buttons, set state to "waiting for response"
        holder.setButtonsEnabled(false);
        email.setInviteState(Email.INVITE_STATE_WAITING);

        ConexusStore.joinConexus(conexus.getId(), userID, emailID, new AcceptCallback(email, manager));
    }

    /**
     * Actions to take when request to join course or conexus returns from server.
     */
    private static final class AcceptCallback extends CallbackManager.NetworkCallback<EmailNotificationFragment> {

        private Email email;

        public AcceptCallback(Email email, CallbackManager<EmailNotificationFragment> manager) {
            super(manager);
            this.email = email;
        }

        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                //set data to reflect change
                email.setInviteState(Email.INVITE_STATE_ACCEPTED);

                //send request to delete email.
                EmailStore.delete(email.getId(), new DeleteResponse(email));

                AppSession.showShortToast(getStatusString(email));
            } else {
                //show error to user
                AppSession.showLongToast(EmailNotificationAdapter.getFailureString(email));
                email.setInviteState(Email.INVITE_STATE_RECEIVED);
            }
        }

        @Override
        public void onImmediateError(VolleyError error) {
            //show error, reset state
            StoreUtil.showExceptionMessage(error);
            email.setInviteState(Email.INVITE_STATE_RECEIVED);
        }


        @Override
        public void onResumeBefore(EmailNotificationFragment object) {
            //update view on resume
            object.notifyDataSetChanged();
        }
    }

    /**
     * Check verification.
     * Send request to ignore invitation.
     * @param email invitation email
     * @param holder view holder corresponding to email data
     */
    private void onIgnoreClick(Email email, ViewHolder holder) {
        if (AppSession.checkVerification(getNavigationActivity())) {
            return;
        }

        email.setInviteState(Email.INVITE_STATE_IGNORED);

        //set state of view
        holder.buttonLayout.setVisibility(View.GONE);
        holder.messageText.setText(getStatusString(email));

        EmailStore.delete(email.getId(), new DeleteResponse(email));
    }

    /**
     * Sets the display content of an invitation email.
     * @param email invitation email
     * @param holder view holder corresponding to email data
     * @return true if there was no error
     */
    private boolean setInviteContent(Email email, ViewHolder holder) {

        String userName, type, name, id;
        boolean deleted;

        //determine if course or conexus invite
        if (email.getInviteType() == Email.INVITE_COURSE) {
            Course course = email.getCourse();
            type = COURSE;

            deleted = course == null;
            if (!deleted) {
                name = course.getName();
                id = course.getId();
            } else {
                name = id = null;
            }
        } else if (email.getInviteType() == Email.INVITE_CONEXUS) {
            Conexus conexus = email.getConexus();
            type = CONEXUS;

            deleted = conexus == null;
            if (!deleted) {
                name = conexus.getName();
                id = conexus.getId();
            } else {
                name = id = null;
            }
        } else {
            return false;
        }

        String content;

        if (email.isSender()) {
            userName = null;
            //add receiving user
            ArrayList<User> toUsers = email.getToUsers();
            if (toUsers != null) {
                User toUser = toUsers.get(0);
                if (toUser != null) {
                    userName = toUser.getDisplayName();
                }
            }
            if (userName == null) {
                //show generic username
                userName = CN_MEMBERS;
            }

            if (deleted) {
                //show deleted content message
                content = CONTENT_HEAD_SELF_SENDER + " " +
                        userName + " " +
                        CONTENT_MID_SELF_SENDER_DELETED + " " +
                        type + " " +
                        DELETED_CONTENT_MID + " " +
                        type + " " +
                        DELETED_CONTENT_TAIL;

                setContentString(content, email, holder);
                return false;
            }

            if (name == null || id == null) {
                setContentString(DATA_ERROR, email, holder);
                return false;
            }

            content = CONTENT_HEAD_SELF_SENDER + " " +
                    userName + " " +
                    CONTENT_MID_SELF_SENDER + " " +
                    type + " " +
                    name;

            setContentString(content, email, holder);
            return false;

        } else {
            //the sender is not me
            userName = email.getSender().getDisplayName();

            if (deleted) {
                //show deleted content
                content = userName + " " +
                        DELETED_CONTENT_HEAD + " " +
                        type + ", " +
                        DELETED_CONTENT_MID + " " +
                        type + " " +
                        DELETED_CONTENT_TAIL;

                setContentString(content, email, holder);
                return false;
            }

            if (name == null || id == null) {
                setContentString(DATA_ERROR, email, holder);
                return false;
            }

            content = userName + " " + CONTENT_HEAD + " " + type + " " + name;
            setContentString(content, email, holder);

            return true;
        }
    }

    /**
     * Sets content in the contentText text view of the view holder
     * @param contentString text to set
     * @param email email text pertains to
     * @param holder view holder that email pertains to
     */
    private void setContentString(String contentString, Email email, ViewHolder holder) {
        try {
            CharSequence content = contentString == null ?
                    Html.fromHtml(email.getContent()) : contentString;
            holder.contentText.setText(content);
        } catch (NullPointerException e) {
            holder.contentText.setText("");
        }
    }

    /**
     * Gets the string that corresponds to the invite type.
     * @param email the email to get the invite type of
     * @return string corresponding to invite type.
     */
    private static String getInviteTypeString(Email email) {
        int type = email.getInviteType();

        if (type == Email.INVITE_COURSE) {
            return COURSE;
        } else if (type == Email.INVITE_CONEXUS) {
            return CONEXUS;
        }

        return null;
    }

    /**
     * Get string representing status of email.
     * @param email invitation email to get status of
     * @return string representing status of email.
     */
    private static String getStatusString(Email email) {
        String type = EmailNotificationAdapter.getInviteTypeString(email);

        if (type == null) type = " ";
        else type = " " + type + " ";

        int state = email.getInviteState();
        String status;

        if (state == Email.INVITE_STATE_ACCEPTED) status = SUCCESS_ACCEPT_HEAD;
        else if (state == Email.INVITE_STATE_IGNORED) status = SUCCESS_IGNORE_HEAD;
        else return null;

        return status + type + SUCCESS_TAIL;
    }

    /**
     * Gets message that communicates failure to join a course or conexus.
     * @param email invitation email
     * @return failure message
     */
    private static String getFailureString(Email email) {
        return FAILURE_HEAD + " " + getInviteTypeString(email);
    }

    /**
     * Actions to perform when a request to delete an email returns.
     */
    private static class DeleteResponse implements ResponseCallback {
        private Email email;

        public DeleteResponse(Email email) {
            this.email = email;
        }

        @Override
        public void onResponse(JSONObject response) {
            email.setDeleted(true);
        }

        @Override
        public void onError(VolleyError error) {
            //don't badger user
        }
    }
}
