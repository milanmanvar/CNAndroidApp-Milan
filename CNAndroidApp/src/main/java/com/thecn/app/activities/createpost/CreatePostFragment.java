package com.thecn.app.activities.createpost;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.MultiplePickGalleryActivity;
import com.thecn.app.activities.course.CourseActivity_New;
import com.thecn.app.activities.homefeed.HomeFeedActivity;
import com.thecn.app.adapters.ThumbnailAdapters.PostThumbnailAdapter;
import com.thecn.app.adapters.VideoLinkAdapter;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.content.PostingGroup;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.ImageStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.images.BitmapUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.PostChangeController;
import com.thecn.app.tools.text.TextUtil;
import com.thecn.app.views.ExpandableGridView;
import com.thecn.app.views.list.ExpandableListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
* Fragment for creating a post
*/
public class CreatePostFragment extends Fragment {

    public static final String TAG = CreatePostFragment.class.getSimpleName();

    //request codes for onActivityResult
    private static final int POST_VISIBILITY_REQUEST = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    //expandable grid views for adding images dynamically
    //todo best practice this could be replaced with nested linear layouts...
    private ExpandableGridView imageGrid;
    private PostThumbnailAdapter postThumbnailAdapter;
    private Uri currentImageUri;

    //expandable listview for adding videos dynamically
    //todo best practice this could be replaced with a linear layout....
    private ExpandableListView videoList;
    private VideoLinkAdapter videoLinkAdapter;//only uses youtube video links

    private String text;
    private ArrayList<PostingGroup> mVisibleGroups;
    //groups that are sent over network but not shown to user
    private ArrayList<PostingGroup> mInvisibleGroups;

    private ArrayList<Course> mCourses; //courses and conexuses to post to
    private ArrayList<Conexus> mConexuses;
    //ids of images to add to post and youtube links to add
    private String[] imageIDs, youtubeLinks;

    private Button visibilityButton;
    private Button removeAllPhotosButton;
    private Button removeAllVideosButton;
    private Button addVideoLinkButton;

    private ImageButton submitPost;

    //used to add youtube links to the post
    private EditText videoLinkText;
    //post content
    private EditText postText;
    private Course currentCourse;

    private CallbackManager<CreatePostFragment> callbackManager;

