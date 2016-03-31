package com.thecn.app.activities.course;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.course.Gradebook;
import com.thecn.app.models.course.GradebookCategory;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.GradeStore;
import com.thecn.app.stores.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Shows tasks from a Course.  This is old and needs to be updated.  Current method for
 * showing tasks is to manually parse content, which is not good.  A better approach would
 * be to use JSoup (already in the libs folder).  A WebView could also be used to format
 * the content automatically.  Task related actions could be intercepted and handled using a WebInterface
 * (but don't enable Javascript, because the user could possible write in something malicious).
 */

public class CourseGradebookFragment_New extends Fragment {

    public static final String TAG = CourseGradebookFragment_New.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";
    //prevents async changes to task data
    protected SwipeRefreshLayout swipeRefreshLayout;
    private Course mCourse;
    private ArrayList<GradebookCategory> catIds = new ArrayList<>();
    private HashMap<GradebookCategory, ArrayList<Gradebook>> listDataChild = new HashMap<GradebookCategory, ArrayList<Gradebook>>();
    private android.widget.ExpandableListView expandableListView;
    public TextView txtCourseGrade, txtLblWeight;

    /**
     * @param mCourse must have course object to put in arguments
     * @return new instance
     */
    public static CourseGradebookFragment_New newInstance(Course mCourse) {
        CourseGradebookFragment_New fragment = new CourseGradebookFragment_New();

        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }


    /**
     * Initialize data
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
    }

    /**
     * Gets references to task title field and left right buttons.
     * Also sets up swipe refresh layout.
     * A ViewPager would be much nicer here for scrolling through subtasks...
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_gradebook_new, container, false);

        expandableListView = (android.widget.ExpandableListView) view.findViewById(R.id.lvExp);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshlayout);
        txtCourseGrade = (TextView) view.findViewById(R.id.courseGrade);
        txtLblWeight = (TextView) view.findViewById(R.id.gradebook_tblTxtWeight);

        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getData();
            }
        });
        getData();
        return view;
    }

    private void getData() {
        if (BaseStore.isOnline(getActivity())) {
            GradeStore.getAllGradebookData(mCourse.getId(), new ResponseCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("Response:", "" + response);
                    if (response != null)
                        parseGradebookData(response);
                }

                @Override
                public void onError(VolleyError error) {

                }
            });
            GradeStore.getGradebookInfo(mCourse.getId(), new ResponseCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("Response grade info:", "" + response);
                    if (response != null)
                        parseCourseGrade(response);
                }

                @Override
                public void onError(VolleyError error) {

                }
            });
        } else {
            AppSession.showLongToast("No internet connection.  Try again later.");
        }
    }

    private void parseCourseGrade(JSONObject response) {
        try {
            JSONObject jsMain = response;
            JSONObject jsData = jsMain.getJSONObject("data");
            JSONObject jsonMe = jsData.getJSONObject("me");
            if (!jsonMe.getString("percentage").toString().trim().equalsIgnoreCase("") && !jsonMe.getString("percentage").toString().trim().equalsIgnoreCase("0")) {
                txtCourseGrade.setText(getString(R.string.lbl_course_grade, jsonMe.getString("grade_letter").toString().trim(), "(" + jsonMe.getString("percentage").toString().trim()) + "%)");
            } else
                txtCourseGrade.setText(getString(R.string.lbl_course_grade, "", "Not available"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseGradebookData(JSONObject response) {
        double tempWeight = 0;
        ArrayList<Gradebook> gradebookArrayList = new ArrayList<>();
        try {
            JSONObject jsMain = response;
            JSONArray jsData = jsMain.getJSONArray("data");
            Gradebook gradebook;
            for (int i = 0; i < jsData.length(); i++) {
                gradebook = new Gradebook();
                JSONObject jsonObject = jsData.getJSONObject(i);
                JSONObject jsItem = jsonObject.getJSONObject("item");

                gradebook.setGrade_letter(jsonObject.getString("grade_letter"));
                gradebook.setGrade(jsonObject.getString("grade").toString().trim().equalsIgnoreCase("") ? "0" : jsonObject.getString("grade").toString().trim());
                gradebook.setId(jsItem.getString("id").toString().trim());
                gradebook.setIsAnarSeed(jsItem.getBoolean("is_anar_seed"));
                gradebook.setIsDeletable(jsItem.getBoolean("is_deletable"));
                gradebook.setIsDisplayable(jsItem.getBoolean("is_displayable"));
                gradebook.setIsGradeConvertible(jsItem.getBoolean("is_grade_convertible"));
                gradebook.setIsVisible(jsItem.getBoolean("visible"));
                gradebook.setItem_contentType(jsItem.getString("content_type").toString().trim());
                gradebook.setItem_grade(jsItem.getString("grade").toString().trim().equalsIgnoreCase("") ? "0" : jsItem.getString("grade").toString().trim());
                gradebook.setAvgGrade(jsItem.getString("average_grade").toString().trim());
                gradebook.setItem_name(jsItem.getString("name").toString().trim());
                gradebook.setItem_percentage(jsItem.getDouble("percentage"));
                gradebook.setItem_type(jsItem.getString("type").toString().trim());
                gradebook.setCategoryId(jsItem.getString("category_id").toString().trim() != null ? jsItem.getString("category_id").toString().trim() : "");
                gradebookArrayList.add(gradebook);

                GradebookCategory gradebookCategory = new GradebookCategory();
                if (jsonObject.has("category")) {
                    JSONObject jsCat = jsonObject.getJSONObject("category");
                    gradebookCategory.setCatId(jsCat.getString("id").toString().trim());
                    gradebookCategory.setCatName(jsCat.getString("name").toString().trim());
                    gradebookCategory.setCatWeight(jsCat.getString("weight").toString().trim());
                    gradebookCategory.setUserGrade(jsCat.getString("average_grade_percentage").toString().trim());
                    gradebookCategory.setCatGrade(jsCat.getString("user_grade").toString().trim());
                    gradebookCategory.setAvgGradePer(jsCat.getString("user_grade").toString().trim());

                } else {
                    gradebookCategory.setCatId(gradebook.getCategoryId());
                    gradebookCategory.setCatName("Default");
                    gradebookCategory.setCatWeight("");
                    gradebookCategory.setUserGrade("");
                    gradebookCategory.setAvgGradePer("");
                    gradebookCategory.setCatGrade("");
                }
                if (gradebook.getItem_type().toString().trim().equalsIgnoreCase("extra")){
                    gradebookCategory.setCatName("Bonus");
                    gradebookCategory.setAvgBonusWeight(""+(int)gradebook.getItem_percentage());
                }
                gradebook.setGradebookCategory(gradebookCategory);
                if (!catIds.contains(gradebookCategory)) {
                    if (gradebook.getGradebookCategory().getCatName().equalsIgnoreCase("Bonus"))
                        catIds.add(0, gradebook.getGradebookCategory());
                    else
                        catIds.add(gradebook.getGradebookCategory());
                }
                if (gradebook.getGradebookCategory().getCatWeight().toString().trim() != null && !gradebook.getGradebookCategory().getCatWeight().toString().trim().equalsIgnoreCase("") && !gradebook.getGradebookCategory().getCatWeight().toString().trim().equalsIgnoreCase("null"))
                    tempWeight = tempWeight + Double.parseDouble(gradebook.getGradebookCategory().getCatWeight().toString().trim());

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < catIds.size(); i++) {
            ArrayList<Gradebook> gItems = new ArrayList<>();
            double result = 0;
            for (int j = 0; j < gradebookArrayList.size(); j++) {
                if (catIds.get(i).getCatId().equals(gradebookArrayList.get(j).getCategoryId())) {
                    gItems.add(gradebookArrayList.get(j));
                    double percentage = gradebookArrayList.get(j).getItem_percentage();
                    double grade = Double.parseDouble(gradebookArrayList.get(j).getGrade().toString().trim());
                    double itemGrade = Double.parseDouble(gradebookArrayList.get(j).getItem_grade().toString().trim());
                    if (itemGrade != 0)
                        result += ((grade * percentage) / itemGrade);
                }
            }
            catIds.get(i).setAvgGradePer(String.format("%.2f", result));
            listDataChild.put(catIds.get(i), gItems);
        }
        if (tempWeight > 0)
            txtLblWeight.setVisibility(View.VISIBLE);
        else
            txtLblWeight.setVisibility(View.INVISIBLE);
        expandableListView.setAdapter(new ExpandableListGradebookAdapter(getActivity(), catIds, listDataChild,this));
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        for (int i = 0; i < expandableListView.getExpandableListAdapter().getGroupCount(); i++)
            expandableListView.expandGroup(i);
    }

}
