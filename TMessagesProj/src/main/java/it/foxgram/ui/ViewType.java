/*
 * This is the source code of FoxGram for Android v. 3.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2023.
 */
package it.foxgram.ui;

enum ViewType {
    ACCOUNT,
    ADD_EXCEPTION,
    BLUR_INTENSITY,
    CHAT_BLUR_INTENSITY,
    CAMERA_SELECTOR,
    CHAT,
    CHECKBOX,
    CHECKBOX_CELL,
    CREATION_TEXT_CELL,
    DC_STYLE_SELECTOR,
    DETAILED_SETTINGS,
    DYNAMIC_BUTTON_SELECTOR,
    EDIT_TOPIC,
    EMOJI_PACK_SET_CELL,
    HEADER,
    HEADER_NO_SHADOW,
    HINT_HEADER,
    IMAGE_HEADER,
    MANAGE_CHAT,
    MENU_ITEM,
    PLACEHOLDER,
    PROFILE_PREVIEW,
    RADIO,
    SETTINGS,
    SHADOW,
    SLIDE_CHOOSE,
    STICKER_HOLDER,
    STICKER_SIZE,
    SUGGESTED_OPTIONS,
    SWITCH,
    TEXT_CELL,
    TEXT_CHECK_CELL2,
    TEXT_HINT,
    TEXT_HINT_WITH_PADDING,
    TEXT_RADIO,
    THEME_SELECTOR,
    UPDATE,
    UPDATE_CHECK;

    static ViewType fromInt(int i) {
        return values()[i];
    }

    int toInt() {
        return ordinal();
    }
}
