package com.thecn.app.activities.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.tools.text.CNNumberLinker;
import com.thecn.app.tools.CallbackManager;

/**
 * Activity for viewing a user's introduction (view of it in the Profile Header is truncated).
 */
public class IntroductionActivity extends NavigationActivity {

    public static final String ARG_ID = IntroductionActivity.class.getName() + "id";
    public static final String ARG_CN_NUMBER = IntroductionActivity.class.getName() + "cn_number";
    public static final String ARG_CONTENT = IntroductionActivity.class.getName() + "content";

    /**
     * Get data and add fragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            String cnNumber = getIntent().getStringExtra(ARG_CN_NUMBER);
            String content = getIntent().getStringExtra(ARG_CONTENT);
            IntroductionFragment f = IntroductionFragment.getInstance(cnNumber, content);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f)
                    .commit();
        }
    }

    /**
     * Don't open duplicate profile page.
     * @param id id of user
     */
    @Override
    public void openProfileByID(String id) {
        String thisID = getIntent().getStringExtra(ARG_ID);
        if (!thisID.equals(id)) {
            super.openProfileByID(id);
        } else {
            //go back to profile activity
            finish();
        }
    }

    /**
     * Don't open duplicate profile page.
     * @param cnNumber cn number of user
     */
    @Override
    public void openProfileByCNNumber(String cnNumber) {
        String thisNumber = getIntent().getStringExtra(ARG_CN_NUMBER);
        if (!thisNumber.equals(cnNumber)) {
            super.openProfileByCNNumber(cnNumber);
        } else {
            //go back to profile activity
            finish();
        }
    }

    /**
     * Create custom animation
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
    * Displays the user's introduction.
    */
    public static class IntroductionFragment extends Fragment {
        public static final String TAG = IntroductionFragment.class.getName();

        private static final String INTRO = "'s Introduction";

        private CallbackManager<IntroductionFragment> callbackManager;

        /**
         * Get instance with arguments
         * @param cnNumber user's cn number
         * @param content introduction content
         * @return new instance of this class
         */
        public static IntroductionFragment getInstance(String cnNumber, String content) {
            Bundle args = new Bundle();
            args.putString(ARG_CN_NUMBER, cnNumber);
            args.putString(ARG_CONTENT, content);

            IntroductionFragment f = new IntroductionFragment();
            f.setArguments(args);
            return f;
        }

        /**
         * Set the title of the activity
         */
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String cnNumber = getArguments().getString(ARG_CN_NUMBER);
            getIntroActivity().setActionBarAndTitle(cnNumber + INTRO);
        }

        /**
         * Cast activity into IntroductionActivity
         */
        private IntroductionActivity getIntroActivity() {
            return (IntroductionActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            callbackManager = new CallbackManager<>();
        }

        /**
         * Dynamically create scroll view and text view and set up content.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setFillViewport(true);

            CNNumberLinker numberLinker = new CNNumberLinker();
            numberLinker.setCallbackManager(callbackManager);

            String string = getArguments().getString(ARG_CONTENT);
            CharSequence content = numberLinker.linkify(string);

            TextView textView = new TextView(getActivity());
            textView.setText(content);
            textView.setBackgroundResource(R.drawable.white_object);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            textView.setAutoLinkMask(Linkify.ALL);
            textView.setMovementMethod(LinkMovementMethod.getInstance());

            int m = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
            textView.setPadding(m, m, m, m);

            scrollView.addView(textView);

            return scrollView;
        }

        @Override
        public void onResume() {
            super.onResume();
            callbackManager.resume(this);
        }

        @Override
        public void onPause() {
            callbackManager.pause();
            super.onPause();
        }
    }
}
