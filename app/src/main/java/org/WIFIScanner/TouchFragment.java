package org.WIFIScanner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class TouchFragment extends Activity implements OnClickListener {

    TextView textview;
    TextView information;
    TextView information2;
    TextView information3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment);  // layout xml 과 자바파일을 연결

        Intent intent = new Intent(this.getIntent());
        String iotName = intent.getStringExtra("TouchFragment_iotName");
        String iot_information = intent.getStringExtra("TouchFragment_iotInformation");
        //set textview text

        textview = (TextView)findViewById(R.id.fragment_text);
        information = (TextView)findViewById(R.id.fragment_info);
        information2 = (TextView)findViewById(R.id.fragment_info2);
        information3 = (TextView)findViewById(R.id.fragment_info3);

        textview.setText(iotName + " information");
        information.setText(iot_information);

        switch (iotName){
            case "iot1":
                ImageView m = (ImageView) findViewById(R.id.fragment_imageView);
                m.setImageDrawable(getResources().getDrawable(R.drawable.iot1_fridger));
                m.getLayoutParams().height = 800;
                m.getLayoutParams().width = 600;

                information2.setText("냉장고");
                information3.setText("온도 = 0");
                break;
            case "iot2":
                information2.setText("청소기");
                break;
            case "iot3":
                information2.setText("티비");
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
    }
}
