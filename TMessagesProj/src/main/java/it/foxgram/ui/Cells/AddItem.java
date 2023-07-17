package it.foxgram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProgressButton;

public class AddItem extends FrameLayout {

    private final SimpleTextView textView;
    private final ProgressButton addButton;
    private boolean needDivider;
    public String menuId;

    public AddItem(Context context) {
        super(context);

        textView = new SimpleTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        textView.setMaxLines(1);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 22, 0, 22, 0));

        addButton = new ProgressButton(context);
        addButton.setText(LocaleController.getString("Add", R.string.Add));
        addButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        addButton.setProgressColor(Theme.getColor(Theme.key_featuredStickers_buttonProgress));
        addButton.setBackgroundRoundRect(Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed));
        addView(addButton, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, 28, Gravity.TOP | Gravity.END, 0, 18, 14, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(64));
        measureChildWithMargins(addButton, widthMeasureSpec, 0, heightMeasureSpec, 0);
        measureChildWithMargins(textView, widthMeasureSpec, addButton.getMeasuredWidth(), heightMeasureSpec, 0);
    }

    public void setData(String text, String id, boolean isPremium, boolean divider) {
        needDivider = divider;
        menuId = id;
        setWillNotDraw(!needDivider);
        textView.setText(text);
        if (isPremium) {
            Drawable drawable = getContext().getDrawable(R.drawable.msg_premium_liststar).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuPhoneCats), PorterDuff.Mode.MULTIPLY));
            textView.setRightDrawable(drawable);
        } else {
            textView.setRightDrawable(null);
        }
    }

    public void setAddOnClickListener(OnClickListener onClickListener) {
        addButton.setOnClickListener(onClickListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(0, getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
        }
    }
}
