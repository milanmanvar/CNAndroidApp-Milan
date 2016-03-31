package com.thecn.app.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;

import java.util.List;

/**
 * Common methods for checking whether certain services are available in
 * an environment.
 */
public class ServiceChecker {

    /**
     * Check if we can read AND write from storage.
     * @return true if readable and writable
     */
    public static boolean isStorageReadableAndWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Check if we can only read from storage
     * @return true if read only
     */
    public static boolean isStorageReadOnly() {
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    /**
     * Check if storage is available
     * @return true if storage available
     */
    public static boolean isStorageAvailable() {
        return isStorageReadableAndWritable() || isStorageReadOnly();
    }

    /**
     * Check if download manager is available
     * @param context used to get package manager
     * @return true if download manager is available
     */
    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            //all versions supported have download manager available
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
