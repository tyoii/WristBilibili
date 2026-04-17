package cn.luern0313.wristbilibili.util;

import android.view.View;

import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * 兼容旧代码：历史上单独的 ListViewTouchListener
 */
public class ListViewTouchListener extends ViewTouchListener
{
    public ListViewTouchListener(View view, TitleView.TitleViewListener titleViewListener)
    {
        super(view, titleViewListener);
    }

    public ListViewTouchListener(View view, TitleView.TitleViewListener titleViewListener, CustomViewListener customViewListener)
    {
        super(view, titleViewListener, customViewListener);
    }
}
