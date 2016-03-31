package com.thecn.app.tools.network;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.stores.AttachmentStore;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.ServiceChecker;
import com.thecn.app.tools.text.TextUtil;

import org.json.JSONObject;

/**
 * Utility methods for downloading files.
 */
public class Downloader {

    /**
     * Uses an attachment to download a file from the CN website.
     * @param attachment attachment to download
     * @param title title of download manager notification
     * @param context used to get download manager
     */
    public static void downloadAttachment(Attachment attachment, final String title, final Context context) {
        String contentID = attachment.getId();
        if (contentID == null || contentID.length() < 1) {
            AppSession.showDataLoadError("attachment");
            return;
        }

        if (!ServiceChecker.isDownloadManagerAvailable(context)) {
            AppSession.showLongToast("Cannot access download manager");
            return;
        }

        AppSession.showLongToast("Downloading...");

        if (TextUtil.isNullOrEmpty(attachment.getName()) ||
            TextUtil.isNullOrEmpty(attachment.getExtension())) {

            //get attachment data, then download through manager
            AttachmentStore.getAttachment(attachment.getId(), new ResponseCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    Attachment newAttachment = AttachmentStore.getData(response);
                    if (newAttachment == null || newAttachment.getId() == null
                        || newAttachment.getName() == null || newAttachment.getExtension() == null) {

                        AppSession.showDataLoadError("attachment");
                        return;
                    }

                    downloadThroughManager(newAttachment, title, context);

                }

                @Override
                public void onError(VolleyError error) {
                    StoreUtil.showExceptionMessage(error);
                }
            });
        } else {
            //if necessary data present, just download immediately
            downloadThroughManager(attachment, title, context);
        }

    }

    /**
     * Use download manager to download an attachment
     * @param attachment attachment to download
     * @param title title of download manager notification
     * @param context used to get download manager
     */
    private static void downloadThroughManager(Attachment attachment, String title, Context context) {
        String url = getURL(attachment.getId());
        Uri uri = Uri.parse(url);

        //set up request
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(title);
        request.setDescription(attachment.getNameWithExtension());

        //account for different versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, attachment.getNameWithExtension());

        //set mime type
        String mimeType = attachment.getMimeType();
        if (mimeType != null) request.setMimeType(mimeType);

        //enqueue request
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    /**
     * Get url of attachment
     * @param contentID id of attachment
     * @return url of attachment
     */
    private static String getURL(String contentID) {
        return BaseStore.SITE_URL + "/program/attachment/view/" + contentID;
    }
}
