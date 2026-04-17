package cn.luern0313.wristbilibili.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;

public class SelectPartAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;
    private final String[] options;
    private final SelectPartListener listener;

    public SelectPartAdapter(LayoutInflater inflater, String[] options, SelectPartListener listener)
    {
        this.inflater = inflater;
        this.options = options == null ? new String[0] : options;
        this.listener = listener;
    }

    @Override
    public int getCount()
    {
        return options.length;
    }

    @Override
    public Object getItem(int position)
    {
        return options[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(view == null)
            view = inflater.inflate(R.layout.item_select_part, parent, false);

        TextView textView = view.findViewById(R.id.sp_item_text);
        textView.setText(options[position]);
        textView.setOnClickListener(v -> {
            if(listener != null) listener.onClick(v.getId(), position);
        });
        return view;
    }

    public interface SelectPartListener
    {
        void onClick(int id, int position);
    }
}
