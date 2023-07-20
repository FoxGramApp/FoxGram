/*
 * This is the source code of Telegram for Android v. 7.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import it.foxgram.android.StoreUtils;
import com.android.billingclient.api.ProductDetails;

import java.util.Objects;

public class BuildVars {

    // Debug
    public static boolean DEBUG_VERSION = BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean LOGS_ENABLED = BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean DEBUG_PRIVATE_VERSION = BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean IGNORE_VERSION_CHECK = false;
    public static boolean MAGIC_FOX_EXCEPTIONS = false;

    // FoxGram
    public static boolean USE_CLOUD_STRINGS = true;
    public static boolean CHECK_UPDATES = false;
    public static int BUILD_VERSION = BuildConfig.BUILD_VERSION;
    public static String BUILD_VERSION_STRING = BuildConfig.BUILD_VERSION_STRING;

    // Scoped Storage for SDK 30+
    public static boolean NO_SCOPED_STORAGE = Build.VERSION.SDK_INT <= 29;

    // Telegram
    public static int TELEGRAM_BUILD_VERSION = 3387;
    public static String TELEGRAM_BUILD_VERSION_STRING = "9.7.0";

    // App ID and HASH
    // If you want make a fork you have to replace ID and HASH with yours
    public static int APP_ID = 12921654;
    public static String APP_HASH = "8ebeb77d7170894d560ca40bdeabac3a";

    // Google
    // You can disable Safety Net Key leaving no text here
    public static String SAFETYNET_KEY = "";
    public static String SMS_HASH = isStandaloneApp() ? "w0lkcmTZkKh" : (DEBUG_VERSION ? "O2P2z+/jBpJ" : "oLeq9AcOZkT");
    public static String PLAYSTORE_APP_URL = "https://play.google.com/store";
    public static String GOOGLE_AUTH_CLIENT_ID = "202131402753-gtu7gp4c23t9aiq4s149ded2vueiadcf.apps.googleusercontent.com";

    // Huawei
    // Add if you have your app published on App Gallery
    // The Huawei part of code is available in Telegram's repo
    // public static String HUAWEI_APP_ID = "101184875";

    // Billing
    // You can use this flag to disable Google Play Billing
    public static boolean IS_BILLING_UNAVAILABLE = StoreUtils.isFromPlayStore();

    static {
        if (ApplicationLoader.applicationContext != null) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            LOGS_ENABLED = DEBUG_VERSION || sharedPreferences.getBoolean("logsEnabled", DEBUG_VERSION);
        }
    }

    private static Boolean standaloneApp;
    private static Boolean betaApp;

    public static boolean isStandaloneApp() {
        if (standaloneApp == null) {
            standaloneApp = ApplicationLoader.applicationContext != null && "it.foxgram.android".equals(ApplicationLoader.applicationContext.getPackageName());
        }
        return standaloneApp;
    }

    public static boolean isBetaApp() {
        if (betaApp == null) {
            betaApp = ApplicationLoader.applicationContext != null && "it.foxgram.android.beta".equals(ApplicationLoader.applicationContext.getPackageName());
        }
        return betaApp;
    }

    public static boolean isHuaweiStoreApp() {
        return ApplicationLoader.isHuaweiStoreBuild();
    }

    public static boolean useInvoiceBilling() {
        return BillingController.billingClientEmpty || DEBUG_VERSION || isStandaloneApp() || isBetaApp() || isHuaweiStoreApp() || hasDirectCurrency();
    }

    private static boolean hasDirectCurrency() {
        if (!BillingController.getInstance().isReady() || BillingController.PREMIUM_PRODUCT_DETAILS == null) {
            return false;
        }
        for (ProductDetails.SubscriptionOfferDetails offerDetails : BillingController.PREMIUM_PRODUCT_DETAILS.getSubscriptionOfferDetails()) {
            for (ProductDetails.PricingPhase phase : offerDetails.getPricingPhases().getPricingPhaseList()) {
                for (String cur : MessagesController.getInstance(UserConfig.selectedAccount).directPaymentsCurrency) {
                    if (Objects.equals(phase.getPriceCurrencyCode(), cur)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}