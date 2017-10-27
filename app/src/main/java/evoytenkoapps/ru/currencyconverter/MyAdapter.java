package evoytenkoapps.ru.currencyconverter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import data.Valute;

/**
 * Описывает адаптер для спиннеров чтобы можно было подсвечивать выбранный элемент
 */

public class MyAdapter extends ArrayAdapter
{
    private final String TAG = this.getClass().getSimpleName();
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<Valute> mList;
    private final int mResource;
    private int choosenPosition = -1;

    public MyAdapter(Context context, int resource, List<Valute> data)
    {
        super(context, resource, data);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        mList = data;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        return createItemView(position, convertView, parent);
    }

    @Override
    public
    @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent)
    {
        final View view = mInflater.inflate(mResource, parent, false);

        TextView tv = (TextView) view.findViewById(R.id.tv_spinner);
        tv.setText(mList.get(position).toString());

        ImageView iv = (ImageView) view.findViewById(R.id.iv_checked);

        if (position == choosenPosition)
        {
            iv.setVisibility(View.VISIBLE);
        }
        return view;
    }

    // Чтобы задать выбранный курс в spinner
    void setPos(int position)
    {
        choosenPosition = position;
    }
}