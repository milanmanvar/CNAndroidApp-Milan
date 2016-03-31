package com.thecn.app.activities.picturechooser;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.profile.Avatar;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.stores.ImageStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.images.BitmapUtil;
import com.thecn.app.tools.images.ImageUtil;

import org.json.JSONObject;

/**
* Used to upload a bitmap to the server, either to be used
*/
public class UploadTask extends AsyncTask<Void, Void, Void> {

    //used for sending broadcasts.
    public static final String AVATAR_SUCCESS = "profile_picture_intent";
    public static final String BANNER_SUCCESS = "banner_success";
    public static final String SUCCESS_TAG = "was_successful";

    //rect used to crop bitmap from file
    private final BitmapUtil.SectionRectF mRect;
    //file path to image
    private String mFilePath;

    //limit of the size of the bitmap.
    private static final int DIM_LIMIT = 1024;

    private int type;

    //display strings
    private static final String UPLOAD_SUCCESS = "Upload complete!";
    private static final String UPLOAD_SUCCESS_MESSAGE = "Profile picture updated.";
    private static final String UPLOAD_FAILURE = "Upload failed";
    private static final String UPLOAD_FAILURE_MESSAGE = "Could not upload picture.";
    private static final String FILE_ERROR = "Image no longer exists.";
    private static final String BITMAP_ERROR = "Could not get image from file.";

    //source id of bitmap uploaded that was used to create a cropped version
    private String sourceID;

    public UploadTask(String filePath, int type, BitmapUtil.SectionRectF sectionRectF) {
        mFilePath = filePath;
        mRect = sectionRectF;
        this.type = type;
        mRect.maxOutputWidth = DIM_LIMIT;
        mRect.maxOutputHeight = DIM_LIMIT;
    }

    /**
     * Checks for errors.  If none, upload the image and then crop it (using the api).
     * Callbacks handle the rest.
     */
    @Override
    protected Void doInBackground(Void... voids) {
        if (!ImageUtil.isFilePathValid(mFilePath)) {
            pushNotification(UPLOAD_FAILURE, FILE_ERROR);
            return null;
        }

        Bitmap bitmap = BitmapUtil.insideFitBitmapFromFile(mFilePath, mRect.maxOutputWidth, mRect.maxOutputHeight);

        if (bitmap == null) {
            pushNotification(UPLOAD_FAILURE, BITMAP_ERROR);
            return null;
        }

        float newWidth = (float) bitmap.getWidth();
        float newHeight = (float) bitmap.getHeight();
        float originalWidth = (float) mRect.originalWidth;
        float originalHeight = (float) mRect.originalHeight;

        //get new coords in proportion to new bitmap size
        mRect.left = newWidth * mRect.left / originalWidth;
        mRect.top = newHeight * mRect.top / originalHeight;
        mRect.right = newWidth * mRect.right / originalWidth;
        mRect.bottom = newHeight * mRect.bottom / originalHeight;

        boolean correctDimensions = BitmapUtil.dimensionsWithinBounds(
                (int) mRect.left,
                (int) mRect.top,
                (int) (mRect.right - mRect.left),
                (int) (mRect.bottom - mRect.top),
                bitmap
        );

        if (!correctDimensions) {
            pushNotification(UPLOAD_FAILURE, BITMAP_ERROR);
            return null;
        }

        //set the new bitmap dimensions
        mRect.originalWidth = bitmap.getWidth();
        mRect.originalHeight = bitmap.getHeight();

        JSONObject response = ImageStore.uploadImage(bitmap);

        if (StoreUtil.success(response)) {
            cropPicture(response);
        } else {
            showFailure();
        }

        return null;
    }

