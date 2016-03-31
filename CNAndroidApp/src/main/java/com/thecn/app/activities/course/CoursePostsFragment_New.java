package com.thecn.app.activities.course;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.controllers.AnarBarController;
import com.thecn.app.tools.volley.MyVolley;

/**
 * Show posts pertaining to this Course
 */

public class CoursePostsFragment_New extends Fragment {
    public static final String TAG = CoursePostsFragment_New.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";
    private Course mCourse;
    private AnarBarController anarBarController;
    private View headerView;
    private ImageLoader imageLoader = MyVolley.getImageLoader();
    private LinearLayout l;
    /**
     * Used to update the logged in user's image if it is changed
     */
    private BroadcastReceiver mUserUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            anarBarController.setUserImages();

        }
    };

    /**
     * Must supply a course to put in arguments
     */
    public static CoursePostsFragment_New newInstance(Course mCourse) {
        CoursePostsFragment_New fragment = new CoursePostsFragment_New();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
//        ((CourseActivity_New)getActivity()).setActionBarAndTitle("Course");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post_list_new, null);
        setUpHeaderView(v);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );


        return v;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        if (mCourse.getTasks().size() > 0)
//                    ((ContentPageActivity_New)getActivity()).performClickForTask();
//                else
//                    ((ContentPageActivity_New)getActivity()).performClickForPost();

    }

    /**
     * Sets course picture, name, school, number, and sets up the anar bar.
     */
    private void setUpHeaderView(View v) {
        String avatarUrl = mCourse.getLogoURL() + ".w160.jpg";


        String courseName = mCourse.getName();
        courseName = courseName != null ? courseName : "(No name found)";
        TextView courseNameTxtView = (TextView) v.findViewById(R.id.course_name);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        courseNameTxtView.setTypeface(typeface);
        courseNameTxtView.setText(courseName);

        anarBarController = new AnarBarController(v.findViewById(R.id.anar_bar_parent),
                mCourse);
        addAnarBarCallbacks();


    }

    /**
     * add callbacks for when user clicks on something in the anar bar
     */
    private void addAnarBarCallbacks() {
        try {
            final User user = AppSession.getInstance().getUser();

            if (user != null) {
                //opens the logged in user's profile
                anarBarController.setOnUserClick(new AnarBarController.ImageCallback() {
                    @Override
                    public void onImageClick() {
                        ((NavigationActivity) getActivity())
                                .openProfileByID(user.getId());
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            final User topUser = anarBarController.getTopUserModel();

            if (topUser != null) {
                //opens the top scoring user's profile
                anarBarController.setOnTopUserClick(new AnarBarController.ImageCallback() {
                    @Override
                    public void onImageClick() {
                        ((NavigationActivity) getActivity())
                                .openProfileByID(topUser.getId());
                    }
                });
            }
        } catch (NullPointerException e) {
           e.printStackTrace();
        }
//        if (mCourse.getTasks().size() > 0)
//            ((ContentPageActivity_New) (getActivity())).performClickForTask();
//        else
//            ((ContentPageActivity_New) (getActivity())).performClickForPost();
    }


    /**
     * Unregister broadcast receiver to prevent DeadObjectExceptions
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUserUpdater);
    }

    /**
     * Get id for this fragment
     *
     * @return unique id for this fragment instance
     */
    @Override
    public String toString() {
        Course course = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
        return TAG + course.getId();
    }
}
