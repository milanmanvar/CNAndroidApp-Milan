package com.thecn.app.activities.email;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.adapters.EmailAdapter;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.text.CNNumberLinker;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.network.GlobalGson;
import com.thecn.app.tools.text.InternalURLSpan;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class EmailFragment extends ListFragment {

    public static final String TAG = EmailFragment.class.getSimpleName();

    private EmailAdapter adapter;
    private ArrayList<Email> mEmails;
    //contains to, from, cc info, etc
    private View mHeaderView;

    private User mMe;

    //the root email of this thread
    private Email mParentEmail;

    private EditText mContentText;
    private Button mSubmitButton;

    private CallbackManager<EmailFragment> callbackManager;

    private static final String EMAIL_KEY = "email";

    public static EmailFragment newInstance(Email parentEmail) {
        EmailFragment fragment = new EmailFragment();
        Bundle args = new Bundle();
        args.putSerializable(EMAIL_KEY, parentEmail);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Used to update logged in user's data.
     */
    private BroadcastReceiver mUserUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            if (mHeaderView != null) {
                setUpHeader();
            }
        }
    };

    /**
     * Get the email object, create the list of sub emails, and
     * register broadcast receiver.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mParentEmail = (Email) getArguments().getSerializable(EMAIL_KEY);

        mEmails = mParentEmail.getSubEmails();
        if (mEmails != null) {
            //display emails from oldest to newest (top to bottom)
            Collections.reverse(mEmails);
        } else {
            mEmails = new ArrayList<>();
        }

        mMe = AppSession.getInstance().getUser();

        callbackManager = new CallbackManager<>();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email, container, false);
    }

    /**
     * Get View references.  Set up the header, ListView, and its adapter.
     * Set the on click listener for the submit new email button.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHeaderView = getLayoutInflater(savedInstanceState)
                .inflate(R.layout.email_header, null);
        setUpHeader();

        setListAdapter(null);

        ListView listView = getListView();
        listView.setDivider(null);
        listView.addHeaderView(mHeaderView);
        listView.setBackgroundColor(getResources().getColor(R.color.background_color));

        mContentText = (EditText) view.findViewById(R.id.message_text);

        adapter = new EmailAdapter(getActivity(), callbackManager, mEmails);
        setListAdapter(adapter);

        mSubmitButton = (Button) view.findViewById(R.id.submit_email);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        //can't submit emails without verification
        if (AppSession.needsVerification()) {
            setSubmitLayoutVisibility(View.GONE);
        }
    }

    /**
     * Don't show the submit email view if user is not verified.
     */
    @Override
    public void onResume() {
        super.onResume();
        int visibility = AppSession.needsVerification() ? View.GONE : View.VISIBLE;
        setSubmitLayoutVisibility(visibility);
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Sets the visibility of the submit layout
     * @param visibility either {@link android.view.View#VISIBLE} or {@link android.view.View#GONE}
     */
    private void setSubmitLayoutVisibility(int visibility) {
        View view = getView();
        if (view == null) return;
        view = view.findViewById(R.id.submit_layout);
        if (view == null) return;
        view.setVisibility(visibility);
    }

    /**
     * Send this reply email to the server.
     * Uses {@link com.thecn.app.stores.EmailStore#sendEmail(org.json.JSONObject, String, com.thecn.app.stores.ResponseCallback)}
     */
    private void sendEmail() {
        JSONObject email = constructEmail();

        if (email != null) {

            mSubmitButton.setEnabled(false);

            EmailStore.sendEmail(email, "?return_detail=1", new SendEmailCallback(callbackManager));
        }

    }

    /**
     * Used when a request to submit an email reply returns.
     */
    private static class SendEmailCallback extends CallbackManager.NetworkCallback<EmailFragment> {
        public SendEmailCallback(CallbackManager<EmailFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeBefore(EmailFragment object) {
            object.onResponse();
        }

        @Override
        public void onResumeWithResponse(EmailFragment object) {
            if (wasSuccessful()) {
                AppSession.showLongToast("Message sent.");
                object.onSuccess(response);
            } else {
                AppSession.showLongToast("Could not send email.");
            }
        }

        @Override
        public void onResumeWithError(EmailFragment object) {
            StoreUtil.showExceptionMessage(error);
        }
    }

    /**
     * Wipe the submission text and enable the submission button again.
     */
    public void onResponse() {
        mContentText.setText(null);
        mSubmitButton.setEnabled(true);
    }

    //change data source to new email list

    /**
     * Change the data source to a new reply email list.
     * @param response data from the server
     */
    public void onSuccess(JSONObject response) {
        try {
            Email responseEmail = EmailStore.fromJSON(response.getJSONObject("data"));
            if (responseEmail != null) {
                //add the new emails and change the data source
                responseEmail.setSender(mMe);
                mEmails.add(responseEmail);
                adapter.changeDataSource(mEmails);
                getListView().setSelection(adapter.getCount() - 1);
            }
        } catch (JSONException e) {
            //something went wrong
        }
    }

    //construct email object from message field to send to server

    /**
     * Constructs json representation of email object ot send to server.
     * @return json representation of an email object.
     */
    private JSONObject constructEmail() {

        //check for errors first
        String parentId = mParentEmail.getId();
        if (parentId == null) {
            showDataErrorMessage();
            return null;
        }

        String content;

        try {
            content = mContentText.getText().toString();

            if (content == null || content.length() == 0) {
                AppSession.showLongToast("Email content cannot be blank.");
                return null;
            }
        } catch (NullPointerException e) {
            return null;
        }

        ArrayList<Email.Address> receivers = getReceivers(mParentEmail);
        if (receivers == null || receivers.size() == 0) {
            showDataErrorMessage();
            return null;
        }

        Email.Address sender;

        try {
            sender = Email.getAddressFromUser(mMe);
        } catch (NullPointerException e) {
            return null;
        }

        //no data errors!

        HashMap<String, Object> email = new HashMap<>();
        email.put("parent_id", parentId);
        email.put("content", content);
        email.put("receivers", receivers);
        email.put("sender", sender);
        email.put("type", "normal");

        try {
            Gson gson = GlobalGson.getGson();
            return new JSONObject(gson.toJson(email));
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Show a general error message to the user.
     */
    private void showDataErrorMessage() {
        AppSession.showLongToast("Error sending message");
    }

    /**
     * Get an address from this user if it is not the logged in user.
     * @param user the user to check
     * @return an address or null if the user is the logged in user.
     */
    private Email.Address getAddressFromUserIfNotMe(User user) {
        try {
            if (mMe.getId().equals(user.getId())) {
                return null;
            }
            return Email.getAddressFromUser(user);

        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Uses {@link #getAddressFromUserIfNotMe(User user)} to get a list
     * of all addresses that are not the logged in user's.
     * @param users
     * @return
     */
    private ArrayList<Email.Address> getAddressesFromUsersThatAreNotMe(ArrayList<User> users) {
        if (users != null) {
            ArrayList<Email.Address> addresses = new ArrayList<>();

            for (User user : users) {
                Email.Address address = getAddressFromUserIfNotMe(user);

                if (address != null) addresses.add(address);
            }

            return addresses;
        }

        return null;
    }

    /**
     * Get a list of the recipients of this email.
     * @param parentEmail the email to get recipients from
     * @return a list of email addresses
     */
    private ArrayList<Email.Address> getReceivers(Email parentEmail) {

        ArrayList<Email.Address> receivers = new ArrayList<>();

        try {
            //users specified in the "To:" field
            ArrayList<Email.Address> toAddresses = getAddressesFromUsersThatAreNotMe(parentEmail.getToUsers());
            if (toAddresses != null && toAddresses.size() > 0) {
                receivers.addAll(toAddresses);
            }

            //users specified in the "CC: " field
            ArrayList<Email.Address> ccAddresses = getAddressesFromUsersThatAreNotMe(parentEmail.getCCUsers());
            if (ccAddresses != null) {
                for (Email.Address address : ccAddresses) {
                    if (address != null) {
                        address.setReceiveType("cc");
                    }
                }
                receivers.addAll(ccAddresses);
            }

            //only email addresses, no User data
            ArrayList<Email.Address> nonMemberAddresses = parentEmail.getNonMemberRecipients();
            if (nonMemberAddresses != null && nonMemberAddresses.size() > 0) {
                receivers.addAll(nonMemberAddresses);
            }

            //don't check parent sender for whether the user is me
            Email.Address parentSender = Email.getAddressFromUser(parentEmail.getSender());
            receivers.add(parentSender);

        } catch (NullPointerException e) {
            return null;
        }

        return receivers;
    }

    /**
     * Don't do anything.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {}

    /**
     * Get references to views.
     * Set up the top part of the header.  Show string representation of the users
     * that this email thread is shared among.  Includes "to" and "cc".  Add links to
     * users if their data is available, otherwise they are simple email addresses.
     * Set up a view to show the root email information.
     */
    private void setUpHeader() {
        TextView holder;

        holder = (TextView) mHeaderView.findViewById(R.id.email_from_text);

        try {
            holder.setMovementMethod(LinkMovementMethod.getInstance());
            holder.setText(getUserSpan(mParentEmail.getSender()));
        } catch (NullPointerException e) {
            holder.setText("");
        }

        holder = (TextView) mHeaderView.findViewById(R.id.email_to_text);

        ArrayList<Email.Address> toEmailAddresses = new ArrayList<>();
        ArrayList<Email.Address> ccEmailAddresses = new ArrayList<>();
        //just email addresses
        ArrayList<Email.Address> nonMemberRecipients = mParentEmail.getNonMemberRecipients();
        //add email addresses to the appropriate list of emails: "cc" or "to"
        if (nonMemberRecipients != null && nonMemberRecipients.size() > 0) {
            for (Email.Address address : nonMemberRecipients) {
                if (address != null) {
                    String type = address.getReceiveType();

                    if (type != null && type.equals("cc")) {
                        ccEmailAddresses.add(address);
                    } else {
                        toEmailAddresses.add(address);
                    }
                }
            }
        }

        //add main participants in this email thread
        try {
            holder.setMovementMethod(LinkMovementMethod.getInstance());
            ArrayList<User> toUsers = mParentEmail.getToUsers();
            SpannableStringBuilder builder = new SpannableStringBuilder();
            if (toUsers != null && toUsers.size() > 0) {
                builder.append(getUserList(toUsers));
            }
            if (toEmailAddresses.size() > 0) {
                if (toUsers != null && toUsers.size() > 0) {
                    builder.append(", ");
                }
                builder.append(getEmailList(toEmailAddresses));
            }
            holder.setText(builder);
        } catch (NullPointerException e) {
            holder.setText("");
        }

        holder = (TextView) mHeaderView.findViewById(R.id.email_cc_text);

        //add cc'd participants in this email thread
        try {
            holder.setMovementMethod(LinkMovementMethod.getInstance());
            ArrayList<User> ccUsers = mParentEmail.getCCUsers();
            SpannableStringBuilder builder = new SpannableStringBuilder();
            if (ccUsers != null && ccUsers.size() > 0) {
                builder.append(getUserList(ccUsers));
            }
            if (ccEmailAddresses.size() > 0) {
                if (ccUsers != null && ccUsers.size() > 0) {
                    builder.append(", ");
                }
                builder.append(getEmailList(ccEmailAddresses));
            }

            if (builder.length() == 0) {
                throw new NullPointerException();
            }

            holder.setText(builder);
        } catch (NullPointerException e) {
            holder.setText("");
            mHeaderView.findViewById(R.id.cc_field).setVisibility(View.GONE);
        }

        //set up the view for the root (parent) email (which is part of the header of the list)
        ImageView userAvatar = (ImageView) mHeaderView.findViewById(R.id.user_avatar);
        TextView nameText = (TextView) mHeaderView.findViewById(R.id.name_text);
        TextView cnNumberText = (TextView) mHeaderView.findViewById(R.id.cn_number_text);
        TextView dateText = (TextView) mHeaderView.findViewById(R.id.date_text);
        TextView subjectText = (TextView) mHeaderView.findViewById(R.id.email_subject_text);
        TextView contentText = (TextView) mHeaderView.findViewById(R.id.email_content_text);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        contentText.setTypeface(typeface);

        //use logged in user object if sender is the logged in user.
        final User sender;
        if (mParentEmail.getSender().isMe()) {
            sender = AppSession.getInstance().getUser();
        } else {
            sender = mParentEmail.getSender();
        }

        //set the avatar
        try {
            String avatarUrl = sender.getAvatar().getView_url();
            int maxDimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65f, getResources().getDisplayMetrics());
            MyVolley.loadIndexedUserImage(avatarUrl, userAvatar, -1, maxDimen);

        } catch (NullPointerException e) {
            // no user or no avatar data
        }

        //set click listener to open root email sender's profile
        if (sender != null) {
            userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationActivity().openProfileByID(sender.getId());
                }
            });
        }

        //set user's name
        try {
            nameText.setText(mParentEmail.getSender().getDisplayName());
        } catch (NullPointerException e) {
            nameText.setText("");
        }

        //set user's cn number
        try {
            cnNumberText.setText(mParentEmail.getSender().getCNNumber());
        } catch (NullPointerException e) {
            cnNumberText.setText("");
        }

        //set the date of the email
        try {
            dateText.setText(mParentEmail.getDisplayTime());
        } catch (NullPointerException e) {
            dateText.setText("");
        }

        //set the subject
        try {
            subjectText.setText(mParentEmail.getSubject());
        } catch (NullPointerException e) {
            subjectText.setText("");
        }

        //linkify cn numbers in the body and set the email body.
        try {
            CNNumberLinker linker = new CNNumberLinker();
            linker.setCallbackManager(callbackManager);
            CharSequence content = linker.linkify(mParentEmail.getContent());
            contentText.setText(content);
        } catch (NullPointerException e) {
            contentText.setText("");
        }
    }

    private NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    /**
     * Lists users into a SpannableStringBuilder (linkifies the text).
     * Uses {@link #getUserSpan(com.thecn.app.models.user.User)} to get linkified user span.
     * @param users list of users to add to the builder.
     * @return builder that contains the linkified user text.
     */
    private SpannableStringBuilder getUserList(ArrayList<User> users) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (users.size() > 0) {
            builder.append(getUserSpan(users.get(0)));

            for (int i = 1; i < users.size(); i++) {
                builder.append(", ");
                builder.append(getUserSpan(users.get(i)));
            }
        }

        return builder;
    }

    /**
     * Gets a span for a user, linking it to their profile.
     * @param user the user to create the text from
     * @return linkified text rep. of the user
     */
    private SpannableString getUserSpan(final User user) {
        final NavigationActivity activity = (NavigationActivity) getActivity();

        String name = user.getDisplayName();
        final SpannableString span = new SpannableString(name);
        span.setSpan(new InternalURLSpan(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openProfileByID(user.getId());
            }
        }), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }

    /**
     * Get a string builder that contains email addresses as text.
     * @param addresses addresses to add to the text
     * @return a builder that holds the email address text.
     */
    private SpannableStringBuilder getEmailList(ArrayList<Email.Address> addresses) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (addresses.size() > 0) {
            String email = getEmail(addresses.get(0));
            if (email != null) builder.append(email);

            for (int i = 1; i < addresses.size(); i++) {
                email = getEmail(addresses.get(i));
                if (email != null) {
                    builder.append(", ");
                    builder.append(email);
                }
            }
        }

        return builder;
    }

    /**
     * Gets the email address string from an Address object.
     * Utility method.
     * @param address object from which to get the String.
     * @return the email address as text.
     */
    private String getEmail(Email.Address address) {
        if (address != null) {
            return address.getId();
        }

        return null;
    }

    /**
     * Unregister receiver
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUserUpdater);
    }
}
