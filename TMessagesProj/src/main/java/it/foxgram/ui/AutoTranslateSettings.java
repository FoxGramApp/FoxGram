package it.foxgram.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.GroupCreateActivity;

import it.foxgram.android.FoxConfig;
import it.foxgram.android.translator.AutoTranslateConfig;

public class AutoTranslateSettings extends BaseSettingsActivity {

    private int imageRow;
    private int headerDefaultSettingsRow;
    private int alwaysTranslateRow;
    private int neverTranslateRow;
    private int autoTranslateHintRow;
    private int addExceptionsRow;
    private int alwaysAllowExceptionRow;
    private int neverAllowExceptionRow;
    private int exceptionsHintRow;
    private int resetExceptionsRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("AutoTranslate", R.string.AutoTranslate);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == alwaysTranslateRow || position == neverTranslateRow) {
            FoxConfig.setAutoTranslate(position == alwaysTranslateRow);
            listAdapter.notifyItemRangeChanged(alwaysTranslateRow, 2, PARTIAL);
        } else if (position == alwaysAllowExceptionRow || position == neverAllowExceptionRow) {
            final boolean isAllow = position == alwaysAllowExceptionRow;
            if (AutoTranslateConfig.getExceptions(isAllow).size() == 0) {
                Bundle args = new Bundle();
                args.putBoolean(isAllow ? "isAlwaysShare" : "isNeverShare", true);
                args.putInt("chatAddType", 1);
                GroupCreateActivity fragment = new GroupCreateActivity(args);
                fragment.setDelegate(ids -> {
                    for (int i = 0; i < ids.size(); i++) {
                        AutoTranslateConfig.setEnabled(ids.get(i), 0, isAllow);
                    }
                    listAdapter.notifyItemChanged(position, PARTIAL);
                });
                presentFragment(fragment);
            } else {
                presentFragment(new AutoTranslationException(isAllow));
            }
        } else if (position == resetExceptionsRow && AutoTranslateConfig.getAllExceptions().size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("NotificationsDeleteAllExceptionTitle", R.string.NotificationsDeleteAllExceptionTitle));
            builder.setMessage(LocaleController.getString("NotificationsDeleteAllExceptionAlert", R.string.NotificationsDeleteAllExceptionAlert));
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                AutoTranslateConfig.resetExceptions();
                listAdapter.notifyItemRangeChanged(alwaysAllowExceptionRow, 2, PARTIAL);
                listAdapter.notifyItemChanged(resetExceptionsRow);
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        imageRow = rowCount++;

        headerDefaultSettingsRow = rowCount++;
        alwaysTranslateRow = rowCount++;
        neverTranslateRow = rowCount++;
        autoTranslateHintRow = rowCount++;
        addExceptionsRow = rowCount++;
        alwaysAllowExceptionRow = rowCount++;
        neverAllowExceptionRow = rowCount++;
        exceptionsHintRow = rowCount++;
        resetExceptionsRow = rowCount++;
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
                    if (position == autoTranslateHintRow) {
                        hintCell.setText(LocaleController.getString("AutoTranslateDesc", R.string.AutoTranslateDesc));
                    } else if (position == exceptionsHintRow) {
                        hintCell.setText(LocaleController.getString("AutoTranslateDesc2", R.string.AutoTranslateDesc2));
                    }
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerDefaultSettingsRow) {
                        headerCell.setText(LocaleController.getString("AutoTranslateTitle", R.string.AutoTranslateTitle));
                    } else if (position == addExceptionsRow) {
                        headerCell.setText(LocaleController.getString("AddExceptions", R.string.AddExceptions));
                    }
                    break;
                case RADIO:
                    RadioCell radioCell = (RadioCell) holder.itemView;
                    if (position == alwaysTranslateRow) {
                        if (partial) {
                            radioCell.setChecked(FoxConfig.autoTranslate, true);
                        } else {
                            radioCell.setText(LocaleController.getString("AlwaysTranslate", R.string.AlwaysTranslate), FoxConfig.autoTranslate, true);
                        }
                    } else if (position == neverTranslateRow) {
                        if (partial) {
                            radioCell.setChecked(!FoxConfig.autoTranslate, true);
                        } else {
                            radioCell.setText(LocaleController.getString("NeverTranslate", R.string.NeverTranslate), !FoxConfig.autoTranslate, true);
                        }
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == alwaysAllowExceptionRow) {
                        settingsCell.setTextAndValue(LocaleController.getString("AlwaysAllow", R.string.AlwaysAllow), getExceptionText(AutoTranslateConfig.getExceptions(true).size()), partial, true);
                    } else if (position == neverAllowExceptionRow) {
                        settingsCell.setTextAndValue(LocaleController.getString("NeverAllow", R.string.NeverAllow), getExceptionText(AutoTranslateConfig.getExceptions(false).size()), partial, false);
                    } else if (position == resetExceptionsRow) {
                        settingsCell.setTag(Theme.key_dialogTextRed);
                        settingsCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                        settingsCell.setCanDisable(true);
                        settingsCell.setText(LocaleController.getString("NotificationsDeleteAllException", R.string.NotificationsDeleteAllException), false);
                    }
                    break;
            }
        }

        private String getExceptionText(int count) {
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
                backupImageView.setStickerNum(55);
                stickerHeaderCell.addView(backupImageView, LayoutHelper.createLinear(140, 140, Gravity.CENTER, 0, 20, 0, 20));
                view = stickerHeaderCell;
            }
            return view;
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            boolean canReset = AutoTranslateConfig.getAllExceptions().size() > 0;
            return viewType == ViewType.ADD_EXCEPTION ||
                    viewType == ViewType.RADIO ||
                    viewType == ViewType.SETTINGS && position != resetExceptionsRow ||
                    viewType == ViewType.SETTINGS && canReset;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == autoTranslateHintRow || position == exceptionsHintRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == headerDefaultSettingsRow || position == addExceptionsRow) {
                return ViewType.HEADER;
            } else if (position == alwaysTranslateRow || position == neverTranslateRow) {
                return ViewType.RADIO;
            } else if (position == alwaysAllowExceptionRow || position == neverAllowExceptionRow || position == resetExceptionsRow) {
                return ViewType.SETTINGS;
            } else if (position == imageRow) {
                return ViewType.IMAGE_HEADER;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
