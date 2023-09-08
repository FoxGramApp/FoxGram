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

import it.foxgram.android.FoxConfig;

public class FoxTextUtils {
    public static String getTitleText() {
        int currentAccount = UserConfig.selectedAccount;
        TLRPC.User selfUser = UserConfig.getInstance(currentAccount).getCurrentUser();
        switch (FoxConfig.nameType) {
            case FoxConfig.DEFAULT_NAME:
                return getAppName();
            case FoxConfig.USER_NAME:
                return selfUser.first_name + " " + (selfUser.last_name != null ? selfUser.last_name : "");
            case FoxConfig.TG_USER_NAME:
                return selfUser.username;
            case FoxConfig.MY_STORY:
                switch (FoxConfig.oldTitleText) {
                    case FoxConfig.DEFAULT_NAME:
                    case FoxConfig.MY_STORY:
                    default:
                        return getAppName();
                    case FoxConfig.USER_NAME:
                        return selfUser.first_name + " " + (selfUser.last_name != null ? selfUser.last_name : "");
                    case FoxConfig.TG_USER_NAME:
                        return selfUser.username;
                }
        }
        return null;
    }

    public static void saveOldTitleText(int type) {
        switch (type) {
            case FoxConfig.DEFAULT_NAME:
            case FoxConfig.MY_STORY:
            default:
                FoxConfig.oldTitleText = 0;
                break;
            case FoxConfig.USER_NAME:
                FoxConfig.oldTitleText = 1;
                break;
            case FoxConfig.TG_USER_NAME:
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
        if (BuildConfig.BUILD_VERSION_STRING.contains("Beta")) {
            return LocaleController.getString("ColorVersionAppNameBeta", R.string.ColorVersionAppNameBeta);
        } else if (BuildConfig.BUILD_VERSION_STRING.contains("Alpha")) {
            return LocaleController.getString("ColorVersionAppNameAlpha", R.string.ColorVersionAppNameAlpha);
        } else if (BuildConfig.BUILD_TYPE.equals("pbeta") || BuildConfig.BUILD_TYPE.equals("debug")) {
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

    public static String getUpdatesChannel() {
        return FoxConfig.betaUpdates ? "Release Preview" : LocaleController.getString("Stable", R.string.Stable);
    }

    public static String getOfficialChannel() {
        return "FoxGramApp";
    }
}
