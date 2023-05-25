package it.colorgram.ui;

import android.text.Spannable;
import android.text.SpannableString;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.video.Quality;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckCell2;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.colorgram.android.AlertController;
import it.colorgram.android.ColorConfig;
import it.colorgram.android.camera.CameraXUtils;
import it.colorgram.android.entities.EntitiesHelper;
import it.colorgram.android.media.AudioEnhance;
import it.colorgram.ui.Cells.CameraTypeSelector;
import it.colorgram.ui.Cells.StickerSize;

public class colorgramChatSettings extends BaseSettingsActivity implements NotificationCenter.NotificationCenterDelegate {

    private int stickerSizeHeaderRow;
    private int stickerSizeRow;
    private int stickerSizeDividerRow;
    private int chatHeaderRow;
    private int jumpChannelRow;
    private int showGreetings;
    private int hideKeyboardRow;
    private int playGifAsVideoRow;
    private int chatDividerRow;
    private int foldersHeaderRow;
    private int showFolderWhenForwardRow;
    private int foldersDividerRow;
    private int messageMenuHeaderRow;
    private int showAddToSMRow;
    private int showRepeatRow;
    private int showNoQuoteForwardRow;
    private int showReportRow;
    private int showMessageDetailsRow;
    private int showCopyPhotoRow;
    private int showPatpatRow;
    private int audioVideoDividerRow;
    private int audioVideoHeaderRow;
    private int rearCameraStartingRow;
    private int confirmSendRow;
    private int confirmSendGifsRow;
    private int confirmSendStickersRow;
    private int confirmSendAudioRow;
    private int confirmSendVideoRow;
    private int showDeleteRow;
    private int hideAllTabRow;
    private int cameraTypeHeaderRow;
    private int cameraTypeSelectorRow;
    private int cameraXOptimizeRow;
    private int cameraXQualityRow;
    private int cameraAdviseRow;
    private int proximitySensorRow;
    private int suppressionRow;
    private int turnSoundOnVDKeyRow;
    private int openArchiveOnPullRow;
    private int hideTimeOnStickerRow;
    private int onlineStatusRow;
    private int hideSendAsChannelRow;

