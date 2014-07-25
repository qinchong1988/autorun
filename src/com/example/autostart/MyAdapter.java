package com.example.autostart;

import java.util.ArrayList;  
import java.util.HashMap;  
  
import android.content.Context;  
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.CheckBox;  
import android.widget.TextView;  
import android.widget.ImageView;
  
public class MyAdapter extends BaseAdapter{  
    // ������ݵ�list  
    private ArrayList<HashMap<String, Object>> list;  
    // ��������CheckBox��ѡ��״��  
    private static HashMap<Integer,Boolean> isSelected;  
    // ������  
    private Context context;  
    // �������벼��  
    private LayoutInflater inflater = null;  
      
    // ������  
    public MyAdapter(ArrayList<HashMap<String, Object>> list, Context context) {  
        this.context = context;  
        this.list = list;  
        inflater = LayoutInflater.from(context);  
        isSelected = new HashMap<Integer, Boolean>();  
        // ��ʼ������  
        initDate();  
    }  
    
    public void refresh(ArrayList<HashMap<String, Object>> list){
    	this.list = list;
    	initDate();
    	notifyDataSetChanged();
    }
  
    // ��ʼ��isSelected������  
    private void initDate(){
        for(int i=0; i<list.size();i++) {  
            getIsSelected().put(i,false);  
        }  
    }  
  
    @Override  
    public int getCount() {  
        return list.size();  
    }  
  
    @Override  
    public Object getItem(int position) {  
        return list.get(position);  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        ViewHolder holder = null;  
        if (convertView == null) {  
            // ���ViewHolder����  
            holder = new ViewHolder();  
                // ���벼�ֲ���ֵ��convertview  
            convertView = inflater.inflate(R.layout.list, null); 
            holder.img = (ImageView) convertView.findViewById(R.id.img);
            holder.tv = (TextView) convertView.findViewById(R.id.tv);  
            holder.receivers = (TextView) convertView.findViewById(R.id.receivers);  
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb);  
            // Ϊview���ñ�ǩ  
            convertView.setTag(holder);  
        } else {  
            // ȡ��holder  
            holder = (ViewHolder) convertView.getTag();  
            }  
        holder.img.setImageDrawable((Drawable) list.get(position).get("icon"));
        // ����list��TextView����ʾ  
        holder.tv.setText(list.get(position).get("appName").toString());  
        holder.receivers.setText(list.get(position).get("packageReceiver").toString());  
        // ����isSelected������checkbox��ѡ��״��  
        holder.cb.setChecked(getIsSelected().get(position));  
        return convertView;  
    }  
  
    public static HashMap<Integer,Boolean> getIsSelected() {  
        return isSelected;  
    }  
  
    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {  
    	MyAdapter.isSelected = isSelected;  
    }  

}  