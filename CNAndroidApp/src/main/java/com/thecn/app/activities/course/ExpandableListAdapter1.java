package com.thecn.app.activities.course;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseExpandableListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.thecn.app.R;
import com.thecn.app.activities.createpost.CreatePostActivity;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.course.CourseTaskLinkDetail;
import com.thecn.app.models.course.SubTask;
import com.thecn.app.models.course.Task;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.tools.network.Downloader;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by utsav.k on 26-11-2015.
 */
public class ExpandableListAdapter1 extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<Task>> _listDataChild;
    private CharSequence content;
    private String id;

    public ExpandableListAdapter1(Context context, ArrayList<String> listDataHeader,
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

        final WebView txtListChild = (WebView) convertView
                .findViewById(R.id.task_content);
        final TextView txtListChildShowAll = (TextView) convertView
                .findViewById(R.id.task_showAll);
        RadioGroup llSubTask = (RadioGroup) convertView.findViewById(R.id.row_layout);
        final HorizontalScrollView hScroll = (HorizontalScrollView) convertView.findViewById(R.id.horizontalScrollView);

        txtListChild.getSettings().setJavaScriptEnabled(true);
        txtListChild.getSettings().setDomStorageEnabled(true);


        content = childText.getUnformattedContent();
        id = childText.getId();

        txtListChildShowAll.setTag(R.string.key_contect, content);
        txtListChildShowAll.setTag(R.string.key_id, id);

//        txtListChildShowAll.setTag(childText);

        if (content != null && content.toString().trim().length() > 100)
            txtListChildShowAll.setVisibility(View.VISIBLE);
        else
            txtListChildShowAll.setVisibility(View.GONE);
        txtListChild.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("Url:", "data-taskactionlink-type called...." + "=" + url);
                String[] temp = url.split("&");
                String dataId = "";
                for (int i = 0; i < temp.length; i++) {
                    if (temp[i].contains("data-taskactionlink-data-id")) {
                        dataId = temp[i];
                        break;
                    }
                }
                if (dataId.contains("=")) {
                    String tempData[] = dataId.split("=");
                    dataId = tempData.length > 1 ? tempData[1] : "";
                }
                Log.e("url id:", "" + dataId);
                if (url.contains("data-taskactionlink-type=create_post")) {

                    if (txtListChild.canGoBack()) {
                        txtListChild.goBack();
                    }
                    Intent intent = new Intent(_context, CreatePostActivity.class);
                    _context.startActivity(intent);

                } else if (url.contains("data-taskactionlink-type=view_content_page")) {

                    CourseStore.getCourseContentPage(dataId, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("content page:", "" + response);
                            Intent iDetail = new Intent(_context, TaskLinkDetail.class);
                            iDetail.putExtra("data", parseLinkResponse(response));
                            _context.startActivity(iDetail);
                        }

                        @Override
                        public void onError(VolleyError error) {
                        }
                    });
                } else if (url.contains("data-taskactionlink-type=view_content_post")) {

                    CourseStore.getCourseContentPost(dataId, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("content post:", "" + response);
                            Intent iDetail = new Intent(_context, TaskLinkDetail.class);
                            iDetail.putExtra("data", parseLinkResponse(response));
                            _context.startActivity(iDetail);
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });

                } else if (url.contains("data-taskactionlink-type=view_post")) {
                    PostStore.getPostById(dataId,id, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Post post = PostStore.getData(response);

                            if (post != null) {
                                ((NavigationActivity) _context).openPostPage(post, false);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });
                } else if (url.contains("data-taskactionlink-type=download_attachment")) {
                    final Attachment attachment = new Attachment(dataId);
                    beginDownload(attachment);
                    MyVolley.act_log("download_attachment", childText.getId(), attachment.getId());
                } else if (url.startsWith("www.") || url.startsWith("http")) {
                    Intent iView = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    _context.startActivity(iView);
                } else if (url.contains("data-taskactionlink-type=view_survey")) {
                    PostStore.getPostById(dataId,id, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Post post = PostStore.getData(response);

                            if (post != null) {
                                ((NavigationActivity) _context).openPostPage(post, false);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });
                } else if (url.contains("data-taskactionlink-type=view_event")) {
                    PostStore.getPostById(dataId,id, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Post post = PostStore.getData(response);

                            if (post != null) {
                                ((NavigationActivity) _context).openPostPage(post, false);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });
                }else if (url.contains("data-taskactionlink-type=view_quiz")) {
                    PostStore.getPostById(dataId,id, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Post post = PostStore.getData(response);

                            if (post != null) {
                                ((NavigationActivity) _context).openPostPage(post, false);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });
                }else if (url.contains("data-taskactionlink-type=view_classcast")) {
                    PostStore.getPostById(dataId,id, new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Post post = PostStore.getData(response);

                            if (post != null) {
                                ((NavigationActivity) _context).openPostPage(post, false);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });
                }


                else {
                    Toast.makeText(_context, "This type of SmartLink is not currently supported", Toast.LENGTH_LONG).show();
                }

                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });
        txtListChild.loadData(getHtmlString(childText.getUnformattedContent()), "text/html; charset=UTF-8", null);

        if (llSubTask.getChildCount() > 0)
            llSubTask.removeAllViewsInLayout();

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;

        if (childText.getSubTasks() != null && childText.getSubTasks().size() > 0) {
            Collections.sort(childText.getSubTasks(), new FishNameComparator());
            try {
                llSubTask.setVisibility(View.VISIBLE);
                for (int i = -1; i < childText.getSubTasks().size(); i++) {

                    RadioButton btnSubTask;
                    if (i == -1) {
                        btnSubTask = getRadioButton("Main");
                        btnSubTask.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                notifyDataSetChanged();
                            }
                        });
                        btnSubTask.setChecked(true);
                    } else {
                        btnSubTask = getRadioButton(Html.fromHtml(childText.getSubTasks().get(i).getTitle()).toString());
                        btnSubTask.setTag(childText.getSubTasks().get(i));
                        btnSubTask.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (v.getTag() != null) {
                                    SubTask subTask = (SubTask) v.getTag();
                                    if (subTask.getDisplayText() != null)
                                        content = subTask.getDisplayText();
                                    else if (subTask.getDescription() != null)
                                        content = subTask.getDescription();
                                    else
                                        content = "";
                                    id = subTask.getId();

                                    txtListChildShowAll.setTag(R.string.key_contect, content);
                                    txtListChildShowAll.setTag(R.string.key_id, id);

                                    txtListChild.loadData(getHtmlString(content.toString()), "text/html; charset=UTF-8", null);
                                    if (content.toString().trim().length() > 100)
                                        txtListChildShowAll.setVisibility(View.VISIBLE);
                                    else
                                        txtListChildShowAll.setVisibility(View.GONE);
                                    hScroll.scrollTo(v.getLeft(), v.getTop());
                                }

                            }
                        });
                    }
                    btnSubTask.setId(i + 1);
                    llSubTask.addView(btnSubTask, params);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            llSubTask.setVisibility(View.GONE);
        txtListChildShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CharSequence content = (CharSequence) v.getTag(R.string.key_contect);
                String id = (String) v.getTag(R.string.key_id);
                Intent iFullTask = new Intent(_context, TaskFullScreen1.class);
                iFullTask.putExtra("content", content);
                iFullTask.putExtra("id", id);
                _context.startActivity(iFullTask);
            }
        });
        return convertView;
    }

    private void beginDownload(Attachment attachment) {
        String title = "CN File Download";
        Downloader.downloadAttachment(attachment, title, _context);
    }
    public class FishNameComparator implements Comparator<SubTask> {
        public int compare(SubTask left, SubTask right) {
            if (left.getSubTaskSequence() > right.getSubTaskSequence())
                return 1;
            else if (left.getSubTaskSequence() < right.getSubTaskSequence())
                return -1;
            else
                return 0;
        }
    }

    private CourseTaskLinkDetail parseLinkResponse(JSONObject response) {
        CourseTaskLinkDetail task = new CourseTaskLinkDetail();
        JSONObject jsonMain = null;
        try {
            jsonMain = response;
            if (jsonMain.getBoolean("result")) {
                JSONObject jsData = jsonMain.getJSONObject("data");
                task.setId(jsData.getString("id").toString().trim());
                task.setCourseId(jsData.getString("course_id").toString().trim());
                task.setText(jsData.getString("text").toString().trim());
                task.setTitle(jsData.getString("title").toString().trim());
                task.setcTime(jsData.getDouble("ctime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return task;
    }

    private RadioButton getRadioButton(String title) {
        RadioButton button = new RadioButton(_context);
        button.setBackgroundResource(R.drawable.group_fragment_button);
        button.setButtonDrawable(new StateListDrawable()); //removes circle part of button
        button.setText(title);
        button.setGravity(Gravity.CENTER);

        return button;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public String getGroup(int groupPosition) {
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
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosition).isSelectable();
    }

    private String getHtmlString(CharSequence formattedHtml) {
        String script = "<script type='text/javascript'>" +
                "window.onload = function() {" +
                "var anchors = document.getElementsByTagName('a');" +
                "for (var i = 0; i < anchors.length ; i++) {" +
                "anchors[i].addEventListener('click',clicked,false);" +
                "}" +
                "function clicked(e) {" +
                "if (e.currentTarget.tagName.toLowerCase() == 'a') {" +
                "var elem = e.currentTarget;" +
                "e.preventDefault();" +
                "var attributes = {};" +
                "for (var i = 0; i < elem.attributes.length; i++) {" +
                "var name = elem.attributes.item(i).nodeName;" +
                "var value = elem.attributes.item(i).nodeValue;" +
                "attributes[ name ] = value;" +
                "}" +
                "var link;" +
                "if (attributes['data-type'] == 'taskactionlink') {" +
                "link = 'cnapp://?';" +
                "for (var key in attributes) {" +
                "link = link+key+'='+attributes[key]+'&';" +
                "}" +
                "link = link.replace('#', '');" +
                "} else {" +
                "link = elem.href;" +
                "}" +
                "window.location.href = link;" +
                "return false;" +
                "}" +
                "}" +
                "}" +
                "</script>";
        String htmlString = "<html><head><title></title></head><body style=\"background:transparent; margin:0px; \"><b></b><br /><br /> <b>" + formattedHtml + "</b><br /> " + script + "</body></html>";
        Log.e("test", htmlString);
        return htmlString;
    }
}
