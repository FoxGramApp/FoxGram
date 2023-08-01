package it.foxgram.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;

import java.util.ArrayList;

import it.foxgram.android.AlertController;
import it.foxgram.android.FoxConfig;
import it.foxgram.android.StoreUtils;
import it.foxgram.android.http.FileDownloader;
import it.foxgram.android.magic.FOXENC;
import it.foxgram.android.updates.AppDownloader;
import it.foxgram.android.updates.PlayStoreAPI;
import it.foxgram.android.updates.UpdateManager;
import it.foxgram.android.utils.FoxTextUtils;
import it.foxgram.ui.Cells.UpdateAvailableCell;
import it.foxgram.ui.Cells.UpdateCheckCell;

public class FoxGramUpdateSettings extends BaseSettingsActivity {

    private int updateSectionAvailableRow;
    private int updateSectionDividerRow;
    private int updateCheckHeaderRow;
    private int updateCheckRow;
    private int notifyWhenAvailableRow;
    private int infoHeaderRow;
    private int updatesChannelRow;
    private int infoUpdatesChannelRow;
    private int versionInfoRow;
    private int baseVersionRow;
    private int buildTypeRow;
    private int buildInfoRow;
    private int updateTypeRow;
    private int downloadSourceRow;
    private int releaseDateRow;
    private boolean checkingUpdates;
    private TextCell changeBetaMode;

    private FOXENC.UpdateAvailable updateAvailable;
    private UpdateCheckCell updateCheckCell;
    private UpdateAvailableCell updateCell;

