package it.foxgram.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

import it.foxgram.android.LockConfig;

public class LockedChatsException extends BaseSettingsActivity {

    private final boolean isAllow;

    private int addUsersOrGroupsRow;
    private int exceptionsStartRow;
    private int exceptionsEndRow;
    private int dividerRow;
    private int deleteAllExceptionsRow;
    private int divider2Row;

    public LockedChatsException(boolean isAllow) {
        this.isAllow = isAllow;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == addUsersOrGroupsRow) {
            Bundle args = new Bundle();
            args.putBoolean(isAllow ? "isAllowLockChat" : "isNerverLockChat", true);
            args.putInt("chatAddType", 1);
            GroupCreateActivity fragment = new GroupCreateActivity(args);
            fragment.setDelegate(ids -> {
                for (int i = 0; i < ids.size(); i++) {
                    LockConfig.setEnabled(ids.get(i), 0, isAllow);
                }
                updateRowsId();
                listAdapter.notifyDataSetChanged();
            });
            presentFragment(fragment);
        } else if (position >= exceptionsStartRow && position < exceptionsEndRow) {
            Bundle args = new Bundle();
            long uid = LockConfig.getExceptions(isAllow).get(position - exceptionsStartRow).dialogId;
            if (DialogObject.isUserDialog(uid)) {
                args.putLong("user_id", uid);
            } else {
                args.putLong("chat_id", -uid);
            }
            args.putBoolean("isSettings", true);
            args.putBoolean("isAllowLockChat", isAllow);
            presentFragment(new ProfileActivity(args));
        } else if (position == deleteAllExceptionsRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("NotificationsDeleteAllLocks", R.string.NotificationsDeleteAllLocks));
            builder.setMessage(LocaleController.getString("NotificationsDeleteAllLocksAlert", R.string.NotificationsDeleteAllLocksAlert));
            builder.setPositiveButton(LocaleController.getString("RemoveLock", R.string.RemoveLock), (dialogInterface, i) -> {
                LockConfig.removeAllTypeExceptions(isAllow);
                finishFragment();
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

        addUsersOrGroupsRow = rowCount++;
        exceptionsStartRow = rowCount;
        rowCount += LockConfig.getExceptions(isAllow).size();
        exceptionsEndRow = rowCount;
        dividerRow = rowCount++;
        deleteAllExceptionsRow = rowCount++;
        divider2Row = rowCount++;
    }

    @Override
    protected String getActionBarTitle() {
        return isAllow ? LocaleController.getString("AlwaysAllow", R.string.AlwaysAllow) : LocaleController.getString("NeverAllow", R.string.NeverAllow);
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case ADD_EXCEPTION:
                    ManageChatTextCell actionCell = (ManageChatTextCell) holder.itemView;
                    if (position == addUsersOrGroupsRow) {
                        actionCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                        actionCell.setText(LocaleController.getString("PrivacyAddAnException", R.string.PrivacyAddAnException), null, R.drawable.msg_contact_add, false);
                    }
                    break;
                case MANAGE_CHAT:
                    ManageChatUserCell userCell = (ManageChatUserCell) holder.itemView;
                    LockConfig.LockedChatsException info = LockConfig.getExceptions(isAllow).get(position - exceptionsStartRow);
                    userCell.setDelegate((cell, click) -> {
                        if (click) {
                            final ArrayList<String> items = new ArrayList<>();
                            final ArrayList<Integer> actions = new ArrayList<>();
                            final ArrayList<Integer> icons = new ArrayList<>();
                            if (info.chat instanceof TLRPC.Chat && ((TLRPC.Chat) info.chat).forum) {
                                items.add(LocaleController.getString("EditTopicLocks", R.string.EditTopicLocks));
                                actions.add(0);
                                icons.add(R.drawable.msg_edit);
                            }
                            items.add(LocaleController.getString("RemoveLock", R.string.RemoveLock));
                            icons.add(R.drawable.msg_delete);
                            actions.add(1);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setItems(items.toArray(new CharSequence[actions.size()]), AndroidUtilities.toIntArray(icons), (dialogInterface, i) -> {
                                if (actions.get(i) == 0) {
                                    LockedChatsGroupInfo lockedChatsGroupInfo = new LockedChatsGroupInfo((TLRPC.Chat) info.chat, isAllow);
                                    presentFragment(lockedChatsGroupInfo);
                                } else if (actions.get(i) == 1) {
                                    showConfirmDelete(position, info);
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            showDialog(alertDialog);
                            alertDialog.setItemColor(items.size() - 1, Theme.getColor(Theme.key_dialogTextRed), Theme.getColor(Theme.key_dialogRedIcon));
                        }
                        return true;
                    });
                    userCell.setData(info.chat, null, null, position != exceptionsEndRow - 1);
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == deleteAllExceptionsRow) {
                        settingsCell.setTag(Theme.key_dialogTextRed);
                        settingsCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                        settingsCell.setText(LocaleController.getString("NotificationsDeleteAllLocks", R.string.NotificationsDeleteAllLocks), false);
                    }
            }
        }

        private void showConfirmDelete(int position, LockConfig.LockedChatsException info) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity());
            builder2.setTitle(LocaleController.getString("RemoveLock", R.string.RemoveLock));
            String chatTitle;
            if (info.chat instanceof TLRPC.Chat) {
                chatTitle = ((TLRPC.Chat) info.chat).title;
            } else {
                chatTitle = ContactsController.formatName(((TLRPC.User) info.chat).first_name, ((TLRPC.User) info.chat).last_name);
            }
            builder2.setMessage(LocaleController.formatString("EditRemoveLockText", R.string.EditRemoveLockText, chatTitle));
            builder2.setPositiveButton(LocaleController.getString("RemoveLock", R.string.RemoveLock), (dialogInterface2, i2) -> {
                if (info.chat instanceof TLRPC.Chat) {
                    LockConfig.removeGroupException(info.dialogId);
                } else {
                    LockConfig.setDefault(info.dialogId, 0);
                }
                if (LockConfig.getExceptions(isAllow).isEmpty()) {
                    finishFragment();
                } else {
                    listAdapter.notifyItemRemoved(position);
                    updateRowsId();
                }
            });
            builder2.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder2.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.ADD_EXCEPTION || viewType == ViewType.MANAGE_CHAT || viewType == ViewType.SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == addUsersOrGroupsRow) {
                return ViewType.ADD_EXCEPTION;
            } else if (position >= exceptionsStartRow && position < exceptionsEndRow) {
                return ViewType.MANAGE_CHAT;
            } else if (position == dividerRow || position == divider2Row) {
                return ViewType.SHADOW;
            } else if (position == deleteAllExceptionsRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
