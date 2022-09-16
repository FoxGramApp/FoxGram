package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.ExoPlayer;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BringAppForegroundService;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.PhotoViewer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhotoViewerWebView extends FrameLayout {
    private final static int YT_NOT_STARTED = -1,
        YT_COMPLETED = 0,
        YT_PLAYING = 1,
        YT_PAUSED = 2,
        YT_BUFFERING = 3;

    private int currentAccount = UserConfig.selectedAccount;

    private PhotoViewer photoViewer;

    private WebView webView;
    private View progressBarBlackBackground;
    private RadialProgressView progressBar;
    private View pipItem;

    private String youtubeStoryboardsSpecUrl;
    private List<String> youtubeStoryboards = new ArrayList<>();
    private String currentYoutubeId;
    private boolean isYouTube;
    private TLRPC.WebPage currentWebpage;

    private float playbackSpeed;
    private boolean setPlaybackSpeed;

    private boolean isPlaying;
    private int videoDuration;
    private int currentPosition;
    private float bufferedPosition;

    private boolean isTouchDisabled;

    private Runnable progressRunnable = () -> {
        if (isYouTube) {
            runJsCode("pollPosition();");
        }

        if (isPlaying) {
            AndroidUtilities.runOnUIThread(this.progressRunnable, 500);
        }
    };

    private class YoutubeProxy {
        @JavascriptInterface
        public void onPlayerLoaded() {
            AndroidUtilities.runOnUIThread(() -> {
                progressBar.setVisibility(View.INVISIBLE);
                if (setPlaybackSpeed) {
                    setPlaybackSpeed = false;
                    setPlaybackSpeed(playbackSpeed);
                }
                pipItem.setEnabled(true);
                pipItem.setAlpha(1.0f);

                if (photoViewer != null) {
                    photoViewer.checkFullscreenButton();
                }
            });
        }

        @JavascriptInterface
        public void onPlayerStateChange(String state) {
            int stateInt = Integer.parseInt(state);
            boolean wasPlaying = isPlaying;
            isPlaying = stateInt == YT_PLAYING || stateInt == YT_BUFFERING;
            checkPlayingPoll(wasPlaying);
            int exoState;
            boolean playWhenReady;
            switch (stateInt) {
                default:
                case YT_NOT_STARTED:
                    exoState = ExoPlayer.STATE_IDLE;
                    playWhenReady = false;
                    break;
                case YT_PLAYING:
                    exoState = ExoPlayer.STATE_READY;
                    playWhenReady = true;
                    break;
                case YT_PAUSED:
                    exoState = ExoPlayer.STATE_READY;
                    playWhenReady = false;
                    break;
                case YT_BUFFERING:
                    exoState = ExoPlayer.STATE_BUFFERING;
                    playWhenReady = true;
                    break;
                case YT_COMPLETED:
                    exoState = ExoPlayer.STATE_ENDED;
                    playWhenReady = false;
                    break;
            }
            if (exoState == ExoPlayer.STATE_READY) {
                if (progressBarBlackBackground.getVisibility() != INVISIBLE) {
                    AndroidUtilities.runOnUIThread(()-> progressBarBlackBackground.setVisibility(View.INVISIBLE), 300);
                }
            }
            AndroidUtilities.runOnUIThread(()-> photoViewer.updateWebPlayerState(playWhenReady, exoState));
        }

        @JavascriptInterface
        public void onPlayerNotifyDuration(int duration) {
            videoDuration = duration * 1000;

            if (youtubeStoryboardsSpecUrl != null) {
                processYoutubeStoryboards(youtubeStoryboardsSpecUrl);
                youtubeStoryboardsSpecUrl = null;
            }
        }

        @JavascriptInterface
        public void onPlayerNotifyCurrentPosition(int position) {
            currentPosition = position * 1000;
        }

        @JavascriptInterface
        public void onPlayerNotifyBufferedPosition(float position) {
            bufferedPosition = position;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public PhotoViewerWebView(PhotoViewer photoViewer, Context context, View pip) {
        super(context);

        this.photoViewer = photoViewer;

        pipItem = pip;
        webView = new WebView(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                processTouch(event);
                return super.onTouchEvent(event);
            }

            @Override
            public void draw(Canvas canvas) {
                super.draw(canvas);
                if (PipVideoOverlay.getInnerView() == this && progressBarBlackBackground.getVisibility() == VISIBLE) {
                    canvas.drawColor(Color.BLACK);
                    drawBlackBackground(canvas, getWidth(), getHeight());
                }
            }
        };
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 17) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isYouTube || Build.VERSION.SDK_INT < 17) {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBarBlackBackground.setVisibility(View.INVISIBLE);
                    pipItem.setEnabled(true);
                    pipItem.setAlpha(1.0f);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!VideoSeekPreviewImage.IS_YOUTUBE_PREVIEWS_SUPPORTED) {
                    return null;
                }
                String url = request.getUrl().toString();
                if (isYouTube && url.startsWith("https://www.youtube.com/youtubei/v1/player?key=")) {
                    Utilities.externalNetworkQueue.postRunnable(()->{
                        try {
                            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                            con.setRequestMethod("POST");
                            for (Map.Entry<String, String> en : request.getRequestHeaders().entrySet()) {
                                con.addRequestProperty(en.getKey(), en.getValue());
                            }
                            con.setDoOutput(true);
                            OutputStream out = con.getOutputStream();
                            out.write(new JSONObject()
                                            .put("context", new JSONObject()
                                                    .put("client", new JSONObject()
                                                            .put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36,gzip(gfe)")
                                                            .put("clientName", "WEB")
                                                            .put("clientVersion", request.getRequestHeaders().get("X-Youtube-Client-Version"))
                                                            .put("osName", "Windows")
                                                            .put("osVersion", "10.0")
                                                            .put("originalUrl", "https://www.youtube.com/watch?v=" + currentYoutubeId)
                                                            .put("platform", "DESKTOP")))
                                            .put("videoId", currentYoutubeId)
                                    .toString().getBytes("UTF-8"));
                            out.close();

                            InputStream in = con.getResponseCode() == 200 ? con.getInputStream() : con.getErrorStream();
                            byte[] buffer = new byte[10240]; int c;
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            while ((c = in.read(buffer)) != -1) {
                                bos.write(buffer, 0, c);
                            }
                            bos.close();
                            in.close();

                            String str = bos.toString("UTF-8");
                            JSONObject obj = new JSONObject(str);
                            JSONObject storyboards = obj.optJSONObject("storyboards");
                            if (storyboards != null) {
                                JSONObject renderer = storyboards.optJSONObject("playerStoryboardSpecRenderer");
                                if (renderer != null) {
                                    String spec = renderer.optString("spec");
                                    if (spec != null) {
                                        if (videoDuration == 0) {
                                            youtubeStoryboardsSpecUrl = spec;
                                        } else {
                                            processYoutubeStoryboards(spec);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });
                }
                return null;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isYouTube) {
                    Browser.openUrl(view.getContext(), url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        addView(webView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        progressBarBlackBackground = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                drawBlackBackground(canvas, getMeasuredWidth(), getMeasuredHeight());
            }
        };
        progressBarBlackBackground.setBackgroundColor(0xff000000);
        progressBarBlackBackground.setVisibility(View.INVISIBLE);
        addView(progressBarBlackBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        progressBar = new RadialProgressView(context);
        progressBar.setVisibility(View.INVISIBLE);
        addView(progressBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
    }

    public boolean hasYoutubeStoryboards() {
        return !youtubeStoryboards.isEmpty();
    }

    private void processYoutubeStoryboards(String url) {
        int duration = getVideoDuration() / 1000;

        youtubeStoryboards.clear();

        String[] specParts = url.split("\\|");
        String baseUrl = specParts[0].split("\\$")[0] + "2/";
        String sgpPart = specParts[0].split("\\$N")[1];

        String sighPart;
        if (specParts.length == 3) {
            sighPart = specParts[2].split("M#")[1];
        } else if (specParts.length == 2) {
            sighPart = specParts[1].split("t#")[1];
        } else {
            sighPart = specParts[3].split("M#")[1];
        }

        int boardsCount = 0;

        if (duration < 250) {
            boardsCount = (int) Math.ceil((duration / 2f) / 25);
        } else if (duration >= 250 && duration < 1000) {
            boardsCount = (int) Math.ceil((duration / 4f) / 25);
        } else if (duration >= 1000) {
            boardsCount = (int) Math.ceil((duration / 10f) / 25);
        }

        for (int i = 0; i < boardsCount; i++) {
            youtubeStoryboards.add(String.format(Locale.ROOT, "%sM%d%s&sigh=%s", baseUrl, i, sgpPart, sighPart));
        }
    }

    public int getYoutubeStoryboardImageCount(int position) {
        int index = youtubeStoryboards.indexOf(getYoutubeStoryboard(position));
        if (index != -1) {
            if (index == youtubeStoryboards.size() - 1) {
                int duration = getVideoDuration() / 1000;
                int totalImages = 0;
                if (duration < 250) {
                    totalImages = (int) Math.ceil(duration / 2f);
                } else if (duration >= 250 && duration < 1000) {
                    totalImages = (int) Math.ceil(duration / 4f);
                } else if (duration >= 1000) {
                    totalImages = (int) Math.ceil(duration / 10f);
                }
                return totalImages - (youtubeStoryboards.size() - 1) * 25;
            }
            return 25;
        }
        return 0;
    }

    public String getYoutubeStoryboard(int position) {
        int duration = getVideoDuration() / 1000;

        int i = -1;
        if (duration < 250) {
            i = (int) (position / 2f) / 25;
        } else if (duration >= 250 && duration < 1000) {
            i = (int) (position / 4f) / 25;
        } else if (duration >= 1000) {
            i = (int) ((position / 10f) / 25);
        }
        return i != -1 && i < youtubeStoryboards.size() ? youtubeStoryboards.get(i) : null;
    }

    public int getYoutubeStoryboardImageIndex(int position) {
        int duration = getVideoDuration() / 1000;

        int i = -1;
        if (duration < 250) {
            i = (int) Math.ceil(position / 2f) % 25;
        } else if (duration >= 250 && duration < 1000) {
            i = (int) Math.ceil(position / 4f) % 25;
        } else if (duration >= 1000) {
            i = (int) Math.ceil(position / 10f) % 25;
        }
        return i;
    }

    public void setTouchDisabled(boolean touchDisabled) {
        isTouchDisabled = touchDisabled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isTouchDisabled) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public WebView getWebView() {
        return webView;
    }

    private void checkPlayingPoll(boolean wasPlaying) {
        if (!wasPlaying && isPlaying) {
            AndroidUtilities.runOnUIThread(progressRunnable, 500);
        } else if (wasPlaying && !isPlaying) {
            AndroidUtilities.cancelRunOnUIThread(progressRunnable);
        }
    }

    public void seekTo(long seekTo) {
        seekTo(seekTo, true);
    }

    public void seekTo(long seekTo, boolean seekAhead) {
        boolean playing = isPlaying;
        currentPosition = (int) seekTo;
        if (playing) {
            pauseVideo();
        }
        if (playing) {
            AndroidUtilities.runOnUIThread(() -> {
                runJsCode("seekTo(" + Math.round(seekTo / 1000f) + ", " + seekAhead + ");");

                AndroidUtilities.runOnUIThread(this::playVideo, 100);
            }, 100);
        } else {
            runJsCode("seekTo(" + Math.round(seekTo / 1000f) + ", " + seekAhead + ");");
        }
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public float getBufferedPosition() {
        return bufferedPosition;
    }

    private void runJsCode(String code) {
        if (Build.VERSION.SDK_INT >= 21) {
            webView.evaluateJavascript(code, null);
        } else {
            try {
                webView.loadUrl("javascript:" + code);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    protected void processTouch(MotionEvent event) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (webView.getParent() == this) {
            int w = currentWebpage.embed_width != 0 ? currentWebpage.embed_width : 100;
            int h = currentWebpage.embed_height != 0 ? currentWebpage.embed_height : 100;
            int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            int viewHeight = MeasureSpec.getSize(heightMeasureSpec);
            float minScale = Math.min(viewWidth / (float) w, viewHeight / (float) h);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) webView.getLayoutParams();
            layoutParams.width = (int) (w * minScale);
            layoutParams.height = (int) (h * minScale);
            layoutParams.topMargin = (viewHeight - layoutParams.height) / 2;
            layoutParams.leftMargin = (viewWidth - layoutParams.width) / 2;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void drawBlackBackground(Canvas canvas, int w, int h) {

    }

    public boolean isLoaded() {
        return progressBar.getVisibility() != View.VISIBLE;
    }

    public boolean isInAppOnly() {
        return isYouTube && "inapp".equals(MessagesController.getInstance(currentAccount).youtubePipType);
    }

    public boolean openInPip() {
        boolean inAppOnly = isInAppOnly();
        if (!inAppOnly && !checkInlinePermissions()) {
            return false;
        }
        if (progressBar.getVisibility() == View.VISIBLE) {
            return false;
        }
        if (PipVideoOverlay.isVisible()) {
            PipVideoOverlay.dismiss();
            AndroidUtilities.runOnUIThread(this::openInPip, 300);
            return true;
        }

        progressBarBlackBackground.setVisibility(VISIBLE);
        if (PipVideoOverlay.show(inAppOnly, (Activity) getContext(), this, webView, currentWebpage.embed_width, currentWebpage.embed_height, false)) {
            PipVideoOverlay.setPhotoViewer(PhotoViewer.getInstance());
        }
        return true;
    }

    public boolean isYouTube() {
        return isYouTube;
    }

    public boolean isControllable() {
        return isYouTube();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void showControls() {
        // TODO: Show controls after leaving PIP
    }

    public void hideControls() {
        // TODO: Hide controls in PIP
    }

    public void playVideo() {
        if (isPlaying || !isControllable()) {
            return;
        }

        runJsCode("playVideo();");
        isPlaying = true;

        checkPlayingPoll(false);
    }

    public void pauseVideo() {
        if (!isPlaying || !isControllable()) {
            return;
        }

        runJsCode("pauseVideo();");
        isPlaying = false;

        checkPlayingPoll(true);
    }

    public void setPlaybackSpeed(float speed) {
        playbackSpeed = speed;
        if (progressBar.getVisibility() != View.VISIBLE) {
            if (isYouTube) {
                runJsCode("setPlaybackSpeed(" + speed + ");");
            }
        } else {
            setPlaybackSpeed = true;
        }
    }

    @SuppressLint("AddJavascriptInterface")
    public void init(int seekTime, TLRPC.WebPage webPage) {
        currentWebpage = webPage;
        currentYoutubeId = WebPlayerView.getYouTubeVideoId(webPage.embed_url);
        String originalUrl = webPage.url;
        requestLayout();

        try {
            if (currentYoutubeId != null) {
                progressBarBlackBackground.setVisibility(View.VISIBLE);
                isYouTube = true;
                if (Build.VERSION.SDK_INT >= 17) {
                    webView.addJavascriptInterface(new YoutubeProxy(), "YoutubeProxy");
                }
                int seekToTime = 0;
                if (originalUrl != null) {
                    try {
                        Uri uri = Uri.parse(originalUrl);
                        String t = seekTime > 0 ? "" + seekTime : null;
                        if (t == null) {
                            t = uri.getQueryParameter("t");
                            if (t == null) {
                                t = uri.getQueryParameter("time_continue");
                            }
                        }
                        if (t != null) {
                            if (t.contains("m")) {
                                String[] arg = t.split("m");
                                seekToTime = Utilities.parseInt(arg[0]) * 60 + Utilities.parseInt(arg[1]);
                            } else {
                                seekToTime = Utilities.parseInt(t);
                            }
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                InputStream in = getContext().getAssets().open("youtube_embed.html");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[10240];
                int c;
                while ((c = in.read(buffer)) != -1) {
                    bos.write(buffer, 0, c);
                }
                bos.close();
                in.close();
                webView.loadDataWithBaseURL("https://messenger.telegram.org/", String.format(Locale.US, bos.toString("UTF-8"), currentYoutubeId, seekToTime), "text/html", "UTF-8", "https://youtube.com");
            } else {
                HashMap<String, String> args = new HashMap<>();
                args.put("Referer", "messenger.telegram.org");
                webView.loadUrl(webPage.embed_url, args);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }

        pipItem.setEnabled(false);
        pipItem.setAlpha(0.5f);

        progressBar.setVisibility(View.VISIBLE);
        if (currentYoutubeId != null) {
            progressBarBlackBackground.setVisibility(View.VISIBLE);
        }
        webView.setVisibility(View.VISIBLE);
        webView.setKeepScreenOn(true);
        if (currentYoutubeId != null && "disabled".equals(MessagesController.getInstance(currentAccount).youtubePipType)) {
            pipItem.setVisibility(View.GONE);
        }
    }

    public boolean checkInlinePermissions() {
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(getContext())) {
            return true;
        } else {
            AlertsCreator.createDrawOverlayPermissionDialog((Activity) getContext(), null);
        }
        return false;
    }

    public void exitFromPip() {
        if (webView == null) {
            return;
        }
        if (ApplicationLoader.mainInterfacePaused) {
            try {
                getContext().startService(new Intent(ApplicationLoader.applicationContext, BringAppForegroundService.class));
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        progressBarBlackBackground.setVisibility(VISIBLE);
        ViewGroup parent = (ViewGroup) webView.getParent();
        if (parent != null) {
            parent.removeView(webView);
        }
        addView(webView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        PipVideoOverlay.dismiss();
    }

    public void release() {
        webView.stopLoading();
        webView.loadUrl("about:blank");
        webView.destroy();
        AndroidUtilities.cancelRunOnUIThread(progressRunnable);
    }
}
