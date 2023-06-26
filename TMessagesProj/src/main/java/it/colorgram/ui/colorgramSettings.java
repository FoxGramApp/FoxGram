package it.colorgram.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.webkit.WebStorage;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatThemeController;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.FloatingDebug.FloatingDebugController;
import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.ProfileActivity;

import java.util.Set;

import it.colorgram.android.Crashlytics;
import it.colorgram.android.ColorConfig;
import it.colorgram.android.StoreUtils;
import it.colorgram.android.updates.UpdateManager;

public class colorgramSettings extends BaseSettingsActivity {

    private int divisorInfoRow;
    private static int colorRow;
    private int categoryHeaderRow;
    private int updatesRow;
    private int dividerUpdates;
    private int generalSettingsRow;
    private int translateSettingsRow;
    private int appearanceSettingsRow;
    private int updatesCheckView;
    private int chatSettingsRow;
    private int experimentalSettingsRow;
    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int sourceCodeRow;
    private int supportTranslationRow;
    private int bugReportRow;
    private int bugDividerRow;
    private int debugHeaderRow;
    private int sendLogsRow;
    private int sendLastLogsRow;
    private int clearLogsRow;
    private int switchBackendRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("ColorSetting", R.string.ColorSetting);
    }

    @Override
    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.round_settings_backup_restore, LocaleController.getString("ExportSettings", R.string.ExportSettings));
        menuItem.addSubItem(2, R.drawable.round_settings_backup_reset, LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));
        return menuItem;
    }

    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            ColorConfig.shareSettings(this);
        } else if (id == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));
            builder.setMessage(LocaleController.getString("ResetSettingsAlert", R.string.ResetSettingsAlert));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.setPositiveButton(LocaleController.getString("Reset", R.string.Reset), (dialogInterface, i) -> {
                int differenceUI = ColorConfig.getDifferenceUI();
                boolean isDefault = ColorConfig.emojiPackSelected.equals("default");
                ColorConfig.resetSettings();
                Theme.lastHolidayCheckTime = 0;
                Theme.dialogs_holidayDrawable = null;
                reloadDialogs();
                reloadMainInfo();
                ColorConfig.doRebuildUIWithDiff(differenceUI, parentLayout);
                if (!isDefault) {
                    Emoji.reloadEmoji();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);
                }
                BulletinFactory.of(colorgramSettings.this).createSimpleBulletin(R.raw.forward, LocaleController.getString("ResetSettingsHint", R.string.ResetSettingsHint)).show();
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        }
    }

    private int pressCount = 0;
    private Theme.ResourcesProvider resourcesProvider;

    private ProfileActivity.NestedFrameLayout contentView;

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == colorRow) {
            pressCount++;
            if (pressCount >= 2) {
                BottomSheet.Builder builder = new BottomSheet.Builder(context);
                builder.setTitle(LocaleController.getString("DebugMenu", R.string.DebugMenu), true);
                CharSequence[] items;
                items = new CharSequence[]{
                        LocaleController.getString("DebugMenuImportContacts", R.string.DebugMenuImportContacts),
                        LocaleController.getString("DebugMenuReloadContacts", R.string.DebugMenuReloadContacts),
                        LocaleController.getString("DebugMenuResetContacts", R.string.DebugMenuResetContacts),
                        LocaleController.getString("DebugMenuResetDialogs", R.string.DebugMenuResetDialogs),
                        BuildVars.DEBUG_VERSION ? null : BuildVars.LOGS_ENABLED ? LocaleController.getString("DebugMenuDisableLogs", R.string.DebugMenuDisableLogs) : LocaleController.getString("DebugMenuEnableLogs", R.string.DebugMenuEnableLogs),
                        LocaleController.getString("DebugMenuClearMediaCache", R.string.DebugMenuClearMediaCache),
                        LocaleController.getString("DebugMenuCallSettings", R.string.DebugMenuCallSettings),
                        null,
                        LocaleController.getString("DebugMenuCheckAppUpdate", R.string.DebugMenuCheckAppUpdate),
                        LocaleController.getString("DebugMenuReadAllDialogs", R.string.DebugMenuReadAllDialogs),
                        BuildVars.DEBUG_PRIVATE_VERSION ? SharedConfig.disableVoiceAudioEffects ? "Enable voip audio effects" : "Disable voip audio effects" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Clean app update" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Reset suggestions" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? LocaleController.getString(R.string.DebugMenuClearWebViewCache) : null,
                        LocaleController.getString(SharedConfig.debugWebView ? R.string.DebugMenuDisableWebViewDebug : R.string.DebugMenuEnableWebViewDebug),
                        AndroidUtilities.isTabletInternal() && BuildVars.DEBUG_PRIVATE_VERSION ? SharedConfig.forceDisableTabletMode ? "Enable tablet mode" : "Disable tablet mode" : null,
                        LocaleController.getString(SharedConfig.isFloatingDebugActive ? R.string.FloatingDebugDisable : R.string.FloatingDebugEnable),
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Force remove premium suggestions" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Share device info" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Force performance class" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION && !InstantCameraView.allowBigSizeCameraDebug() ? !SharedConfig.bigCameraForRound ? "Force big camera for round" : "Disable big camera for round" : null
                };
                builder.setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        getUserConfig().syncContacts = true;
                        getUserConfig().saveConfig(false);
                        getContactsController().forceImportContacts();
                    } else if (which == 1) {
                        getContactsController().loadContacts(false, 0);
                    } else if (which == 2) {
                        getContactsController().resetImportedContacts();
                    } else if (which == 3) {
                        getMessagesController().forceResetDialogs();
                    } else if (which == 4) {
                        BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED;
                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).apply();
                        updateRowsId();
                        listAdapter.notifyDataSetChanged();
                    } else if (which == 5) {
                        getMessagesStorage().clearSentMedia();
                        SharedConfig.setNoSoundHintShowed(false);
                        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                        editor.remove("archivehint").remove("proximityhint").remove("archivehint_l").remove("speedhint").remove("gifhint").remove("reminderhint").remove("soundHint").remove("themehint").remove("bganimationhint").remove("filterhint").remove("n_0").apply();
                        MessagesController.getEmojiSettings(currentAccount).edit().remove("featured_hidden").remove("emoji_featured_hidden").apply();
                        SharedConfig.textSelectionHintShows = 0;
                        SharedConfig.lockRecordAudioVideoHint = 0;
                        SharedConfig.stickersReorderingHintUsed = false;
                        SharedConfig.forwardingOptionsHintShown = false;
                        SharedConfig.messageSeenHintCount = 3;
                        SharedConfig.emojiInteractionsHintCount = 3;
                        SharedConfig.dayNightThemeSwitchHintCount = 3;
                        SharedConfig.fastScrollHintCount = 3;
                        ChatThemeController.getInstance(currentAccount).clearCache();
                        getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                        RestrictedLanguagesSelectActivity.cleanup();
                    } else if (which == 6) {
                        VoIPHelper.showCallDebugSettings(getParentActivity());
                    } else if (which == 7) {
                        SharedConfig.toggleRoundCamera16to9();
                    } else if (which == 8) {
                        ((LaunchActivity) getParentActivity()).checkAppUpdate(true);
                    } else if (which == 9) {
                        getMessagesStorage().readAllDialogs(-1);
                    } else if (which == 10) {
                        SharedConfig.toggleDisableVoiceAudioEffects();
                    } else if (which == 11) {
                        SharedConfig.pendingAppUpdate = null;
                        SharedConfig.saveConfig();
                        ColorConfig.updateData.set(null);
                        ColorConfig.applyUpdateData();
                        ColorConfig.saveUpdateStatus(0);
                        ColorConfig.remindUpdate(-1);
                        ColorConfig.saveLastUpdateCheck(true);
                        if (!StoreUtils.isDownloadedFromAnyStore()) UpdateManager.deleteUpdate();
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    } else if (which == 12) {
                        Set<String> suggestions = getMessagesController().pendingSuggestions;
                        suggestions.add("VALIDATE_PHONE_NUMBER");
                        suggestions.add("VALIDATE_PASSWORD");
                        getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                    } else if (which == 13) {
                        ApplicationLoader.applicationContext.deleteDatabase("webview.db");
                        ApplicationLoader.applicationContext.deleteDatabase("webviewCache.db");
                        WebStorage.getInstance().deleteAllData();
                    } else if (which == 14) {
                        SharedConfig.toggleDebugWebView();
                        Toast.makeText(getParentActivity(), LocaleController.getString(SharedConfig.debugWebView ? R.string.DebugMenuWebViewDebugEnabled : R.string.DebugMenuWebViewDebugDisabled), Toast.LENGTH_SHORT).show();
                    } else if (which == 15) {
                        SharedConfig.toggleForceDisableTabletMode();

                        Activity activity = AndroidUtilities.findActivity(context);
                        final PackageManager pm = activity.getPackageManager();
                        final Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
                        activity.finishAffinity(); // Finishes all activities.
                        activity.startActivity(intent);    // Start the launch activity
                        System.exit(0);
                    } else if (which == 16) {
                        FloatingDebugController.setActive((LaunchActivity) getParentActivity(), !FloatingDebugController.isActive());
                    } else if (which == 17) {
                        getMessagesController().loadAppConfig();
                        TLRPC.TL_help_dismissSuggestion req = new TLRPC.TL_help_dismissSuggestion();
                        req.suggestion = "VALIDATE_PHONE_NUMBER";
                        req.peer = new TLRPC.TL_inputPeerEmpty();
                        getConnectionsManager().sendRequest(req, (response, error) -> {
                            TLRPC.TL_help_dismissSuggestion req2 = new TLRPC.TL_help_dismissSuggestion();
                            req2.suggestion = "VALIDATE_PASSWORD";
                            req2.peer = new TLRPC.TL_inputPeerEmpty();
                            getConnectionsManager().sendRequest(req2, (res2, err2) -> getMessagesController().loadAppConfig());
                        });
                    } else if (which == 18) {
                        int cpuCount = ConnectionsManager.CPU_COUNT;
                        int memoryClass = ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
                        long minFreqSum = 0, minFreqCount = 0;
                        long maxFreqSum = 0, maxFreqCount = 0;
                        long curFreqSum = 0, curFreqCount = 0;
                        long capacitySum = 0, capacityCount = 0;
                        StringBuilder cpusInfo = new StringBuilder();
                        for (int i = 0; i < cpuCount; i++) {
                            Long minFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_min_freq");
                            Long curFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_cur_freq");
                            Long maxFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                            Long capacity = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpu_capacity");
                            cpusInfo.append("#").append(i).append(" ");
                            if (minFreq != null) {
                                cpusInfo.append("min=").append(minFreq / 1000L).append(" ");
                                minFreqSum += (minFreq / 1000L);
                                minFreqCount++;
                            }
                            if (curFreq != null) {
                                cpusInfo.append("cur=").append(curFreq / 1000L).append(" ");
                                curFreqSum += (curFreq / 1000L);
                                curFreqCount++;
                            }
                            if (maxFreq != null) {
                                cpusInfo.append("max=").append(maxFreq / 1000L).append(" ");
                                maxFreqSum += (maxFreq / 1000L);
                                maxFreqCount++;
                            }
                            if (capacity != null) {
                                cpusInfo.append("cpc=").append(capacity).append(" ");
                                capacitySum += capacity;
                                capacityCount++;
                            }
                            cpusInfo.append("\n");
                        }
                        StringBuilder info = new StringBuilder();
                        info.append(Build.MANUFACTURER).append(", ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(", ").append(Build.DEVICE).append(") ").append(" (android ").append(Build.VERSION.SDK_INT).append(")\n");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            info.append("SoC: ").append(Build.SOC_MANUFACTURER).append(", ").append(Build.SOC_MODEL).append("\n");
                        }
                        String gpuModel = AndroidUtilities.getSysInfoString("/sys/kernel/gpu/gpu_model");
                        if (gpuModel != null) {
                            info.append("GPU: ").append(gpuModel);
                            Long minClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_min_clock");
                            Long mminClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_mm_min_clock");
                            Long maxClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_max_clock");
                            if (minClock != null) {
                                info.append(", min=").append(minClock / 1000L);
                            }
                            if (mminClock != null) {
                                info.append(", mmin=").append(mminClock / 1000L);
                            }
                            if (maxClock != null) {
                                info.append(", max=").append(maxClock / 1000L);
                            }
                            info.append("\n");
                        }
                        ConfigurationInfo configurationInfo = ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
                        info.append("GLES Version: ").append(configurationInfo.getGlEsVersion()).append("\n");
                        info.append("Memory: class=").append(AndroidUtilities.formatFileSize(memoryClass * 1024L * 1024L));
                        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                        ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
                        info.append(", total=").append(AndroidUtilities.formatFileSize(memoryInfo.totalMem));
                        info.append(", avail=").append(AndroidUtilities.formatFileSize(memoryInfo.availMem));
                        info.append(", low?=").append(memoryInfo.lowMemory);
                        info.append(" (threshold=").append(AndroidUtilities.formatFileSize(memoryInfo.threshold)).append(")");
                        info.append("\n");
                        info.append("Current class: ").append(SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass())).append(", measured: ").append(SharedConfig.performanceClassName(SharedConfig.measureDevicePerformanceClass()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            info.append(", suggest=").append(Build.VERSION.MEDIA_PERFORMANCE_CLASS);
                        }
                        info.append("\n");
                        info.append(cpuCount).append(" CPUs");
                        if (minFreqCount > 0) {
                            info.append(", avgMinFreq=").append(minFreqSum / minFreqCount);
                        }
                        if (curFreqCount > 0) {
                            info.append(", avgCurFreq=").append(curFreqSum / curFreqCount);
                        }
                        if (maxFreqCount > 0) {
                            info.append(", avgMaxFreq=").append(maxFreqSum / maxFreqCount);
                        }
                        if (capacityCount > 0) {
                            info.append(", avgCapacity=").append(capacitySum / capacityCount);
                        }
                        info.append("\n").append(cpusInfo);

                        showDialog(new ShareAlert(getParentActivity(), null, info.toString(), false, null, false) {
                            @Override
                            protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic) {
                                AndroidUtilities.runOnUIThread(() -> BulletinFactory.createInviteSentBulletin(getParentActivity(), contentView, dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, getThemedColor(Theme.key_undo_background), getThemedColor(Theme.key_undo_infoColor)).show(), 250);
                            }
                        });
                    } else if (which == 19) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                        builder2.setTitle("Force performance class");
                        int currentClass = SharedConfig.getDevicePerformanceClass();
                        int trueClass = SharedConfig.measureDevicePerformanceClass();
                        builder2.setItems(new CharSequence[]{
                                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? "**HIGH**" : "HIGH") + (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? " (measured)" : "")),
                                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? "**AVERAGE**" : "AVERAGE") + (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? " (measured)" : "")),
                                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_LOW ? "**LOW**" : "LOW") + (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW ? " (measured)" : ""))
                        }, (dialog2, which2) -> {
                            int newClass = 2 - which2;
                            if (newClass == trueClass) {
                                SharedConfig.overrideDevicePerformanceClass(-1);
                            } else {
                                SharedConfig.overrideDevicePerformanceClass(newClass);
                            }
                        });
                        builder2.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        builder2.show();
                    } else if (which == 20) {
                        SharedConfig.toggleRoundCamera();
                    }
                });
                showDialog(builder.create());
            } else {
                try {
                    Toast.makeText(getParentActivity(), LocaleController.getString("OnDebugClick", R.string.OnDebugClick), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        } else if (position == channelUpdatesRow) {
            MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("ChannelUsername", R.string.ChannelUsername), this, 1);
        } else if (position == sourceCodeRow) {
            Browser.openUrl(getParentActivity(), "https://github.com/Pierlu096/Color");
        } else if (position == supportTranslationRow) {
            Browser.openUrl(getParentActivity(), "https://crowdin.com/project/colorgram");
        } else if (position == generalSettingsRow) {
            presentFragment(new colorgramGeneralSettings());
        } else if (position == translateSettingsRow) {
            presentFragment(new colorgramTranslationsSettings());
        } else if (position == chatSettingsRow) {
            presentFragment(new colorgramChatSettings());
        } else if (position == experimentalSettingsRow) {
            presentFragment(new colorgramExperimentalSettings());
        } else if (position == appearanceSettingsRow) {
            presentFragment(new colorgramAppearanceSettings());
        } else if (position == updatesCheckView) {
            presentFragment(new colorgramUpdateSettings());
        } else if (position == bugReportRow) {
            AndroidUtilities.addToClipboard(Crashlytics.getReportMessage() + "\n\n#bug");
            BulletinFactory.of(colorgramSettings.this).createCopyBulletin(LocaleController.getString("ReportDetailsCopied", R.string.ReportDetailsCopied)).show();
        } else if (position == sendLogsRow) {
            ProfileActivity.sendLogs(getParentActivity(), false);
        } else if (position == sendLastLogsRow) {
            ProfileActivity.sendLogs(getParentActivity(), true);
        } else if (position == clearLogsRow) {
            FileLog.cleanupLogs();
        } else if (position == switchBackendRow) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder1.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
            builder1.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder1.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> {
                SharedConfig.pushAuthKey = null;
                SharedConfig.pushAuthKeyId = null;
                SharedConfig.saveConfig();
                getConnectionsManager().switchBackend(true);
            });
            builder1.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder1.create());
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        colorRow = -1;
        updatesRow = -1;
        dividerUpdates = -1;
        bugDividerRow = -1;
        debugHeaderRow = -1;
        sendLogsRow = -1;
        sendLastLogsRow = -1;
        clearLogsRow = -1;
        switchBackendRow = -1;

        if (StoreUtils.isFromCheckableStore() || !StoreUtils.isDownloadedFromAnyStore()) {
            updatesRow = rowCount++;
            colorRow = rowCount++;
            updatesCheckView = rowCount++;
            dividerUpdates = rowCount++;
        }

        categoryHeaderRow = rowCount++;
        generalSettingsRow = rowCount++;
        translateSettingsRow = rowCount++;
        appearanceSettingsRow = rowCount++;
        chatSettingsRow = rowCount++;
        experimentalSettingsRow = rowCount++;

        divisorInfoRow = rowCount++;
        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        sourceCodeRow = rowCount++;
        supportTranslationRow = rowCount++;
        bugReportRow = rowCount++;

        if (BuildVars.LOGS_ENABLED) {
            bugDividerRow = rowCount++;
            debugHeaderRow = rowCount++;
            sendLogsRow = rowCount++;
            sendLastLogsRow = rowCount++;
            clearLogsRow = rowCount++;
        }
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            switchBackendRow = rowCount++;
        }
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == generalSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("General", R.string.General), R.drawable.msg_media, true);
                    } else if (position == translateSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("TranslationsTitle", R.string.TranslationsTitle), R.drawable.msg_translate, true);
                    } else if (position == chatSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Chat", R.string.Chat), R.drawable.msg_msgbubble3, true);
                    } else if (position == channelUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("OfficialChannel", R.string.OfficialChannel), "@" + LocaleController.getString("ChannelUsername", R.string.ChannelUsername), R.drawable.msg_channel, true);
                    } else if (position == experimentalSettingsRow) {
                        String isEnabled = ColorConfig.isDevOptEnabled() ? LocaleController.getString("DevOptEnabled", R.string.DevOptEnabled) : LocaleController.getString("DevOptDisabled", R.string.DevOptDisabled);
                        textCell.setTextAndValueAndIcon(LocaleController.getString("Experimental", R.string.Experimental), isEnabled, R.drawable.outline_science_white, true);
                    } else if (position == appearanceSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Appearance", R.string.Appearance), R.drawable.settings_appearance, true);
                    } else if (position == updatesCheckView) {
                        textCell.setTextAndIcon(LocaleController.getString("ViewUpdate", R.string.ViewUpdate), R.drawable.msg_settings, true);
                    }
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == updatesRow) {
                        headerCell.setText(LocaleController.getString("AppVersionHeader", R.string.AppVersionHeader));
                    } else if (position == infoHeaderRow) {
                        headerCell.setText(LocaleController.getString("Info", R.string.Info));
                    } else if (position == categoryHeaderRow) {
                        headerCell.setText(LocaleController.getString("Categories", R.string.Categories));
                    } else if (position == debugHeaderRow) {
                        headerCell.setText(LocaleController.getString("SettingsDebug", R.string.SettingsDebug));
                    }
                    break;
                case DETAILED_SETTINGS:
                    TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) holder.itemView;
                    textDetailCell.setMultilineDetail(true);
                    if (position == supportTranslationRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("TranslateColor", R.string.TranslateColor), LocaleController.getString("TranslateColorDesc", R.string.TranslateColorDesc), R.drawable.round_translate_white_28, true);
                    } else if (position == colorRow) {
                        String updateInfo = LocaleController.getString("UpdateInfo", R.string.UpdateInfo) + " " + BuildConfig.BUILD_VERSION_STRING + " " + "(" + BuildConfig.BUILD_VERSION + ")";
                        String appName;
                        if (updateInfo.contains("Beta")) appName = LocaleController.getString("ColorVersionAppNameBeta", R.string.ColorVersionAppNameBeta);
                        else if (updateInfo.contains("Alpha")) appName = LocaleController.getString("ColorVersionAppNameAlpha", R.string.ColorVersionAppNameAlpha);
                        else appName = LocaleController.getString("ColorVersionAppName", R.string.ColorVersionAppName);
                        textDetailCell.setTextAndValueAndIcon(appName, updateInfo, R.drawable.color_logo, false);
                    } else if (position == sourceCodeRow) {
                        String commitInfo = String.format("%s commit, %s", BuildConfig.GIT_COMMIT_HASH, LocaleController.formatDateAudio(BuildConfig.GIT_COMMIT_DATE, false));
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("SourceCode", R.string.SourceCode), commitInfo, R.drawable.outline_source_white_28, true);
                    } else if (position == bugReportRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CopyReportDetails", R.string.CopyReportDetails), LocaleController.getString("CopyReportDetailsDesc", R.string.CopyReportDetailsDesc), R.drawable.bug_report, true);
                    } else if (position == sendLogsRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("DebugSendLogs", R.string.DebugSendLogs), LocaleController.getString("SendLogsDesc", R.string.SendLogsDesc), R.drawable.msg_send, true);
                    } else if (position == sendLastLogsRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("DebugSendLastLogs", R.string.DebugSendLastLogs), LocaleController.getString("SendLatestLogsDesc", R.string.SendLatestLogsDesc), R.drawable.msg_recent, true);
                    } else if (position == clearLogsRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("DebugClearLogs", R.string.DebugClearLogs), LocaleController.getString("ClearLogsDesc", R.string.ClearLogsDesc), R.drawable.msg_clear, switchBackendRow != -1);
                    } else if (position == switchBackendRow) {
                        textDetailCell.setTextAndValueAndIcon("Switch Backend", "Switch to test Server", R.drawable.msg_switch, false);
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.TEXT_CELL || viewType == ViewType.DETAILED_SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == divisorInfoRow || position == dividerUpdates || position == bugDividerRow) {
                return ViewType.SHADOW;
            } else if (position == generalSettingsRow || position == translateSettingsRow || position == chatSettingsRow ||
                    position == channelUpdatesRow || position == experimentalSettingsRow || position == appearanceSettingsRow ||
                    position == updatesCheckView) {
                return ViewType.TEXT_CELL;
        } else if (position == updatesRow || position == infoHeaderRow || position == categoryHeaderRow || position == debugHeaderRow) {
                return ViewType.HEADER;
            } else if (position == colorRow || position == supportTranslationRow || position == sourceCodeRow ||
                    position == bugReportRow || position == sendLastLogsRow || position == sendLogsRow ||
                    position == clearLogsRow || position == switchBackendRow) {
                return ViewType.DETAILED_SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
