package it.foxgram.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.GroupCreateActivity;

import it.foxgram.android.FoxConfig;
import it.foxgram.android.LockConfig;

public class LockedChatsSettings extends BaseSettingsActivity {

    private int headerDefaultSettingsRow;
    private int enableLockRow;
    private int disableLockRow;
    private int lockHintRow;
    private int addExceptionsRow;
    private int alwaysAllowExceptionRow;
    private int neverAllowExceptionRow;
    private int resetExceptionsRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("LockedChats", R.string.LockedChats);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == enableLockRow || position == disableLockRow) {
            FoxConfig.setLockedChats(position == enableLockRow);
            listAdapter.notifyItemRangeChanged(enableLockRow, 2, PARTIAL);
        } else if (position == alwaysAllowExceptionRow || position == neverAllowExceptionRow) {
            final boolean isAllow = position == alwaysAllowExceptionRow;
            if (LockConfig.getExceptions(isAllow).size() == 0) {
                Bundle args = new Bundle();
                args.putBoolean(isAllow ? "isAllowLockChat" : "isNeverLockChat", true);
                args.putInt("chatAddType", 1);
                GroupCreateActivity fragment = new GroupCreateActivity(args);
                fragment.setDelegate(ids -> {
                    for (int i = 0; i < ids.size(); i++) {
                        LockConfig.setEnabled(ids.get(i), 0, isAllow);
                    }
                    listAdapter.notifyItemChanged(position, PARTIAL);
                });
                presentFragment(fragment);
            } else {
                presentFragment(new LockedChatsException(isAllow));
            }
        } else if (position == resetExceptionsRow && LockConfig.getAllExceptions().size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("NotificationsDeleteAllLocks", R.string.NotificationsDeleteAllLocks));
            builder.setMessage(LocaleController.getString("NotificationsDeleteAllLocksAlert", R.string.NotificationsDeleteAllLocksAlert));
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                LockConfig.resetExceptions();
                listAdapter.notifyItemRangeChanged(alwaysAllowExceptionRow, 2, PARTIAL);
                listAdapter.notifyItemChanged(resetExceptionsRow);
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        headerDefaultSettingsRow = rowCount++;
        enableLockRow = rowCount++;
        disableLockRow = rowCount++;
        lockHintRow = rowCount++;
        addExceptionsRow = rowCount++;
        alwaysAllowExceptionRow = rowCount++;
        neverAllowExceptionRow = rowCount++;
        resetExceptionsRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {


        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell hintCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == lockHintRow) {
                        hintCell.setText(LocaleController.getString("LockedChatsDesc", R.string.LockedChatsDesc));
                    }
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerDefaultSettingsRow) {
                        headerCell.setText(LocaleController.getString("EnableChatLock", R.string.EnableChatLock));
                    } else if (position == addExceptionsRow) {
                        headerCell.setText(LocaleController.getString("WhatChats", R.string.WhatChats));
                    }
                    break;
                case RADIO:
                    RadioCell radioCell = (RadioCell) holder.itemView;
                    if (position == enableLockRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.lockedChats, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Enable", R.string.Enable), FoxConfig.lockedChats, true);
                        }
                    } else if (position == disableLockRow) {
                        if (partial) {
                            radioCell.setChecked(!FoxConfig.lockedChats, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Disable", R.string.Disable), !FoxConfig.lockedChats, true);
                        }
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == alwaysAllowExceptionRow) {
                        settingsCell.setTextAndValue(LocaleController.getString("LockedChats", R.string.LockedChats), getExceptionText(LockConfig.getExceptions(true).size()), partial, true);
                    } else if (position == neverAllowExceptionRow) {
                        settingsCell.setTextAndValue(LocaleController.getString("KeepAlwaysUnlocked", R.string.KeepAlwaysUnlocked), getExceptionText(LockConfig.getExceptions(false).size()), partial, false);
                    } else if (position == resetExceptionsRow) {
                        settingsCell.setTag(Theme.key_dialogTextRed);
                        settingsCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                        settingsCell.setCanDisable(true);
                        settingsCell.setText(LocaleController.getString("NotificationsDeleteAllLocks", R.string.NotificationsDeleteAllLocks), false);
                    }
                    break;
            }
        }

        private String getExceptionText(int count) {
            String value = LocaleController.formatPluralString("Chats", count);
            value = count > 0 ? value : LocaleController.getString("FilterAddChats", R.string.FilterAddChats);
            return value;
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            boolean canReset = LockConfig.getAllExceptions().size() > 0;
            return viewType == ViewType.ADD_EXCEPTION ||
                    viewType == ViewType.RADIO ||
                    viewType == ViewType.SETTINGS && position != resetExceptionsRow ||
                    viewType == ViewType.SETTINGS && canReset;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == lockHintRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == headerDefaultSettingsRow || position == addExceptionsRow) {
                return ViewType.HEADER;
            } else if (position == enableLockRow || position == disableLockRow) {
                return ViewType.RADIO;
            } else if (position == alwaysAllowExceptionRow || position == neverAllowExceptionRow || position == resetExceptionsRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
