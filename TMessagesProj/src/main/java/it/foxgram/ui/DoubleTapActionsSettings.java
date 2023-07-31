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

public class DoubleTapActionsSettings extends BaseSettingsActivity {
    private int imageRow;
    private int infoRow;
    private int doubleTapTypeRow;
    private int doubleTapNoneRow;
    private int doubleTapReactRow;
    private int doubleTapForwardRow;
    private int doubleTapEditRow;
    private int doubleTapDeleteRow;
    private int doubleTapSaveMessageRow;
    private int doubleTapCopyRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("DoubleTap", R.string.DoubleTap);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == doubleTapNoneRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_DISABLED);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        } else if (position == doubleTapCopyRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_COPY_TEXT);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        } else if (position == doubleTapDeleteRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_DELETE);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        } else if (position == doubleTapForwardRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_FORWARD);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        } else if (position == doubleTapReactRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_REACT);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        } else if (position == doubleTapEditRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_EDIT);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        } else if (position == doubleTapSaveMessageRow) {
            FoxConfig.saveDoubleTapType(FoxConfig.DOUBLE_TAP_SAVE_MESSAGE);
            listAdapter.notifyItemRangeChanged(doubleTapNoneRow, listAdapter.getItemCount(), PARTIAL);
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        imageRow = rowCount++;

        doubleTapTypeRow = rowCount++;
        doubleTapNoneRow = rowCount++;
        doubleTapReactRow = rowCount++;
        doubleTapForwardRow = rowCount++;
        doubleTapEditRow = rowCount++;
        doubleTapSaveMessageRow = rowCount++;
        doubleTapDeleteRow = rowCount++;
        doubleTapCopyRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new DoubleTapActionsSettings.ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("DoubleTapDesc", R.string.DoubleTapDesc));
                    }
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == doubleTapTypeRow) {
                        headerCell.setText(LocaleController.getString("DoubleTap", R.string.DoubleTap));
                    }
                    break;
                case RADIO:
                    RadioCell radioCell = (RadioCell) holder.itemView;
                    if (position == doubleTapCopyRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_COPY_TEXT, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Copy", R.string.Copy), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_COPY_TEXT, true);
                        }
                    } else if (position == doubleTapEditRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_EDIT, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Edit", R.string.Edit), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_EDIT, true);
                        }
                    } else if (position == doubleTapDeleteRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_DELETE, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Delete", R.string.Delete), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_DELETE, true);
                        }
                    } else if (position == doubleTapReactRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_REACT, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Reactions", R.string.Reactions), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_REACT, true);
                        }
                    } else if (position == doubleTapForwardRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_FORWARD, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Forward", R.string.Forward), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_FORWARD, true);
                        }
                    } else if (position == doubleTapNoneRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_DISABLED, true);
                        } else {
                            radioCell.setText(LocaleController.getString("Disable", R.string.Disable), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_DISABLED, true);
                        }
                    } else if (position == doubleTapSaveMessageRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_SAVE_MESSAGE, true);
                        } else {
                            radioCell.setText(LocaleController.getString("AddToSavedMessages", R.string.AddToSavedMessages), FoxConfig.doubleTapType == FoxConfig.DOUBLE_TAP_SAVE_MESSAGE, true);
                        }
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
                backupImageView.setStickerNum(9);
                stickerHeaderCell.addView(backupImageView, LayoutHelper.createLinear(140, 140, Gravity.CENTER, 0, 20, 0, 5));
                TextView textView = new TextView(context);
                textView.setText(LocaleController.getString("DoubleTapDesc2", R.string.DoubleTapDesc2));
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
            return viewType == ViewType.RADIO || viewType == ViewType.TEXT_CELL;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == infoRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == doubleTapTypeRow) {
                return ViewType.HEADER;
            } else if (position == doubleTapCopyRow || position == doubleTapReactRow ||
                    position == doubleTapEditRow || position == doubleTapNoneRow ||
                    position == doubleTapForwardRow || position == doubleTapDeleteRow ||
                    position == doubleTapSaveMessageRow) {
                return ViewType.RADIO;
            } else if (position == imageRow) {
                return ViewType.IMAGE_HEADER;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
