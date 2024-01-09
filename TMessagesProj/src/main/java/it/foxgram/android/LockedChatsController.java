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

package it.foxgram.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LockedChatsController {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("FoxLockedChats", Context.MODE_PRIVATE);

    public static boolean isChatLocked(long dialogId) { // TODO
        if (getAllChats().size() == 0 || !FoxConfig.lockChats) {
            return false;
        }

        return preferences.getBoolean(getLockKey(dialogId), true);
    }

    public static void setChatLocked(long dialogId, boolean enable) {
        preferences.edit().putBoolean(getLockKey(dialogId), enable).apply();
    }

    public static String getLockKey(long dialogId) {
        return "locked_" + dialogId;
    }

    public static class LockParams {
        public long dialogId;
        public boolean enable;
        public TLObject chat;

        public LockParams(long dialogId, boolean enable) {
            this.dialogId = dialogId;
            this.enable = enable;
            if (dialogId > 0) {
                this.chat = MessagesController.getInstance(UserConfig.selectedAccount).getUser(dialogId);
            } else {
                this.chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId);
            }
        }
    }

    public static List<LockedChatsController.LockParams> getAllChats() {
        return preferences.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("locked_"))
                .map(entry -> new LockParams(
                        Long.parseLong(entry.getKey().split("_")[1]),
                        (boolean) entry.getValue()))
                .filter(lock -> lock.chat != null)
                .filter(param -> param.enable)
                .filter(o1 -> !TextUtils.isEmpty(o1.chat instanceof TLRPC.User ? ((TLRPC.User) o1.chat).first_name : ((TLRPC.Chat) o1.chat).title))
                .sorted((Comparator.comparing(o1 -> o1.chat instanceof TLRPC.User ?
                        ((TLRPC.User) o1.chat).first_name :
                        ((TLRPC.Chat) o1.chat).title)))
                .filter(entry -> entry.enable)
                .collect(Collectors.toList());
    }

    public static void resetLockedChats() {
        preferences.edit().clear().apply();
    }
}
