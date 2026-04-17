package cn.luern0313.wristbilibili.util;

import android.widget.AbsListView;

/**
 * 列表触底加载监听（最小可编译实现）
 */
public class ViewScrollListener implements AbsListView.OnScrollListener
{
    private final CustomScrollResult customScrollResult;

    public ViewScrollListener(CustomScrollResult customScrollResult)
    {
        this.customScrollResult = customScrollResult;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if(customScrollResult == null) return;
        if(scrollState != SCROLL_STATE_IDLE) return;
        if(!customScrollResult.rule()) return;

        int count = view.getCount();
        if(count == 0) return;
        int lastVisible = view.getLastVisiblePosition();
        if(lastVisible >= count - 1)
            customScrollResult.result();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        // no-op
    }

    public interface CustomScrollResult
    {
        boolean rule();
        void result();
    }
}
