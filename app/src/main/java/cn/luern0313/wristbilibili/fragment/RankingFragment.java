package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.RankingAdapter;
import cn.luern0313.wristbilibili.api.RankingApi;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 排行榜（最小可编译实现）
 */
public class RankingFragment extends Fragment
{
    private static final String ARG_RID = "ridArg";

    private Context ctx;
    private View rootLayout;
    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    private RankingApi rankingApi;
    private final ArrayList<RankingModel.RankingVideoModel> rankingVideoArrayList = new ArrayList<>();
    private RankingAdapter rankingAdapter;
    private TitleView.TitleViewListener titleViewListener;

    private final Handler handler = new Handler();
    private String rid = "0";

    public RankingFragment() {}

    public static RankingFragment newInstance(String rid)
    {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RID, rid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            rid = getArguments().getString(ARG_RID, "0");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_ranking, container, false);
        rankingApi = new RankingApi();

        exceptionHandlerView = rootLayout.findViewById(R.id.rk_exception);
        listView = rootLayout.findViewById(R.id.rk_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.rk_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(this::getRanking));

        rankingAdapter = new RankingAdapter(inflater, rankingVideoArrayList, listView, this::onViewClick);
        listView.setAdapter(rankingAdapter);
        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        listView.setVisibility(View.GONE);
        waveSwipeRefreshLayout.setRefreshing(true);
        getRanking();

        return rootLayout;
    }

    private void getRanking()
    {
        new Thread(() -> {
            try
            {
                RankingModel model = rankingApi.getRankingVideo(rid);
                handler.post(() -> {
                    waveSwipeRefreshLayout.setRefreshing(false);
                    if(model != null && model.getVideoModelArrayList() != null)
                    {
                        rankingVideoArrayList.clear();
                        rankingVideoArrayList.addAll(model.getVideoModelArrayList());
                        rankingAdapter.notifyDataSetChanged();
                        exceptionHandlerView.hideAllView();
                        listView.setVisibility(View.VISIBLE);
                    }
                    else
                        exceptionHandlerView.noData();
                });
            }
            catch (IOException e)
            {
                handler.post(() -> {
                    waveSwipeRefreshLayout.setRefreshing(false);
                    exceptionHandlerView.noWeb();
                });
            }
        }).start();
    }

    private void onViewClick(int viewId, int position)
    {
        RankingModel.RankingVideoModel model = rankingVideoArrayList.get(position);
        if(viewId == R.id.rk_video_lay)
        {
            Intent intent = new Intent(ctx, VideoActivity.class);
            intent.putExtra(VideoActivity.ARG_AID, model.getAid());
            intent.putExtra(VideoActivity.ARG_BVID, model.getBvid());
            startActivity(intent);
        }
        else if(viewId == R.id.rk_video_video_up)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", model.getMid());
            startActivity(intent);
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
