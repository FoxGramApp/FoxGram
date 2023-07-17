package it.foxgram.ui;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.PremiumPreviewFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import it.foxgram.android.AlertController;
import it.foxgram.android.FoxConfig;
import it.foxgram.android.translator.AutoTranslateConfig;
import it.foxgram.android.translator.BaseTranslator;
import it.foxgram.android.translator.DeepLTranslator;
import it.foxgram.android.translator.Translator;
import it.foxgram.android.translator.TranslatorHelper;

public class FoxGramTranslationsSettings extends BaseSettingsActivity {
    private final boolean supportLanguageDetector;

    private int translationHeaderRow;
    private int showTranslateButtonRow;
    private int translationStyle;
    private int translationProviderSelectRow;
    private int destinationLanguageSelectRow;
    private int doNotTranslateSelectRow;
    private int autoTranslateRow;
    private int keepMarkdownRow;
    private int deepLFormalityRow;
    private int translateEntireChatRow;
    private int hintTranslation1;
    private int hintTranslation2;
    private int divisorTranslationRow;
    private int divisorTranslateButtonRow;
    private int entireHeaderTranslationsRow;
    private int entireTranslationSettingsDivisor;
    private int translatorSettingsHeader;

    public FoxGramTranslationsSettings() {
        supportLanguageDetector = LanguageDetector.hasSupport();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("TranslationsTitle", R.string.TranslationsTitle);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == translationStyle) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("TranslatorTypeColor", R.string.TranslatorTypeColor));
            types.add(BaseTranslator.INLINE_STYLE);
            arrayList.add(LocaleController.getString("TranslatorTypeTG", R.string.TranslatorTypeTG));
            types.add(BaseTranslator.DIALOG_STYLE);
            AlertController.show(arrayList, LocaleController.getString("TranslatorType", R.string.TranslatorType), types.indexOf(FoxConfig.translatorStyle), context, i -> {
                FoxConfig.setTranslatorStyle(types.get(i));
                listAdapter.notifyItemChanged(translationStyle, PARTIAL);
            });
        }else if (position == translationProviderSelectRow) {
            final int oldProvider = FoxConfig.translationProvider;
            Translator.showTranslationProviderSelector(context, param -> {
                if (param) {
                    listAdapter.notifyItemChanged(translationProviderSelectRow, PARTIAL);
                } else {
                    listAdapter.notifyItemRangeChanged(translationProviderSelectRow, 2, PARTIAL);
                }
                listAdapter.notifyItemChanged(hintTranslation2);
                if (oldProvider != FoxConfig.translationProvider) {
                    updateListAnimated();
                }
            });
        } else if (position == destinationLanguageSelectRow) {
            presentFragment(new SelectLanguageSettings());
        } else if (position == doNotTranslateSelectRow) {
            if (!supportLanguageDetector) {
                BulletinFactory.of(this).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), LocaleController.getString("BrokenMLKitDetail", R.string.BrokenMLKitDetail), null).show();
                return;
            }
            presentFragment(new DoNotTranslateSettings());
        } else if (position == autoTranslateRow) {
            if (!getUserConfig().isPremium() && FoxConfig.translationProvider == Translator.PROVIDER_TELEGRAM) {
                showDialog(new PremiumFeatureBottomSheet(FoxGramTranslationsSettings.this, PremiumPreviewFragment.PREMIUM_FEATURE_TRANSLATIONS, false));
                return;
            }
            if (!supportLanguageDetector) {
                BulletinFactory.of(this).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), LocaleController.getString("BrokenMLKitDetail", R.string.BrokenMLKitDetail), null).show();
                return;
            }
            presentFragment(new AutoTranslateSettings());
        } else if (position == keepMarkdownRow) {
            if (!getUserConfig().isPremium() && FoxConfig.translationProvider == Translator.PROVIDER_TELEGRAM) {
                showDialog(new PremiumFeatureBottomSheet(FoxGramTranslationsSettings.this, PremiumPreviewFragment.PREMIUM_FEATURE_TRANSLATIONS, false));
                return;
            }
            FoxConfig.toggleKeepTranslationMarkdown();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.keepTranslationMarkdown);
            }
        } else if (position == showTranslateButtonRow) {
            FoxConfig.toggleShowTranslate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(FoxConfig.showTranslate);
            }
        } else if (position == translateEntireChatRow) {
            if (!getUserConfig().isPremium() && FoxConfig.translationProvider == Translator.PROVIDER_TELEGRAM) {
                showDialog(new PremiumFeatureBottomSheet(FoxGramTranslationsSettings.this, PremiumPreviewFragment.PREMIUM_FEATURE_TRANSLATIONS, false));
            } else {
                FoxConfig.toggleTranslateEntireChat();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(FoxConfig.translateEntireChat);
                }
            }
        } else if (position == deepLFormalityRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("DeepLFormalityDefault", R.string.DeepLFormalityDefault));
            types.add(DeepLTranslator.FORMALITY_DEFAULT);
            arrayList.add(LocaleController.getString("DeepLFormalityMore", R.string.DeepLFormalityMore));
            types.add(DeepLTranslator.FORMALITY_MORE);
            arrayList.add(LocaleController.getString("DeepLFormalityLess", R.string.DeepLFormalityLess));
            types.add(DeepLTranslator.FORMALITY_LESS);
            AlertController.show(arrayList, LocaleController.getString("DeepLFormality", R.string.DeepLFormality), types.indexOf(FoxConfig.deepLFormality), context, i -> {
                FoxConfig.setDeepLFormality(types.get(i));
                listAdapter.notifyItemChanged(deepLFormalityRow, PARTIAL);
            });
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        translationHeaderRow = rowCount++;
        showTranslateButtonRow = rowCount++;
        divisorTranslateButtonRow = rowCount++;

        entireHeaderTranslationsRow = TranslatorHelper.showPremiumFeatures() || TranslatorHelper.isSupportAutoTranslate() ? rowCount++: -1;
        translateEntireChatRow = TranslatorHelper.showPremiumFeatures() && TranslatorHelper.isSupportAutoTranslate() ? rowCount++: -1;
        autoTranslateRow = TranslatorHelper.isSupportAutoTranslate() ? rowCount++ : -1;
        entireTranslationSettingsDivisor = TranslatorHelper.showPremiumFeatures() || TranslatorHelper.isSupportAutoTranslate() ? rowCount++: -1;

        translatorSettingsHeader = rowCount++;
        translationStyle = rowCount++;
        translationProviderSelectRow = rowCount++;
        destinationLanguageSelectRow = rowCount++;
        doNotTranslateSelectRow = rowCount++;
        deepLFormalityRow = FoxConfig.translationProvider == Translator.PROVIDER_DEEPL ? rowCount++ : -1;
        keepMarkdownRow = TranslatorHelper.isSupportMarkdown() ? rowCount++ : -1;
        hintTranslation1 = rowCount++;
        hintTranslation2 = rowCount++;
        divisorTranslationRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new FoxGramTranslationsSettings.ListAdapter();
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
                    if (position == translationHeaderRow) {
                        headerCell.setText(LocaleController.getString("TranslateButtonSettings", R.string.TranslateButtonSettings));
                    } else if (position == entireHeaderTranslationsRow) {
                        headerCell.setText(LocaleController.getString("EntireTranslations", R.string.EntireTranslations));
                    } else if (position == translatorSettingsHeader) {
                        headerCell.setText(LocaleController.getString("TranslationsSettings", R.string.TranslationsSettings));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    textCheckCell.setCheckBoxIcon(0);
                    boolean isLocked = !getUserConfig().isPremium() && FoxConfig.translationProvider == Translator.PROVIDER_TELEGRAM;
                    if (position == keepMarkdownRow) {
                       textCheckCell.setTextAndValueAndCheck(LocaleController.getString("KeepMarkdown", R.string.KeepMarkdown), LocaleController.getString("KeepMarkdownDesc", R.string.KeepMarkdownDesc), FoxConfig.keepTranslationMarkdown, true, false);
                       textCheckCell.setCheckBoxIcon(isLocked ? R.drawable.permission_locked : 0);
                    } else if (position == showTranslateButtonRow) {
                       textCheckCell.setTextAndCheck(LocaleController.getString("ShowTranslateButton", R.string.ShowTranslateButton), FoxConfig.showTranslate, true);
                    } else if (position == translateEntireChatRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("ShowTranslateChatButton", R.string.ShowTranslateChatButton), LocaleController.getString("ShowTranslateChatButtonDesc", R.string.ShowTranslateChatButtonDesc), FoxConfig.translateEntireChat, true, true);
                        textCheckCell.setCheckBoxIcon(isLocked ? R.drawable.permission_locked : 0);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == hintTranslation1) {
                        textInfoPrivacyCell.setTopPadding(0);
                        textInfoPrivacyCell.setText(LocaleController.getString("TranslateMessagesInfo1", R.string.TranslateMessagesInfo1));
                    } else if (position == hintTranslation2) {
                        Pair<ArrayList<String>, ArrayList<Integer>> providers = Translator.getProviders();
                        ArrayList<String> names = providers.first;
                        ArrayList<Integer> types = providers.second;
                        if (names == null || types == null) {
                            return;
                        }
                        int index = types.indexOf(FoxConfig.translationProvider);
                        if (index < 0) {
                            index = types.indexOf(Translator.PROVIDER_GOOGLE);
                        }
                        textInfoPrivacyCell.setTopPadding(0);
                        textInfoPrivacyCell.setText(LocaleController.formatString("TranslationProviderInfo", R.string.TranslationProviderInfo, names.get(index)));
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == translationProviderSelectRow) {
                        Pair<ArrayList<String>, ArrayList<Integer>> providers = Translator.getProviders();
                        ArrayList<String> names = providers.first;
                        ArrayList<Integer> types = providers.second;
                        if (names == null || types == null) {
                            return;
                        }
                        int index = types.indexOf(FoxConfig.translationProvider);
                        if (index < 0) {
                            textSettingsCell.setTextAndValue(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), names.get(Translator.PROVIDER_GOOGLE), partial,true);
                        } else {
                            textSettingsCell.setTextAndValue(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), names.get(index), partial,true);
                        }
                    } else if (position == destinationLanguageSelectRow) {
                        String language = FoxConfig.translationTarget;
                        CharSequence value;
                        if (language.equals("app")) {
                            value = LocaleController.getString("Default", R.string.Default);
                        } else {
                            Locale locale = Locale.forLanguageTag(language);
                            if (!TextUtils.isEmpty(locale.getScript())) {
                                value = HtmlCompat.fromHtml(AndroidUtilities.capitalize(locale.getDisplayScript()), HtmlCompat.FROM_HTML_MODE_LEGACY);
                            } else {
                                value = AndroidUtilities.capitalize(locale.getDisplayName());
                            }
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("TranslationLanguage", R.string.TranslationLanguage), value, partial,true);
                    } else if (position == doNotTranslateSelectRow) {
                        String doNotTranslateCellValue = null;
                        HashSet<String> langCodes = DoNotTranslateSettings.getRestrictedLanguages(false);
                        if (langCodes.size() == 1) {
                            try {
                                String language = langCodes.iterator().next();
                                if (language.equals("app")) {
                                    doNotTranslateCellValue = LocaleController.getString("Default", R.string.Default);
                                } else {
                                    Locale locale = Locale.forLanguageTag(language);
                                    if (!TextUtils.isEmpty(locale.getScript())) {
                                        doNotTranslateCellValue = HtmlCompat.fromHtml(AndroidUtilities.capitalize(locale.getDisplayScript()), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                                    } else {
                                        doNotTranslateCellValue = AndroidUtilities.capitalize(locale.getDisplayName());
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        } else if (langCodes.size() == 0) {
                            doNotTranslateCellValue = LocaleController.getString("EmptyExceptions", R.string.EmptyExceptions);
                        }
                        if (doNotTranslateCellValue == null)
                            doNotTranslateCellValue = String.format(LocaleController.getPluralString("Languages", langCodes.size()), langCodes.size());
                        if (!supportLanguageDetector) {
                            doNotTranslateCellValue = LocaleController.getString("EmptyExceptions", R.string.EmptyExceptions);
                            textSettingsCell.setAlpha(0.5f);
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("DoNotTranslate", R.string.DoNotTranslate), doNotTranslateCellValue, partial,true);
                    } else if (position == deepLFormalityRow) {
                        String value;
                        switch (FoxConfig.deepLFormality) {
                            case DeepLTranslator.FORMALITY_DEFAULT:
                                value = LocaleController.getString("DeepLFormalityDefault", R.string.DeepLFormalityDefault);
                                break;
                            case DeepLTranslator.FORMALITY_MORE:
                                value = LocaleController.getString("DeepLFormalityMore", R.string.DeepLFormalityMore);
                                break;
                            case DeepLTranslator.FORMALITY_LESS:
                            default:
                                value = LocaleController.getString("DeepLFormalityLess", R.string.DeepLFormalityLess);
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("DeepLFormality", R.string.DeepLFormality), value, partial,true);
                    } else if (position == translationStyle) {
                        String value;
                        switch (FoxConfig.translatorStyle) {
                            case BaseTranslator.INLINE_STYLE:
                                value = LocaleController.getString("TranslatorTypeColor", R.string.TranslatorTypeColor);
                                break;
                            case BaseTranslator.DIALOG_STYLE:
                            default:
                                value = LocaleController.getString("TranslatorTypeTG", R.string.TranslatorTypeTG);
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("TranslatorType", R.string.TranslatorType), value, partial,true);
                    } else if (position == autoTranslateRow) {
                        String value;
                        if (supportLanguageDetector) {
                            value = FoxConfig.autoTranslate ? LocaleController.getString("UseLessDataAlways", R.string.UseLessDataAlways) : LocaleController.getString("UseLessDataNever", R.string.UseLessDataNever);
                            int always = AutoTranslateConfig.getExceptions(true).size();
                            int never = AutoTranslateConfig.getExceptions(false).size();
                            if (always > 0 && never > 0) {
                                value += " (-" + never + ", +" + always + ")";
                            } else if (always > 0) {
                                value += " (+" + always + ")";
                            } else if (never > 0) {
                                value += " (-" + never + ")";
                            }
                        } else {
                            value = LocaleController.getString("UseLessDataNever", R.string.UseLessDataNever);
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("AutoTranslate", R.string.AutoTranslate), value, keepMarkdownRow != -1);
                        if (!supportLanguageDetector) textSettingsCell.setAlpha(0.5f);
                        ImageView imageView = textSettingsCell.getValueImageView();
                        if (!getUserConfig().isPremium() && FoxConfig.translationProvider == Translator.PROVIDER_TELEGRAM) {
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setImageResource(R.drawable.msg_mini_premiumlock);
                            imageView.setTranslationY(AndroidUtilities.dp(1));
                            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteValueText), PorterDuff.Mode.MULTIPLY));
                        } else {
                            imageView.setVisibility(View.GONE);
                            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
                        }
                        break;
                    }
                }
            }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            if (position == autoTranslateRow || position == doNotTranslateSelectRow) {
                return supportLanguageDetector;
            }
            return viewType == ViewType.SWITCH || viewType == ViewType.SETTINGS;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            return null;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == divisorTranslationRow || position == divisorTranslateButtonRow || position == entireTranslationSettingsDivisor) {
                return ViewType.SHADOW;
            } else if (position == translationHeaderRow || position == entireHeaderTranslationsRow || position == translatorSettingsHeader) {
                return ViewType.HEADER;
            } else if (position == keepMarkdownRow || position == showTranslateButtonRow || position == translateEntireChatRow) {
                return ViewType.SWITCH;
            } else if (position == translationProviderSelectRow || position == destinationLanguageSelectRow || position == deepLFormalityRow ||
                    position == translationStyle || position == doNotTranslateSelectRow || position == autoTranslateRow) {
                return ViewType.SETTINGS;
            } else if (position == hintTranslation1 || position == hintTranslation2) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateListAnimated() {
        if (listAdapter == null) {
            updateRowsId();
            return;
        }
        FoxGramTranslationsSettings.DiffCallback diffCallback = new FoxGramTranslationsSettings.DiffCallback();
        diffCallback.oldRowCount = rowCount;
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        diffCallback.oldFeatures.addAll(diffCallback.getNewFeatures());
        diffCallback.oldFeaturesStart = showTranslateButtonRow + 1;
        diffCallback.oldFeaturesEnd = divisorTranslationRow - 1;
        updateRowsId();
        diffCallback.fillPositions(diffCallback.newPositionToItem);
        try {
            DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(listAdapter);
        } catch (Exception e) {
            FileLog.e(e);
            listAdapter.notifyDataSetChanged();
        }
        AndroidUtilities.updateVisibleRows(listView);
    }

    private class DiffCallback extends DiffUtil.Callback {

        SparseIntArray oldPositionToItem = new SparseIntArray();
        SparseIntArray newPositionToItem = new SparseIntArray();

        int oldRowCount;
        ArrayList<String> oldFeatures = new ArrayList<>();
        int oldFeaturesStart;
        int oldFeaturesEnd;

        @Override
        public int getOldListSize() {
            return oldRowCount;
        }

        @Override
        public int getNewListSize() {
            return rowCount;
        }

        public ArrayList<String> getNewFeatures() {
            ArrayList<String> newFeatures = new ArrayList<>();
            if (TranslatorHelper.isSupportAutoTranslate()) {
                newFeatures.add("Auto");
            }
            newFeatures.add("Style");
            newFeatures.add("Provider");
            newFeatures.add("Language");
            newFeatures.add("DoNotTranslate");
            if (FoxConfig.translationProvider == Translator.PROVIDER_DEEPL) {
                newFeatures.add("Formality");
            }
            if (TranslatorHelper.isSupportMarkdown()) {
                newFeatures.add("Markdown");
            }
            if (TranslatorHelper.showPremiumFeatures() && TranslatorHelper.isSupportAutoTranslate()) {
                newFeatures.add("EntireChat");
            }
            return newFeatures;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ArrayList<String> newFeatures = getNewFeatures();
            int featuresStart = showTranslateButtonRow + 1;
            int featuresEnd = divisorTranslationRow - 1;
            if (newItemPosition >= featuresStart && newItemPosition < featuresEnd) {
                if (oldItemPosition >= oldFeaturesStart && oldItemPosition < oldFeaturesEnd) {
                    String oldItem = oldFeatures.get(oldItemPosition - oldFeaturesStart);
                    String newItem = newFeatures.get(newItemPosition - featuresStart);
                    return TextUtils.equals(oldItem, newItem);
                }
            }
            int oldIndex = oldPositionToItem.get(oldItemPosition, -1);
            int newIndex = newPositionToItem.get(newItemPosition, -1);
            return oldIndex == newIndex && oldIndex >= 0;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return areItemsTheSame(oldItemPosition, newItemPosition);
        }

        public void fillPositions(SparseIntArray sparseIntArray) {
            sparseIntArray.clear();
            int pointer = 0;

            put(++pointer, translationHeaderRow, sparseIntArray);
            put(++pointer, showTranslateButtonRow, sparseIntArray);
            put(++pointer, translationStyle, sparseIntArray);
            put(++pointer, translationProviderSelectRow, sparseIntArray);
            put(++pointer, destinationLanguageSelectRow, sparseIntArray);
            put(++pointer, doNotTranslateSelectRow, sparseIntArray);
            put(++pointer, divisorTranslationRow, sparseIntArray);
            put(++pointer, hintTranslation1, sparseIntArray);
            put(++pointer, hintTranslation2, sparseIntArray);
        }

        private void put(int id, int position, SparseIntArray sparseIntArray) {
            if (position >= 0) {
                sparseIntArray.put(position, id);
            }
        }
    }
}
