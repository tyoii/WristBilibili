package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;

public class PlayerActivity extends BaseActivity {

    private Context ctx;
    private Intent intent;
    private OnlineVideoApi onlineVideoApi;

    private String title;
    private String aid;
    private String cid;
    private String url;
    private String[] urlBackup;
    private String danmakuUrl;
    private int time;

    private PlayerView uiPlayerView;
    private FrameLayout uiDanmakuLayer;
    private View uiLoading;
    private TextView uiLoadingTip;
    private TextView uiHistory;
    private SeekBar uiSeek;
    private TextView uiTimeCurrent;
    private TextView uiTimeTotal;
    private TextView uiBtnDanmaku;

    private ExoPlayer player;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Runnable runnableTimer;
    private Runnable runnableProgressUpdater;
    private Runnable runnableDanmakuTicker;

    private boolean userSeeking = false;
    private boolean danmakuEnabled = true;
    private boolean prepared = false;

    private List<String> playUrls = new ArrayList<>();
    private int currentUrlIndex = 0;

    private final List<DanmakuItem> danmakuItems = new ArrayList<>();
    private int danmakuIndex = 0;

    private static class DanmakuItem {
        long timeMs;
        String text;

        DanmakuItem(long timeMs, String text) {
            this.timeMs = timeMs;
            this.text = text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        ctx = this;
        intent = getIntent();

        title = intent.getStringExtra("title");
        aid = intent.getStringExtra("aid");
        cid = intent.getStringExtra("cid");
        time = intent.getIntExtra("time", 0);

        onlineVideoApi = new OnlineVideoApi(aid, cid);

        initViews();
        initRunnables();
        bindEvents();

        fetchPlayInfo();
    }

    private void initViews() {
        uiPlayerView = findViewById(R.id.player_view);
        uiDanmakuLayer = findViewById(R.id.player_danmaku_layer);
        uiLoading = findViewById(R.id.player_loading);
        uiLoadingTip = findViewById(R.id.player_loadingtip);
        uiHistory = findViewById(R.id.player_history);
        uiSeek = findViewById(R.id.player_seek);
        uiTimeCurrent = findViewById(R.id.player_time_current);
        uiTimeTotal = findViewById(R.id.player_time_total);
        uiBtnDanmaku = findViewById(R.id.player_btn_danmaku);

        uiTimeCurrent.setText(formatTime(time * 1000L));
        uiTimeTotal.setText("00:00");
    }

