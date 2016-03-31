package com.thecn.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Reflection;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Adapter for showing list of post reflections.
 * TODO show sub reflections
 */
public class ReflectionsAdapter extends BaseAdapter {

    private ArrayList<Reflection> mReflections = new ArrayList<Reflection>();

    private LayoutInflater inflater;
    private CallbackManager<? extends Fragment> callbackManager;

    private int iconDimen;

    /**
     * Keep references to views
     */
    static class ViewHolder {
        TextView reflectionContentTextView;
        TextView usernameTextView;
        TextView reflectionTimeTextView;
        ImageView userAvatar;
    }

    /**
     * New instance
     * @param context for resources
     * @param manager for network callbacks
     */
    public ReflectionsAdapter(Context context, CallbackManager<? extends Fragment> manager) {
        inflater = LayoutInflater.from(context);
        callbackManager = manager;
        iconDimen = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                40f,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Add reflection to list, update view
     * @param reflection reflection to add
     */
    public void add(Reflection reflection) {
        mReflections.add(0, reflection);
        notifyDataSetChanged();
    }

    /**
     * Add all reflections, update view
     * @param reflections reflections to add
     */
    public void addAll(ArrayList<Reflection> reflections) {
        mReflections.addAll(reflections);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mReflections.size();
    }

    @Override
    public Reflection getItem(int position) {
        return mReflections.get(mReflections.size() - position - 1);
    }

    @Override
    public long getItemId(int position) {
        return mReflections.size() - position - 1;
    }

    /**
     * Set user name, picture, content, time of reflection.  When user picture clicked,
     * take to profile activity
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Reflection theReflection = getItem(position);

        ViewHolder holder;

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.reflection_view, parent, false);
            holder = new ViewHolder();

            holder.reflectionContentTextView = (TextView) convertView.findViewById(R.id.reflection_content);
            holder.reflectionContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
            holder.reflectionTimeTextView = (TextView) convertView.findViewById(R.id.reflection_time);
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.content_text);
            holder.userAvatar = (ImageView) convertView.findViewById(R.id.user_icon);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final User user;
        if (theReflection.getUser().isMe()) {
            user = AppSession.getInstance().getUser();
        } else {
            user = theReflection.getUser();
        }

        CharSequence text = theReflection.getProcessedText();
        holder.reflectionContentTextView.setText(text);

        holder.usernameTextView.setText(user.getDisplayName());
        holder.reflectionTimeTextView.setText(theReflection.getDisplayTime());

        String avatarUrl = user.getAvatar().getView_url() + ".w160.jpg";
        MyVolley.loadIndexedUserImage(avatarUrl, holder.userAvatar, position, iconDimen);

        holder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) callbackManager.getObject().getActivity()).openProfileByID(user.getId());
            }
        });

        return convertView;

    }
}
