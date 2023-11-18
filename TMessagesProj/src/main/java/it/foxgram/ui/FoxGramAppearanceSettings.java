/*
 * This is the source code of FoxGram for Android v. 3.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2023.
 */
package it.foxgram.ui;

import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;

import it.foxgram.android.AlertController;
import it.foxgram.android.CustomEmojiController;
import it.foxgram.android.FoxConfig;
import it.foxgram.android.utils.FoxTextUtils;
import it.foxgram.ui.Cells.BlurIntensity;
import it.foxgram.ui.Cells.ChatBlurIntensity;
import it.foxgram.ui.Cells.DrawerProfilePreview;
import it.foxgram.ui.Cells.DynamicButtonSelector;
import it.foxgram.ui.Cells.ThemeSelectorDrawer;

public class FoxGramAppearanceSettings extends BaseSettingsActivity implements NotificationCenter.NotificationCenterDelegate {
    private DrawerProfilePreview profilePreviewCell;

    private int drawerRow;
    private int drawerAvatarAsBackgroundRow;
    private int showGradientRow;
    private int showAvatarRow;
    private int drawerDarkenBackgroundRow;
    private int drawerBlurBackgroundRow;
    private int drawerDividerRow;
    private int editBlurHeaderRow;
    private int editBlurRow;
    private int editBlurDividerRow;
    private int themeDrawerHeader;
    private int themeDrawerRow;
    private int themeDrawerDividerRow;
    private int menuItemsRow;
    private int dynamicButtonHeaderRow;
    private int dynamicButtonRow;
    private int dynamicDividerRow;
    private int fontsAndEmojiHeaderRow;
    private int useSystemFontRow;
    private int fontsAndEmojiDividerRow;
    private int chatHeaderRow;
    private int appearanceHeaderRow;
    private int forcePacmanRow;
    private int smoothNavRow;
    private int showSantaHatRow;
    private int showFallingSnowRow;
    private int messageTimeSwitchRow;
    private int roundedNumberSwitchRow;
    private int smartButtonsRow;
    private int appBarShadowRow;
    private int slidingTitleRow;
    private int searchIconInActionBarRow;
    private int appearanceDividerRow;
    private int showPencilIconRow;
    private int showInActionBarRow;
    private int chooseEmojiPackRow;
    private int inAppBlurRow;
    private int inAppBlurDividerRow;
    private int editInAppBlurRow;
    private int editInAppBlurHeaderRow;
    private int showStoriesRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("Appearance", R.string.Appearance);
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiPacksLoaded);
        CustomEmojiController.loadEmojisInfo();
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiPacksLoaded);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == showAvatarRow) {
            FoxConfig.toggleShowAvatarImage();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showAvatarImage);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == showGradientRow) {
            FoxConfig.toggleShowGradientColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showGradientColor);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerDarkenBackgroundRow) {
            FoxConfig.toggleAvatarBackgroundDarken();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.avatarBackgroundDarken);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerBlurBackgroundRow) {
            FoxConfig.toggleAvatarBackgroundBlur();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.avatarBackgroundBlur);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (FoxConfig.avatarBackgroundBlur) {
                listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
            } else {
                listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
            }
            updateRowsId();
        } else if (position == drawerAvatarAsBackgroundRow) {
            FoxConfig.toggleAvatarAsDrawerBackground();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.avatarAsDrawerBackground);
            }
            reloadMainInfo();
            TransitionManager.beginDelayedTransition(profilePreviewCell);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (FoxConfig.avatarAsDrawerBackground) {
                updateRowsId();
                listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (FoxConfig.avatarBackgroundBlur ? 3 : 0));
            } else {
                listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (FoxConfig.avatarBackgroundBlur ? 3 : 0));
                updateRowsId();
            }
        } else if (position == menuItemsRow) {
            presentFragment(new DrawerOrderSettings());
        } else if (position == useSystemFontRow) {
            FoxConfig.toggleUseSystemFont();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.useSystemFont);
            }
            AndroidUtilities.clearTypefaceCache();
            rebuildAllFragmentsWithLast();
        } else if (position == forcePacmanRow) {
            FoxConfig.togglePacmanForced();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.pacmanForced);
            }
        } else if (position == smoothNavRow) {
            FoxConfig.toggleSmoothNav();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.smoothNav);
            }
            restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
        } else if (position == smartButtonsRow) {
            FoxConfig.toggleSmartButtons();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.smartButtons);
            }
        } else if (position == appBarShadowRow) {
            FoxConfig.toggleAppBarShadow();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showAppBarShadow);
            }
            parentLayout.setHeaderShadow(FoxConfig.showAppBarShadow ? parentLayout.getView().getResources().getDrawable(R.drawable.header_shadow).mutate():null);
            SharedConfig.saveDebugConfig();
            rebuildAllFragmentsWithLast();
        } else if (position == showSantaHatRow) {
            FoxConfig.toggleShowSantaHat();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showSantaHat);
            }
            Theme.lastHolidayCheckTime = 0;
            Theme.dialogs_holidayDrawable = null;
            reloadMainInfo();
        } else if (position == showFallingSnowRow) {
            FoxConfig.toggleShowSnowFalling();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showSnowFalling);
            }
            Theme.lastHolidayCheckTime = 0;
            Theme.dialogs_holidayDrawable = null;
            reloadMainInfo();
        } else if (position == slidingTitleRow) {
            FoxConfig.toggleSlidingChatTitle();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.slidingChatTitle);
            }
        } else if (position == messageTimeSwitchRow) {
            FoxConfig.toggleFullTime();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.fullTime);
            }
            LocaleController.getInstance().recreateFormatters();
        } else if (position == roundedNumberSwitchRow) {
            FoxConfig.toggleRoundedNumbers();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.roundedNumbers);
            }
        } else if (position == searchIconInActionBarRow) {
            FoxConfig.toggleSearchIconInActionBar();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.searchIconInActionBar);
            }
        } else if (position == showPencilIconRow) {
            FoxConfig.toggleShowPencilIcon();
            parentLayout.rebuildAllFragmentViews(false, false);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showPencilIcon);
            }
        } else if (position == showInActionBarRow) {
            TLRPC.User selfUser = UserConfig.getInstance(currentAccount).getCurrentUser();

            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("Default", R.string.Default));
            types.add(0);
            arrayList.add(LocaleController.getString("AccountNameTitleBar", R.string.AccountNameTitleBar));
            types.add(1);
            if (!TextUtils.isEmpty(selfUser.username)) {
                arrayList.add(LocaleController.getString("Username", R.string.Username));
                types.add(2);
            } else {
                if (getMessagesController().storiesEnabled() && FoxConfig.showStories) {
                    arrayList.add(LocaleController.getString("ProfileMyStories", R.string.ProfileMyStories));
                    types.add(2);
                }
            }
            if (!TextUtils.isEmpty(selfUser.username) && getMessagesController().storiesEnabled() && FoxConfig.showStories) {
                arrayList.add(LocaleController.getString("ProfileMyStories", R.string.ProfileMyStories));
                types.add(3);
            }

            FoxTextUtils.saveOldTitleText(FoxConfig.nameType);

            AlertController.show(arrayList, LocaleController.getString("TitleBarName", R.string.TitleBarName), types.indexOf(FoxConfig.nameType), context, i -> {
                FoxConfig.saveNameType(types.get(i));
                if (types.get(i) != 3) {
                    FoxTextUtils.saveOldTitleText(types.get(i));
                }
                listAdapter.notifyItemChanged(showInActionBarRow, PARTIAL);
                reloadDialogs();
            });
        } else if (position == chooseEmojiPackRow) {
            presentFragment(new EmojiPackSettings());
        } else if (position == inAppBlurRow) {
            FoxConfig.toggleEditInAppBlur();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.editInAppBlur);
            }
            if (FoxConfig.editInAppBlur) {
                listAdapter.notifyItemRangeInserted(appearanceDividerRow, 3);
            } else {
                listAdapter.notifyItemRangeRemoved(inAppBlurDividerRow, 3);
            }
            updateRowsId();
        } else if (position == showStoriesRow) {
            FoxConfig.toggleShowStories();
            if (view instanceof  TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showStories);
            }
            if (FoxConfig.nameType == FoxConfig.NAME_STORIES) {
                FoxConfig.saveNameType(FoxConfig.oldTitleText);
                listAdapter.notifyItemChanged(showInActionBarRow, PARTIAL);
                reloadDialogs();
            }
            restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        showGradientRow = -1;
        showAvatarRow = -1;
        drawerDarkenBackgroundRow = -1;
        drawerBlurBackgroundRow = -1;
        editBlurHeaderRow = -1;
        editBlurRow = -1;
        editBlurDividerRow = -1;
        showSantaHatRow = -1;
        showFallingSnowRow = -1;
        inAppBlurDividerRow = -1;
        editInAppBlurHeaderRow = -1;
        editInAppBlurRow = -1;

        drawerRow = rowCount++;
        drawerAvatarAsBackgroundRow = rowCount++;
        if (FoxConfig.avatarAsDrawerBackground) {
            showGradientRow = rowCount++;
            showAvatarRow = rowCount++;
            drawerDarkenBackgroundRow = rowCount++;
            drawerBlurBackgroundRow = rowCount++;
        }
        drawerDividerRow = rowCount++;
        if (FoxConfig.avatarBackgroundBlur && FoxConfig.avatarAsDrawerBackground) {
            editBlurHeaderRow = rowCount++;
            editBlurRow = rowCount++;
            editBlurDividerRow = rowCount++;
        }

        themeDrawerHeader = rowCount++;
        themeDrawerRow = rowCount++;
        menuItemsRow = rowCount++;
        themeDrawerDividerRow = rowCount++;

        dynamicButtonHeaderRow = rowCount++;
        dynamicButtonRow = rowCount++;
        dynamicDividerRow = rowCount++;

        appearanceHeaderRow = rowCount++;
        smoothNavRow = rowCount++;
        showPencilIconRow = rowCount++;
        if (((Theme.getEventType() == 0 && FoxConfig.eventType == 0) || FoxConfig.eventType == 1)) {
            showSantaHatRow = rowCount++;
            showFallingSnowRow = rowCount++;
        }
        roundedNumberSwitchRow = rowCount++;
        messageTimeSwitchRow = rowCount++;
        smartButtonsRow = rowCount++;
        forcePacmanRow = rowCount++;
        inAppBlurRow = rowCount++;
        if (FoxConfig.editInAppBlur) {
            inAppBlurDividerRow = rowCount++;
            editInAppBlurHeaderRow = rowCount++;
            editInAppBlurRow = rowCount++;
        }
        appearanceDividerRow = rowCount++;

        fontsAndEmojiHeaderRow = rowCount++;
        chooseEmojiPackRow = rowCount++;
        useSystemFontRow = rowCount++;
        fontsAndEmojiDividerRow = rowCount++;

        chatHeaderRow = rowCount++;
        showInActionBarRow = rowCount++;
        showStoriesRow = rowCount++;
        appBarShadowRow = rowCount++;
        searchIconInActionBarRow = rowCount++;
        slidingTitleRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiPacksLoaded) {
            if (CustomEmojiController.getLoadingStatus() == CustomEmojiController.FAILED) {
                AndroidUtilities.runOnUIThread(CustomEmojiController::loadEmojisInfo, 1000);
            } else {
                listAdapter.notifyItemChanged(chooseEmojiPackRow, PARTIAL);
            }
        }
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == editBlurHeaderRow) {
                        headerCell.setText(LocaleController.getString("BlurIntensity", R.string.BlurIntensity));
                    } else if (position == themeDrawerHeader) {
                        headerCell.setText(LocaleController.getString("SideBarIconSet", R.string.SideBarIconSet));
                    } else if (position == dynamicButtonHeaderRow) {
                        headerCell.setText(LocaleController.getString("ButtonShape", R.string.ButtonShape));
                    } else if (position == fontsAndEmojiHeaderRow) {
                        headerCell.setText(LocaleController.getString("FontsAndEmojis", R.string.FontsAndEmojis));
                    } else if (position == appearanceHeaderRow) {
                        headerCell.setText(LocaleController.getString("Appearance", R.string.Appearance));
                    } else if (position == chatHeaderRow) {
                        headerCell.setText(LocaleController.getString("ChatHeader", R.string.ChatHeader));
                    } else if (position == editInAppBlurHeaderRow) {
                        headerCell.setText(LocaleController.getString("BlurChatIntensity", R.string.BlurChatIntensity));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == showGradientRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShadeBackground", R.string.ShadeBackground), FoxConfig.showGradientColor, true);
                    } else if (position == showAvatarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowAvatar", R.string.ShowAvatar), FoxConfig.showAvatarImage, drawerBlurBackgroundRow != -1);
                    } else if (position == drawerAvatarAsBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarAsBackground", R.string.AvatarAsBackground), FoxConfig.avatarAsDrawerBackground, FoxConfig.avatarAsDrawerBackground);
                    } else if (position == drawerBlurBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarBlur", R.string.AvatarBlur), FoxConfig.avatarBackgroundBlur, true);
                    } else if (position == drawerDarkenBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarDarken", R.string.AvatarDarken), FoxConfig.avatarBackgroundDarken, true);
                    } else if (position == useSystemFontRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("UseSystemFonts", R.string.UseSystemFonts), FoxConfig.useSystemFont, false);
                    } else if (position == messageTimeSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("FormatTimeSeconds", R.string.FormatTimeSeconds), LocaleController.getString("FormatTimeSecondsDesc", R.string.FormatTimeSecondsDesc), FoxConfig.fullTime, true, true);
                    } else if (position == roundedNumberSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("NumberRounding", R.string.NumberRounding), LocaleController.getString("NumberRoundingDesc", R.string.NumberRoundingDesc), FoxConfig.roundedNumbers, true, true);
                    } else if (position == forcePacmanRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("PacManAnimation", R.string.PacManAnimation), FoxConfig.pacmanForced, true);
                    } else if (position == smoothNavRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("SmoothNav", R.string.SmoothNav), FoxConfig.smoothNav, true);
                    } else if (position == smartButtonsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShortcutsForAdmins", R.string.ShortcutsForAdmins), FoxConfig.smartButtons, true);
                    } else if (position == appBarShadowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AppBarShadow", R.string.AppBarShadow), FoxConfig.showAppBarShadow, true);
                    } else if (position == showSantaHatRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ChristmasHat", R.string.ChristmasHat), FoxConfig.showSantaHat, true);
                    } else if (position == showFallingSnowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("FallingSnow", R.string.FallingSnow), FoxConfig.showSnowFalling, true);
                    } else if (position == slidingTitleRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("SlidingTitle", R.string.SlidingTitle), LocaleController.getString("SlidingTitleDesc", R.string.SlidingTitleDesc), FoxConfig.slidingChatTitle, true, false);
                    } else if (position == searchIconInActionBarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("SearchIconTitleBar", R.string.SearchIconTitleBar), FoxConfig.searchIconInActionBar, true);
                    } else if (position == showPencilIconRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowPencilIcon", R.string.ShowPencilIcon), FoxConfig.showPencilIcon, true);
                    } else if (position == inAppBlurRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("Blur", R.string.Blur), FoxConfig.editInAppBlur, false);
                    } else if (position == showStoriesRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ProfileMyStories", R.string.ProfileMyStories), FoxConfig.showStories, true);
                    }
                    break;
                case PROFILE_PREVIEW:
                    DrawerProfilePreview cell = (DrawerProfilePreview) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
                case TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == menuItemsRow) {
                        textCell.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                        textCell.setTextAndIcon(LocaleController.getString("MenuItems", R.string.MenuItems), R.drawable.msg_newfilter, false);
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == chooseEmojiPackRow) {
                        textSettingsCell.setDrawLoading(CustomEmojiController.isLoading(), 30, partial);
                        String emojiPack = CustomEmojiController.getSelectedPackName();
                        textSettingsCell.setTextAndValue(LocaleController.getString("EmojiSets", R.string.EmojiSets), emojiPack, true);
                    } else if (position == showInActionBarRow) {
                        String defaultName;
                        switch (FoxConfig.nameType) {
                            case FoxConfig.NAME_APP:
                                defaultName = LocaleController.getString("Default", R.string.Default);
                                break;
                            case FoxConfig.NAME_DEFAULT_USER:
                                defaultName = LocaleController.getString("AccountNameTitleBar", R.string.AccountNameTitleBar);
                                break;
                            case FoxConfig.NAME_USERNAME:
                                defaultName = LocaleController.getString("Username", R.string.Username);
                                break;
                            case FoxConfig.NAME_STORIES:
                                defaultName = LocaleController.getString("ProfileMyStories", R.string.ProfileMyStories);
                                break;
                            default:
                                defaultName = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("TitleBarName", R.string.TitleBarName), defaultName, partial, true);
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.SWITCH || viewType == ViewType.TEXT_CELL || viewType == ViewType.SETTINGS;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case PROFILE_PREVIEW:
                    view = profilePreviewCell = new DrawerProfilePreview(context);
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case BLUR_INTENSITY:
                    view = new BlurIntensity(context) {
                        @Override
                        protected void onBlurIntensityChange(int percentage, boolean layout) {
                            super.onBlurIntensityChange(percentage, layout);
                            FoxConfig.saveBlurIntensity(percentage);
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(editBlurRow);
                            if (holder != null && holder.itemView instanceof BlurIntensity) {
                                BlurIntensity cell = (BlurIntensity) holder.itemView;
                                if (layout) {
                                    cell.requestLayout();
                                } else {
                                    cell.invalidate();
                                }
                            }
                            reloadMainInfo();
                            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CHAT_BLUR_INTENSITY:
                    view = new ChatBlurIntensity(context) {
                        @Override
                        protected void onBlurIntensityChange(int percentage, boolean layout) {
                            super.onBlurIntensityChange(percentage, layout);
                            FoxConfig.setBlurAlpha(percentage);
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(editInAppBlurRow);
                            if (holder != null && holder.itemView instanceof BlurIntensity) {
                                BlurIntensity cell = (BlurIntensity) holder.itemView;
                                if (layout) {
                                    cell.requestLayout();
                                } else {
                                    cell.invalidate();
                                }
                            }
                            listAdapter.notifyItemChanged(inAppBlurRow, PARTIAL);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case THEME_SELECTOR:
                    view = new ThemeSelectorDrawer(context, FoxConfig.eventType) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            int previousEvent = FoxConfig.eventType;
                            FoxConfig.saveEventType(eventSelected);
                            if (Theme.getEventType() == 0) {
                                if (previousEvent == 0) {
                                    previousEvent = 1;
                                } else if (eventSelected == 0) {
                                    eventSelected = 1;
                                }
                            }
                            if (previousEvent == 1 && eventSelected != 1) {
                                listAdapter.notifyItemRangeRemoved(forcePacmanRow + 1, 2);
                            } else if (previousEvent != 1 && eventSelected == 1) {
                                listAdapter.notifyItemRangeInserted(forcePacmanRow + 1, 2);
                            }
                            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
                            Theme.lastHolidayCheckTime = 0;
                            Theme.dialogs_holidayDrawable = null;
                            reloadMainInfo();
                            updateRowsId();
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case DYNAMIC_BUTTON_SELECTOR:
                    view = new DynamicButtonSelector(context) {
                        @Override
                        protected void onSelectionChange() {
                            super.onSelectionChange();
                            reloadInterface();
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == drawerDividerRow || position == editBlurDividerRow ||
                    position == themeDrawerDividerRow || position == dynamicDividerRow ||
                    position == fontsAndEmojiDividerRow || position == appearanceDividerRow ||
                    position == inAppBlurDividerRow) {
                return ViewType.SHADOW;
            } else if (position == editBlurHeaderRow || position == themeDrawerHeader ||
                    position == dynamicButtonHeaderRow || position == fontsAndEmojiHeaderRow ||
                    position == appearanceHeaderRow || position == chatHeaderRow ||
                    position == editInAppBlurHeaderRow) {
                return ViewType.HEADER;
            } else if (position == roundedNumberSwitchRow || position == messageTimeSwitchRow ||
                    position == useSystemFontRow || position == drawerAvatarAsBackgroundRow ||
                    position == drawerDarkenBackgroundRow || position == drawerBlurBackgroundRow ||
                    position == showGradientRow || position == showAvatarRow ||
                    position == forcePacmanRow || position == smoothNavRow ||
                    position == smartButtonsRow || position == appBarShadowRow ||
                    position == showSantaHatRow || position == showFallingSnowRow ||
                    position == slidingTitleRow || position == searchIconInActionBarRow ||
                    position == showPencilIconRow || position == inAppBlurRow ||
                    position == showStoriesRow) {
                return ViewType.SWITCH;
            } else if (position == drawerRow) {
                return ViewType.PROFILE_PREVIEW;
            } else if (position == editBlurRow) {
                return ViewType.BLUR_INTENSITY;
            } else if (position == editInAppBlurRow) {
                return ViewType.CHAT_BLUR_INTENSITY;
            } else if (position == menuItemsRow) {
                return ViewType.TEXT_CELL;
            } else if (position == themeDrawerRow) {
                return ViewType.THEME_SELECTOR;
            } else if (position == dynamicButtonRow) {
                return ViewType.DYNAMIC_BUTTON_SELECTOR;
            } else if (position == chooseEmojiPackRow || position == showInActionBarRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}