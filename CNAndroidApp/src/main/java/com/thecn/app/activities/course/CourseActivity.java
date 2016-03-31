package com.thecn.app.activities.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.AppSession;
import com.thecn.app.activities.ContentPageActivity;
import com.thecn.app.activities.createpost.CreatePostActivity;
import com.thecn.app.models.course.Course;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

/**
 * Activity analogous to Course page on the website.
 */
public class CourseActivity extends ContentPageActivity {

    private Course mCourse;
    private String courseID;

    //ids for the different fragments used
    public static final int ABOUT_FRAGMENT = 0;
    public static final int TASKS_FRAGMENT = 1;
    public static final int ROSTER_FRAGMENT = 2;

    private static final String mLoadCourseFragmentTag = "load_course";

    private DataGrabber mDataGrabber;

    /**
     * Begins loading course data if null savedInstanceState.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            String courseNumber;

            try {
                mCourse = (Course) getIntent().getSerializableExtra("course");
                courseID = mCourse.getId();
                courseNumber = mCourse.getCourseNumber();
                if (courseNumber == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            //begin loading data
            mDataGrabber = DataGrabber.getInstance(courseID);
            getSupportFragmentManager().beginTransaction()
                    .add(mDataGrabber, mLoadCourseFragmentTag)
                    .commit();

        } else {
            //get the fragment that was already created
            mDataGrabber =
                    (DataGrabber) getSupportFragmentManager().findFragmentByTag(mLoadCourseFragmentTag);

            if (!mDataGrabber.loading) {
                mCourse = (Course) savedInstanceState.getSerializable("course");
            }

            hideProgressBar();
        }

        setActionBarAndTitle("Course");
    }

    public DataGrabber getDataGrabber() {
        return mDataGrabber;
    }

    /**
     * Used to load data from network for this activity
     */
    public static class DataGrabber extends Fragment {

        public boolean loading = false;
        private CallbackManager<DataGrabber> manager;

        public static final String ID_KEY = "id_key";

        /**
         * Loads course by its id
         */
        public static DataGrabber getInstance(String courseID) {
            Bundle args = new Bundle();
            args.putString(ID_KEY, courseID);

            DataGrabber grabber = new DataGrabber();
            grabber.setArguments(args);
            return grabber;
        }

        /**
         * Begins loading data
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            manager = new CallbackManager<>();

            loading = true;
            String id = getArguments().getString(ID_KEY);
            CourseStore.getCourseById(id, new Callback(manager));
        }

        @Override
        public void onResume() {
            super.onResume();
            manager.resume(this);
        }

        @Override
        public void onPause() {
            manager.pause();
            super.onPause();
        }

        /**
         * Called when data returns from the server
         */
        private static class Callback extends CallbackManager.NetworkCallback<DataGrabber> {
            public Callback(CallbackManager<DataGrabber> grabber) {
                super(grabber);
            }

            @Override
            public void onResumeWithResponse(DataGrabber object) {
                Course course = CourseStore.getData(response);
                CourseActivity a = (CourseActivity) object.getActivity();

                if (course != null) {
                    a.onSuccess(course);
                } else {
                    a.onLoadingError();
                }
            }

            @Override
            public void onResumeWithError(DataGrabber object) {
                StoreUtil.showExceptionMessage(error);
                object.getActivity().finish();
            }
        }
    }

    /**
     * Initialize the rest of this activity (including view fragments)
     */
    public void onSuccess(Course course) {
        setCourse(course);
        hideProgressBar();
        initFragments(CourseActivity.TASKS_FRAGMENT);
        getDataGrabber().loading = false;
    }

    public void setCourse(Course course) {
        mCourse = course;
    }

    /**
     * Finish the activity on error
     */
    private void onLoadingError() {
        AppSession.showDataLoadError("course");
        finish();
    }

    /**
     * Add this course as a parameter to {@link com.thecn.app.activities.createpost.CreatePostActivity}
     */
    @Override
    public void pushCreatePostActivity() {
        final Intent intent = new Intent(this, CreatePostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("COURSE", mCourse);
        openPage(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("course", mCourse);
    }

    /**
     * Specify the course posts fragment as the static fragment
     * @return the static fragment
     */
    @Override
    protected FragmentPackage getStaticFragmentPackage() {
        String fragmentKey = "COURSE_" + courseID + "_POSTS";
        return new FragmentPackage("POSTS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CoursePostsFragment.newInstance(mCourse);
            }
        });
    }

    /**
     * Specify the course about, tasks, and roster fragments as the fragments
     * on the sliding panel.
     * @return list of sliding fragments
     */
    @Override
    protected FragmentPackage[] getFragmentPackages() {
        FragmentPackage[] packages = new FragmentPackage[3];
        String fragmentKey;

        fragmentKey = "COURSE_" + courseID + "_ABOUT";
        packages[ABOUT_FRAGMENT] = new FragmentPackage("ABOUT", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CourseAboutFragment.newInstance(mCourse);
            }
        });

        fragmentKey = "COURSE_" + courseID + "_TASKS";
                packages[TASKS_FRAGMENT] = new FragmentPackage("TASKS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CourseTasksFragment.newInstance(mCourse);
            }
        });

        fragmentKey = "COURSE_" + courseID + "_ROSTER";
                packages[ROSTER_FRAGMENT] = new FragmentPackage("ROSTER", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return CourseRosterFragment.newInstance(mCourse);
            }
        });

        return packages;
    }

    /**
     * Make sure no duplicate course pages are opened
     * @param course the course to open
     */
    @Override
    public void openCoursePage(Course course) {
        // dont open duplicate course page
        if (!mCourse.getId().equals(course.getId())) {
            super.openCoursePage(course);
        } else {
            closeNotificationDrawer();
        }
    }
}
