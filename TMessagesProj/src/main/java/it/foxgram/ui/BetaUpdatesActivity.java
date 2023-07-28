package it.foxgram.ui;

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;

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
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.foxgram.android.FoxConfig;

public class BetaUpdatesActivity extends BaseSettingsActivity {
    private int imageRow;
    private int channelHeaderRow;
    private int stableRow;
    private int betaRow;
//    private int alphaRow;
    private int infoRow;

    @Override
    public String getActionBarTitle() {
        return LocaleController.getString("APKsChannel", R.string.APKsChannel);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == stableRow || position == betaRow) {
            if (position == stableRow) {
                if (FoxConfig.betaUpdates) {
                    FoxConfig.toggleBetaUpdates();
                }
            }
            if (position == betaRow) {
                if (!FoxConfig.betaUpdates) {
                    FoxConfig.toggleBetaUpdates();
                }
            }
        }
        listAdapter.notifyItemRangeChanged(stableRow, 2, PARTIAL);
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        imageRow = rowCount++;

        channelHeaderRow = rowCount++;
        stableRow = rowCount++;
        betaRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new BetaUpdatesActivity.ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == channelHeaderRow) {
                        headerCell.setText(LocaleController.getString("InstallPreview", R.string.InstallPreview));
                    }
                    break;
                case RADIO:
                    RadioCell radioCell = (RadioCell) holder.itemView;
                    if (position == stableRow) {
                        if (partial) {
                            radioCell.setChecked(!FoxConfig.betaUpdates, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Stable", R.string.Stable), !FoxConfig.betaUpdates, true);
                        }
                    } else if (position == betaRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.betaUpdates, true);
                        } else {
                            radioCell.setText("Release Preview", FoxConfig.betaUpdates, true);
                        }
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    textInfoPrivacyCell.setText(LocaleController.getString("UpdatesChannelInfo", R.string.UpdatesChannelInfo));
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.RADIO;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.IMAGE_HEADER) {
                LinearLayout stickerHeaderCell = new LinearLayout(context);
                stickerHeaderCell.setOrientation(LinearLayout.VERTICAL);
                StickerImageView backupImageView = new StickerImageView(context, currentAccount);
                backupImageView.setStickerPackName("UtyaDuckFull");
                backupImageView.setStickerNum(2);
                stickerHeaderCell.addView(backupImageView, LayoutHelper.createLinear(140, 140, Gravity.CENTER, 0, 20, 0, 5));
                TextView textView = new TextView(context);
                textView.setText(LocaleController.getString("InstallPreviewDesc", R.string.InstallPreviewDesc));
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
        public ViewType getViewType(int position) {
            if (position == imageRow) {
                return ViewType.IMAGE_HEADER;
            } else if (position == channelHeaderRow) {
                return ViewType.HEADER;
            } else if (position == stableRow || position == betaRow) {
                return ViewType.RADIO;
            } else if (position == infoRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
