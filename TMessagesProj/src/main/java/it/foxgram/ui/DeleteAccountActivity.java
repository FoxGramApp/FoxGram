/*
 * This is the source code of FoxGram for Android v. 3.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2023.
 */
package it.foxgram.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.voip.CellFlickerDrawable;

import java.util.Locale;

public class DeleteAccountActivity extends BaseFragment {
    private RLottieImageView imageView;
    private TextView buttonTextView;
    private TextView descriptionText;
    private TextView titleTextView;
    private boolean flickerButton;

    private void deleteActivity(Context context) {
        Utilities.globalQueue.postRunnable(() -> {
            TLRPC.TL_account_deleteAccount req = new TLRPC.TL_account_deleteAccount();
            req.reason = "UserRequestDelete";
            getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                if (response instanceof TLRPC.TL_boolTrue) {
                    getMessagesController().performLogout(0);
                } else if (error == null || error.code != -1000) {
                    String errorText = LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred);
                    if (error != null) {
                        errorText += "\n" + error.text;
                    }
                    finishFragment(true);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder1.setMessage(errorText);
                    builder1.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                    builder1.show();
                }
            }));
        }, 500);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View createView(Context context) {
        if (actionBar != null) {
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarWhiteSelector), false);
            actionBar.setCastShadows(false);
            actionBar.setAddToContainer(false);
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });
        }
        fragmentView = new ViewGroup(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);

                if (actionBar != null) {
                    actionBar.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
                }

                imageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY));
                if (width > height) {
                    titleTextView.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    buttonTextView.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42), MeasureSpec.EXACTLY));
                } else {
                    titleTextView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    buttonTextView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(24 * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50), MeasureSpec.EXACTLY));
                }
                setMeasuredDimension(width, height);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                if (actionBar != null) {
                    actionBar.layout(0, 0, r, actionBar.getMeasuredHeight());
                }
                int width = r - l;
                int height = b - t;

                int y;
                int x;
                if (r > b) {
                    y = (height - imageView.getMeasuredHeight()) / 2;
                    x = (int) (width * 0.5f - imageView.getMeasuredWidth()) / 2;
                    imageView.layout(x, y, x + imageView.getMeasuredWidth(), y + imageView.getMeasuredHeight());
                    x = (int) (width * 0.4f);
                    y = (int) (height * 0.14f);
                    titleTextView.layout(x, y, x + titleTextView.getMeasuredWidth(), y + titleTextView.getMeasuredHeight());
                    x = (int) (width * 0.4f);
                    y = (int) (height * 0.31f);
                    descriptionText.layout(x, y, x + descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());
                    x = (int) (width * 0.4f + (width * 0.6f - buttonTextView.getMeasuredWidth()) / 2);
                    y = (int) (height * 0.78f);
                } else {
                    y = (int) (height * 0.3f);
                    x = (width - imageView.getMeasuredWidth()) / 2;
                    imageView.layout(x, y, x + imageView.getMeasuredWidth(), y + imageView.getMeasuredHeight());
                    y += imageView.getMeasuredHeight() + AndroidUtilities.dp(24);
                    titleTextView.layout(0, y, titleTextView.getMeasuredWidth(), y + titleTextView.getMeasuredHeight());
                    y += titleTextView.getTextSize() + AndroidUtilities.dp(16);
                    descriptionText.layout(0, y, descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());
                    x = (width - buttonTextView.getMeasuredWidth()) / 2;
                    y = height - buttonTextView.getMeasuredHeight() - AndroidUtilities.dp(48);
                }
                buttonTextView.layout(x, y, x + buttonTextView.getMeasuredWidth(), y + buttonTextView.getMeasuredHeight());
            }
        };
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        ViewGroup viewGroup = (ViewGroup) fragmentView;
        viewGroup.setOnTouchListener((v, event) -> true);

        if (actionBar != null) {
            viewGroup.addView(actionBar);
        }

        imageView = new RLottieImageView(context);
        viewGroup.addView(imageView);

        titleTextView = new TextView(context);
        titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        viewGroup.addView(titleTextView);

        descriptionText = new TextView(context);
        descriptionText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText.setLineSpacing(AndroidUtilities.dp(2), 1);
        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionText.setPadding(AndroidUtilities.dp(48), 0, AndroidUtilities.dp(48), 0);
        viewGroup.addView(descriptionText);

        buttonTextView = new TextView(context) {
            CellFlickerDrawable cellFlickerDrawable;

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (flickerButton) {
                    if (cellFlickerDrawable == null) {
                        cellFlickerDrawable = new CellFlickerDrawable();
                        cellFlickerDrawable.drawFrame = false;
                        cellFlickerDrawable.repeatProgress = 2f;
                    }
                    cellFlickerDrawable.setParentWidth(getMeasuredWidth());
                    AndroidUtilities.rectTmp.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                    cellFlickerDrawable.draw(canvas, AndroidUtilities.rectTmp, AndroidUtilities.dp(4), null);
                    invalidate();
                }
            }
        };

        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setEnabled(false);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_dialogTextRed), Theme.getColor(Theme.key_dialogTextRed)));
        viewGroup.addView(buttonTextView);
        buttonTextView.setOnClickListener(v -> {
                deleteActivity(context);
                finishFragment(true);
        });
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setOnClickListener(v -> {
            if (!imageView.getAnimatedDrawable().isRunning()) {
                imageView.getAnimatedDrawable().setCurrentFrame(0, false);
                imageView.playAnimation();
            }
        });
        imageView.setAnimation(R.raw.double_bottom, 200, 200);
        String buttonText = LocaleController.getString("Deactivate", R.string.Deactivate);
        titleTextView.setText(LocaleController.getString("DeleteAccount", R.string.DeleteAccount));
        descriptionText.setText(LocaleController.getString("TosDeclineDeleteAccount", R.string.TosDeclineDeleteAccount));
        buttonTextView.setText(buttonText);
        new CountDownTimer(30000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                buttonTextView.setText(String.format(Locale.getDefault(), "%s (%d)", buttonText, millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                buttonTextView.setText(buttonText);
                buttonTextView.setEnabled(true);
            }
        }.start();
        flickerButton = true;
        imageView.getAnimatedDrawable().setAutoRepeat(1);
        imageView.playAnimation();
        if (flickerButton) {
            buttonTextView.setPadding(AndroidUtilities.dp(34), AndroidUtilities.dp(8), AndroidUtilities.dp(34), AndroidUtilities.dp(8));
            buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
        return fragmentView;
    }
}
