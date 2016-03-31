package com.thecn.app.activities.course;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.course.Gradebook;
import com.thecn.app.models.course.GradebookCategory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by utsav.k on 26-11-2015.
 */
public class ExpandableListGradebookAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<GradebookCategory> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<GradebookCategory, ArrayList<Gradebook>> _listDataChild;
    private CourseGradebookFragment_New courseGradebookFragment_new;

    public ExpandableListGradebookAdapter(Context context, ArrayList<GradebookCategory> listDataHeader,
                                          HashMap<GradebookCategory, ArrayList<Gradebook>> listChildData, CourseGradebookFragment_New courseGradebookFragment_new) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.courseGradebookFragment_new = courseGradebookFragment_new;
    }

    @Override
    public Gradebook getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Gradebook childText = (Gradebook) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.row_course_gradebook, null);
        }

        final TextView txtListChildTitle = (TextView) convertView
                .findViewById(R.id.row_gradebook_title);
        final TextView txtListChildGrade = (TextView) convertView
                .findViewById(R.id.row_gradebook_grade1);
        final TextView txtListChildWeight = (TextView) convertView
                .findViewById(R.id.row_gradebook_grade2);
        txtListChildTitle.setText(childText.getItem_name());
        String tempGrade, resultGrade = "";
        double result = 0;
        if (!childText.getItem_grade().toString().trim().equalsIgnoreCase("0") && !childText.getGrade().toString().trim().equalsIgnoreCase("")) {
            tempGrade = ((childText.getGrade().toString().trim().equalsIgnoreCase("0") || childText.getGrade().toString().trim().equalsIgnoreCase("")) ? "-" : childText.getGrade().toString().trim()) + "/" +
                    ((childText.getItem_grade().toString().trim().equalsIgnoreCase("0") || childText.getItem_grade().toString().trim().equalsIgnoreCase("")) ? "-" : childText.getItem_grade().toString().trim());
            if (childText.getGrade_letter() != null && !childText.getGrade_letter().equalsIgnoreCase(""))
                tempGrade = "(" + tempGrade + ")";

            result = ((Double.parseDouble(childText.getGrade()) * childText.getItem_percentage()) / Double.parseDouble(childText.getItem_grade()));
        } else
            tempGrade = ((childText.getGrade().toString().trim().equalsIgnoreCase("0") || childText.getGrade().toString().trim().equalsIgnoreCase("")) ? "-" : childText.getGrade().toString().trim());

        if (result > 0)
            resultGrade = "+" + String.format("%.2f", result) + "%";
        else if (result == 0 && getGroup(groupPosition).getCatName().equalsIgnoreCase("Bonus"))
            resultGrade = "+" + String.format("%.2f", result) + "%";
        else
            resultGrade = "";
        txtListChildGrade.setText(childText.getGrade_letter() + " " + tempGrade + "\n" + resultGrade);
        txtListChildWeight.setText("");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public GradebookCategory getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GradebookCategory gradebookCategory = getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.row_course_gradebook_title, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);

        TextView lblListHeaderGrade = (TextView) convertView
                .findViewById(R.id.lblListHeader1);
        lblListHeaderGrade.setTypeface(null, Typeface.BOLD);

        TextView lblListHeaderWeight = (TextView) convertView
                .findViewById(R.id.lblListHeader2);
        lblListHeaderWeight.setTypeface(null, Typeface.BOLD);

        if(gradebookCategory.getCatName().toString().toString().equalsIgnoreCase("bonus")){
//            if(Double.parseDouble(gradebookCategory.getAvgGradePer()) > 0){
                lblListHeaderGrade.setText("+" + gradebookCategory.getAvgGradePer() + "%");
//            }
        }else{
            if(gradebookCategory.getCatGrade().length()>0 && Double.parseDouble(gradebookCategory.getCatGrade())>0)
                lblListHeaderGrade.setText(gradebookCategory.getCatGrade() + "%");
            else
                lblListHeaderGrade.setText("-%");
        }
//        if (Double.parseDouble(gradebookCategory.getAvgGradePer()) > 0) {
//            if (gradebookCategory.getCatName().toString().toString().equalsIgnoreCase("bonus"))
//                lblListHeaderGrade.setText("+" + gradebookCategory.getAvgGradePer() + "%");
//            else
//                lblListHeaderGrade.setText(gradebookCategory.getCatGrade() + "%");
//        } else if (Double.parseDouble(gradebookCategory.getAvgGradePer()) == 0 && gradebookCategory.getCatName().toString().toString().equalsIgnoreCase("bonus")) {
//            lblListHeaderGrade.setText("+" + gradebookCategory.getAvgGradePer() + "%");
//        } else
//            lblListHeaderGrade.setText("-%");
        if (gradebookCategory.getCatWeight() != null && !gradebookCategory.getCatWeight().toString().trim().equalsIgnoreCase("0") && !gradebookCategory.getCatWeight().toString().trim().equalsIgnoreCase("null") && !gradebookCategory.getCatWeight().toString().trim().equalsIgnoreCase(""))
            lblListHeaderWeight.setText(gradebookCategory.getCatWeight() + "%");
        else{

            if(courseGradebookFragment_new.txtLblWeight.getVisibility() == View.VISIBLE){
                lblListHeaderWeight.setText("0%");

                if(gradebookCategory.getCatName().toString().toString().equalsIgnoreCase("bonus")){
                    lblListHeaderWeight.setText("+"+gradebookCategory.getAvgBonusWeight()+ "%");
                }
            }

        }
        lblListHeader.setText(gradebookCategory.getCatName());
        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


}
