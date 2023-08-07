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
        AQUA("AquaIcon", R.drawable.aqua_bg_sa, R.drawable.icon_launcher_foreground, R.string.AppIconAqua),
        DEVELOPER("DeveloperIcon", R.drawable.dev_bg_sa, R.drawable.icon_launcher_foreground, R.string.AppIconDeveloper),
        RAINBOW("RainbowIcon", R.drawable.rainbow_bg, R.drawable.icon_launcher_foreground, R.string.AppIconRainbow, false, true),
        MONO_BLACK("MonoBlackIcon", R.drawable.black_bg_sa, R.drawable.icon_launcher_foreground, R.string.AppIconMonoBlack),
        ARCTIC("ArcticIcon", R.drawable.arctic_bg_sa, R.drawable.icon_launcher_foreground_black, R.string.AppIconArctic),
        VINTAGE("VintageIcon", R.drawable.color_gram_bg_sa, -1, R.string.AppIconVintage, false ,true),
        ARI("AriIcon", R.color.icon_9_launcher_background, R.mipmap.thank_u_next_fg, R.string.AriIcon, false, true),
        PREMIUM("PremiumIcon", R.drawable.premium_bg_sa, R.mipmap.premium_icon_fg, R.string.AppIconPremium, true),
        GALAXY("GalaxyIcon", R.drawable.galaxy_bg, R.drawable.icon_launcher_foreground, R.string.AppIconGalaxy, true),
        SHINE("ShineIcon", R.drawable.shining_fox_bg, R.drawable.icon_launcher_foreground, R.string.AppIconFoxgram, true);

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
