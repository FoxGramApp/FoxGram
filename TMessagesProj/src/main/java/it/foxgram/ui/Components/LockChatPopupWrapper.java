package it.foxgram.ui.Components;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PopupSwipeBackLayout;

import it.foxgram.android.LockConfig;
import it.foxgram.ui.LockedChatsGroupInfo;

public class LockChatPopupWrapper {
    public ActionBarPopupWindow.ActionBarPopupWindowLayout windowLayout;
    private final long dialogId;
    private final int topicId;
    private final ActionBarMenuSubItem defaultItem;
    private ActionBarMenuSubItem enableItem;
    private ActionBarMenuSubItem disableItem;
    private LockChatPopupWrapper.FragmentDelegate delegate;
    private final boolean isAllowLockChat;

    public LockChatPopupWrapper(BaseFragment fragment, boolean isForum, ActionBarMenuItem otherItem, long dialogId, int topicId, boolean isAllowLockChat, Theme.ResourcesProvider resourcesProvider) {
        this.isAllowLockChat = isAllowLockChat;
        Context context = fragment.getParentActivity();
        windowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(context, 0, resourcesProvider);
        windowLayout.setFitItems(true);
        this.dialogId = dialogId;
        this.topicId = topicId;
        PopupSwipeBackLayout swipeBackLayout = otherItem.getPopupLayout().getSwipeBack();
        if (swipeBackLayout != null) {
            var backItem = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_arrow_back, LocaleController.getString("Back", R.string.Back), false, resourcesProvider);
            backItem.setOnClickListener(view -> swipeBackLayout.closeForeground());
        }

        defaultItem = ActionBarMenuItem.addItem(windowLayout, 0, LocaleController.getString("Default", R.string.Default), true, resourcesProvider);

        defaultItem.setOnClickListener(view -> {
            if (topicId == 0) {
                LockConfig.removeGroupException(dialogId);
            } else {
                LockConfig.setDefault(dialogId, topicId);
            }
            updateItems();
            updateLastFragment();
        });

        if (topicId == 0 || LockConfig.isLastTopicAvailable(dialogId, topicId, false)) {
            enableItem = ActionBarMenuItem.addItem(windowLayout, 0, LocaleController.getString("Lock", R.string.Lock), true, resourcesProvider);
            enableItem.setOnClickListener(view -> {
                LockConfig.setEnabled(dialogId, topicId, true);
                updateItems();
                updateLastFragment();
            });
        }

        if (topicId == 0 || LockConfig.isLastTopicAvailable(dialogId, topicId, true)) {
            disableItem = ActionBarMenuItem.addItem(windowLayout, 0, LocaleController.getString("KeepUnlocked", R.string.KeepUnlocked), true, resourcesProvider);
            disableItem.setOnClickListener(view -> {
                LockConfig.setEnabled(dialogId, topicId, false);
                updateItems();
                updateLastFragment();
            });
        }
        updateItems();

        if (isForum) {
            ActionBarMenuSubItem customItem = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_customize, LocaleController.getString("AutoDeleteCustom", R.string.AutoDeleteCustom), false, resourcesProvider);
            customItem.setOnClickListener(view -> {
                otherItem.toggleSubMenu();
                LockedChatsGroupInfo groupInfo = new LockedChatsGroupInfo(MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId), this::updateItems);
                fragment.presentFragment(groupInfo);
            });
        }

        FrameLayout gap = new FrameLayout(context);
        gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider));
        View gapShadow = new View(context);
        gapShadow.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        gap.addView(gapShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        gap.setTag(R.id.fit_width_tag, 1);
        windowLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

        TextView textView = new TextView(context);
        textView.setTag(R.id.fit_width_tag, 1);
        textView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        textView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider));
        textView.setText(LocaleController.getString("LockedChatsDesc", R.string.LockedChatsDesc));
        windowLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    public void updateItems() {
        if (topicId == 0) {
            boolean allowed = LockConfig.getExceptionsById(true, dialogId);
            boolean disabled = LockConfig.getExceptionsById(false, dialogId);
            defaultItem.setChecked(!allowed && !disabled);
            if (enableItem != null) {
                enableItem.setChecked(allowed);
            }
            if (disableItem != null) {
                disableItem.setChecked(disabled);
            }
        } else {
            defaultItem.setChecked(LockConfig.isDefault(dialogId, topicId));
            if (enableItem != null) enableItem.setChecked(LockConfig.hasChatLockConfig(dialogId, topicId) && LockConfig.isChatLock(dialogId, topicId));
            if (disableItem != null) disableItem.setChecked(LockConfig.hasChatLockConfig(dialogId, topicId) && !LockConfig.isChatLock(dialogId, topicId));
        }
    }

    private void updateLastFragment() {
        if (delegate == null) return;
        if (LockConfig.getExceptions(isAllowLockChat).isEmpty()) {
            this.delegate.hideLastFragment();
        } else {
            this.delegate.showLastFragment();
        }
    }

    public void setDelegate(LockChatPopupWrapper.FragmentDelegate delegate) {
        this.delegate = delegate;
    }

    public interface FragmentDelegate {
        void hideLastFragment();
        void showLastFragment();
    }
}