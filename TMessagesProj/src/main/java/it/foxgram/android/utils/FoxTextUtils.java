/*
 * This is the source code of FoxGram for Android v. 3.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2023.
 */
package it.foxgram.android.utils;

import android.content.pm.PackageInfo;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.Locale;

import it.foxgram.android.FoxConfig;

public class FoxTextUtils {
    public static String appName;
    public static String appVersion = BuildConfig.BUILD_VERSION_STRING;
    public static int appBuildNumber = BuildConfig.BUILD_VERSION;

    static {
        appName = getAppName();
    }

    public static String getTitleText() {
        int currentAccount = UserConfig.selectedAccount;
        TLRPC.User selfUser = UserConfig.getInstance(currentAccount).getCurrentUser();
        switch (FoxConfig.nameType) {
            case FoxConfig.NAME_APP:
                return appName;
            case FoxConfig.NAME_DEFAULT_USER:
                return selfUser.first_name + " " + (selfUser.last_name != null ? selfUser.last_name : "");
            case FoxConfig.NAME_USERNAME:
                return selfUser.username;
            case FoxConfig.NAME_STORIES:
                switch (FoxConfig.oldTitleText) {
                    case FoxConfig.NAME_APP:
                    case FoxConfig.NAME_STORIES:
                    default:
                        return appName;
                    case FoxConfig.NAME_DEFAULT_USER:
                        return selfUser.first_name + " " + (selfUser.last_name != null ? selfUser.last_name : "");
                    case FoxConfig.NAME_USERNAME:
                        return selfUser.username;
                }
        }
        return null;
    }

    public static void saveOldTitleText(int type) {
        switch (type) {
            case FoxConfig.NAME_APP:
            case FoxConfig.NAME_STORIES:
            default:
                FoxConfig.oldTitleText = 0;
                break;
            case FoxConfig.NAME_DEFAULT_USER:
                FoxConfig.oldTitleText = 1;
                break;
            case FoxConfig.NAME_USERNAME:
                FoxConfig.oldTitleText = 2;
                break;
        }
    }

    public static String getDoubleTapText() {
        switch (FoxConfig.doubleTapType) {
            case FoxConfig.DOUBLE_TAP_DISABLED:
                return LocaleController.getString("DevOptDisabled", R.string.DevOptDisabled);
            case FoxConfig.DOUBLE_TAP_REACT:
                return LocaleController.getString("Reactions", R.string.Reactions);
            case FoxConfig.DOUBLE_TAP_FORWARD:
                return LocaleController.getString("Forward", R.string.Forward);
            case FoxConfig.DOUBLE_TAP_EDIT:
                return LocaleController.getString("Edit", R.string.Edit);
            case FoxConfig.DOUBLE_TAP_COPY_TEXT:
                return LocaleController.getString("Copy", R.string.Copy);
            case FoxConfig.DOUBLE_TAP_DELETE:
                return LocaleController.getString("Delete", R.string.Delete);
            case FoxConfig.DOUBLE_TAP_SAVE_MESSAGE:
                return LocaleController.getString("AddToSavedMessages", R.string.AddToSavedMessages);
        }
        return null;
    }

    public static String getAppName() {
        if (appVersion.contains("Beta")) {
            return LocaleController.getString("ColorVersionAppNameBeta", R.string.ColorVersionAppNameBeta);
        } else if (appVersion.contains("Alpha")) {
            return LocaleController.getString("ColorVersionAppNameAlpha", R.string.ColorVersionAppNameAlpha);
        } else if (appVersion.contains("debug") || appVersion.contains("pbeta")) {
            String gitBranch = BuildConfig.GIT_COMMIT_HASH;
            return "FoxGram" + " #" + gitBranch;
        } else {
            return LocaleController.getString("ColorVersionAppName", R.string.ColorVersionAppName);
        }
    }

    public static String getAbi() {
        PackageInfo pInfo;
        try {
            pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
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
                    return "x86_64";
                case 0:
                case 9:
                default:
                    return "universal";
            }
        } catch (Exception e) {
            return "universal";
        }
    }

    public static String getOfficialChannel() {
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        return locale.getLanguage().equals(new Locale("it").getLanguage()) ? "FoxGramAppIT" : "FoxGramApp";
    }
}
