/*
 * This is the source code of FoxGram for Android v. 3.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Pierlu096, 2024.
 */

/*
 * This class is not ready to be implemented and it isn't working.
 * It is in working in progress stage and will be added in future updates, maybe.
 * NOT enable it, and note that some feature are missing.
 */

package it.foxgram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.CodeFieldContainer;
import org.telegram.ui.CodeNumberField;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.CustomPhoneKeyboardView;
import org.telegram.ui.Components.Easings;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineTextContainerView;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TextViewSwitcher;
import org.telegram.ui.Components.TransformableLoginButtonView;
import org.telegram.ui.Components.VerticalPositionAutoAnimator;

import java.util.concurrent.atomic.AtomicBoolean;

public class LockedChatsActivity extends BaseFragment {
    public long dialogId;
//    public Runnable runnable;

    private RLottieImageView lockImageView;
    private TextViewSwitcher descriptionTextSwitcher;
    private OutlineTextContainerView outlinePasswordView;
    private EditTextBoldCursor passwordEditText;
    private CodeFieldContainer codeFieldContainer;
    private TextView passcodesDoNotMatchTextView;
    private TextView passcodeAlreadyUsedTextView;

    private ImageView passwordButton;

    private CustomPhoneKeyboardView keyboardView;

    private FrameLayout floatingButtonContainer;
    private VerticalPositionAutoAnimator floatingAutoAnimator;
    private TransformableLoginButtonView floatingButtonIcon;
    private Animator floatingButtonAnimator;

    private boolean postedHidePasscodesDoNotMatch;
    private final Runnable hidePasscodesDoNotMatch = () -> {
        postedHidePasscodesDoNotMatch = false;
        AndroidUtilities.updateViewVisibilityAnimated(passcodesDoNotMatchTextView, false);
    };

    private boolean postedHideAlreadyUsedPasscode;
    private final Runnable hideAlreadyUsedPasscode = () -> {
        postedHideAlreadyUsedPasscode = false;
        AndroidUtilities.updateViewVisibilityAnimated(passcodeAlreadyUsedTextView, false);
    };

    private Runnable onShowKeyboardCallback;

