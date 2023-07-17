package it.foxgram.ui;

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

import it.foxgram.android.AlertController;
import it.foxgram.android.FoxConfig;
import it.foxgram.android.camera.CameraXUtils;
import it.foxgram.android.entities.EntitiesHelper;
import it.foxgram.android.media.AudioEnhance;
import it.foxgram.ui.Cells.CameraTypeSelector;
import it.foxgram.ui.Cells.StickerSize;

public class FoxGramChatSettings extends BaseSettingsActivity implements NotificationCenter.NotificationCenterDelegate {

    private int stickerSizeHeaderRow;
    private int stickerSizeRow;
    private int stickerSizeDividerRow;
    private int chatHeaderRow;
    private int jumpChannelRow;
    private int doubleReactionsRow;
    private int showGreetings;
    private int hideChannelBottomRow;
    private int hideKeyboardRow;
    private int playGifAsVideoRow;
    private int chatDividerRow;
    private int messageMenuHeaderRow;
    private int showAddToSMRow;
    private int showRepeatRow;
    private int showNoQuoteForwardRow;
    private int showReportRow;
    private int showMessageDetailsRow;
    private int showCopyPhotoRow;
    private int audioVideoDividerRow;
    private int audioVideoHeaderRow;
    private int confirmSendRow;
    private int confirmSendGifsRow;
    private int confirmSendStickersRow;
    private int confirmSendAudioRow;
    private int confirmSendVideoRow;
    private int showDeleteRow;
    private int cameraEnableRow;
    private int cameraHeaderRow;
    private int cameraTypeSelectorRow;
    private int cameraXOptimizeRow;
    private int cameraXQualityRow;
    private int cameraAdviseRow;
    private int rearCameraStartingRow;
    private int cameraPreviewRow;
    private int cameraDividerRow;
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
            FoxConfig.toggleJumpChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.jumpChannel);
            }
        } else if (position == doubleReactionsRow) {
            FoxConfig.toggleDoubleTapDisabled();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.doubleTapDisabled);
            }
        } else if (position == showGreetings) {
            FoxConfig.toggleShowGreetings();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showGreetings);
            }
        } else if (position == hideChannelBottomRow) {
            FoxConfig.toggleHideChannelBottom();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked((FoxConfig.hideChannelBottom));
            }
        } else if (position == hideKeyboardRow) {
            FoxConfig.toggleHideKeyboard();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.hideKeyboard);
            }
        } else if (position == playGifAsVideoRow) {
            FoxConfig.toggleGifAsVideo();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.gifAsVideo);
            }
        } else if (position == showAddToSMRow) {
            FoxConfig.contextMenu.toggleSaveMessage();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.saveMessage);
            }
        } else if (position == showRepeatRow) {
            FoxConfig.contextMenu.toggleRepeatMessage();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.repeatMessage);
            }
        } else if (position == showMessageDetailsRow) {
            FoxConfig.contextMenu.toggleMessageDetails();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.messageDetails);
            }
        } else if (position == showNoQuoteForwardRow) {
            FoxConfig.contextMenu.toggleNoQuoteForward();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.noQuoteForward);
            }
        } else if (position == showReportRow) {
            FoxConfig.contextMenu.toggleReportMessage();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.reportMessage);
            }
        } else if (position == showDeleteRow) {
            FoxConfig.contextMenu.toggleClearFromCache();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.clearFromCache);
            }
        } else if (position == showCopyPhotoRow) {
            FoxConfig.contextMenu.toggleCopyPhoto();
            FoxConfig.applyContextMenu();
            if (view instanceof TextCheckbox2Cell) {
                ((TextCheckbox2Cell) view).setChecked(FoxConfig.contextMenu.copyPhoto);
            }
        } else if (position == cameraEnableRow) {
            FoxConfig.toggleCameraEnable();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.cameraEnable);
            }
        } else if (position == cameraXOptimizeRow) {
            FoxConfig.toggleCameraXOptimizedMode();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.useCameraXOptimizedMode);
            }
        } else if (position == cameraXQualityRow) {
            Map<Quality, Size> availableSizes = CameraXUtils.getAvailableVideoSizes();
            Stream<Integer> tmp = availableSizes.values().stream().sorted(Comparator.comparingInt(Size::getWidth).reversed()).map(Size::getHeight);
            ArrayList<Integer> types = tmp.collect(Collectors.toCollection(ArrayList::new));
            ArrayList<String> arrayList = types.stream().map(p -> p + "p").collect(Collectors.toCollection(ArrayList::new));
            AlertController.show(arrayList, LocaleController.getString("CameraQuality", R.string.CameraQuality), types.indexOf(FoxConfig.cameraResolution), context, i -> {
                FoxConfig.saveCameraResolution(types.get(i));
                listAdapter.notifyItemChanged(cameraXQualityRow, PARTIAL);
            });
        } else if (position == rearCameraStartingRow) {
            FoxConfig.toggleUseRearCamera();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.useRearCamera);
            }
        } else if (position == cameraPreviewRow) {
            FoxConfig.toggleCameraPreview();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.cameraPreview);
            }
        } else if (position == proximitySensorRow) {
            FoxConfig.toggleDisableProximityEvents();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.disableProximityEvents);
            }
            restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
        } else if (position == suppressionRow) {
            FoxConfig.toggleVoicesAgc();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.voicesAgc);
            }
        } else if (position == turnSoundOnVDKeyRow) {
            FoxConfig.toggleTurnSoundOnVDKey();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.turnSoundOnVDKey);
            }
        } else if (position == openArchiveOnPullRow) {
            FoxConfig.toggleOpenArchiveOnPull();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.openArchiveOnPull);
            }
        } else if (position == hideTimeOnStickerRow) {
            FoxConfig.toggleHideTimeOnSticker();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.hideTimeOnSticker);
            }
        } else if (position == onlineStatusRow) {
            FoxConfig.toggleShowStatusInChat();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showStatusInChat);
            }
        } else if (position == hideSendAsChannelRow) {
            FoxConfig.toggleHideSendAsChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.hideSendAsChannel);
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
            FoxConfig.confirmSending.toggleStickers();
            FoxConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendStickersRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        } else if (position == confirmSendGifsRow) {
            FoxConfig.confirmSending.toggleGifs();
            FoxConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendGifsRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        } else if (position == confirmSendAudioRow) {
            FoxConfig.confirmSending.toggleAudio();
            FoxConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendAudioRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        } else if (position == confirmSendVideoRow) {
            FoxConfig.confirmSending.toggleVideo();
            FoxConfig.applyConfirmSending();
            listAdapter.notifyItemChanged(confirmSendVideoRow, PARTIAL);
            listAdapter.notifyItemChanged(confirmSendRow, PARTIAL);
        }
    }

    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            FoxConfig.setStickerSize(14);
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
        menuItem.setVisibility(FoxConfig.stickerSizeStack != 14.0f ? View.VISIBLE : View.GONE);
        return menuItem;
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        suppressionRow = -1;
        confirmSendGifsRow = -1;
        confirmSendStickersRow = -1;
        confirmSendAudioRow = -1;
        confirmSendVideoRow = -1;

        stickerSizeHeaderRow = rowCount++;
        stickerSizeRow = rowCount++;
        stickerSizeDividerRow = rowCount++;

        chatHeaderRow = rowCount++;
        jumpChannelRow = rowCount++;
        doubleReactionsRow = rowCount++;
        showGreetings = rowCount++;
        hideChannelBottomRow = rowCount++;
        hideKeyboardRow = rowCount++;
        hideSendAsChannelRow = rowCount++;
        openArchiveOnPullRow = rowCount++;
        onlineStatusRow = rowCount++;
        chatDividerRow = rowCount++;

        cameraHeaderRow = rowCount++;
        cameraEnableRow = rowCount++;
        if (CameraXUtils.isCameraXSupported()) {
            cameraTypeSelectorRow = rowCount++;
            if (FoxConfig.cameraType == 1) {
                cameraXOptimizeRow = rowCount++;
                cameraXQualityRow = rowCount++;
            }
            cameraAdviseRow = rowCount++;
        }
        cameraPreviewRow = rowCount++;
        rearCameraStartingRow = rowCount++;
        cameraDividerRow = rowCount++;

        audioVideoHeaderRow = rowCount++;
        if (AudioEnhance.isAvailable()) {
            suppressionRow = rowCount++;
        }
        turnSoundOnVDKeyRow = rowCount++;
        proximitySensorRow = rowCount++;
        playGifAsVideoRow = rowCount++;
        confirmSendRow = rowCount++;
        if (confirmSendExpanded) {
            confirmSendStickersRow = rowCount++;
            confirmSendGifsRow = rowCount++;
            confirmSendAudioRow = rowCount++;
            confirmSendVideoRow = rowCount++;
        }
        hideTimeOnStickerRow = rowCount++;
        audioVideoDividerRow = rowCount++;

        messageMenuHeaderRow = rowCount++;
        showDeleteRow = rowCount++;
        showCopyPhotoRow = rowCount++;
        showNoQuoteForwardRow = rowCount++;
        showAddToSMRow = rowCount++;
        showRepeatRow = rowCount++;
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
                    } else if (position == audioVideoHeaderRow) {
                        headerCell.setText(LocaleController.getString("MediaSettings", R.string.MediaSettings));
                    } else if (position == messageMenuHeaderRow) {
                        headerCell.setText(LocaleController.getString("ContextMenu", R.string.ContextMenu));
                    } else if (position == stickerSizeHeaderRow) {
                        headerCell.setText(LocaleController.getString("StickersSize", R.string.StickersSize));
                    } else if (position == cameraHeaderRow) {
                        headerCell.setText(LocaleController.getString("CameraType", R.string.CameraType));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == cameraEnableRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera), FoxConfig.cameraEnable, true);
                    } else if (position == cameraXOptimizeRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("PerformanceMode", R.string.PerformanceMode), LocaleController.getString("PerformanceModeDesc", R.string.PerformanceModeDesc), FoxConfig.useCameraXOptimizedMode, true, true);
                    } else if (position == cameraPreviewRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CameraPreview"), FoxConfig.cameraPreview, true);
                    } else if (position == rearCameraStartingRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("UseRearRoundVideos", R.string.UseRearRoundVideos), LocaleController.getString("UseRearRoundVideosDesc", R.string.UseRearRoundVideosDesc), FoxConfig.useRearCamera, true, true);
                    } else if (position == jumpChannelRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("JumpToNextChannel", R.string.JumpToNextChannel), FoxConfig.jumpChannel, true);
                    } else if (position == doubleReactionsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DoubleTapReactions", R.string.DoubleTapReactions), FoxConfig.doubleTapDisabled, true);
                    } else if (position == showGreetings) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("GreetingSticker", R.string.GreetingSticker), FoxConfig.showGreetings, true);
                    } else if (position == hideChannelBottomRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideChannelBottomButton", R.string.HideChannelBottomButton), FoxConfig.hideChannelBottom, true);
                    } else if (position == hideKeyboardRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideChatKeyboard", R.string.HideChatKeyboard), FoxConfig.hideKeyboard, true);
                    } else if (position == playGifAsVideoRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("GIFsAsVideo", R.string.GIFsAsVideo), FoxConfig.gifAsVideo, true);
                    } else if (position == proximitySensorRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableProximityEvents", R.string.DisableProximityEvents), FoxConfig.disableProximityEvents, true);
                    } else if (position == suppressionRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("VoiceEnhancements", R.string.VoiceEnhancements), LocaleController.getString("VoiceEnhancementsDesc", R.string.VoiceEnhancementsDesc), FoxConfig.voicesAgc, true, true);
                    } else if (position == turnSoundOnVDKeyRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("TurnSoundOnVDKey", R.string.TurnSoundOnVDKey), LocaleController.getString("TurnSoundOnVDKeyDesc", R.string.TurnSoundOnVDKeyDesc), FoxConfig.turnSoundOnVDKey, true, true);
                    } else if (position == openArchiveOnPullRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OpenArchiveOnPull", R.string.OpenArchiveOnPull), FoxConfig.openArchiveOnPull, true);
                    } else if (position == hideTimeOnStickerRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideTimeOnSticker", R.string.HideTimeOnSticker), FoxConfig.hideTimeOnSticker, false);
                    } else if (position == onlineStatusRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("OnlineStatus", R.string.OnlineStatus), LocaleController.getString("OnlineStatusDesc", R.string.OnlineStatusDesc), FoxConfig.showStatusInChat, true, false);
                    } else if (position == hideSendAsChannelRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("HideSendAsChannel", R.string.HideSendAsChannel), FoxConfig.hideSendAsChannel, true);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == cameraAdviseRow) {
                        String advise;
                        switch (FoxConfig.cameraType) {
                            case FoxConfig.TELEGRAM_CAMERA:
                                advise = LocaleController.getString("DefaultCameraDesc", R.string.DefaultCameraDesc);
                                break;
                            case FoxConfig.CAMERA_X:
                                advise = LocaleController.getString("CameraXDesc", R.string.CameraXDesc);
                                break;
                            case FoxConfig.SYSTEM_CAMERA:
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
                        textSettingsCell.setTextAndValue(LocaleController.getString("CameraQuality", R.string.CameraQuality), FoxConfig.cameraResolution + "p", partial,false);
                    }
                    break;
                case CHECKBOX:
                    TextCheckbox2Cell textCheckbox2Cell = (TextCheckbox2Cell) holder.itemView;
                    if (position == showDeleteRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("ClearFromCache", R.string.ClearFromCache), FoxConfig.contextMenu.clearFromCache, true);
                    } else if (position == showNoQuoteForwardRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("NoQuoteForward", R.string.NoQuoteForward), FoxConfig.contextMenu.noQuoteForward, true);
                    } else if (position == showAddToSMRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("AddToSavedMessages", R.string.AddToSavedMessages), FoxConfig.contextMenu.saveMessage, true);
                    } else if (position == showRepeatRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("Repeat", R.string.Repeat), FoxConfig.contextMenu.repeatMessage, true);
                    } else if (position == showReportRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("ReportChat", R.string.ReportChat), FoxConfig.contextMenu.reportMessage, true);
                    } else if (position == showMessageDetailsRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("MessageDetails", R.string.MessageDetails), FoxConfig.contextMenu.messageDetails, false);
                    } else if (position == showCopyPhotoRow) {
                        textCheckbox2Cell.setTextAndCheck(LocaleController.getString("CopyPhoto", R.string.CopyPhoto), FoxConfig.contextMenu.copyPhoto, true);
                    }
                    break;
                case TEXT_CHECK_CELL2:
                    TextCheckCell2 checkCell = (TextCheckCell2) holder.itemView;
                    if (position == confirmSendRow) {
                        int confirmSendCount = FoxConfig.confirmSending.count();
                        checkCell.setTextAndCheck(LocaleController.getString("ConfirmSending", R.string.ConfirmSending), confirmSendCount > 0, true, true);
                        checkCell.setCollapseArrow(String.format(Locale.US, "%d/4", confirmSendCount), !confirmSendExpanded, () -> {
                            boolean checked = !checkCell.isChecked();
                            checkCell.setChecked(checked);
                            FoxConfig.confirmSending.setAll(checked);
                            AndroidUtilities.updateVisibleRows(listView);
                        });
                        checkCell.getCheckBox().setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
                        checkCell.getCheckBox().setDrawIconType(0);
                    }
                    break;
                case CHECKBOX_CELL:
                    CheckBoxCell checkBoxCell = (CheckBoxCell) holder.itemView;
                    if (position == confirmSendStickersRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionStickers", R.string.SendMediaPermissionStickers), "", FoxConfig.confirmSending.sendStickers, true, true);
                    } else if (position == confirmSendGifsRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionGifs", R.string.SendMediaPermissionGifs), "", FoxConfig.confirmSending.sendGifs, true, true);
                    } else if (position == confirmSendAudioRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionVoice", R.string.SendMediaPermissionVoice), "", FoxConfig.confirmSending.sendAudio, true, true);
                    } else if (position == confirmSendVideoRow) {
                        checkBoxCell.setText(LocaleController.getString("SendMediaPermissionRound", R.string.SendMediaPermissionRound), "", FoxConfig.confirmSending.sendVideo, true, true);
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
                            if (FoxConfig.stickerSizeStack != 14) {
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
                            int oldValue = FoxConfig.cameraType;
                            FoxConfig.saveCameraType(cameraSelected);
                            if (cameraSelected == FoxConfig.CAMERA_X) {
                                updateRowsId();
                                listAdapter.notifyItemInserted(cameraXOptimizeRow);
                                listAdapter.notifyItemInserted(cameraXQualityRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow);
                            } else if (oldValue == FoxConfig.CAMERA_X){
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
            if (position == chatDividerRow || position == cameraDividerRow ||
                 position == audioVideoDividerRow || position == stickerSizeDividerRow) {
                return ViewType.SHADOW;
            } else if (position == chatHeaderRow || position == audioVideoHeaderRow ||
                    position == messageMenuHeaderRow || position == stickerSizeHeaderRow || position == cameraHeaderRow) {
                return ViewType.HEADER;
            } else if (position == cameraEnableRow || position == cameraXOptimizeRow ||
                    position == cameraPreviewRow || position == rearCameraStartingRow ||
                    position == jumpChannelRow || position == doubleReactionsRow || position == hideKeyboardRow ||
                    position == playGifAsVideoRow || position == showGreetings || position == hideChannelBottomRow ||
                    position == proximitySensorRow || position == suppressionRow || position == turnSoundOnVDKeyRow ||
                    position == openArchiveOnPullRow || position == hideTimeOnStickerRow || position == onlineStatusRow ||
                    position == hideSendAsChannelRow) {
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
                    position == showMessageDetailsRow || position == showCopyPhotoRow) {
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
