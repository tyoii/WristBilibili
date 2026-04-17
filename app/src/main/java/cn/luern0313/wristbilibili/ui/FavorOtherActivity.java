package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.FavorArticleFragment;
import cn.luern0313.wristbilibili.widget.TitleView;

public class FavorOtherActivity extends BaseActivity implements TitleView.TitleViewListener
{
    private Context ctx;
    private Intent intent;
    private TitleView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor_article);
        ctx = this;
        intent = getIntent();

        titleView = findViewById(R.id.favor_other_title);

        int mode = intent.getIntExtra("mode", 0);
        String title = mode == 1 ? getString(R.string.favor_article_title) : getString(R.string.favor_article_title);
        titleView.setTitle(title);

        Fragment fragment = FavorArticleFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.favor_other_frame, fragment);
        ft.commit();
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
