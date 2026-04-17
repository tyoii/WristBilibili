package cn.luern0313.wristbilibili.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import cn.luern0313.wristbilibili.R;

/**
 * 标题栏（最小可编译实现）
 */
public class TitleView extends FrameLayout
{
    public static final int MODE_BACK = 0;
    public static final int MODE_MENU = 1;
    public static final int MODE_VIEWPAGER = 2;

    private View root;
    private ViewFlipper titleFlipper;
    private ImageView leftIcon;
    private ImageView rightIcon;

    public TitleView(Context context)
    {
        this(context, null);
    }

    public TitleView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs)
    {
        root = LayoutInflater.from(context).inflate(R.layout.widget_title, this, true);
        titleFlipper = root.findViewById(R.id.title_title);
        leftIcon = root.findViewById(R.id.title_extraicon);
        rightIcon = root.findViewById(R.id.title_extraicon2);

        if(attrs != null)
        {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleView);
            String title = typedArray.getString(R.styleable.TitleView_title);
            int mode = typedArray.getInt(R.styleable.TitleView_mode, MODE_MENU);
            typedArray.recycle();

            if(title != null) setTitle(title);
            setMode(mode);
        }
        else
            setMode(MODE_MENU);
    }

    public void setMode(int mode)
    {
        switch (mode)
        {
            case MODE_BACK:
                leftIcon.setVisibility(VISIBLE);
                rightIcon.setVisibility(GONE);
                break;
            case MODE_VIEWPAGER:
                leftIcon.setVisibility(VISIBLE);
                rightIcon.setVisibility(VISIBLE);
                break;
            case MODE_MENU:
            default:
                leftIcon.setVisibility(GONE);
                rightIcon.setVisibility(GONE);
                break;
        }
    }

    public void addTitle(String title)
    {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new ViewFlipper.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setText(title == null ? "" : title);
        titleFlipper.addView(textView);
    }

    public void setTitle(String title)
    {
        if(titleFlipper.getChildCount() == 0)
            addTitle(title);
        else
            setTitle(0, title);
    }

    public void setTitle(int index, String title)
    {
        if(index < 0) return;
        while(titleFlipper.getChildCount() <= index)
            addTitle("");

        View child = titleFlipper.getChildAt(index);
        if(child instanceof TextView)
            ((TextView) child).setText(title == null ? "" : title);
    }

    public boolean hide()
    {
        if(getVisibility() != VISIBLE) return false;
        setVisibility(GONE);
        return true;
    }

    public boolean show()
    {
        if(getVisibility() == VISIBLE) return false;
        setVisibility(VISIBLE);
        return true;
    }

    public int getDisplayedChild()
    {
        return titleFlipper.getDisplayedChild();
    }

    public boolean hasNext()
    {
        return titleFlipper.getChildCount() > 0 && titleFlipper.getDisplayedChild() < titleFlipper.getChildCount() - 1;
    }

    public boolean hasPrevious()
    {
        return titleFlipper.getChildCount() > 0 && titleFlipper.getDisplayedChild() > 0;
    }

    public void showNext()
    {
        titleFlipper.showNext();
    }

    public void showPrevious()
    {
        titleFlipper.showPrevious();
    }

    public void setInAnimation(Context context, int animResId)
    {
        Animation animation = AnimationUtils.loadAnimation(context, animResId);
        titleFlipper.setInAnimation(animation);
    }

    public void setOutAnimation(Context context, int animResId)
    {
        Animation animation = AnimationUtils.loadAnimation(context, animResId);
        titleFlipper.setOutAnimation(animation);
    }

    public interface TitleViewListener
    {
        boolean hideTitle();
        boolean showTitle();
    }
}
