package org.WIFIScanner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import java.util.ArrayList;

public class ListviewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<Listviewitem> data;
    private int layout;
    private Context context;
    public ListviewAdapter(Context context, int layout, ArrayList<Listviewitem> data){
        this.inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data=data;
        this.layout=layout;
        this.context=context;
    }
    @Override
    public int getCount(){return data.size();}
    @Override
    public String getItem(int position){return data.get(position).getName();}
    @Override
    public long getItemId(int position){return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView=inflater.inflate(layout,parent,false);
        }
        final int pos = position ;


        Listviewitem listviewitem=data.get(position);
//        ImageView icon=(ImageView)convertView.findViewById(R.id.imageview);
//        icon.setImageResource(listviewitem.getIcon());
        TextView name=(TextView)convertView.findViewById(R.id.textview);
        name.setText(listviewitem.getName());

        final String iotName = listviewitem.getName();

        // button1 클릭 시 TextView(textView1)의 내용 변경.
        Button button1 = (Button) convertView.findViewById(R.id.buttonlist);
        button1.setFocusable(false);
        button1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                printToast( iotName+ " 을 찾습니다.");
                Intent intent = new Intent(
                        v.getContext(), // 현재 화면의 제어권자
                        IOTMap.class); // 다음 넘어갈 클래스 지정
                intent.putExtra("찾기", iotName);
                v.getContext().startActivity(intent); // 다음 화면으로 넘어간다
            }
        });
//        Button button2 = (Button) convertView.findViewById(R.id.buttonlist2);
//        button2.setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//                printToast( iotName+ " 을 찾습니다.");
//            }
//        });
        return convertView;
    }

    public void printToast(String messageToast) {
        Toast.makeText( context, messageToast, Toast.LENGTH_LONG).show();
    }
}