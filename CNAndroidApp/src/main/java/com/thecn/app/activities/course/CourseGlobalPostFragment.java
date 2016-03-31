package com.thecn.app.activities.course;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.fragments.BaseGlobalPostListFragment;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.course.CourseSchool;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.controllers.AnarBarController;
import com.thecn.app.tools.network.GobalPostLoader;
import com.thecn.app.tools.network.PostLoader;
import com.thecn.app.tools.volley.MyVolley;

/**
 * Show posts pertaining to this Course
 */

public class CourseGlobalPostFragment extends BaseGlobalPostListFragment {
    public static final String TAG = CourseGlobalPostFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";
    private static final String NEW_POSTS = "New Posts";
    private static final String NEW_REFLECTIONS = "New Reflections";
    private static final String MOST_LIKED = "Most Liked Posts";
    private static final String MOST_REFLECTED = "Most Reflected Posts";
    private static final String MOST_VISITED = "Most Visited Links";
    private static final String INSTRUCTOR = "Instructor Posts";
    private static final String HIGHLIGHT = "Highlighted Posts";
    private Course mCourse;
    private AnarBarController anarBarController;
    private View headerView;
    private ImageLoader imageLoader = MyVolley.getImageLoader();
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
    public static CourseGlobalPostFragment newInstance(Course mCourse) {
        CourseGlobalPostFragment fragment = new CourseGlobalPostFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Get method list for showing posts from this Course.  They can be
     * chosen by using the FilterActivity
     *
     * @return list of post methods that can be picked from
     */
    @Override
    public GobalPostLoader.MethodList getPostLoaderMethodList() {
        GobalPostLoader.MethodList list = new GobalPostLoader.MethodList();
        list.ensureCapacity(2);
        GobalPostLoader.Method baseMethod = new GobalPostLoader.Method(PostLoader.SOURCE_COURSE);
        baseMethod.id = ((Course) getActivity().getIntent().getSerializableExtra("course")).getId();

        GobalPostLoader.Method method;

        method = new GobalPostLoader.Method(baseMethod);
        method.name = NEW_POSTS;
        list.add(method);

        method = new GobalPostLoader.Method(baseMethod);
        method.name = NEW_REFLECTIONS;
        method.filterType = GobalPostLoader.FILTER_NEW_REFLECTIONS;
        list.add(method);

        method = new GobalPostLoader.Method(baseMethod);
        method.name = MOST_LIKED;
        method.filterType = GobalPostLoader.FILTER_MOST_LIKED;
        list.add(method);

        method = new GobalPostLoader.Method(baseMethod);
        method.name = MOST_REFLECTED;
        method.filterType = GobalPostLoader.FILTER_MOST_REFLECTED;
        list.add(method);

        method = new GobalPostLoader.Method(baseMethod);
        method.name = MOST_VISITED;
        method.filterType = GobalPostLoader.FILTER_MOST_VISITED;
        list.add(method);

        list.startSecondList();

        method = new GobalPostLoader.Method(PostLoader.SOURCE_INSTRUCTOR);
        method.id = baseMethod.id;
        method.name = INSTRUCTOR;
        list.add(method);

        method = new GobalPostLoader.Method(PostLoader.SOURCE_HIGHLIGHT_COURSE);
        method.id = baseMethod.id;
        method.name = HIGHLIGHT;
        list.add(method);

        list.setAllowContentTypeChange(true);
        list.setInterpretAsGlobalPosts(true);
        list.setShowGlobalPosts(true);
        list.setShowContentToggle(true);

        return list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
    }

    /**
     * Create header before returning the root view
     */
    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.course_header, null);
        return inflater.inflate(R.layout.fragment_post_list, null);
    }

    /**
     * Sets up listview, header, broadcastmanager, and post buttons (below header)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        getListView().addHeaderView(headerView, null, false);

        setUpHeaderView();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );

        addPostButton(R.id.post_button, R.id.header_post_button);
        getView().findViewById(R.id.post_button).setVisibility(View.GONE);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position--;
        super.onListItemClick(l, v, position, id);
    }

    /**
     * Sets course picture, name, school, number, and sets up the anar bar.
     */
    private void setUpHeaderView() {
        String avatarUrl = mCourse.getLogoURL() + ".w160.jpg";

        ImageView mImageView = (ImageView) headerView.findViewById(R.id.avatarImg);
        imageLoader.get(avatarUrl,
                ImageLoader.getImageListener(mImageView,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon));

        String courseName = mCourse.getName();
        courseName = courseName != null ? courseName : "(No name found)";
        TextView courseNameTxtView = (TextView) headerView.findViewById(R.id.course_name);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        courseNameTxtView.setTypeface(typeface);
        courseNameTxtView.setText(courseName);

        CourseSchool school = mCourse.getSchool();
        String schoolName = null;
        if (school != null) {
            schoolName = school.getName();
        }
        TextView courseSchoolTxtView = (TextView) headerView.findViewById(R.id.course_school);
        courseSchoolTxtView.setTypeface(typeface);
        courseSchoolTxtView.setText(schoolName);

        String courseNumber = mCourse.getCourseNumber();
        if (courseNumber == null) courseNumber = "";
        TextView courseIdTxtView = (TextView) headerView.findViewById(R.id.course_number);
        courseIdTxtView.setTypeface(typeface);
        courseIdTxtView.setText(courseNumber);


        anarBarController = new AnarBarController(headerView.findViewById(R.id.anar_bar_parent),
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
            //something wrong
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
            //something wrong
        }
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
