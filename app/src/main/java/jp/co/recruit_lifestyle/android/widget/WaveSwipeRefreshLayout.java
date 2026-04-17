package jp.co.recruit_lifestyle.android.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * 兼容旧版 WaveSwipeRefreshLayout 的轻量替代实现。
 *
 * 说明：
 * - 原库仓库依赖已不可用，导致 CI 无法解析。
 * - 当前项目主要使用 setWaveColor / setColorSchemeColors / setOnRefreshListener / setRefreshing。
 * - 这些能力可由 SwipeRefreshLayout 覆盖实现。
 */
public class WaveSwipeRefreshLayout extends SwipeRefreshLayout {

    public WaveSwipeRefreshLayout(Context context) {
        super(context);
    }

    public WaveSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWaveColor(@ColorInt int color) {
        // 近似映射：用进度圈颜色代表“波浪”主色
        setColorSchemeColors(color);
    }
}
