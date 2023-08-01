package it.foxgram.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;

import java.util.ArrayList;

import it.foxgram.android.AlertController;
import it.foxgram.android.FoxConfig;
import it.foxgram.ui.Cells.DcStyleSelector;

public class FoxGramGeneralSettings extends BaseSettingsActivity {
    private int divisorPrivacyRow;
    private int privacyHeaderRow;
    private int phoneNumberSwitchRow;
    private int phoneContactsSwitchRow;
    private int dateRow;
    private int dcIdSettingsHeaderRow;
    private int dcStyleSelectorRow;
    private int dcIdRow;
    private int idTypeRow;
    private int divisorDCIdRow;
    private int hintIdRow;
    private int foldersHeaderRow;
    private int tabsUnreadCounterRow;
    private int foldersDividerRow;
    private int hideAllTabRow;
    private int notificationHeaderRow;
    private int notificationAccentRow;
    private int dividerNotificationRow;
    private int callHeaderRow;
    private int confirmCallSwitchRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("General", R.string.General);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == phoneNumberSwitchRow) {
            FoxConfig.toggleHidePhone();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.hidePhoneNumber);
            }
            reloadInterface();
            reloadMainInfo();
        } else if (position == phoneContactsSwitchRow) {
            FoxConfig.toggleHideContactNumber();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.hideContactNumber);
            }
        } else if (position == dcIdRow) {
            FoxConfig.toggleShowIDAndDC();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showIDAndDC);
            }
            reloadInterface();
        } else if (position == confirmCallSwitchRow) {
            FoxConfig.toggleConfirmCall();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.confirmCall);
            }
        } else if (position == hideAllTabRow) {
            FoxConfig.toggleHideAllTab();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.hideAllTab);
            }
            reloadDialogs();
        } else if (position == notificationAccentRow) {
            FoxConfig.toggleAccentColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.accentAsNotificationColor);
            }
        } else if (position == idTypeRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add("Bot API");
            types.add(0);
            arrayList.add("Telegram API");
            types.add(1);
            AlertController.show(arrayList, LocaleController.getString("IDType", R.string.IDType), types.indexOf(FoxConfig.idType), context, i -> {
                FoxConfig.setIdType(types.get(i));
                listAdapter.notifyItemChanged(idTypeRow, PARTIAL);
                reloadInterface();
            });
        } else if (position == dateRow) {
            FoxConfig.toggleShowAccountRegistrationDate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showAccountRegistrationDate);
                reloadInterface();
            }
        } else if (position == tabsUnreadCounterRow) {
            FoxConfig.toggleTabsUnreadCounter();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.tabsUnreadCounter);
            }
            reloadDialogs();
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        privacyHeaderRow = rowCount++;
        phoneNumberSwitchRow = rowCount++;
        phoneContactsSwitchRow = rowCount++;
        divisorPrivacyRow = rowCount++;

        dcIdSettingsHeaderRow = rowCount++;
        dcStyleSelectorRow = rowCount++;
        dcIdRow = rowCount++;
        dateRow = rowCount++;
        idTypeRow = rowCount++;
        divisorDCIdRow = rowCount++;
        hintIdRow = rowCount++;

        foldersHeaderRow = rowCount++;
        tabsUnreadCounterRow = rowCount++;
        hideAllTabRow = rowCount++;
        foldersDividerRow = rowCount++;

        notificationHeaderRow = rowCount++;
        notificationAccentRow = rowCount++;
        dividerNotificationRow = rowCount++;

        callHeaderRow = rowCount++;
        confirmCallSwitchRow = rowCount++;
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
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == privacyHeaderRow) {
                        headerCell.setText(LocaleController.getString("PrivacyTitle", R.string.PrivacyTitle));
                    } else if (position == callHeaderRow) {
                        headerCell.setText(LocaleController.getString("Calls", R.string.Calls));
                    } else if (position == dcIdSettingsHeaderRow) {
                        headerCell.setText(LocaleController.getString("DC_IDSettings", R.string.DC_IDSettings));
                    } else if (position == foldersHeaderRow) {
                        headerCell.setText(LocaleController.getString("Filters", R.string.Filters));
                    } else if (position == notificationHeaderRow) {
                        headerCell.setText(LocaleController.getString("Notifications", R.string.Notifications));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    textCheckCell.setCheckBoxIcon(0);
                    if (position == phoneNumberSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("HidePhone", R.string.HidePhone), LocaleController.getString("HidePhoneDesc", R.string.HidePhoneDesc), FoxConfig.hidePhoneNumber, true, true);
                    } else if (position == phoneContactsSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("HidePhoneOthers", R.string.HidePhoneOthers), LocaleController.getString("HidePhoneOthersDesc", R.string.HidePhoneOthersDesc), FoxConfig.hideContactNumber, true, true);
                    } else if (position == dateRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowDate", R.string.ShowDate), FoxConfig.showAccountRegistrationDate, true);
                    } else if (position == dcIdRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowID_DC", R.string.ShowID_DC), FoxConfig.showIDAndDC, true);
                    } else if (position == confirmCallSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("ConfirmCalls", R.string.ConfirmCalls), LocaleController.getString("ConfirmCallsDesc", R.string.ConfirmCallsDesc), FoxConfig.confirmCall, true, true);
                    } else if (position == hideAllTabRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideAllChatsFolder", R.string.HideAllChatsFolder), FoxConfig.hideAllTab, true);
                    } else if (position == notificationAccentRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AccentAsNotificationColor", R.string.AccentAsNotificationColor), FoxConfig.accentAsNotificationColor, true);
                    } else if (position == tabsUnreadCounterRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("TabsCounter", R.string.TabsCounter), FoxConfig.tabsUnreadCounter, true);
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == idTypeRow) {
                        String value;
                        switch (FoxConfig.idType) {
                            case 0:
                                value = "Bot API";
                                break;
                            default:
                            case 1:
                                value = "Telegram API";
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("IDType", R.string.IDType), value, partial,false);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == hintIdRow) {
                        textInfoPrivacyCell.setTopPadding(0);
                        textInfoPrivacyCell.setText(LocaleController.getString("IDTypeAbout", R.string.IDTypeAbout));
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.SWITCH || viewType == ViewType.SETTINGS;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.DC_STYLE_SELECTOR) {
                view = new DcStyleSelector(context) {
                    @Override
                    protected void onSelectedStyle() {
                        super.onSelectedStyle();
                        reloadInterface();
                    }
                };
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == divisorPrivacyRow || position == divisorDCIdRow ||
                    position == foldersDividerRow || position == dividerNotificationRow) {
                return ViewType.SHADOW;
            } else if (position == privacyHeaderRow || position == foldersHeaderRow || position == callHeaderRow ||
                    position == dcIdSettingsHeaderRow || position == notificationHeaderRow) {
                return ViewType.HEADER;
            } else if (position == phoneNumberSwitchRow || position == phoneContactsSwitchRow ||
                    position == dateRow || position == dcIdRow ||
                    position == confirmCallSwitchRow || position == tabsUnreadCounterRow ||
                    position == notificationAccentRow || position == hideAllTabRow) {
                return ViewType.SWITCH;
            } else if (position == idTypeRow) {
                return ViewType.SETTINGS;
            } else if (position == hintIdRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == dcStyleSelectorRow) {
                return ViewType.DC_STYLE_SELECTOR;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }

}
