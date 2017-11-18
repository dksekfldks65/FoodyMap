package fudi.fudimap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class StoryAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<Story> al;
    LayoutInflater inf;
    public StoryAdapter(Context context, int layout, ArrayList<Story> al) {
        this.context = context;
        this.layout = layout;
        this.al = al;
        this.inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() { // 총 데이터의 개수
        return al.size();
    }
    @Override
    public Object getItem(int position) { // 해당 행의 데이터
        return al.get(position);
    }
    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inf.inflate(layout, null);




        TextView tv1 = (TextView) convertView.findViewById(R.id.textView1);
        TextView tv2 = (TextView) convertView.findViewById(R.id.textView2);
        ImageView iv = (ImageView) convertView.findViewById(R.id.imageView1);
        TextView tv3 = (TextView) convertView.findViewById(R.id.textView3);

        Story s = al.get(position);
        tv1.setText(s.date);
        tv2.setText(s.title);
        iv.setImageBitmap(s.img);
        tv3.setText(s.memo);
        return convertView;
    }
}

