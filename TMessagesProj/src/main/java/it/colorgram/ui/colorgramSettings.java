package it.colorgram.ui;

import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;

import it.colorgram.android.Crashlytics;
import it.colorgram.android.ColorConfig;
import it.colorgram.android.StoreUtils;

public class colorgramSettings extends BaseSettingsActivity {

    private int divisorInfoRow;
    private static int colorRow;
    private int categoryHeaderRow;
    private int updatesRow;
    private int dividerUpdates;
    private int generalSettingsRow;
    private int appearanceSettingsRow;
    private int updatesCheckView;
    private int chatSettingsRow;
    private int experimentalSettingsRow;
    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int sourceCodeRow;
    private int supportTranslationRow;
    private int bugReportRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("OwlSetting", R.string.OwlSetting);
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

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == colorRow) {
            AndroidUtilities.addToClipboard(BuildConfig.BUILD_VERSION_STRING + " (" + BuildConfig.BUILD_VERSION + ")");
            BulletinFactory.of(colorgramSettings.this).createCopyBulletin(LocaleController.getString("ReportDetailsCopied", R.string.ReportDetailsCopied)).show();
        } else if (position == channelUpdatesRow) {
            MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("ChannelUsername", R.string.ChannelUsername), this, 1);
        } else if (position == sourceCodeRow) {
            Browser.openUrl(getParentActivity(), "https://github.com/Pierlu096/color");
        } else if (position == supportTranslationRow) {
            Browser.openUrl(getParentActivity(), "https://crowdin.com/project/colorgram");
        } else if (position == generalSettingsRow) {
            presentFragment(new colorgramGeneralSettings());
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
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        colorRow = -1;
        updatesRow = -1;
        dividerUpdates = -1;

        if (StoreUtils.isFromCheckableStore() || !StoreUtils.isDownloadedFromAnyStore()) {
            updatesRow = rowCount++;
            colorRow = rowCount++;
            updatesCheckView = rowCount++;
            dividerUpdates = rowCount++;
        }

        categoryHeaderRow = rowCount++;
        generalSettingsRow = rowCount++;
        appearanceSettingsRow = rowCount++;
        chatSettingsRow = rowCount++;
        experimentalSettingsRow = rowCount++;

        divisorInfoRow = rowCount++;
        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        sourceCodeRow = rowCount++;
        supportTranslationRow = rowCount++;
        bugReportRow = rowCount++;
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
                    } else if (position == chatSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Chat", R.string.Chat), R.drawable.msg_msgbubble3, true);
                    } else if (position == channelUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("OfficialChannel", R.string.OfficialChannel), "@" + LocaleController.getString("ChannelUsername", R.string.ChannelUsername), R.drawable.msg_channel, true);
                    } else if (position == experimentalSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Experimental", R.string.Experimental), R.drawable.outline_science_white, true);
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
                    }
                    break;
                case DETAILED_SETTINGS:
                    TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) holder.itemView;
                    textDetailCell.setMultilineDetail(true);
                    if (position == supportTranslationRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("TranslateOwl", R.string.TranslateOwl), LocaleController.getString("TranslateOwlDesc", R.string.TranslateOwlDesc), R.drawable.round_translate_white_28, true);
                    } else if (position == colorRow) {
                        String updateInfo = LocaleController.getString("UpdateInfo", R.string.UpdateInfo) + " " + BuildConfig.BUILD_VERSION_STRING + " " + "(" + BuildConfig.BUILD_VERSION + ")";
                        String appName;
                        if (updateInfo.contains("Beta")) appName = LocaleController.getString("ColorVersionAppNameBeta", R.string.ColorVersionAppNameBeta);
                        else if (updateInfo.contains("Alpha")) appName = LocaleController.getString("ColorVersionAppNameAlpha", R.string.ColorVersionAppNameAlpha);
                        else appName = LocaleController.getString("ColorVersionAppName", R.string.ColorVersionAppName);
                        textDetailCell.setTextAndValueAndIcon(appName, updateInfo, R.drawable.color_logo, false);
                    } else if (position == sourceCodeRow) {
                        String commitInfo = BuildConfig.GIT_COMMIT_NAME + "\n" +  String.format("%s commit, %s", BuildConfig.GIT_COMMIT_HASH, LocaleController.formatDateAudio(BuildConfig.GIT_COMMIT_DATE, false));
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("SourceCode", R.string.SourceCode), commitInfo, R.drawable.outline_source_white_28, true);
                    } else if (position == bugReportRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CopyReportDetails", R.string.CopyReportDetails), LocaleController.getString("CopyReportDetailsDesc", R.string.CopyReportDetailsDesc), R.drawable.bug_report, false);
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
            if (position == divisorInfoRow || position == dividerUpdates) {
                return ViewType.SHADOW;
            } else if (position == generalSettingsRow || position == chatSettingsRow ||
                    position == channelUpdatesRow || position == experimentalSettingsRow || position == appearanceSettingsRow || position == updatesCheckView) {
                return ViewType.TEXT_CELL;
            } else if (position == updatesRow || position == infoHeaderRow || position == categoryHeaderRow) {
                return ViewType.HEADER;
            } else if (position == colorRow || position == supportTranslationRow || position == sourceCodeRow || position == bugReportRow) {
                return ViewType.DETAILED_SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
