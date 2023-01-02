package it.owlgram.android.updates;

import android.app.Activity;

import it.owlgram.android.StoreUtils;
import it.owlgram.android.helpers.FileDownloadHelper;

public class AppDownloader {

    public static int getDownloadProgress() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.getDownloadProgress();
        } else {
            return FileDownloadHelper.getDownloadProgress(UpdateManager.apkFile());
        }
    }

    public static boolean isRunningDownload() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.isRunningDownload();
        } else {
            return FileDownloadHelper.isRunningDownload(UpdateManager.apkFile());
        }
    }

    public static boolean updateDownloaded() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.updateDownloaded();
        } else {
            return UpdateManager.updateDownloaded();
        }
    }

    public static long downloadedBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.downloadedBytes();
        } else {
            return FileDownloadHelper.downloadedBytes(UpdateManager.apkFile());
        }
    }

    public static long totalBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.totalBytes();
        } else {
            return FileDownloadHelper.totalBytes(UpdateManager.apkFile());
        }
    }

    public static void setListener(String id, UpdateListener listener) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.addListener(id, listener);
        } else {
            FileDownloadHelper.addListener(id, listener);
        }
    }

    public static void installUpdate(Activity activity) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.installUpdate();
        } else {
            UpdateManager.installUpdate(activity);
        }
    }

    public interface UpdateListener {
        void onPreStart();

        void onProgressChange(int percentage, long downBytes, long totBytes);

        void onFinished();
    }
}
