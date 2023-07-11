package it.colorgram.android.updates;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.core.util.Pair;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import it.colorgram.android.ColorConfig;
import it.colorgram.android.StoreUtils;
import it.colorgram.android.entities.HTMLKeeper;
import it.colorgram.android.http.FileDownloader;
import it.colorgram.android.http.StandardHTTPRequest;
import it.colorgram.android.magic.COLORENC;

public class UpdateManager {

    public static boolean checkingForChangelogs = false;

    public static void isDownloadedUpdate(UpdateUICallback updateUICallback) {
        new Thread() {
            @Override
            public void run() {
                boolean result = AppDownloader.updateDownloaded();
                AndroidUtilities.runOnUIThread(() -> updateUICallback.onResult(result));
            }
        }.start();
    }

    public interface UpdateUICallback {
        void onResult(boolean result);
    }

    public static String getUpdatesChannel() {
        return ColorConfig.betaUpdates ? "Release Preview" : LocaleController.getString("Stable", R.string.Stable);
    }

    public static void getChangelogs(ChangelogCallback changelogCallback) {
        if (checkingForChangelogs) return;
        checkingForChangelogs = true;
        boolean betaMode = ColorConfig.betaUpdates && !StoreUtils.isDownloadedFromAnyStore();
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        new Thread() {
            @Override
            public void run() {
                try {
                    String url = betaMode ? String.format("https://raw.githubusercontent.com/Pierlu096/colorgramserver/main/Updates/Previews/updates_info_%s.json", locale.getLanguage()) : String.format("https://raw.githubusercontent.com/Pierlu096/colorgramserver/main/Updates/updates_info_%s.json", locale.getLanguage());
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String changelog_text = obj.getString("changelog");
                    if (!changelog_text.equals("null")) {
                        AndroidUtilities.runOnUIThread(() -> changelogCallback.onSuccess(HTMLKeeper.htmlToEntities(changelog_text, null, true, false)));
                    }
                } catch (Exception ignored) {
                } finally {
                    checkingForChangelogs = false;
                }
            }
        }.start();
    }

    public static void checkUpdates(UpdateCallback updateCallback) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.checkUpdates(new PlayStoreAPI.UpdateCheckCallback() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    checkInternal(updateCallback, appUpdateInfo);
                }

                @Override
                public void onError(Exception e) {
                    if (e instanceof PlayStoreAPI.NoUpdateException) {
                        updateCallback.onSuccess(new UpdateNotAvailable());
                    } else {
                        updateCallback.onError(e);
                    }
                }
            });
        } else {
            checkInternal(updateCallback, null);
        }
    }

    public static String getAbi() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
        switch (pInfo.versionCode % 10) {
            case 1:
            case 3:
                return "arm-v7a";
            case 2:
            case 4:
                return "x86";
            case 5:
            case 7:
                return "arm64-v8a";
            case 6:
            case 8:
                return  "x86_64";
            case 0:
            case 9:
                return "universal";
            default:
                return "unknown";
        }
    }

    private static void checkInternal(UpdateCallback updateCallback, AppUpdateInfo psAppUpdateInfo) {
        boolean betaMode = ColorConfig.betaUpdates && !StoreUtils.isDownloadedFromAnyStore();
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    String apkUrl = betaMode ? "https://api.github.com/repos/Pierlu096/ColorBeta/releases/latest" : "https://api.github.com/repos/Pierlu096/colorgramserver/releases/latest";
                    JSONObject update = new JSONObject(new StandardHTTPRequest(apkUrl).request());

                    String url = betaMode ? String.format("https://raw.githubusercontent.com/Pierlu096/colorgramserver/main/Updates/Previews/updates_info_%s.json", locale.getLanguage()) : String.format("https://raw.githubusercontent.com/Pierlu096/colorgramserver/main/Updates/updates_info_%s.json", locale.getLanguage());
                    JSONObject update1 = new JSONObject(new StandardHTTPRequest(url).request());

                    int remoteVersion = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : (psAppUpdateInfo != null ? PlayStoreAPI.getVersionCode(psAppUpdateInfo) : update.getInt("tag_name"));
                    int code = pInfo.versionCode / 10;
                    if (remoteVersion > code) {
                        AndroidUtilities.runOnUIThread(() -> {
                            COLORENC.UpdateAvailable updateAvailable = loadUpdate(update, update1);
                            ColorConfig.saveUpdateStatus(1);
                            updateAvailable.setPlayStoreMetaData(psAppUpdateInfo);
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(updateAvailable));
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                    }
                } catch (Exception e) {
                    AndroidUtilities.runOnUIThread(() -> updateCallback.onError(e));
                }
            }
        }.start();
    }

    public static class UpdateNotAvailable {
    }

    public static COLORENC.UpdateAvailable loadUpdate(JSONObject obj, JSONObject object) {
        return new COLORENC.UpdateAvailable(obj, object);
    }

    public static boolean isAvailableUpdate() {
        boolean updateValid = false;
        try {
            if(ColorConfig.updateData.isPresent()) {
                COLORENC.UpdateAvailable update = ColorConfig.updateData.get();
                if (update.version > BuildVars.BUILD_VERSION && !update.isReminded()) {
                    updateValid = true;
                }
            }
        } catch (Exception ignored){}
        return updateValid;
    }

    public interface UpdateCallback {
        void onSuccess(Object updateResult);

        void onError(Exception e);
    }

    public interface ChangelogCallback {
        void onSuccess(Pair<String, ArrayList<TLRPC.MessageEntity>> updateResult);
    }

    public static File apkFile() {
        return new File(AndroidUtilities.getCacheDir().getAbsolutePath() + "/update.apk");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteUpdate() {
        File file = apkFile();
        if (file.exists())
            file.delete();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadFailed);
    }

    public static boolean updateDownloaded() {
        boolean isCorrupted = true;
        try {
            if (ColorConfig.updateData.isPresent()) {
                if (ColorConfig.updateData.get().fileSize == apkFile().length()) {
                    isCorrupted = false;
                }
            }
        } catch (Exception ignored) {
        }
        boolean isAvailableFile = apkFile().exists() && !FileDownloader.isRunningDownload("appUpdate") && !isCorrupted;
        if (((BuildVars.BUILD_VERSION >= ColorConfig.oldDownloadedVersion && !BuildVars.IGNORE_VERSION_CHECK) || ColorConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            ColorConfig.updateData.set(null);
            ColorConfig.applyUpdateData();
            return false;
        }
        return isAvailableFile;
    }

    public static void installUpdate(Activity activity) {
        ApkInstaller.installApk(activity);
    }
}
