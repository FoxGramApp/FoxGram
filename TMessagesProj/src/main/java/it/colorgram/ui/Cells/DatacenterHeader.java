package it.colorgram.ui.Cells;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.colorgram.android.entities.EntitiesHelper;

public class DatacenterHeader extends LinearLayout {
    public DatacenterHeader(Context context) {
        super(context);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        StickerImageView rLottieImageView = new StickerImageView(context, UserConfig.selectedAccount);
        rLottieImageView.setStickerPackName("UtyaDuck");
        rLottieImageView.setStickerNum(31);
        rLottieImageView.getImageReceiver().setAutoRepeat(1);
        addView(rLottieImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

        TextView textView = new TextView(context);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
        textView.setPadding(0, 0, 0, 35);
        String text = LocaleController.getString("DatacenterStatusDesc", R.string.DatacenterStatusDesc);
        Spannable htmlParsed = new SpannableString(AndroidUtilities.fromHtml(text));
        textView.setText(EntitiesHelper.getUrlNoUnderlineText(htmlParsed));
        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
    }
}
