package com.thecn.app.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to show items in {@link com.thecn.app.activities.navigation.NavigationDrawerFragment} to the left of {@link com.thecn.app.activities.navigation.NavigationActivity}
 */
public class NavDrawerAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {

    private List<Object> mItems; //all items
    private int[] mSectionIndices; //indices of different sections
    private String[] mSectionTitles = {"Courses", "Conexus"};

    private LayoutInflater layoutInflater;

    private final int courseColor, conexusColor, neutralColor, whiteColor;
    private int iconDimen;

    /**
     * New instance
     * @param context for resources
     */
    public NavDrawerAdapter(Context context) {
        mItems = new ArrayList<>();
        mSectionIndices = new int[3];

        loadList();

        layoutInflater = LayoutInflater.from(context);

        Resources r = context.getResources();
        courseColor = r.getColor(R.color.navigation_drawer_item_color_course);
        conexusColor = r.getColor(R.color.navigation_drawer_item_color_conexus);
        neutralColor = r.getColor(R.color.navigation_drawer_item_color_neutral);
        whiteColor = r.getColor(R.color.white);
        iconDimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, r.getDisplayMetrics());
    }

    /**
     * Remove items from list, add new items.  Adds "Home", "Profile" first.
     * Then adds user's courses, then user's conexuses.  Then adds "Logout" and sets section indices.
     */
    public void loadList() {
        mItems.clear();
        mItems.add("Home");
        mItems.add("Profile");

        ArrayList<Course> courses = AppSession.getInstance().getSimpleUserCourses();
        int coursesSize;
        if (courses != null) {
            coursesSize = courses.size();
            mItems.addAll(courses);
        } else {
            coursesSize = 0;
        }

        ArrayList<Conexus> conexuses = AppSession.getInstance().getSimpleUserConexuses();
        int conexusesSize;
        if (conexuses != null) {
            conexusesSize = conexuses.size();
            mItems.addAll(conexuses);
        } else {
            conexusesSize = 0;
        }

        mItems.add("Logout");
        mSectionIndices[0] = 2;
        mSectionIndices[1] = coursesSize + mSectionIndices[0];
        mSectionIndices[2] = conexusesSize + mSectionIndices[1];

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sets content of items depending on what type it is.  E.G. "Home", "course", etc.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.navigation_drawer_item, parent, false);
            holder = new ViewHolder();

            holder.text = (TextView) convertView.findViewById(R.id.text1);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.colorArea = convertView.findViewById(R.id.colored_area);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(0);

        if (getItem(position) instanceof Course) {

            Course course = (Course) getItem(position);
            holder.text.setText(course.getName());

            String url = course.getLogoURL() + ".w160.jpg";
            MyVolley.ImageParams params = new MyVolley.ImageParams(url, holder.imageView);
            params.maxWidth = params.maxHeight = iconDimen;
            params.placeHolderID = params.errorImageResourceID = android.R.color.transparent;

            MyVolley.loadImage(params);

            holder.colorArea.setBackgroundColor(courseColor);

        } else if (getItem(position) instanceof Conexus) {

            Conexus conexus = (Conexus) getItem(position);
            holder.text.setText(conexus.getName());

            String url = conexus.getLogoURL() + ".w160.jpg";
            MyVolley.ImageParams params = new MyVolley.ImageParams(url, holder.imageView);
            params.maxWidth = params.maxHeight = iconDimen;
            params.placeHolderID = params.errorImageResourceID = android.R.color.transparent;

            MyVolley.loadImage(params);

            holder.colorArea.setBackgroundColor(conexusColor);

        } else {

            holder.text.setText((String) getItem(position));
            if (getItem(position).equals("Home")) {
                holder.imageView.setImageResource(R.drawable.left_bar_home_icon);
            } else if (getItem(position).equals("Profile")) {

                String url = AppSession.getInstance().getUser().getAvatar().getView_url() + ".w160.jpg";
                MyVolley.loadIndexedUserImage(url, holder.imageView, position, iconDimen);

            } else if (getItem(position).equals("Logout")) {
                holder.imageView.setImageResource(R.drawable.left_bar_logout_icon);
            }

            holder.colorArea.setBackgroundColor(neutralColor);

        }

        return convertView;
    }

    /**
     * Sets title of appropriate header.
     */
    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = layoutInflater.inflate(R.layout.navigation_drawer_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        holder.text.setVisibility(View.VISIBLE);

        // set header text as first char in name
        if (getItem(position) instanceof Course) {
            holder.text.setText("MY COURSES");
            holder.text.setTextColor(courseColor);
        } else if (getItem(position) instanceof Conexus) {
            holder.text.setText("MY CONEXUS");
            holder.text.setTextColor(conexusColor);
        } else {
            if (position == mSectionIndices[2]) {
                holder.text.setText("");
            } else {
                holder.text.setText("");
                holder.text.setVisibility(View.GONE);
            }

            holder.text.setTextColor(whiteColor);
        }

        return convertView;
    }

    /**
     * Remember that these have to be static, postion=1 should always return
     * the same Id that is.
     */
    @Override
    public long getHeaderId(int position) {
        if (getItem(position) instanceof Course) {
            return 1;
        } else if (getItem(position) instanceof Conexus) {
            return 2;
        } else {
            return 0;
        }
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }
        return mSectionIndices[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    @Override
    public Object[] getSections() {
        return mSectionTitles;
    }

    /**
     * Keep references to views
     */
    class HeaderViewHolder {
        TextView text;
    }

    /**
     * Keep references to views
     */
    class ViewHolder {
        TextView text;
        ImageView imageView;
        View colorArea;
    }

}
