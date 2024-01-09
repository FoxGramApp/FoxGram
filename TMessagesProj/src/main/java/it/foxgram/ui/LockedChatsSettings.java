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

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.GroupCreateActivity;

import it.foxgram.android.FoxConfig;
import it.foxgram.android.LockedChatsController;

public class LockedChatsSettings extends BaseSettingsActivity {
    private int imageRow;
    private int headerDefaultSettingsRow;
    private int enableLockRow;
    private int disableLockRow;
    private int lockedChatsHintRow;
    private int addLockRow;
    private int addLockedChatsRow;
    private int manageLockHintRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("LockedChats", R.string.LockedChats);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == enableLockRow || position == disableLockRow) {
            FoxConfig.setLockChats(position == enableLockRow);
            listAdapter.notifyItemRangeChanged(enableLockRow, 2, PARTIAL);
        } else if (position == addLockedChatsRow) {
            if (LockedChatsController.getAllChats().size() == 0) {
                Bundle args = new Bundle();
                args.putBoolean("isChatLock", true);
                GroupCreateActivity fragment = new GroupCreateActivity(args);
                fragment.setDelegate(ids -> {
                    for (int i = 0; i < ids.size(); i++) {
                        LockedChatsController.setChatLocked(ids.get(i), true);
                    }
                    listAdapter.notifyItemChanged(position, PARTIAL);
                });
                presentFragment(fragment);
            } else {
                presentFragment(new LockedChatsInfo());
            }
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        imageRow = rowCount++;

        headerDefaultSettingsRow = rowCount++;
        enableLockRow = rowCount++;
        disableLockRow = rowCount++;
        lockedChatsHintRow = rowCount++;

        addLockRow = rowCount++;
        addLockedChatsRow = rowCount++;
        manageLockHintRow = rowCount++;

        // TODO: Lock settings rows
        // ...
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
                    if (position == lockedChatsHintRow) {
                        hintCell.setText(LocaleController.getString("LockedChatsDesc", R.string.LockedChatsDesc));
                    } else if (position == manageLockHintRow) {
                        hintCell.setText(LocaleController.getString("LockedChatsDesc2", R.string.LockedChatsDesc2));
                    }
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerDefaultSettingsRow) {
                        headerCell.setText(LocaleController.getString("LockedChats", R.string.LockedChats));
                    } else if (position == addLockRow) {
                       headerCell.setText(LocaleController.getString("LockedChatsInfoHeader", R.string.LockedChatsInfoHeader));
                    }
                    break;
                case RADIO:
                    RadioCell radioCell = (RadioCell) holder.itemView;
                    if (position == enableLockRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.lockChats, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Enable", R.string.Enable), FoxConfig.lockChats, true);
                        }
                    } else if (position == disableLockRow) {
                        if (partial) {
                            radioCell.setChecked(!FoxConfig.lockChats, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Disable", R.string.Disable), !FoxConfig.lockChats, true);
                        }
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == addLockedChatsRow) {
                        settingsCell.setTextAndValue(LocaleController.getString("LockedChats", R.string.LockedChats), getChatCountText(LockedChatsController.getAllChats().size()), partial, true);
                    }
                    break;
            }
        }

        private String getChatCountText(int count) {
            String value = LocaleController.formatPluralString("Chats", count);
            value = count > 0 ? value : LocaleController.getString("FilterAddChats", R.string.FilterAddChats);
            return value;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.IMAGE_HEADER) {
                LinearLayout stickerHeaderCell = new LinearLayout(context);
                stickerHeaderCell.setOrientation(LinearLayout.VERTICAL);
                StickerImageView backupImageView = new StickerImageView(context, currentAccount);
                backupImageView.setStickerPackName("UtyaDuckFull");
                backupImageView.setStickerNum(33);
                stickerHeaderCell.addView(backupImageView, LayoutHelper.createLinear(140, 140, Gravity.CENTER, 0, 20, 0, 20));
                view = stickerHeaderCell;
            }
            return view;
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.ADD_EXCEPTION ||
                    viewType == ViewType.RADIO ||
                    viewType == ViewType.SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == lockedChatsHintRow || position == manageLockHintRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == headerDefaultSettingsRow || position == addLockRow) {
                return ViewType.HEADER;
            } else if (position == enableLockRow || position == disableLockRow) {
                return ViewType.RADIO;
            } else if (position == addLockedChatsRow) {
                return ViewType.SETTINGS;
            } else if (position == imageRow) {
                return ViewType.IMAGE_HEADER;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}