    private boolean confirmSendExpanded;

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == jumpChannelRow) {
            ColorConfig.toggleJumpChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.jumpChannel);
            }
        } else if (position == showGreetings) {
            ColorConfig.toggleShowGreetings();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.showGreetings);
            }
        } else if (position == hideKeyboardRow) {
            ColorConfig.toggleHideKeyboard();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.hideKeyboard);
            }
        } else if (position == playGifAsVideoRow) {
            ColorConfig.toggleGifAsVideo();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.gifAsVideo);
            }
        } else if (position == showFolderWhenForwardRow) {
            ColorConfig.toggleShowFolderWhenForward();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.showFolderWhenForward);
            }
        } else if (position == rearCameraStartingRow) {
            ColorConfig.toggleUseRearCamera();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.useRearCamera);
            }
        } else if (position == showAddToSMRow) {
            ColorConfig.contextMenu.toggleSaveMessage();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.saveMessage);
            }
        } else if (position == showRepeatRow) {
            ColorConfig.contextMenu.toggleRepeatMessage();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.repeatMessage);
            }
        } else if (position == showMessageDetailsRow) {
            ColorConfig.contextMenu.toggleMessageDetails();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.messageDetails);
            }
        } else if (position == showNoQuoteForwardRow) {
            ColorConfig.contextMenu.toggleNoQuoteForward();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.noQuoteForward);
            }
        } else if (position == showReportRow) {
            ColorConfig.contextMenu.toggleReportMessage();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.reportMessage);
            }
        } else if (position == showDeleteRow) {
            ColorConfig.contextMenu.toggleClearFromCache();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.clearFromCache);
            }
        } else if (position == showCopyPhotoRow) {
            ColorConfig.contextMenu.toggleCopyPhoto();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.copyPhoto);
            }
        } else if (position == hideAllTabRow) {
            ColorConfig.toggleHideAllTab();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.hideAllTab);
            }
            reloadDialogs();
        } else if (position == showPatpatRow) {
            ColorConfig.contextMenu.togglePatpat();
            ColorConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(ColorConfig.contextMenu.patpat);
            }
        } else if (position == cameraXOptimizeRow) {
            ColorConfig.toggleCameraXOptimizedMode();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.useCameraXOptimizedMode);
            }
        } else if (position == cameraXQualityRow) {
            Map<Quality, Size> availableSizes = CameraXUtils.getAvailableVideoSizes();
            Stream<Integer> tmp = availableSizes.values().stream().sorted(Comparator.comparingInt(Size::getWidth).reversed()).map(Size::getHeight);
            ArrayList<Integer> types = tmp.collect(Collectors.toCollection(ArrayList::new));
            ArrayList<String> arrayList = types.stream().map(p -> p + "p").collect(Collectors.toCollection(ArrayList::new));
            AlertController.show(arrayList, LocaleController.getString("CameraQuality", R.string.CameraQuality), types.indexOf(ColorConfig.cameraResolution), context, i -> {
                ColorConfig.saveCameraResolution(types.get(i));
                listAdapter.notifyItemChanged(cameraXQualityRow, PARTIAL);
            });
        } else if (position == proximitySensorRow) {
            ColorConfig.toggleDisableProximityEvents();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.disableProximityEvents);
            }
            restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
        } else if (position == suppressionRow) {
            ColorConfig.toggleVoicesAgc();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.voicesAgc);
            }
        } else if (position == turnSoundOnVDKeyRow) {
            ColorConfig.toggleTurnSoundOnVDKey();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.turnSoundOnVDKey);
            }
        } else if (position == openArchiveOnPullRow) {
            ColorConfig.toggleOpenArchiveOnPull();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.openArchiveOnPull);
            }
        } else if (position == hideTimeOnStickerRow) {
            ColorConfig.toggleHideTimeOnSticker();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.hideTimeOnSticker);
            }
        } else if (position == onlineStatusRow) {
            ColorConfig.toggleShowStatusInChat();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.showStatusInChat);
            }
        } else if (position == hideSendAsChannelRow) {
            ColorConfig.toggleHideSendAsChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(ColorConfig.hideSendAsChannel);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_CHAT);
        } else if (position == confirmSendRow) {
            confirmSendExpanded ^= true;
            updateRowsId();
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
            if (confirmSendExpanded) {
                listAdapter.notifyItemRangeInserted(confirmSendRow + 1, 4);
            } else {
                listAdapter.notifyItemRangeRemoved(confirmSendRow + 1, 4);
            }
        } else if (position == confirmSendStickersRow) {
            ColorConfig.confirmSending.toggleStickers();
            ColorConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendStickersRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        } else if (position == confirmSendGifsRow) {
            ColorConfig.confirmSending.toggleGifs();
            ColorConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendGifsRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        } else if (position == confirmSendAudioRow) {
            ColorConfig.confirmSending.toggleAudio();
            ColorConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendAudioRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        } else if (position == confirmSendVideoRow) {
            ColorConfig.confirmSending.toggleVideo();
            ColorConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendVideoRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        }
    }

    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            ColorConfig.setStickerSize(14);
            menuItem.setVisibility(View.GONE);
            listAdapter.notifyItemChanged(stickerSizeRow, new Object());
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("Chat", R.string.Chat);
    }

    @Override
    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_reset, LocaleController.getString("ResetStickersSize", R.string.ResetStickersSize));
        menuItem.setVisibility(ColorConfig.stickerSizeStack != 14.0f ? View.VISIBLE : View.GONE);
        return menuItem;
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        cameraTypeHeaderRow = -1;
        cameraTypeSelectorRow = -1;
        cameraXOptimizeRow = -1;
        cameraXQualityRow = -1;
        cameraAdviseRow = -1;
        suppressionRow = -1;
        confirmSendGifsRow = -1;
        confirmSendStickersRow = -1;
        confirmSendAudioRow = -1;
        confirmSendVideoRow = -1;

        stickerSizeHeaderRow = rowCount++;
        stickerSizeRow = rowCount++;
        stickerSizeDividerRow = rowCount++;

        if (CameraXUtils.isCameraXSupported()) {
            cameraTypeHeaderRow = rowCount++;
            cameraTypeSelectorRow = rowCount++;
            if (ColorConfig.cameraType == 1) {
                cameraXOptimizeRow = rowCount++;
                cameraXQualityRow = rowCount++;
            }
            cameraAdviseRow = rowCount++;
        }

        chatHeaderRow = rowCount++;
        jumpChannelRow = rowCount++;
        showGreetings = rowCount++;
        playGifAsVideoRow = rowCount++;
        hideKeyboardRow = rowCount++;
        hideSendAsChannelRow = rowCount++;
        openArchiveOnPullRow = rowCount++;
        onlineStatusRow = rowCount++;
        chatDividerRow = rowCount++;

        audioVideoHeaderRow = rowCount++;
        if (AudioEnhance.isAvailable()) {
            suppressionRow = rowCount++;
        }
        turnSoundOnVDKeyRow = rowCount++;
        proximitySensorRow = rowCount++;
        rearCameraStartingRow = rowCount++;
        confirmSendRow = rowCount++;
        if (confirmSendExpanded) {
            confirmSendStickersRow = rowCount++;
            confirmSendGifsRow = rowCount++;
            confirmSendAudioRow = rowCount++;
            confirmSendVideoRow = rowCount++;
        }
        hideTimeOnStickerRow = rowCount++;
        audioVideoDividerRow = rowCount++;

        foldersHeaderRow = rowCount++;
        hideAllTabRow = rowCount++;
        showFolderWhenForwardRow = rowCount++;
        foldersDividerRow = rowCount++;

        messageMenuHeaderRow = rowCount++;
        showDeleteRow = rowCount++;
        showCopyPhotoRow = rowCount++;
        showNoQuoteForwardRow = rowCount++;
        showAddToSMRow = rowCount++;
        showRepeatRow = rowCount++;
        showPatpatRow = rowCount++;
        showReportRow = rowCount++;
        showMessageDetailsRow = rowCount++;
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
                    if (position == chatHeaderRow) {
                        headerCell.setText(LocaleController.getString("Chat", R.string.Chat));
                    } else if (position == foldersHeaderRow) {
                        headerCell.setText(LocaleController.getString("Filters", R.string.Filters));
                    } else if (position == audioVideoHeaderRow) {
                        headerCell.setText(LocaleController.getString("MediaSettings", R.string.MediaSettings));
                    } else if (position == messageMenuHeaderRow) {
                        headerCell.setText(LocaleController.getString("ContextMenu", R.string.ContextMenu));
                    } else if (position == stickerSizeHeaderRow) {
                        headerCell.setText(LocaleController.getString("StickersSize", R.string.StickersSize));
                    } else if (position == cameraTypeHeaderRow) {
                        headerCell.setText(LocaleController.getString("CameraType", R.string.CameraType));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == jumpChannelRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("JumpToNextChannel", R.string.JumpToNextChannel), ColorConfig.jumpChannel, true);
                    } else if (position == showGreetings) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("GreetingSticker", R.string.GreetingSticker), ColorConfig.showGreetings, true);
                    } else if (position == hideKeyboardRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideChatKeyboard", R.string.HideChatKeyboard), ColorConfig.hideKeyboard, true);
                    } else if (position == playGifAsVideoRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("GIFsAsVideo", R.string.GIFsAsVideo), ColorConfig.gifAsVideo, true);
                    } else if (position == showFolderWhenForwardRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("FoldersWhenForwarding", R.string.FoldersWhenForwarding), ColorConfig.showFolderWhenForward, true);
                    } else if (position == rearCameraStartingRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("UseRearRoundVideos", R.string.UseRearRoundVideos), LocaleController.getString("UseRearRoundVideosDesc", R.string.UseRearRoundVideosDesc), ColorConfig.useRearCamera, true, true);
                    } else if (position == hideAllTabRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideAllChatsFolder", R.string.HideAllChatsFolder), ColorConfig.hideAllTab, true);
                    } else if (position == cameraXOptimizeRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("PerformanceMode", R.string.PerformanceMode), LocaleController.getString("PerformanceModeDesc", R.string.PerformanceModeDesc), ColorConfig.useCameraXOptimizedMode, true, true);
                    } else if (position == proximitySensorRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableProximityEvents", R.string.DisableProximityEvents), ColorConfig.disableProximityEvents, true);
                    } else if (position == suppressionRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("VoiceEnhancements", R.string.VoiceEnhancements), LocaleController.getString("VoiceEnhancementsDesc", R.string.VoiceEnhancementsDesc), ColorConfig.voicesAgc, true, true);
                    } else if (position == turnSoundOnVDKeyRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("TurnSoundOnVDKey", R.string.TurnSoundOnVDKey), LocaleController.getString("TurnSoundOnVDKeyDesc", R.string.TurnSoundOnVDKeyDesc), ColorConfig.turnSoundOnVDKey, true, true);
                    } else if (position == openArchiveOnPullRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OpenArchiveOnPull", R.string.OpenArchiveOnPull), ColorConfig.openArchiveOnPull, true);
                    } else if (position == hideTimeOnStickerRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideTimeOnSticker", R.string.HideTimeOnSticker), ColorConfig.hideTimeOnSticker, false);
                    } else if (position == onlineStatusRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("OnlineStatus", R.string.OnlineStatus), LocaleController.getString("OnlineStatusDesc", R.string.OnlineStatusDesc), ColorConfig.showStatusInChat, true, false);
                    } else if (position == hideSendAsChannelRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideSendAsChannel", R.string.HideSendAsChannel), ColorConfig.hideSendAsChannel, true);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == cameraAdviseRow) {
                        String advise;
                        switch (ColorConfig.cameraType) {
                            case ColorConfig.TELEGRAM_CAMERA:
                                advise = LocaleController.getString("DefaultCameraDesc", R.string.DefaultCameraDesc);
                                break;
                            case ColorConfig.CAMERA_X:
                                advise = LocaleController.getString("CameraXDesc", R.string.CameraXDesc);
                                break;
                            case ColorConfig.SYSTEM_CAMERA:
                            default:
                                advise = LocaleController.getString("SystemCameraDesc", R.string.SystemCameraDesc);
                                break;
                        }
                        Spannable htmlParsed = new SpannableString(AndroidUtilities.fromHtml(advise));
                        textInfoPrivacyCell.setText(EntitiesHelper.getUrlNoUnderlineText(htmlParsed));
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == cameraXQualityRow) {
                        textSettingsCell.setTextAndValue(LocaleController.getString("CameraQuality", R.string.CameraQuality), ColorConfig.cameraResolution + "p", partial,false);
                    }
                    break;
                case CHECKBOX:
                    TextCheckbox2Cell textCheckbox2Cell = (TextCheckbox2Cell) holder.itemView;
                    if (position == showDeleteRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("ClearFromCache", R.string.ClearFromCache), ColorConfig.contextMenu.clearFromCache, true);
                    } else if (position == showNoQuoteForwardRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("NoQuoteForward", R.string.NoQuoteForward), ColorConfig.contextMenu.noQuoteForward, true);
                    } else if (position == showAddToSMRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("AddToSavedMessages", R.string.AddToSavedMessages), ColorConfig.contextMenu.saveMessage, true);
                    } else if (position == showRepeatRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("Repeat", R.string.Repeat), ColorConfig.contextMenu.repeatMessage, true);
                    } else if (position == showReportRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("ReportChat", R.string.ReportChat), ColorConfig.contextMenu.reportMessage, true);
                    } else if (position == showMessageDetailsRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("MessageDetails", R.string.MessageDetails), ColorConfig.contextMenu.messageDetails, false);
                    } else if (position == showCopyPhotoRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("CopyPhoto", R.string.CopyPhoto), ColorConfig.contextMenu.copyPhoto, true);
                    } else if (position == showPatpatRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("Patpat", R.string.Patpat), ColorConfig.contextMenu.patpat, true);
                    }
                    break;
                case TEXT_CHECK_CELL2:
                    TextCheckCell2 checkCell = (TextCheckCell2) holder.itemView;
                    if (position == confirmSendRow) {
                        int confirmSendCount = ColorConfig.confirmSending.count();
                        checkCell.setTextAndCheck(LocaleController.getString("ConfirmSending", R.string.ConfirmSending), confirmSendCount > 0, true, true);
                        checkCell.setCollapseArrow(String.format(Locale.US, "%d/4", confirmSendCount), !confirmSendExpanded, () -> {
                            boolean checked = !checkCell.isChecked();
                            checkCell.setChecked(checked);
                            ColorConfig.confirmSending.setAll(checked);
                            AndroidUtilities.updateVisibleRows(listView);
                        });
                        checkCell.getCheckBox().setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
                        checkCell.getCheckBox().setDrawIconType(0);
                    }
                    break;
                case CHECKBOX_CELL:
                    CheckBoxCell checkBoxCell = (CheckBoxCell) holder.itemView;
                    if (position == confirmSendStickersRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionStickers", R.string.SendMediaPermissionStickers), "", ColorConfig.confirmSending.sendStickers, true, true);
                    } else if (position == confirmSendGifsRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionGifs", R.string.SendMediaPermissionGifs), "", ColorConfig.confirmSending.sendGifs, true, true);
                    } else if (position == confirmSendAudioRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionVoice", R.string.SendMediaPermissionVoice), "", ColorConfig.confirmSending.sendAudio, true, true);
                    } else if (position == confirmSendVideoRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionRound", R.string.SendMediaPermissionRound), "", ColorConfig.confirmSending.sendVideo, true, true);
                    }
                    checkBoxCell.setPad(1);
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.SWITCH || viewType == ViewType.SETTINGS || viewType == ViewType.CHECKBOX ||
                    viewType == ViewType.TEXT_CHECK_CELL2 || viewType == ViewType.CHECKBOX_CELL;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case STICKER_SIZE:
                    view = new StickerSize(context, parentLayout) {
                        @Override
                        protected void onSeek() {
                            super.onSeek();
                            if (ColorConfig.stickerSizeStack != 14) {
                                menuItem.setVisibility(VISIBLE);
                            } else {
                                menuItem.setVisibility(INVISIBLE);
                            }
                        }
                    };
                    break;
                case CAMERA_SELECTOR:
                    view = new CameraTypeSelector(context) {
                        @Override
                        protected void onSelectedCamera(int cameraSelected) {
                            super.onSelectedCamera(cameraSelected);
                            int oldValue = ColorConfig.cameraType;
                            ColorConfig.saveCameraType(cameraSelected);
                            if (cameraSelected == ColorConfig.CAMERA_X) {
                                updateRowsId();
                                listAdapter.notifyItemInserted(cameraXOptimizeRow);
                                listAdapter.notifyItemInserted(cameraXQualityRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow);
                            } else if (oldValue == ColorConfig.CAMERA_X){
                                listAdapter.notifyItemRemoved(cameraXOptimizeRow);
                                listAdapter.notifyItemRemoved(cameraXQualityRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow - 1);
                                updateRowsId();
                            } else {
                                listAdapter.notifyItemChanged(cameraAdviseRow);
                            }
                        }
                    };
                    break;
            }
            if (view != null) view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == chatDividerRow || position == foldersDividerRow || position == audioVideoDividerRow ||
                    position == stickerSizeDividerRow) {
                return ViewType.SHADOW;
            } else if (position == chatHeaderRow || position == foldersHeaderRow || position == audioVideoHeaderRow ||
                    position == messageMenuHeaderRow || position == stickerSizeHeaderRow || position == cameraTypeHeaderRow) {
                return ViewType.HEADER;
            } else if (position == jumpChannelRow || position == hideKeyboardRow ||
                    position == playGifAsVideoRow || position == showFolderWhenForwardRow ||
                    position == rearCameraStartingRow || position == showGreetings || position == cameraXOptimizeRow ||
                    position == proximitySensorRow || position == suppressionRow || position == turnSoundOnVDKeyRow ||
                    position == openArchiveOnPullRow || position == hideTimeOnStickerRow || position == onlineStatusRow ||
                    position == hideAllTabRow || position == hideSendAsChannelRow) {
                return ViewType.SWITCH;
            } else if (position == stickerSizeRow) {
                return ViewType.STICKER_SIZE;
            } else if (position == cameraTypeSelectorRow) {
                return ViewType.CAMERA_SELECTOR;
            } else if (position == cameraAdviseRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == cameraXQualityRow) {
                return ViewType.SETTINGS;
            } else if (position == showDeleteRow || position == showNoQuoteForwardRow || position == showAddToSMRow ||
                    position == showRepeatRow || position == showReportRow ||
                    position == showMessageDetailsRow || position == showCopyPhotoRow || position == showPatpatRow) {
                return ViewType.CHECKBOX;
            } else if (position == confirmSendRow) {
                return ViewType.TEXT_CHECK_CELL2;
            } else if (position == confirmSendGifsRow || position == confirmSendStickersRow ||
                    position == confirmSendAudioRow || position == confirmSendVideoRow) {
                return ViewType.CHECKBOX_CELL;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }
}
