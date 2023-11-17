/*
 * This is the source code of FoxGram for Android v. 3.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2023.
 */
package it.foxgram.ui;

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import it.foxgram.android.Crashlytics;
import it.foxgram.android.FoxConfig;
import it.foxgram.android.utils.FoxTextUtils;
import it.foxgram.ui.Components.Dialogs.ExperimentalSettingsBottomSheet;

public class FoxGramSettings extends BaseSettingsActivity {

    private int divisorInfoRow;
    private int foxRow;
    private int categoryHeaderRow;
    private int generalSettingsRow;
    private int appearanceSettingsRow;
    private int updatesSettingsRow;
    private int chatSettingsRow;
    private int experimentalSettingsRow;
    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int sourceCodeRow;
    private int supportTranslationRow;
    private int bugReportRow;

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
        }
    }

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
        } else if (position == chatSettingsRow) {
            presentFragment(new FoxGramChatSettings());
        } else if (position == experimentalSettingsRow) {
            openDevOpt(this);
        } else if (position == appearanceSettingsRow) {
            presentFragment(new FoxGramAppearanceSettings());
        } else if (position == updatesSettingsRow) {
            presentFragment(new FoxGramUpdateSettings());
        } else if (position == bugReportRow) {
            AndroidUtilities.addToClipboard(Crashlytics.getReportMessage() + "\n\n#bug");
            BulletinFactory.of(FoxGramSettings.this).createCopyBulletin(LocaleController.getString("ReportDetailsCopied", R.string.ReportDetailsCopied)).show();
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
        foxRow = rowCount++;

        categoryHeaderRow = rowCount++;
        generalSettingsRow = rowCount++;
        appearanceSettingsRow = rowCount++;
        chatSettingsRow = rowCount++;
        updatesSettingsRow = rowCount++;
        experimentalSettingsRow = rowCount++;
        divisorInfoRow = rowCount++;

        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        sourceCodeRow = rowCount++;
        bugReportRow = rowCount++;
        supportTranslationRow = rowCount++;
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
                    } else if (position == bugReportRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CopyReportDetails", R.string.CopyReportDetails), LocaleController.getString("CopyReportDetailsDesc", R.string.CopyReportDetailsDesc), R.drawable.bug_report, false);
                    }
                    break;
            }
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.IMAGE_HEADER) {
                String updateInfo = LocaleController.getString("UpdateInfo", R.string.UpdateInfo) + " " + FoxTextUtils.appVersion + " " + "(" + FoxTextUtils.appBuildNumber + ")";
                String appName = FoxTextUtils.appName;
                LinearLayout imageCell = new LinearLayout(context);
                imageCell.setOrientation(LinearLayout.VERTICAL);
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.fox_login);
                imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogIcon, getResourceProvider()), PorterDuff.Mode.MULTIPLY));
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

        private int pressCount = 0;

        public void showDebugMenu() {
            pressCount++;
            if (pressCount >= 2) {
                BottomSheet.Builder builder = new BottomSheet.Builder(context);
                builder.setTitle(LocaleController.getString("DebugMenu", R.string.DebugMenu), true);
                CharSequence[] items = new CharSequence[]{
                        FoxConfig.unlockedSecretIcons ? LocaleController.getString("HideSecretIcons", R.string.HideSecretIcons) : LocaleController.getString("ShowSecretIcons", R.string.ShowSecretIcons),
                        LocaleController.getString("DebugMenuCheckAppUpdate", R.string.DebugMenuCheckAppUpdate),
                };
                builder.setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        FoxConfig.toggleUnlockedSecretIcons();
                    } else if (which == 1) {
                        ((LaunchActivity) getParentActivity()).checkAppUpdate(true);
                    }
                });
                showDialog(builder.create());
            } else {
                Toast.makeText(getParentActivity(), LocaleController.getString("DebugMenuLongPress", R.string.DebugMenuLongPress), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.TEXT_CELL || viewType == ViewType.DETAILED_SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == divisorInfoRow) {
                return ViewType.SHADOW;
            } else if (position == generalSettingsRow || position == chatSettingsRow ||
                    position == experimentalSettingsRow || position == appearanceSettingsRow ||
                    position == updatesSettingsRow) {
                return ViewType.TEXT_CELL;
            } else if (position == infoHeaderRow || position == categoryHeaderRow) {
                return ViewType.HEADER;
            } else if (position == supportTranslationRow || position == sourceCodeRow ||
                    position == channelUpdatesRow || position == bugReportRow) {
                return ViewType.DETAILED_SETTINGS;
            } else if (position == foxRow) {
                return ViewType.IMAGE_HEADER;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
