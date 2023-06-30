package it.colorgram.android.updates;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.util.Pair;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import it.colorgram.android.ColorConfig;
import it.colorgram.android.StoreUtils;
import it.colorgram.android.entities.HTMLKeeper;
import it.colorgram.android.http.FileDownloader;
import it.colorgram.android.magic.OWLENC;

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
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        new Thread() {
            @Override
            public void run() {
                try {
                    //String url = String.format("https://app.colorgram.org/get_changelogs?lang=%s&version=%s", locale.getLanguage(), BuildVars.BUILD_VERSION);
                    //JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String stableURL = "https://api.github.com/repos/Pierlu096/Color/releases/latest";
                    String releasePreviewURL = "https://api.github.com/repos/Pierlu096/ColorBeta/releases/latest";
                    HttpURLConnection connection = (HttpURLConnection) new URI(stableURL).toURL().openConnection();
                    if (ColorConfig.betaUpdates) {
                        connection = (HttpURLConnection) new URI(releasePreviewURL).toURL().openConnection();
                    }
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", getRandomUserAgent());
                    connection.setRequestProperty("Content-Type", "application/json");

                    StringBuilder textBuilder = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        int c;
                        while ((c = reader.read()) != -1) {
                            textBuilder.append((char) c);
                        }
                    }

                    JSONObject obj = new JSONObject(textBuilder.toString());
                    String changelog_text = obj.getString("body");

                    String translateURI;
                    HttpURLConnection connectionTranslate;

                    translateURI = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=";
                    translateURI += Uri.encode(LocaleController.getInstance().getCurrentLocale().getLanguage());
                    translateURI += "&dt=t&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&kc=7&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&q=";

                    connectionTranslate = (HttpURLConnection) new URI(translateURI).toURL().openConnection();
                    connectionTranslate.setRequestMethod("GET");
                    connectionTranslate.setRequestProperty("User-Agent", getRandomUserAgent());
                    connectionTranslate.setRequestProperty("Content-Type", "application/json");

                    StringBuilder textBuilder2 = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        int c;
                        while ((c = reader.read()) != -1) textBuilder2.append((char) c);
                    }

                    JSONTokener tokener = new JSONTokener(textBuilder2.toString());

                    JSONArray array = new JSONArray(tokener);
                    JSONArray array1 = array.getJSONArray(0);

                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < array1.length(); ++i) {
                        String blockText = array1.getJSONArray(i).getString(0);
                        if (blockText != null && !blockText.equals("null")) result.append(blockText);
                    }

                    if (changelog_text.length() > 0 && changelog_text.charAt(0) == '\n') result.insert(0, "\n");
                    changelog_text = result.toString();
                    if (!changelog_text.equals("null")) {
                        String finalChangelog_text = changelog_text;
                        AndroidUtilities.runOnUIThread(() -> changelogCallback.onSuccess(HTMLKeeper.htmlToEntities(finalChangelog_text, null, true, false)));
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

    private static final String[] userAgents = {
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36,gzip(gfe)",
            "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)",
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 7 Build/MRA51D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.133 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/600.8.9 (KHTML, like Gecko) Version/8.0.8 Safari/600.8.9",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/44.0.2403.89 Chrome/44.0.2403.89 Safari/537.36",
            "Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-G920F Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.0 Chrome/38.0.2125.102 Mobile Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; rv:40.0) Gecko/20100101 Firefox/40.0"
    };

    public static String getRandomUserAgent() {
        int randomNum = Utilities.random.nextInt(userAgents.length);
        return userAgents[randomNum];
    }

    public static boolean isNewVersion(String... v) {
        if (v.length != 2) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            v[i] = v[i].replaceAll("[^0-9]+", "");
            if (Integer.parseInt(v[i]) <= 999) {
                v[i] += "0";
            }
        }
        return Integer.parseInt(v[0]) < Integer.parseInt(v[1]);
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
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        boolean betaMode = ColorConfig.betaUpdates && !StoreUtils.isDownloadedFromAnyStore();
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    int code = pInfo.versionCode / 10;
                    String abi = "unknown";
                    switch (pInfo.versionCode % 10) {
                        case 1:
                        case 3:
                            abi = "arm-v7a";
                            break;
                        case 2:
                        case 4:
                            abi = "x86";
                            break;
                        case 5:
                        case 7:
                            abi = "arm64-v8a";
                            break;
                        case 6:
                        case 8:
                            abi = "x86_64";
                            break;
                        case 0:
                        case 9:
                            abi = "universal";
                            break;
                    }
                    try {
                        String stableURL = "https://api.github.com/repos/Pierlu096/Color/releases/latest";
                        String releasePreviewURL = "https://api.github.com/repos/Pierlu096/ColorBeta/releases/latest";
                        HttpURLConnection connection = (HttpURLConnection) new URI(stableURL).toURL().openConnection();
                        if (betaMode) {
                            connection = (HttpURLConnection) new URI(releasePreviewURL).toURL().openConnection();
                        }
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("User-Agent", getRandomUserAgent());
                        connection.setRequestProperty("Content-Type", "application/json");

                        StringBuilder textBuilder = new StringBuilder();
                        try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            int c;
                            while ((c = reader.read()) != -1) {
                                textBuilder.append((char) c);
                            }
                        }

                        JSONObject update = new JSONObject(textBuilder.toString());
                        JSONArray arr = update.getJSONArray("assets");

                        if (arr.length() == 0) {
                            return;
                        }
                        for (int i = 0; i < arr.length(); i++) {
                            String cpu = abi;
                            String link;
                            String downloadURL = link = arr.getJSONObject(i).getString("browser_download_url");
                            if (ApplicationLoader.isHuaweiStoreBuild()) {
                                downloadURL = link.replace("Colorgram-", "Colorgram-Huawei-");
                            }
                            String size = AndroidUtilities.formatFileSize(arr.getJSONObject(i).getLong("size"));
                            if (link.contains("arm64-v8a") && Objects.equals(cpu, "arm64-v8a") ||
                                    link.contains("armeabi-v7a") && Objects.equals(cpu, "armeabi-v7a") ||
                                    link.contains("x86") && Objects.equals(cpu, "x86") ||
                                    link.contains("x86_64") && Objects.equals(cpu, "x86_64") ||
                                    link.contains("universal") && Objects.equals(cpu, "universal")){
                                break;
                            }
                        }
                        String version = update.getString("name");

                        if (isNewVersion(BuildConfig.BUILD_VERSION_STRING, version)) {
                            AndroidUtilities.runOnUIThread(() -> {
                                OWLENC.UpdateAvailable updateAvailable = loadUpdate(update);
                                ColorConfig.saveUpdateStatus(1);
                                updateAvailable.setPlayStoreMetaData(psAppUpdateInfo);
                                AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(updateAvailable));
                            });
                        } else {
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                    String url = String.format(locale, "https://api.github.com/repos/Pierlu096/Colorgram/releases/latest", locale.getLanguage(), betaMode, URLEncoder.encode(abi, StandardCharsets.UTF_8));
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String update_status = obj.getString("status");
                    if (update_status.equals("no_updates")) {
                        AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                    } else {
                        int remoteVersion = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : (psAppUpdateInfo != null ? PlayStoreAPI.getVersionCode(psAppUpdateInfo) : obj.getInt("version"));
                        if (remoteVersion > code) {
                            OWLENC.UpdateAvailable updateAvailable = loadUpdate(obj);
                            ColorConfig.saveUpdateStatus(1);
                            updateAvailable.setPlayStoreMetaData(psAppUpdateInfo);
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(updateAvailable));
                        } else {
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                        }
                    } */
                } catch (Exception e) {
                    AndroidUtilities.runOnUIThread(() -> updateCallback.onError(e));
                }
            }
        }.start();
    }

    public static class UpdateNotAvailable {
    }

    public static OWLENC.UpdateAvailable loadUpdate(JSONObject obj) {
        return new OWLENC.UpdateAvailable(obj);
    }

    public static boolean isAvailableUpdate() {
        boolean updateValid = false;
        try {
            if(ColorConfig.updateData.isPresent()) {
                OWLENC.UpdateAvailable update = ColorConfig.updateData.get();
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
