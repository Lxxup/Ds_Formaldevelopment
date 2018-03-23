package com.ds.tire.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ds.tire.R;
import com.ds.tire.dao.Order;

import java.util.List;



/**
 * Created by 王寒星 on 2018/3/17.
 */

public class OrderAdapter extends BaseAdapter {

    private Context mContext;
    private int resourceId;
    private List<Order> mObject;
    public OrderAdapter(Context context,int textViewResourceId,List<Order> objects){
        mContext=context;
        resourceId = textViewResourceId;
        mObject=objects;
    }

    class ViewHolder{
        TextView orderId;
        TextView orderStatus;
        TextView orderTime;
    }

    @Override
    public int getCount() {
        return mObject.size();
    }

    @Override
    public Object getItem(int i) {
        return mObject.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Order order = mObject.get(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view = LayoutInflater.from(mContext).inflate(resourceId,parent,false);
            viewHolder =new ViewHolder();
            viewHolder.orderId=(TextView)view.findViewById(R.id.tv_id);
            viewHolder.orderStatus=(TextView)view.findViewById(R.id.tv_steps);
            viewHolder.orderTime=(TextView)view.findViewById(R.id.tv_time);
            view.setTag(viewHolder);
        }
        else{
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.orderId.setText(order.getHis_id());
        viewHolder.orderStatus.setText(order.getStatus()+"");
        viewHolder.orderTime.setText(order.getTime());
        return view;
    }
}
