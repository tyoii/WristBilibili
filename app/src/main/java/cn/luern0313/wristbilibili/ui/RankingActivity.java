package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.RankingFragment;
import cn.luern0313.wristbilibili.widget.TitleView;

public class RankingActivity extends BaseActivity implements TitleView.TitleViewListener
{
    private Context ctx;
    private TitleView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular);
        ctx = this;

        titleView = findViewById(R.id.popular_title);
        titleView.setTitle(getString(R.string.popular_ranking_title));

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.popular_frame, RankingFragment.newInstance("0"));
        transaction.commit();
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }
}
