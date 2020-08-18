package com.bmzy.gpsinfo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bmzy.gpsinfo.R;
import com.bmzy.gpsinfo.bean.UserInfo;

import java.util.List;

public class SpinnerArrayAdapter extends BaseAdapter implements SpinnerAdapter {
    private List<UserInfo> mList;
    private Context mContext;//

    public SpinnerArrayAdapter(Context mContext, List<UserInfo> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * 返回选中项： spinner_base_item选中项样式
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.spinner_base_item, null);
            holder.tv = (TextView) view.findViewById(R.id.spinner_base_item_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.tv.setText(mList.get(i).getName() + " " + mList.get(i).getId());
        return view;
    }

    /**
     * 返回下拉项：spinner_base_dropdown_item下拉列表样式
     */
    @Override
    public android.view.View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.spinner_base_item, null);
            holder.tv = (TextView) convertView.findViewById(R.id.spinner_base_item_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText(mList.get(position).getName() + " " + mList.get(position).getId());
        return convertView;
    }

    public class ViewHolder {
        public TextView tv;
    }
}