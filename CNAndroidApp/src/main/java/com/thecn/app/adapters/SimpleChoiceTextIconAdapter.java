package com.thecn.app.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.util.TextIconEntry;

import java.util.ArrayList;

/**
 * Used to show a list of something that uses an icon image and text to the right of it.
 */
public class SimpleChoiceTextIconAdapter extends BaseAdapter {

    private ArrayList<TextIconEntry> entries = new ArrayList<TextIconEntry>();
    private LayoutInflater inflater;

    /**
     * new instance
     * @param activity get layout inflater from
     * @param entries data
     */
    public SimpleChoiceTextIconAdapter(Activity activity, ArrayList<TextIconEntry> entries) {
        this.entries = entries;
        inflater = activity.getLayoutInflater();
    }

    /**
     * Remove all items in list, update view
     */
    public void clear() {
        entries.clear();
        notifyDataSetChanged();
    }

    /**
     * Add all items into list, update view.
     * @param entries items to add
     */
    public void addAll(ArrayList<TextIconEntry> entries) {
        this.entries.addAll(entries);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public TextIconEntry getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Keep view references
     */
    class ViewHolder {
        ImageView icon;
        TextView textView;
    }

    /**
     * Set image and text views to match given data.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.selectable_list_item_w_icon, null, false);

            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.textView = (TextView) convertView.findViewById(R.id.text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextIconEntry entry = getItem(position);
        holder.icon.setImageResource(entry.resourceID);
        holder.textView.setText(entry.text);

        return convertView;
    }
}