    /**
     * Use uploaded bitmap to create a cropped version.
     * Uses {@link com.thecn.app.stores.ImageStore#createThumbnail(String, boolean, com.thecn.app.tools.images.BitmapUtil.SectionRectF, com.thecn.app.stores.ResponseCallback)}
     * @param json data retrieved after uploading the unchanged bitmap.
     */
    private void cropPicture(JSONObject json) {
        sourceID = getID(json);
        if (sourceID == null) {
            showFailure();
            return;
        }

        ImageStore.createThumbnail(sourceID, true, mRect, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                if (StoreUtil.success(response)) {
                    sendToServer(response);
                } else {
                    showFailure();
                }
            }

            @Override
            public void onError(VolleyError error) {
                StoreUtil.showExceptionMessage(error);
            }
        });
    }

    /**
     * Communicate to the server what to do with the uploaded picture.
     * Either set it as the user's profile picture or set it as the user's banner.
     * @param json data that contains the id of the cropped picture.
     */
    private void sendToServer(JSONObject json) {
        final String id = getID(json);
        if (id == null) {
            showFailure();
            return;
        }

        if (type == PictureChooseActivity.TYPE_AVATAR) {
            changeProfilePic(id);
        } else if (type == PictureChooseActivity.TYPE_BANNER) {
            changeBannerPic(id);
        }
    }

    /**
     * Set the banner picture for the user.
     * Uses {@link com.thecn.app.stores.UserStore#changeBanner(String, String, com.thecn.app.stores.ResponseCallback)}
     * @param id id of the banner picture.
     */
    private void changeBannerPic(String id) {
        UserStore.changeBanner(sourceID, id, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                if (StoreUtil.success(response)) {
                    try {
                        UserProfile profile = AppSession.getInstance().getUser().getUserProfile();
                        String bannerURL = response.getJSONObject("data").getJSONObject("profile").getString("theme_home_banner_url");
                        //set the local banner url
                        synchronized (AppSession.getInstance().userLock) {
                            profile.setBannerURL(bannerURL);
                        }
                    } catch (Exception e) {
                        //whoops
                    }

                    showSuccess(BANNER_SUCCESS);
                } else {
                    showFailure();
                }
            }

            @Override
            public void onError(VolleyError error) {
                StoreUtil.showExceptionMessage(error);
            }
        });
    }

    /**
     * Changes the user's profile picture on the server.
     * Uses {@link com.thecn.app.stores.UserStore#changeProfilePicture(String, com.thecn.app.stores.ResponseCallback)}
     * @param id id of the pic to use as a profile picture
     */
    private void changeProfilePic(String id) {
        UserStore.changeProfilePicture(id, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                if (StoreUtil.success(response)) {
                    String id = getID(response);
                    showSuccess(AVATAR_SUCCESS);

                    if (id != null) {
                        getAvatar(id);
                    }
                } else {
                    showFailure();
                }
            }

            @Override
            public void onError(VolleyError error) {
                StoreUtil.showExceptionMessage(error);
            }
        });
    }

    /**
     * Refreshes the user's avatar object (retrieves from server).
     * @param id id of the avatar to retrieve
     */
    private void getAvatar(String id) {
        if (!AppSession.getInstance().isLoggedIn()) return;

        UserStore.getUserAvatar(id, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                if (StoreUtil.success(response)) {
                    setAvatar(response);
                }
            }

            @Override
            public void onError(VolleyError error) {
                //do nothing
            }
        });
    }

    /**
     * Sets the local user's avatar object in the AppSession.
     * @param json object that contains the id and url of the avatar.
     */
    private void setAvatar(JSONObject json) {
        String id = getID(json);
        String viewURL = getViewURL(json);

        if (viewURL == null || id == null) return;

        Avatar avatar = new Avatar(id, viewURL);
        AppSession.getInstance().setUserAvatar(avatar);
    }

    /**
     * Gets an id object from json
     * @param json the object to get the id from
     * @return the id of a picture
     */
    private String getID(JSONObject json) {
        try {
            return json.getJSONObject("data").getString("id");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the view url of a picture from json
     * @param json json object to get the String from
     * @return the view url of a picture
     */
    private String getViewURL(JSONObject json) {
        try {
            return json.getJSONObject("data").getString("view_url");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Push a notification that tells the user that the image was successfully uploaded.
     * @param intentString the intent filter used to send a broadcast
     */
    private void showSuccess(String intentString) {
        pushNotification(UPLOAD_SUCCESS, UPLOAD_SUCCESS_MESSAGE);
        Intent intent = new Intent(intentString);
        intent.putExtra(SUCCESS_TAG, true);
        LocalBroadcastManager.getInstance(AppSession.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Push a notification that tells the user that the image was not successfully uploaded.
     */
    private void showFailure() {
        pushNotification(UPLOAD_FAILURE, UPLOAD_FAILURE_MESSAGE);
        Intent intent = new Intent(AVATAR_SUCCESS);
        intent.putExtra(SUCCESS_TAG, false);
        AppSession.getInstance().getApplicationContext().sendBroadcast(intent);
    }

    /**
     * Push a notification with specified title and text.
     * @param title the title of the notification
     * @param text the content of the notification
     */
    private void pushNotification(String title, String text) {

        Context context = AppSession.getInstance().getApplicationContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_cn_outline)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
