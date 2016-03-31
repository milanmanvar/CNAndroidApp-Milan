package com.thecn.app.activities.verification;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.activities.WebViewActivity;
import com.thecn.app.tools.text.InternalURLSpan;

/**
 * Used to show check boxes for agreeing to the CN user agreement.  Shows a link to
 * the CN user agreement.
*/
public class TermsOfUseFragment extends BaseFragment {

    private static final String HEAD = "You must agree to the ";
    private static final String TERMS = "Terms of Use";
    private static final String TAIL = " before continuing.";
    private static final String TERMS_USE_URL = "https://www.thecn.com/api/html/terms_of_use.html";

    private static final String TITLE = "Terms of Use";

    private CheckBox mAgreeBox;
    private CheckBox mAgeBox;
    private Button mContinueButton;

    /**
     * Set action bar title
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBarActivity().getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Set up agreement link, continue button, and check boxes
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms_use, container, false);

        InternalURLSpan link = new InternalURLSpan(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.TITLE_KEY, TERMS);
                intent.putExtra(WebViewActivity.URL_KEY, TERMS_USE_URL);
                startActivity(intent);
            }
        });

        SpannableString string = new SpannableString(TERMS);
        string.setSpan(link, 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence message = TextUtils.concat(HEAD, string, TAIL);

        TextView messageText = (TextView) view.findViewById(R.id.message);
        messageText.setText(message);
        messageText.setMovementMethod(LinkMovementMethod.getInstance());

        mContinueButton = (Button) view.findViewById(R.id.continue_button);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActionBarActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CountriesFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mAgreeBox = (CheckBox) view.findViewById(R.id.agree);
        mAgeBox = (CheckBox) view.findViewById(R.id.age);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContinueEnabled();
            }
        };
        mAgreeBox.setOnClickListener(listener);
        mAgeBox.setOnClickListener(listener);

        return view;
    }

    /**
     * Calls {@link #setContinueEnabled()}
     */
    @Override
    public void onStart() {
        super.onStart();
        setContinueEnabled();
    }

    /**
     * Sets continue button enabled if both check boxes are checked.  Else disabled.
     */
    private void setContinueEnabled() {
        boolean enable = mAgreeBox.isChecked() && mAgeBox.isChecked();
        mContinueButton.setEnabled(enable);
    }
}
