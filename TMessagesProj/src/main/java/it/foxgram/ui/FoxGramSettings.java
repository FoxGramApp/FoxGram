/*
 * This is the source code of FoxGram for Android v. 3.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2023.
 */
package it.foxgram.ui;

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebStorage;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.FloatingDebug.FloatingDebugController;
import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stories.recorder.DualCameraView;

import java.util.Set;

import it.foxgram.android.Crashlytics;
import it.foxgram.android.FoxConfig;
import it.foxgram.android.StoreUtils;
import it.foxgram.android.updates.UpdateManager;
import it.foxgram.android.utils.FoxTextUtils;
import it.foxgram.ui.Components.Dialogs.ExperimentalSettingsBottomSheet;

public class FoxGramSettings extends BaseSettingsActivity {

    private int divisorInfoRow;
    private int foxRow;
    private int categoryHeaderRow;
    private int generalSettingsRow;
    private int translateSettingsRow;
    private int appearanceSettingsRow;
    private int updatesSettingsRow;
    private int chatSettingsRow;
    private int experimentalSettingsRow;
    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int sourceCodeRow;
    private int supportTranslationRow;
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
        menuItem.addSubItem(3, R.drawable.bug_report, LocaleController.getString("CopyReportDetails", R.string.CopyReportDetails));
        return menuItem;
    }

    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            FoxConfig.shareSettings(this);
        } else if (id == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));
            builder.setMessage(LocaleController.getString("ResetSettingsAlert", R.string.ResetSettingsAlert));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.setPositiveButton(LocaleController.getString("Reset", R.string.Reset), (dialogInterface, i) -> {
                int differenceUI = FoxConfig.getDifferenceUI();
                boolean isDefault = FoxConfig.emojiPackSelected.equals("default");
                FoxConfig.resetSettings();
                Theme.lastHolidayCheckTime = 0;
                Theme.dialogs_holidayDrawable = null;
                reloadDialogs();
                reloadMainInfo();
                FoxConfig.doRebuildUIWithDiff(differenceUI, parentLayout);
                if (!isDefault) {
                    Emoji.reloadEmoji();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);
                }
                BulletinFactory.of(FoxGramSettings.this).createSimpleBulletin(R.raw.forward, LocaleController.getString("ResetSettingsHint", R.string.ResetSettingsHint)).show();
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        } else if (id == 3) {
            AndroidUtilities.addToClipboard(Crashlytics.getReportMessage() + "\n\n#bug");
            BulletinFactory.of(FoxGramSettings.this).createCopyBulletin(LocaleController.getString("ReportDetailsCopied", R.string.ReportDetailsCopied)).show();
        }
    }

    private int pressCount = 0;
    protected Theme.ResourcesProvider resourcesProvider;
    protected ProfileActivity.NestedFrameLayout contentView;

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == channelUpdatesRow) {
            MessagesController.getInstance(currentAccount).openByUserName(FoxTextUtils.getOfficialChannel(), this, 1);
        } else if (position == sourceCodeRow) {
            Browser.openUrl(getParentActivity(), "https://github.com/Pierlu096/FoxGram");
        } else if (position == supportTranslationRow) {
            Browser.openUrl(getParentActivity(), "https://crowdin.com/project/colorgram");
        } else if (position == generalSettingsRow) {
            presentFragment(new FoxGramGeneralSettings());
        } else if (position == translateSettingsRow) {
            presentFragment(new FoxGramTranslationsSettings());
        } else if (position == chatSettingsRow) {
            presentFragment(new FoxGramChatSettings());
        } else if (position == experimentalSettingsRow) {
            openDevOpt(this);
        } else if (position == appearanceSettingsRow) {
            presentFragment(new FoxGramAppearanceSettings());
        } else if (position == updatesSettingsRow) {
            presentFragment(new FoxGramUpdateSettings());
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

    private void openDevOpt(BaseFragment fragment) {
        if (FoxConfig.isDevOptEnabled()) {
            presentFragment(new FoxGramExperimentalSettings());
        } else {
            ExperimentalSettingsBottomSheet dialog = new ExperimentalSettingsBottomSheet(fragment);
            dialog.show();
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        bugDividerRow = -1;
        debugHeaderRow = -1;
        sendLogsRow = -1;
        sendLastLogsRow = -1;
        clearLogsRow = -1;
        switchBackendRow = -1;

        foxRow = rowCount++;

        categoryHeaderRow = rowCount++;
        generalSettingsRow = rowCount++;
        translateSettingsRow = rowCount++;
        appearanceSettingsRow = rowCount++;
        chatSettingsRow = rowCount++;
        updatesSettingsRow = rowCount++;
        experimentalSettingsRow = rowCount++;
        divisorInfoRow = rowCount++;

        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        sourceCodeRow = rowCount++;
        supportTranslationRow = rowCount++;

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
                    } else if (position == experimentalSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Experimental", R.string.Experimental), R.drawable.outline_science_white, false);
                    } else if (position == appearanceSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Appearance", R.string.Appearance), R.drawable.settings_appearance, true);
                    } else if (position == updatesSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("ColorUpdates", R.string.ColorUpdates), R.drawable.msg_recent, true);
                    }
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == infoHeaderRow) {
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
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("TranslateColor", R.string.TranslateColor), LocaleController.getString("TranslateColorDesc", R.string.TranslateColorDesc), R.drawable.round_translate_white_28, false);
                    } else if (position == channelUpdatesRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("OfficialChannel", R.string.OfficialChannel), LocaleController.getString("OfficialChannelDesc", R.string.OfficialChannelDesc) + " " + "@" + FoxTextUtils.getOfficialChannel(), R.drawable.msg_channel, true);
                    } else if (position == sourceCodeRow) {
                        String commitInfo = String.format("%s commit, %s", BuildConfig.GIT_COMMIT_HASH, LocaleController.formatDateAudio(BuildConfig.GIT_COMMIT_DATE, false));
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("SourceCode", R.string.SourceCode), commitInfo, R.drawable.outline_source_white_28, true);
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
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.IMAGE_HEADER) {
                String updateInfo = LocaleController.getString("UpdateInfo", R.string.UpdateInfo) + " " + FoxTextUtils.appInfo.appVersion + " " + "(" + FoxTextUtils.appInfo.buildNumber + ")";
                String appName = FoxTextUtils.appInfo.appName;
                LinearLayout imageCell = new LinearLayout(context);
                imageCell.setOrientation(LinearLayout.VERTICAL);
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.fox_login);
                imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setClickable(true);
                imageView.setOnLongClickListener((view1) -> {
                    showDebugMenu();
                    return true;
                });
                imageCell.addView(imageView, LayoutHelper.createLinear(140, 140, Gravity.CENTER, 0, 0, 0, -23));

                TextView textView = new TextView(context);
                textView.setText(appName);
                textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
                textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                imageCell.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER | Gravity.TOP, 25, 0, 25, 0));

                TextView subTextView = new TextView(context);
                subTextView.setText(updateInfo);
                subTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                subTextView.setGravity(Gravity.CENTER);
                subTextView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText4));
                subTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                imageCell.addView(subTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER | Gravity.TOP, 25, 0, 25, 30));

                view = imageCell;
            }
            return view;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void showDebugMenu() {
            pressCount++;
            if (pressCount >= 2) {
                BottomSheet.Builder builder = new BottomSheet.Builder(context);
                builder.setTitle(LocaleController.getString("DebugMenu", R.string.DebugMenu), true);
                CharSequence[] items;
                items = new CharSequence[]{
                        FoxConfig.unlockedSecretIcons ? LocaleController.getString("HideSecretIcons", R.string.HideSecretIcons) : LocaleController.getString("ShowSecretIcons", R.string.ShowSecretIcons),
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
                        BuildVars.DEBUG_PRIVATE_VERSION && !InstantCameraView.allowBigSizeCameraDebug() ? !SharedConfig.bigCameraForRound ? "Force big camera for round" : "Disable big camera for round" : null,
                        LocaleController.getString(DualCameraView.dualAvailableStatic(getContext()) ? "DebugMenuDualOff" : "DebugMenuDualOn"),
                        BuildVars.DEBUG_VERSION ? (SharedConfig.useSurfaceInStories ? "back to TextureView in stories" : "use SurfaceView in stories") : null,
                };
                builder.setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        FoxConfig.toggleUnlockedSecretIcons();
                    } else if (which == 1) {
                        getUserConfig().syncContacts = true;
                        getUserConfig().saveConfig(false);
                        getContactsController().forceImportContacts();
                    } else if (which == 2) {
                        getContactsController().loadContacts(false, 0);
                    } else if (which == 3) {
                        getContactsController().resetImportedContacts();
                    } else if (which == 4) {
                        getMessagesController().forceResetDialogs();
                    } else if (which == 5) {
                        BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED;
                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).apply();
                        updateRowsId();
                        listAdapter.notifyDataSetChanged();
                    } else if (which == 6) {
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
                    } else if (which == 7) {
                        VoIPHelper.showCallDebugSettings(getParentActivity());
                    } else if (which == 8) {
                        SharedConfig.toggleRoundCamera16to9();
                    } else if (which == 9) {
                        ((LaunchActivity) getParentActivity()).checkAppUpdate(true);
                    } else if (which == 10) {
                        getMessagesStorage().readAllDialogs(-1);
                    } else if (which == 11) {
                        SharedConfig.toggleDisableVoiceAudioEffects();
                    } else if (which == 12) {
                        SharedConfig.pendingAppUpdate = null;
                        SharedConfig.saveConfig();
                        FoxConfig.updateData.set(null);
                        FoxConfig.applyUpdateData();
                        FoxConfig.saveUpdateStatus(0);
                        FoxConfig.remindUpdate(-1);
                        FoxConfig.saveLastUpdateCheck(true);
                        if (!StoreUtils.isDownloadedFromAnyStore()) UpdateManager.deleteUpdate();
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    } else if (which == 13) {
                        Set<String> suggestions = getMessagesController().pendingSuggestions;
                        suggestions.add("VALIDATE_PHONE_NUMBER");
                        suggestions.add("VALIDATE_PASSWORD");
                        getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                    } else if (which == 14) {
                        ApplicationLoader.applicationContext.deleteDatabase("webview.db");
                        ApplicationLoader.applicationContext.deleteDatabase("webviewCache.db");
                        WebStorage.getInstance().deleteAllData();
                    } else if (which == 15) {
                        SharedConfig.toggleDebugWebView();
                        Toast.makeText(getParentActivity(), LocaleController.getString(SharedConfig.debugWebView ? R.string.DebugMenuWebViewDebugEnabled : R.string.DebugMenuWebViewDebugDisabled), Toast.LENGTH_SHORT).show();
                    } else if (which == 16) {
                        SharedConfig.toggleForceDisableTabletMode();

                        Activity activity = AndroidUtilities.findActivity(context);
                        final PackageManager pm = activity.getPackageManager();
                        final Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
                        activity.finishAffinity(); // Finishes all activities.
                        activity.startActivity(intent);    // Start the launch activity
                        System.exit(0);
                    } else if (which == 17) {
                        FloatingDebugController.setActive((LaunchActivity) getParentActivity(), !FloatingDebugController.isActive());
                    } else if (which == 18) {
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
                    } else if (which == 19) {
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
                    } else if (which == 20) {
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
                    } else if (which == 21) {
                        SharedConfig.toggleRoundCamera();
                    } else if (which == 22) {
                        boolean enabled = DualCameraView.dualAvailableStatic(getContext());
                        MessagesController.getGlobalMainSettings().edit().putBoolean("dual_available", !enabled).apply();
                        try {
                            Toast.makeText(getParentActivity(), LocaleController.getString(!enabled ? R.string.DebugMenuDualOnToast : R.string.DebugMenuDualOffToast), Toast.LENGTH_SHORT).show();
                        } catch (Exception ignored) {}
                    } else if (which == 23) {
                        SharedConfig.toggleSurfaceInStories();
                        for (int i = 0; i < getParentLayout().getFragmentStack().size(); i++) {
                            getParentLayout().getFragmentStack().get(i).storyViewer = null;
                        }
                    }
                });
                showDialog(builder.create());
            } else {
                try {
                    Toast.makeText(getParentActivity(), LocaleController.getString("DebugMenuLongPress", R.string.DebugMenuLongPress), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.TEXT_CELL || viewType == ViewType.DETAILED_SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == divisorInfoRow || position == bugDividerRow) {
                return ViewType.SHADOW;
            } else if (position == generalSettingsRow || position == translateSettingsRow ||
                    position == chatSettingsRow || position == experimentalSettingsRow ||
                    position == appearanceSettingsRow || position == updatesSettingsRow) {
                return ViewType.TEXT_CELL;
            } else if (position == infoHeaderRow || position == categoryHeaderRow ||
                    position == debugHeaderRow) {
                return ViewType.HEADER;
            } else if (position == supportTranslationRow || position == sourceCodeRow ||
                    position == sendLastLogsRow || position == sendLogsRow ||
                    position == clearLogsRow || position == switchBackendRow ||
                    position == channelUpdatesRow) {
                return ViewType.DETAILED_SETTINGS;
            } else if (position == foxRow) {
                return ViewType.IMAGE_HEADER;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
