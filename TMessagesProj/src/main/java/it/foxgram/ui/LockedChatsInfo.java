/*
 * This is the source code of FoxGram for Android v. 3.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2024.
 */

/*
 * This class is not ready to be implemented and it isn't working.
 * It is in working in progress stage and will be added in future updates, maybe.
 * NOT enable it, and note that some feature are missing.
 */

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
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.GroupCreateActivity;

import java.util.ArrayList;

import it.foxgram.android.LockedChatsController;

public class LockedChatsInfo extends BaseSettingsActivity {

    // TODO

    private int lockOtherChatsRow;
    private int lockedChatsStartRow;
    private int lockedChatsEndRow;
    private int dividerRow;
    private int lockedChatsRemoveAllRow;
    private int divider2Row;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == lockOtherChatsRow) {
            Bundle args = new Bundle();
            args.putBoolean("isChatLock", true);
            args.putInt("chatAddType", 1);
            GroupCreateActivity fragment = new GroupCreateActivity(args);
            fragment.setDelegate(ids -> {
                for (int i = 0; i < ids.size(); i++) {
                    LockedChatsController.setChatLocked(ids.get(i), true);
                }
                updateRowsId();
                listAdapter.notifyDataSetChanged();
            });
            presentFragment(fragment);
        } else if (position >= lockedChatsStartRow && position < lockedChatsEndRow) {
            long uid = LockedChatsController.getAllChats().get(position - lockedChatsStartRow).dialogId;
            presentFragment(new LockedChatsActivity(uid));
        } else if (position == lockedChatsRemoveAllRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("RemoveLockedChats", R.string.RemoveLockedChats));
            builder.setMessage(LocaleController.getString("RemoveLockedChatsDesc", R.string.RemoveLockedChatsDesc));
            builder.setPositiveButton(LocaleController.getString("RemoveLock", R.string.RemoveLock), (dialogInterface, i) -> {
                LockedChatsController.resetLockedChats();
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

        lockOtherChatsRow = rowCount++;
        lockedChatsStartRow = rowCount;
        rowCount += LockedChatsController.getAllChats().size();
        lockedChatsEndRow = rowCount;
        dividerRow = rowCount++;
        lockedChatsRemoveAllRow = rowCount++;
        divider2Row = rowCount++;
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("LockedChatsInfoHeader", R.string.LockedChatsInfoHeader);
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
                    if (position == lockOtherChatsRow) {
                        actionCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                        actionCell.setText(LocaleController.getString("LockOtherChats", R.string.LockOtherChats), null, R.drawable.msg_disable, false); // TODO: Add translation
                    }
                    break;
                case MANAGE_CHAT:
                    ManageChatUserCell userCell = (ManageChatUserCell) holder.itemView;
                    LockedChatsController.LockParams params = LockedChatsController.getAllChats().get(position - lockedChatsStartRow);
                    userCell.setDelegate((cell, click) -> {
                        if (click) {
                            final ArrayList<String> items = new ArrayList<>();
                            final ArrayList<Integer> actions = new ArrayList<>();
                            final ArrayList<Integer> icons = new ArrayList<>();
                            items.add(LocaleController.getString("Open", R.string.Open));
                            icons.add(R.drawable.msg_openin);
                            actions.add(0);
                            items.add(LocaleController.getString("RemoveLock", R.string.RemoveLock));
                            icons.add(R.drawable.msg_delete);
                            actions.add(1);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setItems(items.toArray(new CharSequence[actions.size()]), AndroidUtilities.toIntArray(icons), (dialogInterface, i) -> {
                                if (actions.get(i) == 0) {
                                    presentFragment(new LockedChatsActivity(params.dialogId));
                                } else if (actions.get(i) == 1) {
                                    showConfirmDelete(position, params);
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            showDialog(alertDialog);
                            alertDialog.setItemColor(1, Theme.getColor(Theme.key_dialogTextRed), Theme.getColor(Theme.key_dialogRedIcon));
                        }
                        return true;
                    });
                    userCell.setData(params.chat, null, null, position != lockedChatsEndRow - 1);
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == lockedChatsRemoveAllRow) {
                        settingsCell.setTag(Theme.key_dialogTextRed);
                        settingsCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                        settingsCell.setText(LocaleController.getString("RemoveAllLocks", R.string.RemoveAllLocks), false);
                    }
            }
        }

        private void showConfirmDelete(int position, LockedChatsController.LockParams info) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity());
            builder2.setTitle(LocaleController.getString("RemoveLock", R.string.RemoveLock));
            String chatTitle;
            if (info.chat instanceof TLRPC.Chat) {
                chatTitle = ((TLRPC.Chat) info.chat).title;
            } else {
                chatTitle = ContactsController.formatName(((TLRPC.User) info.chat).first_name, ((TLRPC.User) info.chat).last_name);
            }
            builder2.setMessage(LocaleController.formatString("RemoveChatLock", R.string.RemoveChatLock, chatTitle));
            builder2.setPositiveButton(LocaleController.getString("RemoveLock", R.string.RemoveLock), (dialogInterface2, i2) -> {
                LockedChatsController.setChatLocked(info.dialogId, false);
                if (LockedChatsController.getAllChats().isEmpty()) {
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
            if (position == lockOtherChatsRow) {
                return ViewType.ADD_EXCEPTION;
            } else if (position >= lockedChatsStartRow && position < lockedChatsEndRow) {
                return ViewType.MANAGE_CHAT;
            } else if (position == dividerRow || position == divider2Row) {
                return ViewType.SHADOW;
            } else if (position == lockedChatsRemoveAllRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}

