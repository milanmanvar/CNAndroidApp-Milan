package com.thecn.app.activities.composeemail;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.util.Rfc822Tokenizer;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.CNEmailRecipientAdapter;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.tools.network.GlobalGson;
import com.thecn.app.tools.volley.MyVolley;
import com.thecn.app.views.CNEmailRecipientEditTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Allows user to send emails to any CN member(s).  Does not currently support sending
 * to groups, such as Courses or Conexus.
 */

public class ComposeEmailActivity extends ActionBarActivity {

    private ProgressDialog mProgressDialog;
    private CNEmailRecipientEditTextView mRecipientView, mCCView;
    private Toast mToast;

    private SendEmailFragment mSendEmailFragment;
    private static final String mSendEmailFragmentTag = "send_email_fragment";

    public static final String RECIPIENT_TAG = "recipient_tag";

    /**
     * Sets up recipient, cc, subject, and message views.
     * Gets progress dialog ready to show when sending info to server.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_email);

        //contains list of users/email addresses to send the email to
        mRecipientView = (CNEmailRecipientEditTextView) findViewById(R.id.email_recipients);
        mRecipientView.setTokenizer(new Rfc822Tokenizer());
        CNEmailRecipientAdapter recipientAdapter = new CNEmailRecipientAdapter(this);
        mRecipientView.setAdapter(recipientAdapter);

        //contains list of users/email addresses to cc
        mCCView = (CNEmailRecipientEditTextView) findViewById(R.id.email_cc_recipients);
        mCCView.setTokenizer(new Rfc822Tokenizer());
        CNEmailRecipientAdapter ccAdapter = new CNEmailRecipientAdapter(this);
        mCCView.setAdapter(ccAdapter);

        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG); //reusing toast, did not forget to show
        mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

        //attempts to cancel request on back pressed...but does not always work
        mProgressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                //this does not always work...sometimes request already received
                MyVolley.cancelRequests(EmailStore.SEND_EMAIL_TAG);
                dismiss();
            }
        };
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Sending message...");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("New Email");

        if (savedInstanceState == null) {
            //add the fragment for network communication
            mSendEmailFragment = new SendEmailFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(mSendEmailFragment, mSendEmailFragmentTag)
                    .commit();

            //adds a user if it was included in the intent
            User user = (User) getIntent().getSerializableExtra(RECIPIENT_TAG);
            if (user != null) {
                mRecipientView.addUser(user);
            }
        } else {
            mSendEmailFragment =
                    (SendEmailFragment) getSupportFragmentManager().findFragmentByTag(mSendEmailFragmentTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compose_email, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send_email) {
            //use the fragment to send data over network
            mSendEmailFragment.sendEmail();
            return true;
        } else if (id == android.R.id.home) {
            //return to previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Constructs an email from the fields.  Shows an error and returns null if something is wrong.
     * @return json representation of the email from the info in the fields
     */
    public JSONObject constructEmail() {

        ArrayList<Email.Address> recipients = new ArrayList<Email.Address>(mRecipientView.getRecipients());
        if (recipients.size() == 0) {
            showToast("Email must have recipients.");
            return null;
        }

        EditText subjectText = (EditText) findViewById(R.id.subject);
        Editable subject = subjectText.getText();
        if (subject == null || subject.length() == 0) {
            showToast("Subject cannot be blank.");
            return null;
        }

        EditText contentText = (EditText) findViewById(R.id.content);
        Editable content = contentText.getText();
        if (content == null || content.length() == 0) {
            showToast("Email content cannot be blank.");
            return null;
        }

        ArrayList<Email.Address> ccRecipients = mCCView.getRecipients();
        for (Email.Address address : ccRecipients) {
            address.setReceiveType("cc");
        }
        recipients.addAll(ccRecipients);

        HashMap<String, Object> email = new HashMap<String, Object>();
        email.put("receivers", recipients);
        email.put("sender", getSender());
        email.put("subject", subject.toString());
        email.put("content", content.toString());
        email.put("type", "normal");

        try {
            Gson gson = GlobalGson.getGson();
            return new JSONObject(gson.toJson(email));
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Gets the logged in user
     * @return logged in user info in an Address object
     */
    private Email.Address getSender() {
        User me = AppSession.getInstance().getUser();

        return new Email.Address(me.getId(), "user");
    }

    /**
     * Shows or dismisses the progress dialog
     * @param show whether to show the progress dialog
     */
    public void showProgressDialog(boolean show) {
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Shows toast to user
     * @param text message to show to user
     */
    private void showToast(String text) {
        mToast.setText(text);
        mToast.show();
    }

    /**
     * Save whether the progress dialog was showing
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("in_progress", mProgressDialog.isShowing());
        mProgressDialog.dismiss();
        super.onSaveInstanceState(outState);
    }

    /**
     * Restore the progress dialog if it was showing
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("in_progress")) {
            showProgressDialog(true);
        }
    }
}