    @Override
    public boolean onFragmentCreate() {
        if (FoxConfig.updateData.isPresent()) {
            updateAvailable = FoxConfig.updateData.get();
            if (updateAvailable.isReminded()) {
                updateAvailable = null;
            } else if (updateAvailable.version <= BuildVars.BUILD_VERSION) {
                updateAvailable = null;
                FoxConfig.saveUpdateStatus(0);
            }
        }
        AppDownloader.setListener("settings", new AppDownloader.UpdateListener() {
            @Override
            public void onPreStart() {
            }

            @Override
            public void onProgressChange(int percentage, long downBytes, long totBytes) {
                if (updateCell != null) {
                    updateCell.setPercentage(percentage, downBytes, totBytes);
                }
            }

            @Override
            public void onFinished() {
                if (!StoreUtils.isFromPlayStore()) changeBetaMode.setEnabled(!AppDownloader.updateDownloaded());
                updateCheckCell.setCanCheckForUpdate(!AppDownloader.updateDownloaded() && !PlayStoreAPI.isRunningDownload());
                if (PlayStoreAPI.updateDownloaded()) {
                    updateCheckCell.setDownloaded();
                }
                if (updateCell != null) {
                    if (AppDownloader.updateDownloaded()) {
                        updateCell.setInstallMode();
                    } else {
                        updateCell.setConfirmMode();
                    }
                }
            }
        });
        return super.onFragmentCreate();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("ColorUpdates", R.string.ColorUpdates);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == notifyWhenAvailableRow) {
            FoxConfig.toggleNotifyUpdates();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.notifyUpdates);
            }
        } else if (position == updatesChannelRow) {
            if (!UpdateManager.updateDownloaded() && !checkingUpdates) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add(LocaleController.getString("Stable", R.string.Stable));
                types.add(0);
                arrayList.add("Release Preview");
                types.add(1);
                int updateChannel = FoxConfig.betaUpdates ? 1 : 0;
                AlertController.show(arrayList, LocaleController.getString("InstallPreview", R.string.InstallPreview), types.indexOf(updateChannel), context, i -> {
                    switch (types.get(i)) {
                        case 0:
                            if (FoxConfig.betaUpdates) {
                                FoxConfig.toggleBetaUpdates();
                                FileDownloader.cancel("appUpdate");
                                listAdapter.notifyItemChanged(updatesChannelRow, PARTIAL);
                                UpdateManager.deleteUpdate();
                                if (updateAvailable != null) {
                                    FoxConfig.remindUpdate(updateAvailable.version);
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                                    updateAvailable = null;
                                    listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                                    listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                                    updateRowsId();
                                }
                                checkUpdates();
                            }
                            break;
                        case 1:
                            if (!FoxConfig.betaUpdates) {
                                FoxConfig.toggleBetaUpdates();
                                FileDownloader.cancel("appUpdate");
                                listAdapter.notifyItemChanged(updatesChannelRow, PARTIAL);
                                UpdateManager.deleteUpdate();
                                if (updateAvailable != null) {
                                    FoxConfig.remindUpdate(updateAvailable.version);
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                                    updateAvailable = null;
                                    listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                                    listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                                    updateRowsId();
                                }
                                checkUpdates();
                            }
                            break;
                    }
                });
            }
        } else if (position == versionInfoRow) {
            AndroidUtilities.addToClipboard(BuildConfig.BUILD_VERSION_STRING);
        } else if (position == baseVersionRow) {
            AndroidUtilities.addToClipboard(BuildVars.TELEGRAM_VERSION_STRING);
        } else if (position == buildInfoRow) {
            AndroidUtilities.addToClipboard(String.valueOf(BuildConfig.BUILD_VERSION));
        } else if (position == buildTypeRow) {
            AndroidUtilities.addToClipboard(BuildConfig.BUILD_TYPE);
        } else if (position == updateTypeRow) {
            AndroidUtilities.addToClipboard(FoxTextUtils.getAppName());
        } else if (position == downloadSourceRow) {
            String source = StoreUtils.isFromPlayStore() ? "Play Store" : StoreUtils.isFromHuaweiStore() ? "Huawei Store" : "APK";
            AndroidUtilities.addToClipboard(source);
        } else if (position == releaseDateRow) {
            AndroidUtilities.addToClipboard(LocaleController.formatDateAudio(BuildConfig.GIT_COMMIT_DATE, true));
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        updateSectionAvailableRow = -1;
        updateSectionDividerRow = -1;

        if (updateAvailable != null && !StoreUtils.isDownloadedFromAnyStore()) {
            updateSectionAvailableRow = rowCount++;
            updateSectionDividerRow = rowCount++;
        }

        updateCheckHeaderRow = rowCount++;
        updateCheckRow = rowCount++;
        notifyWhenAvailableRow = rowCount++;
        if (!StoreUtils.isDownloadedFromAnyStore()) {
            updatesChannelRow = rowCount++;
            infoUpdatesChannelRow = rowCount++;
        }

        infoHeaderRow = rowCount++;
        versionInfoRow = rowCount++;
        buildInfoRow = rowCount++;
        baseVersionRow = rowCount++;
        buildTypeRow = rowCount++;
        updateTypeRow = rowCount++;
        downloadSourceRow = rowCount++;
        releaseDateRow = rowCount++;
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
                case UPDATE:
                    UpdateAvailableCell updateCell = (UpdateAvailableCell) holder.itemView;
                    FoxGramUpdateSettings.this.updateCell = updateCell;
                    updateCell.setUpdate(
                            updateAvailable.title,
                            updateAvailable.description,
                            updateAvailable.note,
                            updateAvailable.banner
                    );
                    if (AppDownloader.isRunningDownload()) {
                        updateCell.setDownloadMode();
                    } else {
                        if (AppDownloader.updateDownloaded()) {
                            updateCell.setInstallMode();
                        } else {
                            updateCell.setConfirmMode();
                        }
                    }
                    updateCell.setPercentage(AppDownloader.getDownloadProgress(), AppDownloader.downloadedBytes(), AppDownloader.totalBytes());
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == updateCheckHeaderRow) {
                        headerCell.setText(LocaleController.getString("InAppUpdates", R.string.InAppUpdates));
                    } else if (position == infoHeaderRow) {
                        headerCell.setText(LocaleController.getString("UpdatesInfoSettings", R.string.UpdatesInfoSettings));
                    }
                    break;
                case UPDATE_CHECK:
                    UpdateCheckCell updateCheckCell = (UpdateCheckCell) holder.itemView;
                    updateCheckCell.loadLastStatus();
                    updateCheckCell.setCanCheckForUpdate(!AppDownloader.updateDownloaded() && !PlayStoreAPI.isRunningDownload());
                    if (PlayStoreAPI.updateDownloaded()) {
                        updateCheckCell.setDownloaded();
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == notifyWhenAvailableRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AutoUpdate", R.string.AutoUpdate), LocaleController.getString("AutoUpdatePrompt", R.string.AutoUpdatePrompt), FoxConfig.notifyUpdates, true, true);
                    }
                    break;
                case TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == updatesChannelRow) {
                        changeBetaMode = textCell;
                        changeBetaMode.setTextAndValue(LocaleController.getString("APKsChannel", R.string.APKsChannel), FoxTextUtils.getUpdatesChannel(),partial, false);
                    } else if (position == versionInfoRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("InstalledVersion", R.string.InstalledVersion), BuildConfig.BUILD_VERSION_STRING, R.drawable.msg_info, true);
                    } else if (position == baseVersionRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("BaseVersionUpdated", R.string.BaseVersionUpdated), BuildVars.TELEGRAM_VERSION_STRING + " (" + BuildVars.TELEGRAM_BUILD_VERSION + ")", R.drawable.msg_draw_arrow, true);
                    } else if (position == buildTypeRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("BuildType", R.string.BuildType), BuildConfig.BUILD_TYPE,R.drawable.msg_map_type, true);
                    } else if (position == buildInfoRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("BuildVersion", R.string.BuildVersion), String.valueOf(BuildConfig.BUILD_VERSION), R.drawable.msg_text_check, true);
                    } else if (position == updateTypeRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("UpdateType", R.string.UpdateType), FoxTextUtils.getAppName(),R.drawable.round_update_white_28, true);
                    } else if (position == downloadSourceRow) {
                        String source = StoreUtils.isFromPlayStore() ? "Play Store" : StoreUtils.isFromHuaweiStore() ? "Huawei Store" : "APK";
                        textCell.setTextAndValueAndIcon(LocaleController.getString("DownloadSource", R.string.DownloadSource), source, R.drawable.msg_current_location, true);
                    } else if (position == releaseDateRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("ReleaseDate", R.string.ReleaseDate),LocaleController.formatDateAudio(BuildConfig.GIT_COMMIT_DATE, true), R.drawable.msg_calendar2, false);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    textInfoPrivacyCell.setText(LocaleController.getString("InstallPreviewDesc", R.string.InstallPreviewDesc) + " " + LocaleController.getString("UpdatesChannelInfo", R.string.UpdatesChannelInfo));
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.SWITCH || viewType == ViewType.TEXT_CELL;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case UPDATE:
                    view = new UpdateAvailableCell(context) {
                        @Override
                        protected void onInstallUpdate() {
                            super.onInstallUpdate();
                            UpdateManager.installUpdate(getParentActivity());
                        }

                        @Override
                        protected void onConfirmUpdate() {
                            super.onConfirmUpdate();
                            if (!FileDownloader.isRunningDownload("appUpdate")) {
                                if (FileDownloader.downloadFile(context, "appUpdate", UpdateManager.apkFile(), updateAvailable.fileLink))
                                    FoxConfig.saveOldVersion(updateAvailable.version);
                                updateCell.setDownloadMode();
                            }
                        }

                        @Override
                        protected void onRemindUpdate() {
                            super.onRemindUpdate();
                            updateCheckCell.setCheckTime();
                            if (updateAvailable != null) {
                                UpdateManager.deleteUpdate();
                                FoxConfig.remindUpdate(updateAvailable.version);
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                                updateCheckCell.setCheckTime();
                                updateAvailable = null;
                                listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                                listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                                updateRowsId();
                            }
                        }
                    };
                    break;
                case UPDATE_CHECK:
                    view = new UpdateCheckCell(context, true) {
                        @Override
                        protected void onCheckUpdate() {
                            super.onCheckUpdate();
                            if (PlayStoreAPI.updateDownloaded()) {
                                PlayStoreAPI.installUpdate();
                            } else if (StoreUtils.isFromPlayStore() && !PlayStoreAPI.updateDownloaded() && FoxConfig.lastUpdateStatus == 1 && updateAvailable != null) {
                                PlayStoreAPI.openUpdatePopup(getParentActivity());
                            } else if (!AppDownloader.updateDownloaded()) {
                                checkUpdates();
                            }
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    updateCheckCell = (UpdateCheckCell) view;
                    break;
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == updateSectionDividerRow) {
                return ViewType.SHADOW;
            } else if (position == updateSectionAvailableRow) {
                return ViewType.UPDATE;
            } else if (position == updateCheckHeaderRow || position == infoHeaderRow) {
                return ViewType.HEADER;
            } else if (position == updateCheckRow) {
                return ViewType.UPDATE_CHECK;
            } else if (position == notifyWhenAvailableRow) {
                return ViewType.SWITCH;
            } else if (position == updatesChannelRow || position == versionInfoRow ||
                    position == buildInfoRow || position == buildTypeRow ||
                    position == updateTypeRow || position == downloadSourceRow ||
                    position == releaseDateRow || position == baseVersionRow) {
                return ViewType.TEXT_CELL;
            } else if (position == infoUpdatesChannelRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }

    private void checkUpdates() {
        updateCheckCell.setCheckingStatus();
        UpdateManager.checkUpdates(new UpdateManager.UpdateCallback() {
            @Override
            public void onSuccess(Object updateResult) {
                checkingUpdates = false;
                FoxConfig.saveLastUpdateCheck();
                if (updateResult instanceof FOXENC.UpdateAvailable) {
                    if (updateAvailable == null) {
                        FoxConfig.saveUpdateStatus(1);
                        FoxConfig.remindUpdate(-1);
                        updateAvailable = (FOXENC.UpdateAvailable) updateResult;
                        FoxConfig.updateData.set(updateAvailable);
                        FoxConfig.applyUpdateData();
                        if (StoreUtils.isFromPlayStore()) {
                            PlayStoreAPI.openUpdatePopup(getParentActivity());
                        } else {
                            listAdapter.notifyItemRangeInserted(updateSectionAvailableRow, 2);
                            listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                            updateRowsId();
                        }
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    }
                    updateCheckCell.setUpdateAvailableStatus();
                } else {
                    FoxConfig.saveUpdateStatus(0);
                    updateCheckCell.setCheckTime();
                    if (updateAvailable != null) {
                        FoxConfig.updateData.set(null);
                        FoxConfig.applyUpdateData();
                        updateAvailable = null;
                        if (!StoreUtils.isFromPlayStore()) {
                            listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                            listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                            updateRowsId();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                FileLog.e(e);
                FoxConfig.saveUpdateStatus(2);
                updateCheckCell.setFailedStatus();
            }
        });
    }
}
