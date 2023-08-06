package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.drawable.icon_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconDefault),
        MONET("MonetIcon", R.color.fox_gram_background, R.drawable.fox_fm_my,R.string.MonetIconColor, false, true),
        AQUA("AquaIcon", R.drawable.icon_4_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconAqua),
        DEVELOPER("DeveloperIcon", R.drawable.iconc_1_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconDeveloper),
        RAINBOW("RainbowIcon", R.drawable.icon_7_launcher_background, R.drawable.icon_8_launcher_foreground, R.string.AppIconRainbow, false, true),
        MONO_BLACK("MonoBlackIcon", R.drawable.iconc_2_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconMonoBlack),
        ARCTIC("ArcticIcon", R.drawable.iconc_3_background_sa, R.drawable.iconc_3_launcher_foreground, R.string.AppIconArctic),
        VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, -1, R.string.AppIconVintage, false ,true),
        ARI("AriIcon", R.color.icon_9_launcher_background, R.mipmap.icon_8_launcher_foreground, R.string.AriIcon, false, true),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground, R.string.AppIconPremium, true),
        GALAXY("GalaxyIcon", R.drawable.icon_10_launcher_background, R.drawable.icon_10_launcher_foreground, R.string.AppIconGalaxy, true),
        SHINE("ShineIcon", R.drawable.icon_5_launcher_background, R.drawable.icon_7_launcher_foreground, R.string.AppIconFoxgram, true);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;
        public final boolean hidden;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this(key, background, foreground, title, premium, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium, boolean hidden) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
            this.hidden = hidden;
        }
    }
}
