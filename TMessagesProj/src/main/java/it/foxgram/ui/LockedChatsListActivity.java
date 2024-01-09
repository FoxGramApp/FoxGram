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

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.foxgram.android.LockedChatsController;

public class LockedChatsListActivity extends BaseSettingsActivity {
    private int imageRow;
    private int addLocksRow;
    private int lockedChatsStartRow;
    private int lockedChatsEndRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("LockedChats", R.string.LockedChats);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position >= lockedChatsStartRow && position < lockedChatsEndRow) {
            Bundle args = new Bundle();
            long uid = LockedChatsController.getAllChats().get(position - lockedChatsStartRow).dialogId;
            args.putLong("dialog_id", uid);
            presentFragment(new ChatActivity(args)); // TODO
        } else if (position == addLocksRow) {
            presentFragment(new LockedChatsInfo());
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        if (LockedChatsController.getAllChats().size() == 0) {
            imageRow = rowCount++;
            addLocksRow = rowCount++;
        } else {
            lockedChatsStartRow = rowCount++;
            rowCount += LockedChatsController.getAllChats().size();
            lockedChatsEndRow = rowCount++;
        }
    }

    @Override
    protected BaseSettingsActivity.BaseListAdapter createAdapter() {
        return new LockedChatsListActivity.ListAdapter();
    }

    private class ListAdapter extends BaseSettingsActivity.BaseListAdapter {
        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case MANAGE_CHAT:
                    ManageChatUserCell userCell = (ManageChatUserCell) holder.itemView;
                    LockedChatsController.LockParams params = LockedChatsController.getAllChats().get(position - lockedChatsStartRow);
                    userCell.setData(params.chat, null, null, position != lockedChatsEndRow - 1);
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == addLocksRow) {
                        settingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
                        settingsCell.setText(LocaleController.getString("LockOtherChats", R.string.LockOtherChats), false);
                    }
                    break;
            }
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.IMAGE_HEADER) {
                LinearLayout stickerHeaderCell = new LinearLayout(context);
                stickerHeaderCell.setOrientation(LinearLayout.VERTICAL);
                StickerImageView backupImageView = new StickerImageView(context, currentAccount);
                backupImageView.setStickerPackName("UtyaDuckFull");
                backupImageView.setStickerNum(0);
                stickerHeaderCell.addView(backupImageView, LayoutHelper.createLinear(140, 140, Gravity.CENTER, 0, 20, 0, 5));
                TextView textView = new TextView(context);
                textView.setText(LocaleController.getString("NoFolderFound", R.string.NoFolderFound));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(0, AndroidUtilities.dp(10), 0, AndroidUtilities.dp(17));
                textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText4));
                textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                stickerHeaderCell.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER | Gravity.TOP, 25, 0, 25, 0));
                view = stickerHeaderCell;
            }
            return view;
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.MANAGE_CHAT || viewType == ViewType.SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position >= lockedChatsStartRow && position < lockedChatsEndRow) {
                return ViewType.MANAGE_CHAT;
            } else if (position == imageRow) {
                return ViewType.IMAGE_HEADER;
            } else if (position == addLocksRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