    /**
     * Initialize data
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        callbackManager = new CallbackManager<>();

        mVisibleGroups = new ArrayList<>();
        mInvisibleGroups = new ArrayList<>();
        mCourses = new ArrayList<>();
        mConexuses = new ArrayList<>();
        getDataFromIntent();

        //used to associate post with images and youtube links
        imageIDs = new String[]{};
        youtubeLinks = new String[]{};

        postThumbnailAdapter = new PostThumbnailAdapter(this);
        if(videoLinkAdapter == null)
            videoLinkAdapter = new VideoLinkAdapter(this);
    }

    /**
     * Get {@link com.thecn.app.models.course.Course} or {@link com.thecn.app.models.conexus.Conexus} data from activity's intent
     */
    private void getDataFromIntent() {
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            Course course = (Course) intent.getSerializableExtra("COURSE");
            Conexus conexus = (Conexus) intent.getSerializableExtra("CONEXUS");

            //add a course or conexus to groups if was in intent
            if (course != null) {
                currentCourse = course;
                mCourses.add(course);
                mInvisibleGroups.add(PostingGroup.course);
            }
            else if (conexus != null) {
                mConexuses.add(conexus);
                mInvisibleGroups.add(PostingGroup.conexus);
            }
            else mVisibleGroups.add(PostingGroup.allMembers);
        }
    }

    /**
     * Gets references to views, sets on click listeners, initializes visibility text, and sets up adapters.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //set appropriate view states

        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        postText = (EditText) view.findViewById(R.id.post_text);

        submitPost = (ImageButton) view.findViewById(R.id.post_button);
        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitPostClicked();
            }
        });

        ImageButton backButton = (ImageButton) view.findViewById(R.id.cancel_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        visibilityButton = (Button) view.findViewById(R.id.content_toggle);
        visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushPostVisibilityActivity();
            }
        });
        setVisibilityButtonText();

        ImageButton cameraButton = (ImageButton) view.findViewById(R.id.add_picture);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushTakePhotoActivity();
            }
        });

        Button galleryButton = (Button) view.findViewById(R.id.add_from_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushGalleryActivity();
            }
        });

        removeAllPhotosButton = (Button) view.findViewById(R.id.remove_photos_button);
        removeAllPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postThumbnailAdapter.removeAll();
            }
        });
        postThumbnailAdapter.setRemoveAllPhotosButton(removeAllPhotosButton);

        removeAllVideosButton = (Button) view.findViewById(R.id.remove_videos_button);
        removeAllVideosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoLinkAdapter.removeAll();
            }
        });
        videoLinkAdapter.setRemoveAllVideosButton(removeAllVideosButton);

        videoLinkText = (EditText) view.findViewById(R.id.add_videos_text);
        videoLinkAdapter.setLinkEditText(videoLinkText);

        addVideoLinkButton = (Button) view.findViewById(R.id.add_videos_button);
        addVideoLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String linkText = videoLinkText.getText().toString();
                if (linkText.length() > 0)
                    videoLinkAdapter.add(linkText);
            }
        });

        imageGrid = (ExpandableGridView) view.findViewById(R.id.image_thumbnail_view);
        int orientation = getActivity().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            imageGrid.setNumColumns(4);
        else imageGrid.setNumColumns(2);

        videoList = (ExpandableListView) view.findViewById(R.id.video_link_view);

        postThumbnailAdapter.setButtonEnabled();
        videoLinkAdapter.setButtonEnabled();

        imageGrid.setAdapter(postThumbnailAdapter);
        videoList.setAdapter(videoLinkAdapter);

        return view;
    }

    /**
     * Makes a post (with or without images) or shows an error message.
     */
    private void onSubmitPostClicked() {
        submitPost.setEnabled(false);
        //check to make sure we're online
        if (BaseStore.isOnline(getActivity())) {
            text = postText.getText().toString();

            youtubeLinks = videoLinkAdapter.getAllItems();

            if (text != null && text.length() > 0) {
                if (postThumbnailAdapter.getCount() > 0) {
                    //make post after uploading images
                    String[] paths = postThumbnailAdapter.getFilePaths();
                    ProgressDialogFragment.showUncancelable("Uploading images", getActivity());
                    new Thread(new PostWithImagesRunnable(paths, callbackManager)).start();
                } else {
                    //make post by itself, no images
                    ProgressDialogFragment.showUncancelable("Posting...", getActivity());
                    makePost();
                }
            } else {
                AppSession.showLongToast("Post text cannot be blank");
                submitPost.setEnabled(true);
            }
        } else {
            AppSession.showLongToast("No internet connection.  Try again later.");
            submitPost.setEnabled(true);
        }
    }

    /**
     * Send post data to server.  This should be called after images are uploaded.
     * Uses {@link com.thecn.app.stores.PostStore#makePost(String, String[], String[], String[], String[], String[], com.thecn.app.stores.ResponseCallback)}
     * todo should images be uploaded as soon as the user chooses them?
     */
    private void makePost() {
        //show progress dialog
        ProgressDialogFragment df = ProgressDialogFragment.get(getActivity());
        if (df != null) df.setMessage("Posting...");
        else ProgressDialogFragment.showUncancelable("Posting...", getActivity());

        //get the data that pertains to the post
        String[] courseIDs = Course.getIds(mCourses);
        String[] conexusIDs = Conexus.getIds(mConexuses);
        String[] groupIDs = new String[mVisibleGroups.size() + mInvisibleGroups.size()];

        String[] visibleGroupIDs = PostingGroup.getIds(mVisibleGroups);
        String[] invisibleGroupIDs = PostingGroup.getIds(mInvisibleGroups);

        copyInto(visibleGroupIDs, groupIDs, 0);
        copyInto(invisibleGroupIDs, groupIDs, visibleGroupIDs.length);

        PostStore.makePost(text,
                courseIDs,
                conexusIDs,
                groupIDs,
                imageIDs,
                youtubeLinks,
                new MakePostResponse(callbackManager)
        );
    }

    /**
     * Used when a request making a post gets a response.
     */
    private  class MakePostResponse extends CallbackManager.NetworkCallback<CreatePostFragment> {
        public MakePostResponse(CallbackManager<CreatePostFragment> manager) {
            super(manager);
        }

        @Override
        public void onImmediateResponse(final JSONObject response) {
            if (wasSuccessful()) {
                Post post = PostStore.getData(response);
                if (post == null) {
                    AppSession.showLongToast("Post submitted, but couldn't retrieve data");
                    return;
                }

                AppSession.showLongToast("Post submitted");
                //broadcast added post
                PostChangeController.sendAddedBroadcast(post);
            } else {
                AppSession.showLongToast("Error submitting post");
            }
        }

        @Override
        public void onImmediateError(VolleyError error) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeBefore(CreatePostFragment object) {
            ProgressDialogFragment.dismiss(object.getActivity());

            if (wasSuccessful()) {
                object.getActivity().finish();

                if(currentCourse!=null){

                    boolean check = false;
                    for(int i=0;i<mCourses.size();i++) {
                        if(mCourses.get(i).getId().equals(currentCourse.getId())){
                            check = true;
                            break;
                        }

                    }

                    if(check) {
                        final Intent intent = new Intent(getActivity(), CourseActivity_New.class);
                        intent.putExtra("course", currentCourse);
                        intent.putExtra("checkPost",true);
                        startActivityForResult(intent,255);
                    }else{
                        startActivity(new Intent(getActivity(), HomeFeedActivity.class));
                    }

                }else{
                    startActivity(new Intent(getActivity(), HomeFeedActivity.class));
                }

            }
        }
    }

    /**
     * Used to upload images one by one to the server
     * todo instead should images be uploaded as soon as user chooses them?
     * Static so that no fragment/activity references are kept asynchronously.
     */
    private static class PostWithImagesRunnable implements Runnable {

        //max dimensions of an image sent to server
        private static final int MAX_WIDTH = 1024;
        private static final int MAX_HEIGHT = 768;

        private CallbackManager<CreatePostFragment> callbackManager;
        //file paths to images
        private String[] paths;

        public PostWithImagesRunnable(String[] paths, CallbackManager<CreatePostFragment> manager) {
            this.paths = paths;
            callbackManager = manager;
        }


        @Override
        public void run() {
            final ArrayList<String> ids = new ArrayList<>();
            final int numImages = paths.length;
            boolean error = false;

            JSONObject response = null;

            //upload images one by one
            for (int i = 0; !error && i < paths.length; i++) {
                final int index = i;

                //update progress on main thread
                callbackManager.addCallback(new CallbackManager.Callback<CreatePostFragment>() {
                    @Override
                    public void execute(CreatePostFragment object) {
                        ProgressDialogFragment pdf = ProgressDialogFragment.get(object.getActivity());
                        if (pdf == null) return;

                        pdf.setMessage("Uploading images... " + index + "/" + numImages);
                    }
                });

                final String filePath = paths[i];

                try {
                    response = ImageStore.uploadImage(
                            BitmapUtil.insideFitBitmapFromFile(filePath, MAX_WIDTH, MAX_HEIGHT));
                } catch (Exception e) {
                    error = true;
                }

                if (!error && StoreUtil.success(response)) {
                    try {
                        ids.add(response.getJSONObject("data").getString("id"));
                    } catch (JSONException e) {
                        error = true;
                    }
                } else {
                    error = true;
                }
            }

            //return ids to the fragment so they can be used to associate with the post
            final String[] finalIDs = ids.toArray(new String[ids.size()]);
            final boolean finalError = error;
            callbackManager.addCallback(new CallbackManager.Callback<CreatePostFragment>() {
                @Override
                public void execute(CreatePostFragment object) {
                    if (finalError) {
                        ProgressDialogFragment.dismiss(object.getActivity());
                        AppSession.showLongToast("Error uploading images");
                        object.submitPost.setEnabled(true);
                    } else {
                        //if successful, make post with associated image ids
                        object.imageIDs = finalIDs;
                        object.makePost();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Copies an array from the starting index into another array.
     *
     * todo There are already Java utility methods for this....
     * @param source array to copy from
     * @param dest array to copy into
     * @param startIndex where to start in the destination array
     */
    private void copyInto(String[] source, String[] dest, int startIndex) {
        int i = 0;
        int j = startIndex;

        while (j < dest.length && i < source.length) {
            dest[j] = source[i];

            i++;
            j++;
        }
    }

    /**
     * Pushes {@link PostVisibilityActivity} onto Task stack.
     * Pass in all current data about visibility.
     */
    private void pushPostVisibilityActivity() {
        Intent intent = new Intent(getActivity(), PostVisibilityActivity.class);
        intent.putExtra("GROUPS", mVisibleGroups);
        intent.putExtra("COURSES", mCourses);
        intent.putExtra("CONEXUSES", mConexuses);
        startActivityForResult(intent, POST_VISIBILITY_REQUEST);
    }

    /**
     * Push {@link com.thecn.app.activities.MultiplePickGalleryActivity} onto Task stack.
     * Pass in all current data on image filepaths, so they can be checked.
     */
    private void pushGalleryActivity() {
        Intent intent = new Intent(getActivity(), MultiplePickGalleryActivity.class);
        ArrayList<String> filePathList = new ArrayList<>(Arrays.asList(postThumbnailAdapter.getFilePaths()));
        intent.putStringArrayListExtra("FILE_PATHS", filePathList);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    /**
     * Push an activity that can handle {@link android.provider.MediaStore}'s ACTION_IMAGE_CAPTURE action
     * Shows errors if camera is not available or there are too many images already.
     */
    private void pushTakePhotoActivity() {
        if (postThumbnailAdapter.getCount() < 15) {
            PackageManager pkgManager = getActivity().getPackageManager();

            //check for camera
            if (pkgManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(pkgManager) != null) {
                    currentImageUri = getTimestampUri();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                AppSession.showLongToast("Unable to use the camera.  Permission must be granted.");
            }
        } else {
            AppSession.showLongToast("Cannot add more than 15 photos");
        }
    }

    /**
     * Used to perform actions when data is returned from other activities
     * started by this fragment.
     * @param requestCode visibility, camera, or gallery
     * @param resultCode tells whether operation successful
     * @param data data passed back
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == POST_VISIBILITY_REQUEST) {
                //update visibility

                mVisibleGroups = (ArrayList<PostingGroup>) data.getSerializableExtra("V_GROUPS");
                mInvisibleGroups = (ArrayList<PostingGroup>) data.getSerializableExtra("INV_GROUPS");
                mCourses = (ArrayList<Course>) data.getSerializableExtra("COURSES");
                mConexuses = (ArrayList<Conexus>) data.getSerializableExtra("CONEXUSES");

                setVisibilityButtonText();

            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //add pic from camera
                addImageFromCurrentUri();
                postThumbnailAdapter.setButtonEnabled();
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                //add pics from file
                postThumbnailAdapter.removeAll();
                ArrayList<String> paths = data.getStringArrayListExtra("FILE_PATHS");

                for (String path : paths) postThumbnailAdapter.add(path);
                postThumbnailAdapter.setButtonEnabled();
            }
        }
    }

    /**
     * Sets visibility button display.  Used to show a summary of the visibility settings
     * chosen by the user.
     */
    private void setVisibilityButtonText() {
        String text = "";

        boolean groupExists = mVisibleGroups.size() > 0;
        boolean onlyOneCourseOrConexus = false;

        if (!groupExists) {

            //if only one course or conexus, show its name in the text
            //and nothing else

            if (mCourses.size() == 1 && mConexuses.size() == 0) {

                text = mCourses.get(0).getName();
                onlyOneCourseOrConexus = true;

            } else if (mConexuses.size() == 1 && mCourses.size() == 0) {

                text = mConexuses.get(0).getName();
                onlyOneCourseOrConexus = true;
            }
        }

        if (!onlyOneCourseOrConexus) {

            //if groups exist or there are more than one of either courses
            //or conexus, then list the groups by name and list the courses/conexus
            //by number

            int groupNameLastPos = mVisibleGroups.size() - 1;

            for (int i = 0; i < groupNameLastPos; i++)
                text += mVisibleGroups.get(i).getName() + ", ";

            if (groupExists) text += mVisibleGroups.get(groupNameLastPos).getName();

            int numCourses = mCourses.size();
            boolean courseExists = mCourses.size() > 0;

            if (courseExists) {
                if (groupExists) {
                    if (mVisibleGroups.size() > 1) {
                        text += ",\n";
                    } else {
                        text += ", ";
                    }
                }

                text += Integer.toString(numCourses) + " Course";
                if (numCourses > 1) text += "s";
            }

            int numConexuses = mConexuses.size();

            if (mConexuses.size() > 0) {
                if (groupExists || courseExists) text += ", ";

                text += Integer.toString(numConexuses) + " Conexus";
                if (numConexuses > 1) text += "es";
            }
        }

        visibilityButton.setText(text);
    }

    /**
     * Get string form of filepath
     */
    private void addImageFromCurrentUri() {
        String filePath = getPath(currentImageUri);
        if (filePath != null) {
            postThumbnailAdapter.add(filePath);
        }
    }

    /**
     * get filepath from a {@link android.database.Cursor} object using a {@link android.net.Uri}
     * @param uri path to an image
     * @return string form of path to an image.
     */
    private String getPath(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }

        return null;
    }

    /**
     * Generate a timestamp uri to pass to the image capture activity so that it saves the file
     * to that URI.
     * @return the uri formed from the timestamp
     */
    private Uri getTimestampUri() {
        String timeStamp = TextUtil.getTimeStamp();
        String imageFileName = "THECN_IMG_CAPTURE_" + timeStamp + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, imageFileName);

        return getActivity()
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
