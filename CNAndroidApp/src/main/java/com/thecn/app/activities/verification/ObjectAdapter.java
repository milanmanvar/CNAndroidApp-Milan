package com.thecn.app.activities.verification;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.thecn.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic adapter for showing items that only consist of one text view.
 * Keeps track of two lists, one for containing
 * ALL the data and one for displaying certain sections of that data, based on
 * whatever filtering of the data might occur.
*/
public class ObjectAdapter<T> extends BaseAdapter implements Filterable {
    private List<T> mList;
    private List<T> mDisplayList;
    private Context mContext;
    private int mTextColor = -1;
    private int mBackgroundColor = -1;

    private final int mPadding;

    /**
     * New instance
     * @param list list of objects to display
     * @param context context
     */
    public ObjectAdapter(List<T> list, Context context) {
        mList = mDisplayList = list;
        mContext = context;
        mPadding = (int) context.getResources().getDimension(R.dimen.dialog_padding);
    }

    @Override
    public int getCount() {
        return mDisplayList.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return mDisplayList.get(i);
    }

    /**
     * Sets background color of items
     * @param backgroundColor resource id of color
     */
    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
    }

    /**
     * Sets text color of items
     * @param textColor resource id of text color
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public void changeDataSource(List<T> list) {
        mList = list;
        notifyDataSetChanged();
    }

    /**
     * Set text and appropriate colors
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(mContext);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);

            if (mTextColor != -1) {
                textView.setTextColor(mTextColor);
            }

            if (mBackgroundColor != -1) {
                textView.setBackgroundColor(mBackgroundColor);
            }
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(getItem(i).toString());

        return textView;
    }

    /**
     * Gets filter to user to filter data in this adapter.
     * Uses an object's toString() method for filtering.
     * @return filter
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            /**
             * Filters out items in list that do not contain the CharSequence
             */
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<T> list = new ArrayList<>();
                String query = charSequence.toString().trim().toLowerCase();
                if (query.length() == 0) {
                    return null;
                }

                for (T object : mList) {
                    String string = object.toString().trim().toLowerCase();
                    if (string.contains(query)) {
                        list.add(object);
                    }
                }

                FilterResults results = new FilterResults();
                results.count = list.size();
                results.values = list;

                return results;
            }

            /**
             * Set the display list to show results of filter.  If results null,
             * show all the data.
             */
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults == null) {
                    mDisplayList = mList;
                } else {
                    mDisplayList = (List<T>) filterResults.values;
                }

                notifyDataSetChanged();
            }
        };
    }
}
