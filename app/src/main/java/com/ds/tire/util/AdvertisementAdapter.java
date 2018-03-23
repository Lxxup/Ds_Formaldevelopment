package com.ds.tire.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ds.tire.R;
import com.ds.tire.dao.Advertisement;
import com.ds.tire.dao.Order;

import java.util.List;


/**
 * Created by developer on 2018/3/17.
 */

public class AdvertisementAdapter extends BaseAdapter {
    private Context mContext;
    private int resourceId;
    private List<Advertisement> mObject;
    private List<Integer> mImageResource;
    public AdvertisementAdapter(Context context,int textViewResourceId,List<Advertisement> objects,List<Integer> imageresource){
        mContext=context;
        resourceId = textViewResourceId;
        mObject=objects;
        mImageResource=imageresource;
    }

    class ViewHolder{
        TextView advertisement;
        ImageView imageId;
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
        Advertisement advertisement = mObject.get(position);
        View view;
        AdvertisementAdapter.ViewHolder viewHolder;
        if(convertView==null){
            view = LayoutInflater.from(mContext).inflate(resourceId,parent,false);
            viewHolder =new AdvertisementAdapter.ViewHolder();
            viewHolder.advertisement=(TextView)view.findViewById(R.id.tv_adver_list);
            viewHolder.imageId=(ImageView)view.findViewById(R.id.ima_adver_list);
            view.setTag(viewHolder);
        }
        else{
            view=convertView;
            viewHolder=(AdvertisementAdapter.ViewHolder)view.getTag();
        }
        viewHolder.advertisement.setText(advertisement.getAdvertisement());
        viewHolder.imageId.setBackgroundResource(mImageResource.get(position));
        return view;
    }
}