    private void initRunnables() {
        runnableTimer = () -> {
            if (prepared) {
                new Thread(() -> {
                    try {
                        int playTime = (int) Math.max(0, player != null ? player.getCurrentPosition() / 1000 : time);
                        onlineVideoApi.playHistory(playTime, false);
                    } catch (Exception ignored) {
                    }
                }).start();
            }
            mainHandler.postDelayed(runnableTimer, 10000);
        };

        runnableProgressUpdater = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    long pos = Math.max(0, player.getCurrentPosition());
                    long dur = player.getDuration() > 0 ? player.getDuration() : 0;

                    if (!userSeeking && dur > 0) {
                        int progress = (int) Math.min(1000, (pos * 1000L / dur));
                        uiSeek.setProgress(progress);
                    }

                    uiTimeCurrent.setText(formatTime(pos));
                    uiTimeTotal.setText(formatTime(dur));
                }
                mainHandler.postDelayed(this, 300);
            }
        };

        runnableDanmakuTicker = new Runnable() {
            @Override
            public void run() {
                if (player != null && prepared && danmakuEnabled && !danmakuItems.isEmpty()) {
                    long now = player.getCurrentPosition();
                    while (danmakuIndex < danmakuItems.size() && danmakuItems.get(danmakuIndex).timeMs <= now + 50) {
                        showDanmaku(danmakuItems.get(danmakuIndex).text);
                        danmakuIndex++;
                    }
                }
                mainHandler.postDelayed(this, 80);
            }
        };
    }

    private void bindEvents() {
        uiBtnDanmaku.setOnClickListener(v -> {
            danmakuEnabled = !danmakuEnabled;
            uiBtnDanmaku.setText(danmakuEnabled ? "弹幕:开" : "弹幕:关");
            if (!danmakuEnabled) {
                uiDanmakuLayer.removeAllViews();
            }
        });

        uiSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long target = duration * progress / 1000L;
                        uiTimeCurrent.setText(formatTime(target));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSeeking = false;
                if (player != null) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long target = duration * seekBar.getProgress() / 1000L;
                        player.seekTo(target);
                        resetDanmakuIndex(target);
                    }
                }
            }
        });
    }

    private void fetchPlayInfo() {
        uiLoading.setVisibility(View.VISIBLE);
        uiLoadingTip.setText("解析视频链接中...");

        new Thread(() -> {
            try {
                onlineVideoApi.connectionVideoUrl();
                url = onlineVideoApi.getVideoUrl();
                urlBackup = onlineVideoApi.getVideoBackupUrl();
                danmakuUrl = onlineVideoApi.getDanmakuUrl();

                buildPlayUrlList();
                parseDanmakuAsync();

                mainHandler.post(this::preparePlayer);
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    uiLoading.setVisibility(View.VISIBLE);
                    uiLoadingTip.setText("视频链接获取失败");
                });
            }
        }).start();
    }

    private void buildPlayUrlList() {
        playUrls.clear();
        if (!TextUtils.isEmpty(url)) playUrls.add(url);
        if (urlBackup != null) {
            for (String b : urlBackup) {
                if (!TextUtils.isEmpty(b) && !playUrls.contains(b)) {
                    playUrls.add(b);
                }
            }
        }
        currentUrlIndex = 0;
    }

    private void preparePlayer() {
        if (playUrls.isEmpty()) {
            uiLoading.setVisibility(View.VISIBLE);
            uiLoadingTip.setText("没有可播放的视频链接");
            return;
        }

        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000);

        HashMap<String, String> headers = OnlineVideoApi.getPlayerHeaders();
        if (headers != null && !headers.isEmpty()) {
            dataSourceFactory.setDefaultRequestProperties(headers);
        }

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();

        uiPlayerView.setPlayer(player);
        uiPlayerView.setUseController(false);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    prepared = true;
                    uiLoading.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    uiLoading.setVisibility(View.VISIBLE);
                    uiLoadingTip.setText("缓冲中...");
                } else if (playbackState == Player.STATE_ENDED) {
                    syncAndFinish(true);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                tryNextSourceOrFail();
            }
        });

        playUrlWithCurrentIndex(time * 1000L);

        mainHandler.post(runnableTimer);
        mainHandler.post(runnableProgressUpdater);
        mainHandler.post(runnableDanmakuTicker);
    }

    private void playUrlWithCurrentIndex(long seekMs) {
        if (player == null || currentUrlIndex < 0 || currentUrlIndex >= playUrls.size()) {
            return;
        }

        String targetUrl = playUrls.get(currentUrlIndex);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(targetUrl));

        player.setMediaItem(mediaItem);
        player.prepare();
        if (seekMs > 0) {
            player.seekTo(seekMs);
            resetDanmakuIndex(seekMs);
        }
        player.play();
    }

    private void tryNextSourceOrFail() {
        currentUrlIndex++;
        if (currentUrlIndex < playUrls.size()) {
            long keepPos = player != null ? Math.max(0, player.getCurrentPosition()) : 0;
            uiLoading.setVisibility(View.VISIBLE);
            uiLoadingTip.setText("主链接失效，切换备用线路...");
            playUrlWithCurrentIndex(keepPos);
        } else {
            uiLoading.setVisibility(View.VISIBLE);
            uiLoadingTip.setText("播放失败：所有线路均不可用");
            Toast.makeText(ctx, "播放失败，请稍后重试", Toast.LENGTH_LONG).show();
        }
    }

    private void parseDanmakuAsync() {
        new Thread(() -> {
            try {
                List<DanmakuItem> parsed = parseDanmakuXml(danmakuUrl);
                synchronized (danmakuItems) {
                    danmakuItems.clear();
                    danmakuItems.addAll(parsed);
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private List<DanmakuItem> parseDanmakuXml(String xmlUrl) throws Exception {
        List<DanmakuItem> list = new ArrayList<>();

        URL u = new URL(xmlUrl);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");

        InputStream inputStream = new BufferedInputStream(conn.getInputStream());

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, "UTF-8");

        int type = parser.getEventType();
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG && "d".equals(parser.getName())) {
                String p = parser.getAttributeValue(null, "p");
                String text = parser.nextText();
                if (!TextUtils.isEmpty(p) && !TextUtils.isEmpty(text)) {
                    String[] arr = p.split(",");
                    if (arr.length > 0) {
                        try {
                            float sec = Float.parseFloat(arr[0]);
                            long timeMs = (long) (sec * 1000L);
                            list.add(new DanmakuItem(timeMs, text));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            type = parser.next();
        }

        inputStream.close();
        conn.disconnect();

        return list;
    }

    private void showDanmaku(String text) {
        if (uiDanmakuLayer.getWidth() <= 0 || uiDanmakuLayer.getHeight() <= 0) {
            return;
        }

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setShadowLayer(2f, 1f, 1f, Color.BLACK);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        int maxWidth = (int) (uiDanmakuLayer.getWidth() * 0.78f);
        tv.setMaxWidth(maxWidth);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        int lines = Math.max(1, uiDanmakuLayer.getHeight() / dp(22));
        int lane = (int) (Math.random() * lines);
        lp.topMargin = lane * dp(22);
        lp.leftMargin = uiDanmakuLayer.getWidth();
        tv.setLayoutParams(lp);

        uiDanmakuLayer.addView(tv);

        float distance = uiDanmakuLayer.getWidth() + Math.max(dp(80), tv.getPaint().measureText(text));
        long duration = 4600L;

        tv.animate()
                .translationX(-distance)
                .setDuration(duration)
                .setInterpolator(new LinearInterpolator())
                .withEndAction(() -> uiDanmakuLayer.removeView(tv))
                .start();
    }

    private int dp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void resetDanmakuIndex(long currentPositionMs) {
        uiDanmakuLayer.removeAllViews();
        synchronized (danmakuItems) {
            int l = 0;
            int r = danmakuItems.size() - 1;
            int ans = danmakuItems.size();
            while (l <= r) {
                int m = (l + r) >>> 1;
                if (danmakuItems.get(m).timeMs >= currentPositionMs) {
                    ans = m;
                    r = m - 1;
                } else {
                    l = m + 1;
                }
            }
            danmakuIndex = ans;
        }
    }

    private String formatTime(long ms) {
        if (ms <= 0 || ms == C.TIME_UNSET) return "00:00";
        long totalSec = ms / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    private void syncAndFinish(boolean isFin) {
        uiHistory.setVisibility(View.VISIBLE);
        int playTime = (int) Math.max(0, player != null ? player.getCurrentPosition() / 1000 : time);
        new Thread(() -> {
            try {
                onlineVideoApi.playHistory(playTime, isFin);
            } catch (Exception ignored) {
            } finally {
                runOnUiThread(this::finish);
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            return;
        }
        // 手表上右滑退出时尽量同步一次
        if (prepared) {
            int playTime = (int) Math.max(0, player != null ? player.getCurrentPosition() / 1000 : time);
            new Thread(() -> {
                try {
                    onlineVideoApi.playHistory(playTime, false);
                } catch (Exception ignored) {
                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mainHandler.removeCallbacksAndMessages(null);
        uiDanmakuLayer.removeAllViews();

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
