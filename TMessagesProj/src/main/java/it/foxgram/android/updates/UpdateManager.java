package it.foxgram.android.updates;

import android.app.Activity;
import android.content.pm.PackageInfo;

import androidx.core.util.Pair;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;

import it.foxgram.android.FoxConfig;
import it.foxgram.android.StoreUtils;
import it.foxgram.android.entities.HTMLKeeper;
import it.foxgram.android.http.FileDownloader;
import it.foxgram.android.http.StandardHTTPRequest;
import it.foxgram.android.magic.FOXENC;
import it.foxgram.android.utils.FoxTextUtils;

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

    public static boolean betaMode() {
        return FoxConfig.betaUpdates && !StoreUtils.isDownloadedFromAnyStore();
    }

    public static void getChangelogs(ChangelogCallback changelogCallback) {
        if (checkingForChangelogs) return;
        checkingForChangelogs = true;
        new Thread() {
            @Override
            public void run() {
                try {
                    String url = "https://raw.githubusercontent.com/FoxGramApp/FoxAssets/main/Updates/update_params.json";
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    boolean skipBeta = obj.getBoolean("skip_beta_check");
                    String changelog_text = obj.getString(String.format("%s-%s-changelog", FoxTextUtils.getAppLanguage(), betaMode() && !skipBeta ? "beta" : "stable"));
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

    private static void checkInternal(UpdateCallback updateCallback, AppUpdateInfo psAppUpdateInfo) {
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    String url = "https://raw.githubusercontent.com/Pierlu096/FoxAssets/main/Updates/update_params.json";
                    JSONObject update = new JSONObject(new StandardHTTPRequest(url).request());
                    boolean skipBeta = update.getBoolean("skip_beta_check");
                    int remoteVersion = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : (psAppUpdateInfo != null ? PlayStoreAPI.getVersionCode(psAppUpdateInfo) : update.getInt(String.format("%s-build_number", UpdateManager.betaMode() && !skipBeta ? "beta" : "stable")));
                    int code = pInfo.versionCode / 10;
                    if (remoteVersion > code) {
                        FOXENC.UpdateAvailable updateAvailable = loadUpdate(update);
                        FoxConfig.saveUpdateStatus(1);
                        updateAvailable.setPlayStoreMetaData(psAppUpdateInfo);
                        AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(updateAvailable));
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

    public static FOXENC.UpdateAvailable loadUpdate(JSONObject obj) throws JSONException {
        return new FOXENC.UpdateAvailable(obj);
    }

    public static boolean isAvailableUpdate() {
        boolean updateValid = false;
        try {
            if(FoxConfig.updateData.isPresent()) {
                FOXENC.UpdateAvailable update = FoxConfig.updateData.get();
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
            if (FoxConfig.updateData.isPresent()) {
                if (FoxConfig.updateData.get().fileSize == apkFile().length()) {
                    isCorrupted = false;
                }
            }
        } catch (Exception ignored) {
        }
        boolean isAvailableFile = apkFile().exists() && !FileDownloader.isRunningDownload("appUpdate") && !isCorrupted;
        if (((BuildVars.BUILD_VERSION >= FoxConfig.oldDownloadedVersion && !BuildVars.IGNORE_VERSION_CHECK) || FoxConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            FoxConfig.updateData.set(null);
            FoxConfig.applyUpdateData();
            return false;
        }
        return isAvailableFile;
    }

    public static void installUpdate(Activity activity) {
        ApkInstaller.installApk(activity);
    }
}
