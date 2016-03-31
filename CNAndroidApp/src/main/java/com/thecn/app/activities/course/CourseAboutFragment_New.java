package com.thecn.app.activities.course;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.course.CourseSchool;
import com.thecn.app.models.course.Instructor;
import com.thecn.app.models.course.InstructorUsers;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.controllers.AnarBarController;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Shows a small amount of information about this Course
 */

public class CourseAboutFragment_New extends Fragment {

    public static final String TAG = CourseAboutFragment_New.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";
    private AnarBarController anarBarController;
    private View headerView;
    private ImageLoader imageLoader = MyVolley.getImageLoader();
    private Course mCourse;

    public CourseAboutFragment_New() {
        // Required empty public constructor
    }

    /**
     * @param mCourse must have a Course object to put in arguments
     */
    public static CourseAboutFragment_New newInstance(Course mCourse) {
        CourseAboutFragment_New fragment = new CourseAboutFragment_New();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Initialize course data
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
    }

    /**
     * Sets course name, number, status, school, instructors, and about data into the views.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_about_new, container, false);
        setUpHeaderView(view);

        String courseName = mCourse.getName();
        courseName = courseName != null ? courseName : "(No name found)";
//        TextView nameContent = (TextView) view.findViewById(R.id.name_content);
//        nameContent.setText(courseName);

        String courseNumber = mCourse.getCourseNumber();
        if (courseNumber == null) courseNumber = "";
        TextView courseNumberContent = (TextView) view.findViewById(R.id.course_number_content);
        courseNumberContent.setText(courseNumber);

        ImageView ivStatus = (ImageView) view.findViewById(R.id.status_iv);
        String courseStatus = getCourseStatus(ivStatus);
        TextView statusContent = (TextView) view.findViewById(R.id.status_content);
        statusContent.setText(courseStatus);


        CourseSchool school = mCourse.getSchool();
        String schoolName = null;
        if (school != null) {
            schoolName = school.getName();
        }
        schoolName = schoolName == null ? "" : schoolName;
        TextView schoolContent = (TextView) view.findViewById(R.id.school_content);
        schoolContent.setText(schoolName);

        ArrayList<Instructor> instructors = getCourseInstructorList();

        TextView instructorLabel = (TextView) view.findViewById(R.id.instructor_label);
        if (instructors != null && instructors.size() > 1) {
            instructorLabel.setText("Instructors");
        }

        String instructorString = getCourseInstructorString(instructors);
        TextView instructorContent = (TextView) view.findViewById(R.id.instructor_content);
        instructorContent.setText(instructorString);

        String courseAbout = mCourse.getAbout();
        if (courseAbout != null) {
            ((TextView) view.findViewById(R.id.about_message))
                    .setText(Html.fromHtml(courseAbout));
        } else {
            view.findViewById(R.id.about_layout)
                    .setVisibility(View.GONE);
        }

        return view;
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
    }


    /**
     * Gets and returns string representation of the status of this course
     *
     * @return course status
     */
    private String getCourseStatus(ImageView ivStatus) {
        boolean started = mCourse.getIsStart();
        boolean ended = mCourse.getIsEnd();


        if (started) {
            ivStatus.setImageResource(R.drawable.about_started);
            return "Active";
        } else if (ended) {
            ivStatus.setImageResource(R.drawable.about_ended);
            return "Ended";
        } else {
            ivStatus.setImageResource(R.drawable.about_unavailabel);
            return "Unavailable";
        }
    }

    /**
     * Gets the list of instructors for this course
     *
     * @return instructors
     */
    private ArrayList<Instructor> getCourseInstructorList() {
        InstructorUsers insObject = mCourse.getInstructorUsers();
        ArrayList<Instructor> instructors = null;

        if (insObject != null) {
            instructors = insObject.getInstructors();
        }

        return instructors;
    }

    /**
     * Gets string representation of instructor list for this course.
     *
     * @param instructors list of instructors
     * @return string representation of instructors
     */
    private String getCourseInstructorString(ArrayList<Instructor> instructors) {
        String instructorString = "(no instructor found)";

        if (instructors != null && instructors.size() > 0) {
            instructorString = instructors.get(0).getDisplayName();

            for (int i = 1; i < instructors.size(); i++) {
                instructorString += ", " + instructors.get(i).getDisplayName();
            }
        }

        return instructorString;
    }
}
