package com.thecn.app.activities.createpost;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.PostVisibilityAdapter;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.content.PostingGroup;

import java.util.ArrayList;

/**
 * Shows a list of visibility options for when a user is creating a post.
 */
public class PostVisibilityActivity extends ListActivity {

    public static final String TAG = PostVisibilityActivity.class.getSimpleName();

    private PostVisibilityAdapter adapter;

    /**
     * Initializes adapter and listview, sets checked items as passed in intent, sets up update button
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new PostVisibilityAdapter(this);

        setContentView(R.layout.activity_post_visibility);
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(adapter);

        setCheckedItemsFromIntent();

        Button updateButton = (Button) findViewById(R.id.update_visibility);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateVisibility();
            }
        });
    }

    /**
     * Finishes without changing anything.
     */
    @Override
    public void onBackPressed() {
        AppSession.showLongToast("Settings not updated");
        finish();
    }

    /**
     * Sets items checked if specified in the intent
     */
    private void setCheckedItemsFromIntent() {

        ArrayList<PostingGroup> visibleGroups = (ArrayList<PostingGroup>) getIntent().getSerializableExtra("GROUPS");
        ArrayList<Course> courses = (ArrayList<Course>) getIntent().getSerializableExtra("COURSES");
        ArrayList<Conexus> conexuses = (ArrayList<Conexus>) getIntent().getSerializableExtra("CONEXUSES");

        ListView lv = getListView();

        for (PostingGroup group : visibleGroups) {
            int index = adapter.getItem(group);

            if (index >= 0) lv.setItemChecked(index, true);
        }

        for (Course course : courses) {
            int index = adapter.getItem(course);

            if (index >= 0) lv.setItemChecked(index, true);
        }

        for (Conexus conexus : conexuses) {
            int index = adapter.getItem(conexus);

            if (index >= 0) lv.setItemChecked(index, true);
        }
    }

    /**
     * Uses state of the views to update visibility settings.  Sets this result and finishes activity.
     * If no items are checked, show error to user.
     */
    private void updateVisibility() {

        PostVisibilityItemCollection collection = getCheckedItems();

        if (collection.getTotalItems() > 0) {
            ArrayList<PostingGroup> visibleGroups = collection.getGroups();
            ArrayList<Course> courses = collection.getCourses();
            ArrayList<Conexus> conexuses = collection.getConexuses();

            ArrayList<PostingGroup> invisibleGroups = new ArrayList<PostingGroup>();

            if (courses.size() > 0) invisibleGroups.add(PostingGroup.course);
            if (conexuses.size() > 0) invisibleGroups.add(PostingGroup.conexus);

            Intent intent = new Intent();
            intent.putExtra("V_GROUPS", visibleGroups);
            intent.putExtra("INV_GROUPS", invisibleGroups);
            intent.putExtra("COURSES", courses);
            intent.putExtra("CONEXUSES", conexuses);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            AppSession.showLongToast("At least one item must be selected.");
        }
    }

    /**
     * Object that holds three array list that pertain to visibility settings
     * for groups, courses, and conexuses
     */
    public class PostVisibilityItemCollection {
        public ArrayList<PostingGroup> mGroups;
        public ArrayList<Course> mCourses;
        public ArrayList<Conexus> mConexuses;

        public PostVisibilityItemCollection(ArrayList<PostingGroup> group,
                                            ArrayList<Course> courses,
                                            ArrayList<Conexus> conexuses) {
            mGroups = group;
            mCourses = courses;
            mConexuses = conexuses;
        }

        public int getTotalItems() {
            return mGroups.size() + mCourses.size() + mConexuses.size();
        }

        public ArrayList<PostingGroup> getGroups() {
            return mGroups;
        }

        public void setGroups(ArrayList<PostingGroup> mGroup) {
            this.mGroups = mGroup;
        }

        public ArrayList<Course> getCourses() {
            return mCourses;
        }

        public void setCourses(ArrayList<Course> mCourses) {
            this.mCourses = mCourses;
        }

        public ArrayList<Conexus> getConexuses() {
            return mConexuses;
        }

        public void setConexuses(ArrayList<Conexus> mConexuses) {
            this.mConexuses = mConexuses;
        }
    }

    /**
     * Constructs a post visibility item collection from the state of the list (whichever items are checked).
     * @return an object that specifies which visibility items were checked.
     */
    public PostVisibilityItemCollection getCheckedItems() {
        ArrayList<PostingGroup> groups = new ArrayList<>();
        ArrayList<Course> courses = new ArrayList<>();
        ArrayList<Conexus> conexuses = new ArrayList<>();

        for (int i = 0; i < getListView().getCount(); i++) {
            if (getListView().isItemChecked(i)) {
                Object object = adapter.getItem(i);

                if (object instanceof PostingGroup) {
                    groups.add((PostingGroup) object);
                } else if (object instanceof Course) {
                    courses.add((Course) object);
                } else if (object instanceof Conexus) {
                    conexuses.add((Conexus) object);
                }
            }
        }

        return new PostVisibilityItemCollection(groups, courses, conexuses);
    }

    /**
     * Sets the state of other checkboxes when one checkbox is clicked.
     * This action will be different depending on the checkbox.
     */
    public void onListItemClick(ListView l, View v, int position, long id) {

        //all members (global)
        //set all others unchecked
        if (position == 0 && l.isItemChecked(0))
            for (int i = 1; i < adapter.getCount(); i++)
                l.setItemChecked(i, false);

        //my followers
        //uncheck global and only me
        else if (position == 1 && l.isItemChecked(1))
            notGlobalOrOnlyMe(l);

        //my colleagues
        //uncheck global and only me
       /* else if (position == 2 && l.isItemChecked(2))
            notGlobalOrOnlyMe(l);*/

        //only me
        //uncheck all members, my followers, my colleagues
        else if (position == 2 && l.isItemChecked(2)) {
            l.setItemChecked(0, false);
            l.setItemChecked(1, false);
//            l.setItemChecked(2, false);

            for (int i = 3; i < adapter.getCount(); i++) {
                l.setItemChecked(i, false);
            }

        } else if (position > 2 && l.isItemChecked(position))
            //course or conexus, uncheck global and only me
            notGlobalOrOnlyMe(l);
    }

    /**
     * Unchecks "global" and "only me" check boxes.
     * @param lv listview where items can be found.
     */
    private void notGlobalOrOnlyMe(ListView lv) {
        lv.setItemChecked(0, false);
        lv.setItemChecked(2, false);
    }
}