    public LockedChatsActivity(long dialogId) {
        this.dialogId = dialogId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        View fragmentContentView;
        FrameLayout frameLayout = new FrameLayout(context);
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        scrollView.setFillViewport(true);
        fragmentContentView = scrollView;

        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int frameBottom;
                if (keyboardView.getVisibility() != View.GONE && measureKeyboardHeight() >= AndroidUtilities.dp(20)) {
                    if (isCustomKeyboardVisible()) {
                        fragmentContentView.layout(0, 0, getMeasuredWidth(), frameBottom = getMeasuredHeight() - AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP) + measureKeyboardHeight());
                    } else {
                        fragmentContentView.layout(0, 0, getMeasuredWidth(), frameBottom = getMeasuredHeight());
                    }
                } else if (keyboardView.getVisibility() != View.GONE) {
                    fragmentContentView.layout(0, 0, getMeasuredWidth(), frameBottom = getMeasuredHeight() - AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP));
                } else {
                    fragmentContentView.layout(0, 0, getMeasuredWidth(), frameBottom = getMeasuredHeight());
                }

                keyboardView.layout(0, frameBottom, getMeasuredWidth(), frameBottom + AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP));
                notifyHeightChanged();
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(width, height);

                int frameHeight = height;
                if (keyboardView.getVisibility() != View.GONE && measureKeyboardHeight() < AndroidUtilities.dp(20)) {
                    frameHeight -= AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP);
                }
                fragmentContentView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(frameHeight, MeasureSpec.EXACTLY));
                keyboardView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP), MeasureSpec.EXACTLY));
            }
        };
        contentView.setDelegate((keyboardHeight, isWidthGreater) -> {
            if (keyboardHeight >= AndroidUtilities.dp(20) && onShowKeyboardCallback != null) {
                onShowKeyboardCallback.run();
                onShowKeyboardCallback = null;
            }
        });
        fragmentView = contentView;
        contentView.addView(fragmentContentView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        keyboardView = new CustomPhoneKeyboardView(context);
        keyboardView.setVisibility(isCustomKeyboardVisible() ? View.VISIBLE : View.GONE);
        contentView.addView(keyboardView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP));
        if (actionBar != null) {
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarWhiteSelector), false);
            actionBar.setCastShadows(false);

            FrameLayout codeContainer = new FrameLayout(context);

            LinearLayout innerLinearLayout = new LinearLayout(context);
            innerLinearLayout.setOrientation(LinearLayout.VERTICAL);
            innerLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            frameLayout.addView(innerLinearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            lockImageView = new RLottieImageView(context);
            lockImageView.setFocusable(false);
            lockImageView.setAnimation(R.raw.tsv_setup_intro, 120, 120);
            lockImageView.setAutoRepeat(false);
            lockImageView.playAnimation();
            lockImageView.setVisibility(!AndroidUtilities.isSmallScreen() && AndroidUtilities.displaySize.x < AndroidUtilities.displaySize.y ? View.VISIBLE : View.GONE);
            innerLinearLayout.addView(lockImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL));

            TextView titleTextView = new TextView(context);
            titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            titleTextView.setText(LocaleController.getString(R.string.EnterYourPasscode));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            innerLinearLayout.addView(titleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

            descriptionTextSwitcher = new TextViewSwitcher(context);
            descriptionTextSwitcher.setFactory(() -> {
                TextView tv = new TextView(context);
                tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLineSpacing(AndroidUtilities.dp(2), 1);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                return tv;
            });
            descriptionTextSwitcher.setInAnimation(context, R.anim.alpha_in);
            descriptionTextSwitcher.setOutAnimation(context, R.anim.alpha_out);
            innerLinearLayout.addView(descriptionTextSwitcher, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 8, 20, 0));

            TextView forgotPasswordButton = new TextView(context);
            forgotPasswordButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            forgotPasswordButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            forgotPasswordButton.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
            forgotPasswordButton.setGravity((isPassword() ? Gravity.LEFT : Gravity.CENTER_HORIZONTAL) | Gravity.CENTER_VERTICAL);
            forgotPasswordButton.setOnClickListener(v -> AlertsCreator.createForgotPasscodeDialog(context).show());
            forgotPasswordButton.setVisibility(View.VISIBLE);
            forgotPasswordButton.setText(LocaleController.getString(R.string.ForgotPasscode));
            frameLayout.addView(forgotPasswordButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));
            VerticalPositionAutoAnimator.attach(forgotPasswordButton);

            passcodesDoNotMatchTextView = new TextView(context);
            passcodesDoNotMatchTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            passcodesDoNotMatchTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
            passcodesDoNotMatchTextView.setText(LocaleController.getString(R.string.PasscodesDoNotMatchTryAgain));
            passcodesDoNotMatchTextView.setPadding(0, AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12));
            AndroidUtilities.updateViewVisibilityAnimated(passcodesDoNotMatchTextView, false, 1f, false);
            frameLayout.addView(passcodesDoNotMatchTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

            passcodeAlreadyUsedTextView = new TextView(context);
            passcodeAlreadyUsedTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            passcodeAlreadyUsedTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
            passcodeAlreadyUsedTextView.setText(LocaleController.getString("PasscodeAlreadyUsed", R.string.PasscodeAlreadyUsed));
            passcodeAlreadyUsedTextView.setPadding(0, AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12));
            AndroidUtilities.updateViewVisibilityAnimated(passcodeAlreadyUsedTextView, false, 1f, false);
            frameLayout.addView(passcodeAlreadyUsedTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

            outlinePasswordView = new OutlineTextContainerView(context);
            outlinePasswordView.setText(LocaleController.getString(R.string.EnterPassword));

            passwordEditText = new EditTextBoldCursor(context);
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            passwordEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            passwordEditText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            passwordEditText.setBackground(null);
            passwordEditText.setMaxLines(1);
            passwordEditText.setLines(1);
            passwordEditText.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            passwordEditText.setSingleLine(true);
            passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setTypeface(Typeface.DEFAULT);
            passwordEditText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated));
            passwordEditText.setCursorSize(AndroidUtilities.dp(20));
            passwordEditText.setCursorWidth(1.5f);
            int padding = AndroidUtilities.dp(16);
            passwordEditText.setPadding(padding, padding, padding, padding);
            passwordEditText.setOnFocusChangeListener((v, hasFocus) -> outlinePasswordView.animateSelection(hasFocus ? 1 : 0));

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(passwordEditText, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

            passwordButton = new ImageView(context);
            passwordButton.setImageResource(R.drawable.msg_message);
            passwordButton.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            passwordButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector), 1));
            AndroidUtilities.updateViewVisibilityAnimated(passwordButton, false, 0.1f, false);

            AtomicBoolean isPasswordShown = new AtomicBoolean(false);
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {}

                });

            passwordButton.setOnClickListener(v -> {
                isPasswordShown.set(!isPasswordShown.get());

                int selectionStart = passwordEditText.getSelectionStart(), selectionEnd = passwordEditText.getSelectionEnd();
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | (isPasswordShown.get() ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD));
                passwordEditText.setSelection(selectionStart, selectionEnd);
                passwordButton.setColorFilter(Theme.getColor(isPasswordShown.get() ? Theme.key_windowBackgroundWhiteInputFieldActivated : Theme.key_windowBackgroundWhiteHintText));
            });
            linearLayout.addView(passwordButton, LayoutHelper.createLinearRelatively(24, 24, 0, 0, 0, 14, 0));

            outlinePasswordView.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            codeContainer.addView(outlinePasswordView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 0));

            passwordEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
                processDone();
                return true;
            });
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (postedHidePasscodesDoNotMatch) {
                        codeFieldContainer.removeCallbacks(hidePasscodesDoNotMatch);
                        hidePasscodesDoNotMatch.run();
                    }
                        if (postedHideAlreadyUsedPasscode) {
                            codeFieldContainer.removeCallbacks(hideAlreadyUsedPasscode);
                            hideAlreadyUsedPasscode.run();
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                passwordEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    public void onDestroyActionMode(ActionMode mode) {
                    }

                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }
                });

                codeFieldContainer = new CodeFieldContainer(context) {
                    @Override
                    protected void processNextPressed() {
                        processDone();
                    }
                };
                codeFieldContainer.setNumbersCount(4, CodeFieldContainer.TYPE_PASSCODE);
                for (CodeNumberField f : codeFieldContainer.codeField) {
                    f.setShowSoftInputOnFocusCompat(!isCustomKeyboardVisible());
                    f.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    f.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                    f.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            if (postedHidePasscodesDoNotMatch) {
                                codeFieldContainer.removeCallbacks(hidePasscodesDoNotMatch);
                                hidePasscodesDoNotMatch.run();
                            }
                            if (postedHideAlreadyUsedPasscode) {
                                codeFieldContainer.removeCallbacks(hideAlreadyUsedPasscode);
                                hideAlreadyUsedPasscode.run();
                            }
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });
                    f.setOnFocusChangeListener((v, hasFocus) -> {
                        keyboardView.setEditText(f);
                        keyboardView.setDispatchBackWhenEmpty(true);
                    });
                }
                codeContainer.addView(codeFieldContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 40, 10, 40, 0));

                innerLinearLayout.addView(codeContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 32, 0, 72));

                floatingButtonContainer = new FrameLayout(context);
                StateListAnimator animator = new StateListAnimator();
                animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButtonIcon, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
                animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButtonIcon, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
                floatingButtonContainer.setStateListAnimator(animator);
                floatingButtonContainer.setOutlineProvider(new ViewOutlineProvider() {
                    @SuppressLint("NewApi")
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                    }
                });
                floatingAutoAnimator = VerticalPositionAutoAnimator.attach(floatingButtonContainer);
                frameLayout.addView(floatingButtonContainer, LayoutHelper.createFrame(56, 56, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 24, 16));
                floatingButtonContainer.setOnClickListener(view -> processDone());

                floatingButtonIcon = new TransformableLoginButtonView(context);
                floatingButtonIcon.setTransformType(TransformableLoginButtonView.TRANSFORM_ARROW_CHECK);
                floatingButtonIcon.setProgress(0f);
                floatingButtonIcon.setColor(Theme.getColor(Theme.key_chats_actionIcon));
                floatingButtonIcon.setDrawBackground(false);
                floatingButtonContainer.setContentDescription(LocaleController.getString(R.string.Next));
                floatingButtonContainer.addView(floatingButtonIcon, LayoutHelper.createFrame(56, 56));

                Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
                floatingButtonContainer.setBackground(drawable);

                updateFields();
            }

        return fragmentView;
    }

    @Override
    public boolean hasForceLightStatusBar() {
        return false;
    }

    private void setCustomKeyboardVisible(boolean visible, boolean animate) {
        if (visible) {
            AndroidUtilities.hideKeyboard(fragmentView);
            AndroidUtilities.requestAltFocusable(getParentActivity(), classGuid);
        } else {
            AndroidUtilities.removeAltFocusable(getParentActivity(), classGuid);
        }

        if (!animate) {
            keyboardView.setVisibility(visible ? View.VISIBLE : View.GONE);
            keyboardView.setAlpha(visible ? 1 : 0);
            keyboardView.setTranslationY(visible ? 0 : AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP));
            fragmentView.requestLayout();
        } else {
            ValueAnimator animator = ValueAnimator.ofFloat(visible ? 0 : 1, visible ? 1 : 0).setDuration(150);
            animator.setInterpolator(visible ? CubicBezierInterpolator.DEFAULT : Easings.easeInOutQuad);
            animator.addUpdateListener(animation -> {
                float val = (float) animation.getAnimatedValue();
                keyboardView.setAlpha(val);
                keyboardView.setTranslationY((1f - val) * AndroidUtilities.dp(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP) * 0.75f);
                fragmentView.requestLayout();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (visible) {
                        keyboardView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!visible) {
                        keyboardView.setVisibility(View.GONE);
                    }
                }
            });
            animator.start();
        }
    }

    private void setFloatingButtonVisible(boolean visible, boolean animate) {
        if (floatingButtonAnimator != null) {
            floatingButtonAnimator.cancel();
            floatingButtonAnimator = null;
        }
        if (!animate) {
            floatingAutoAnimator.setOffsetY(visible ? 0 : AndroidUtilities.dp(70));
            floatingButtonContainer.setAlpha(visible ? 1f : 0f);
            floatingButtonContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        } else {
            ValueAnimator animator = ValueAnimator.ofFloat(visible ? 0 : 1, visible ? 1 : 0).setDuration(150);
            animator.setInterpolator(visible ? AndroidUtilities.decelerateInterpolator : AndroidUtilities.accelerateInterpolator);
            animator.addUpdateListener(animation -> {
                float val = (float) animation.getAnimatedValue();
                floatingAutoAnimator.setOffsetY(AndroidUtilities.dp(70) * (1f - val));
                floatingButtonContainer.setAlpha(val);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (visible) {
                        floatingButtonContainer.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!visible) {
                        floatingButtonContainer.setVisibility(View.GONE);
                    }
                    if (floatingButtonAnimator == animation) {
                        floatingButtonAnimator = null;
                    }
                }
            });
            animator.start();
            floatingButtonAnimator = animator;
        }
    }

    private void animateSuccessAnimation(Runnable callback) {
        if (!isPinCode()) {
            callback.run();
            return;
        }
        for (int i = 0; i < codeFieldContainer.codeField.length; i++) {
            CodeNumberField field = codeFieldContainer.codeField[i];
            field.postDelayed(()-> field.animateSuccessProgress(1f), i * 75L);
        }
        codeFieldContainer.postDelayed(() -> {
            for (CodeNumberField f : codeFieldContainer.codeField) {
                f.animateSuccessProgress(0f);
            }
            callback.run();
        }, codeFieldContainer.codeField.length * 75L + 350L);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setCustomKeyboardVisible(isCustomKeyboardVisible(), false);
        if (lockImageView != null) {
            lockImageView.setVisibility(!AndroidUtilities.isSmallScreen() && AndroidUtilities.displaySize.x < AndroidUtilities.displaySize.y ? View.VISIBLE : View.GONE);
        }
        if (codeFieldContainer != null && codeFieldContainer.codeField != null) {
            for (CodeNumberField f : codeFieldContainer.codeField) {
                f.setShowSoftInputOnFocusCompat(!isCustomKeyboardVisible());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isCustomKeyboardVisible()) {
            AndroidUtilities.runOnUIThread(this::showKeyboard, 200);
        }
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);

        if (isCustomKeyboardVisible()) {
            AndroidUtilities.hideKeyboard(fragmentView);
            AndroidUtilities.requestAltFocusable(getParentActivity(), classGuid);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AndroidUtilities.removeAltFocusable(getParentActivity(), classGuid);
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
    }

    private void showKeyboard() {
        if (isPinCode()) {
            codeFieldContainer.codeField[0].requestFocus();
            if (!isCustomKeyboardVisible()) {
                AndroidUtilities.showKeyboard(codeFieldContainer.codeField[0]);
            }
        } else if (isPassword()) {
            passwordEditText.requestFocus();
            AndroidUtilities.showKeyboard(passwordEditText);
        }
    }

    private void updateFields() {
        String text;
        text = LocaleController.getString(R.string.EnterYourPasscodeInfo);

        boolean animate = !(descriptionTextSwitcher.getCurrentView().getText().equals(text) || TextUtils.isEmpty(descriptionTextSwitcher.getCurrentView().getText()));
        descriptionTextSwitcher.setText(LocaleController.getString(R.string.EnterYourPasscodeInfo), animate);
        if (isPinCode()) {
            AndroidUtilities.updateViewVisibilityAnimated(codeFieldContainer, true, 1f, animate);
            AndroidUtilities.updateViewVisibilityAnimated(outlinePasswordView, false, 1f, animate);
        } else if (isPassword()) {
            AndroidUtilities.updateViewVisibilityAnimated(codeFieldContainer, false, 1f, animate);
            AndroidUtilities.updateViewVisibilityAnimated(outlinePasswordView, true, 1f, animate);
        }
        boolean show = isPassword();
        if (show) {
            onShowKeyboardCallback = () -> {
                setFloatingButtonVisible(true, animate);
                AndroidUtilities.cancelRunOnUIThread(onShowKeyboardCallback);
            };
            AndroidUtilities.runOnUIThread(onShowKeyboardCallback, 3000);
        } else {
            setFloatingButtonVisible(false, animate);
        }
        setCustomKeyboardVisible(isCustomKeyboardVisible(), animate);
        showKeyboard();
    }

    private boolean isCustomKeyboardVisible() {
        return false;
    }

    private boolean isPinCode() {
        return SharedConfig.passcodeType == SharedConfig.PASSCODE_TYPE_PIN;
    }

    private boolean isPassword() {
        return SharedConfig.passcodeType == SharedConfig.PASSCODE_TYPE_PASSWORD;
    }

    private void processDone() {
        if (isPassword() && passwordEditText.getText().length() == 0) {
            onPasscodeError();
            return;
        }
        String password = isPinCode() ? codeFieldContainer.getCode() : passwordEditText.getText().toString();
        if (!SharedConfig.checkPasscode(password)) {
            passwordEditText.setText("");
            for (CodeNumberField f : codeFieldContainer.codeField) {
                f.setText("");
            }
            if (isPinCode()) {
                codeFieldContainer.codeField[0].requestFocus();
            }
            onPasscodeError();
            return;
        }

        passwordEditText.clearFocus();
        AndroidUtilities.hideKeyboard(passwordEditText);
        for (CodeNumberField f : codeFieldContainer.codeField) {
            f.clearFocus();
            AndroidUtilities.hideKeyboard(f);
        }
        keyboardView.setEditText(null);

//        animateSuccessAnimation(() -> runnable.run()); TODO
        animateSuccessAnimation(() -> {
            return;
        });
    }

//    public void setAnimatedAnimationCallback(Runnable callback) {
//        this.runnable = callback;
//    }

    private void onPasscodeError() {
        if (getParentActivity() == null) return;
        try {
            fragmentView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        } catch (Exception ignore) {}
        if (isPinCode()) {
            for (CodeNumberField f : codeFieldContainer.codeField) {
                f.animateErrorProgress(1f);
            }
        } else {
            outlinePasswordView.animateError(1f);
        }
        AndroidUtilities.shakeViewSpring(isPinCode() ? codeFieldContainer : outlinePasswordView, isPinCode() ? 10 : 4, () -> AndroidUtilities.runOnUIThread(()->{
            if (isPinCode()) {
                for (CodeNumberField f : codeFieldContainer.codeField) {
                    f.animateErrorProgress(0f);
                }
            } else {
                outlinePasswordView.animateError(0f);
            }
        }, isPinCode() ? 150 : 1000));
    }

//    public void openChatActivity(long dialogId) {
//        ChatActivity.of(dialogId); TODO
//    }

}
