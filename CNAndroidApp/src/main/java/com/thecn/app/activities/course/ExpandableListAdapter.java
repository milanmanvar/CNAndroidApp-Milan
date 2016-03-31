package com.thecn.app.activities.course;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.course.Task;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by utsav.k on 26-11-2015.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<Task>> _listDataChild;

    public ExpandableListAdapter(Context context, ArrayList<String> listDataHeader,
                                 HashMap<String, ArrayList<Task>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Task getChild(int groupPosition, int childPosititon) {
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

        final Task childText = (Task) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.row_course_task, null);
        }

        final TextView txtListChild = (TextView) convertView
                .findViewById(R.id.task_content);
        TextView txtListChildShowAll = (TextView) convertView
                .findViewById(R.id.task_showAll);
        LinearLayout llSubTask = (LinearLayout) convertView.findViewById(R.id.row_layout);
        txtListChild.setMovementMethod(LinkMovementMethod.getInstance());
        if(txtListChild.getText().toString().length()<=0)
        txtListChild.setText(childText.getFormattedContent());
        txtListChildShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iFullTask = new Intent(_context, TaskFullScreen.class);
                iFullTask.putExtra("content", childText.getUnformattedContent());
                iFullTask.putExtra("id", childText.getId());
                _context.startActivity(iFullTask);
            }
        });
//        if (childText.getSubTasks() != null && llSubTask.getChildCount()<=0) {
//            Log.e("Sub task size:", "" + childText.getSubTasks().size());
//
//            Button btnSubTaskMain = new Button(_context);
//            btnSubTaskMain.setText("Main");
//
//            btnSubTaskMain.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    txtListChild.setText(childText.getFormattedContent());
//                }
//            });
//
//            llSubTask.addView(btnSubTaskMain);
//
//            for(int i=0;i<childText.getSubTasks().size();i++) {
//                Log.e("text:", "" + childText.getSubTasks().get(i).getDisplayText());
//                Button btnSubTask = new Button(_context);
////                LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,40);
////                btnSubTask.setLayoutParams(llParam);
//                btnSubTask.setText(Html.fromHtml(childText.getSubTasks().get(i).getTitle()));
//
//
//                btnSubTask.setTag(childText.getSubTasks().get(i).getDisplayText());
//                btnSubTask.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        txtListChild.setText(Html.fromHtml((String)v.getTag()));
//                    }
//                });
//
//                llSubTask.addView(btnSubTask);
//
//            }
//        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
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
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.row_course_task_title, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
