package org.WIFIScanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

import java.util.Comparator;
import java.util.Random;

class Descending implements Comparator<DataSet> {

    @Override
    public int compare(DataSet o1, DataSet o2) {
        Double arg1 = o1.rssi;
        Double arg2 = o2.rssi;
        return arg2.compareTo(arg1);
    }

}

class Ascending implements Comparator<DataSet> {

    @Override
    public int compare(DataSet o1, DataSet o2) {
        Double arg1 = o2.rssi;
        Double arg2 = o1.rssi;
        return arg2.compareTo(arg1);
    }

}

public class IOTMap extends Activity implements View.OnTouchListener{

    Button btnList;

    /////////////////////////////////////////
    Button btnMove;
    Button btnStart;
    private ImageView image;
    private Button button1, button2;
    private Animation up, down;
    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Accelometer
    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;


    private int map_hori = 1280;
    private int map_verti = 720;

    private int [][] pixel_map = new int[map_hori][map_verti];

    private boolean [][] pos_grid = new boolean[128][72];

    //private int [][] id_map = new int[map_hori][map_verti];
    //////////////////////
    //////////////////////
    /*Wizets*/
    private TextView tv_roll, tv_pitch;

    /*Used for Accelometer & Gyroscoper*/
    private SensorManager hSensorManager = null;
    private UserSensorListner userSensorListner;
    private Sensor mGyroscopeSensor = null;
    private Sensor mAccelerometer = null;
    private Sensor mOrientatioin = null;

    /*Sensor variables*/
    private float[] mGyroValues = new float[3];
    private float[] mAccValues = new float[3];
    private double mAccPitch, mAccRoll;

    /*for unsing complementary fliter*/
    private float a = 0.2f;
    private static final float NS2S = 1.0f/1000000000.0f;
    private double pitch = 0, roll = 0;
    private double timestamp;
    private double dt;
    private double temp;
    private boolean running;
    private boolean gyroRunning;
    private boolean accRunning;


    double accX = 0;
    double accY = 0;
    double accZ = 0;
    double cor_accX =0;
    double cor_accY =0;
    double cor_accZ =0;
    double ori_val = 0;
    double ori_first =0;
    boolean is_ori =false;


    double veloX =0;
    double veloY =0;
    double accG = 0;
    int calib_count = 100;
    double sumAcc =0;
    boolean is_step = false;
    int step_count =0;
    int anim_count =1;
    int anim_change = 0;

    //벽탐지 좌표 저장할 변수
    boolean Wall_at_ver = false;
    boolean Wall_at_hori = false;

    int AtWallHoriX=0, AtWallHoriY=0;
    int AtWallVertiX=0, AtWallVertiY=0;
    /////////////////////////
    //
    ArrayList<DataSet> ds = new ArrayList<DataSet>();
    ArrayList<DataSet> iot1ds = new ArrayList<DataSet>();
    ArrayList<DataSet> iot2ds = new ArrayList<DataSet>();
    ArrayList<DataSet> iot3ds = new ArrayList<DataSet>();

    ///////////////////////////
    //IoT '찾기'눌렀을 때 사용
    String find_IoT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 화면 가로 고정
        setContentView(R.layout.map_layout);  // layout xml 과 자바파일을 연결
        btnList = (Button) findViewById(R.id.btnList);
        //btnList.setOnClickListener(this);
        btnList.setOnTouchListener(this);
        //맵 설정
        ///////////////////////////////////////////////////////////////////////////////////// broadcast
        container = (LinearLayout)findViewById(R.id.map_dynamicArea);
        Intent intent = new Intent(this.getIntent());
        //찾기 버튼 눌렀을 때
        find_IoT = intent.getStringExtra("찾기");
        try {
            switch (find_IoT) {
                case "chang_room"://test case
                    ImageView m = (ImageView) findViewById(R.id.iot1);
                    m.setImageDrawable(getResources().getDrawable(R.drawable.looking));
                    break;
                case "iot1":
                    m = (ImageView) findViewById(R.id.iot1);
                    m.setImageDrawable(getResources().getDrawable(R.drawable.looking));
                    break;
                case "iot2":
                    m = (ImageView) findViewById(R.id.iot2);
                    m.setImageDrawable(getResources().getDrawable(R.drawable.looking));
                    break;
                case "iot3":
                    m = (ImageView) findViewById(R.id.iot3);
                    m.setImageDrawable(getResources().getDrawable(R.drawable.looking));
                    break;
                default:
            }
        } catch(Exception e){
            //nothing
        }
        ImageView iot1 = findViewById(R.id.iot1);
        iot1.setVisibility(View.INVISIBLE);
        ImageView iot2 = findViewById(R.id.iot2);
        iot2.setVisibility(View.INVISIBLE);
        ImageView iot3 = findViewById(R.id.iot3);
        iot3.setVisibility(View.INVISIBLE);
        //image touch listener
        ImageView touch_iot1 = (ImageView)findViewById(R.id.iot1);
        touch_iot1.setOnTouchListener(this);
        ImageView touch_iot2 = (ImageView)findViewById(R.id.iot2);
        touch_iot2.setOnTouchListener(this);
        ImageView touch_iot3 = (ImageView)findViewById(R.id.iot3);
        touch_iot3.setOnTouchListener(this);
        /////////////////////////////////////////////////////////////////////////////////////
        for(int i= 0; i< map_hori; i++){
            for(int j= 0; j< map_verti; j++){
                pixel_map[i][j] =0;
                //id_map[i][j] = 0;
            }
        }
        for(int i= 0; i< 128; i++){
            for(int j= 0; j< 72; j++){
                pos_grid[i][j]= false;
            }
        }
        //pos_grid
        //실제 벽들 벽처리
        //왼쪽방 문 왼쪽벽
        for(int i= 0; i< 210; i++){
            for(int j= 160; j< 180 ; j++){
                pixel_map[i][j] =1;
            }
        }
        //왼쪽방 문 오른쪽벽~오른쪽방 문왼쪽
        for(int i= 300; i< 530; i++){
            for(int j= 160; j< 180 ; j++){
                pixel_map[i][j] =1;
            }
        }
        //오른쪽방 문 오른쪽벽
        for(int i= 700; i< map_hori; i++){
            for(int j= 160; j< 180 ; j++){
                pixel_map[i][j] =1;
            }
        }
        //왼쪽방과 오른쪽방 사이 벽
        for(int i= 370; i< 490; i++){
            for(int j= 160; j< map_verti ; j++){
                pixel_map[i][j] =1;
            }
        }

        //위쪽벽
        for(int i= 0; i< map_hori; i++){
            for(int j= 0; j< 10 ; j++){
                pixel_map[i][j] =1;
            }
        }
        //아래벽
        for(int i= 0; i< map_hori; i++){
            for(int j= 690; j< map_verti ; j++){
                pixel_map[i][j] =1;
            }
        }
        //왼쪽벽
        for(int i= 0; i< 10; i++){
            for(int j= 0; j< map_verti ; j++){
                pixel_map[i][j] =1;
            }
        }
        //오른쪽 벽
        for(int i= 1250; i< map_hori; i++){
            for(int j= 0; j< map_verti ; j++){
                pixel_map[i][j] =1;
            }
        }
        //////////////////////////
        //각 iot 기기 위치 설정.
        //개방
        /*
        ImageView s = (ImageView) findViewById(R.id.iot1);
        s.setX((float)490.0);
        s.setY((float)860.0);
        //벽
        s = (ImageView) findViewById(R.id.iot2);
        s.setX(355.0f);
        s.setY(51330f);
        //코너
        s = (ImageView) findViewById(R.id.iot3);
        s.setX(1230.0f);
        s.setY(200.0f);

        */
        ///////////////
        image = (ImageView) findViewById(R.id.user);
        btnMove = (Button) findViewById(R.id.btnMove);

        //btnMove.setOnClickListener(this);

        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        tv_roll = (TextView)findViewById(R.id.tv_roll);
        tv_pitch = (TextView)findViewById(R.id.tv_pitch);

        //위치잡기에 사용할 가속도,자이로,오리엔테이션 센서 불러옴
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        userSensorListner = new UserSensorListner();
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer= mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientatioin = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        btnMove.setOnTouchListener(this);

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnTouchListener(this);
        /////////////////////////////////
        ///////////////////////////////

        LinearLayout ll = (LinearLayout)findViewById(R.id.linearLayoutWhole);
        ll.setOnTouchListener(this);




    }

    public int move_x(int dir, int dist){
        if(dir == 1) return -dist;
        else if(dir == 3)return dist;
        else return 0;
    }
    public int move_y(int dir, int dist){
        if(dir == 2) return dist;
        else if(dir == 4)return -dist;
        else return 0;
    }

    public int IsWall(int x,int y){
        //간단하게, 좌우 위 아래 일정 범위 내로 벽 즉, 1이라는 값이 있다면 벽에 있는 디바이스

        //벽과 코너 둘다 검사할 것이기 때문에, 만약 0 이면 개활지, 1이면 벽, 2이면 코너
        int range =100;

        int dist =0;

        Wall_at_ver = false;
        Wall_at_hori = false;

        while(dist<range){
            for(int dir =1; dir<=4; dir++){
                if(x+move_x(dir,dist) <0 ) continue;
                if(x+move_x(dir,dist) >=map_hori ) continue;
                if(y+move_y(dir,dist) <0 ) continue;
                if(y+move_y(dir,dist) >=map_verti ) continue;

                if(pixel_map[x+move_x(dir,dist)][y] ==1) {
                    if(!Wall_at_hori){
                        AtWallHoriX = x+move_x(dir,dist);
                        AtWallHoriY = y;
                    }
                    Wall_at_hori = true;
                }
                if(pixel_map[x][y+move_y(dir,dist)] ==1) {
                    if(!Wall_at_ver){
                        AtWallVertiX = x;
                        AtWallVertiY = y+move_y(dir,dist);
                    }
                    Wall_at_ver = true;
                }
            }
            dist++;
        }
        if(Wall_at_hori && Wall_at_ver) return 2;
        else if (Wall_at_hori || Wall_at_ver) return 1;

        return 0;
    }

    public double CalNorm(double x, double m , double std){
        return ((1/Math.sqrt(2*Math.PI*std*std)) * Math.exp((-(x-m)*(x-m))/(2*std*std)));
    }

    //-SALA algorithm 적용 함수
    public void Calposition(ArrayList<DataSet> curIot){
        //Log.e("functionCall", " 진입: " + curIot.get(0).iot_id);

        //if(curIot.size() <10) return;

        //Rssi 가 센 순서대로 정렬하여, 가장 샌 window 크기만큼의 좌표를 이용할 것.
        //Descending descending = new Descending();
        //Collections.sort(curIot, descending);

        //초기 위치값은 window 크기 만큼의 점 좌표의 평균을 이용
        double xsum =0;
        double ysum =0;
        int window =0;

        //개방된 경우 10개, 벽의 경우 5개, 코너의 경우 3개
        if(curIot.get(0).iot_id.equals("iot1") ) window = 10;
        else if(curIot.get(0).iot_id.equals("iot2") ) window = 5;
        else if(curIot.get(0).iot_id.equals("iot3") ) window = 3;
        else if(curIot.get(0).iot_id.equals("hyun") ) window = 10;


        //window 사이즈 만큼의, rssi 가장 높았던 좌표들을 단순히 평균내어 초기위치 정함

        for (int i = 0; i < window; i++) {
            //if (ds.size() <= i - 1) break;
            xsum += curIot.get(i).xpos;
            ysum += curIot.get(i).ypos;
        }


        //initX 와 initY는 해당 iot 기기의 초기 좌표.
        double initX = xsum / window;
        double initY = ysum / window;



        Log.e("LOG", " 처음위치..[X]:" + String.format("%.4f", initX)
                + "           [Y]:" + String.format("%.4f", initY));
        //wall, corner handling 필요. 벽탐지는 어떻게..


        //벽 검사 루틴.
        int is_wall = IsWall((int) initX, (int) initY);


        if (is_wall == 2) Log.e("LOG", " 코너에 있다");
        else if (is_wall == 1) Log.e("LOG", " 벽에 있다");
        else if (is_wall == 0) Log.e("LOG", " 개방되어 있다");

        //for using wall-corner controll
        //ArrayList<DataSet> iot3ds = new ArrayList<DataSet>();
        ArrayList<DataSet> tempDS = new ArrayList<>();

        if (is_wall == 2) {// corner case
            //corner case 에서는 , 가장 센 3개의 좌표를 좌,우, 대각선 사영시켜서 사용할 것

            for (int i = 0; i < 3; i++) {
                tempDS.add(curIot.get(i));
            }

            //추려낸 3개의 좌표를 각각 사영시킨 후, 데이터셋에 추가시키는 과정
            for (int i = 0; i < 3; i++){
                double tempX = tempDS.get(i).xpos;
                double tempY = tempDS.get(i).ypos;

                double delta = 0;
                double x_flip = 0;
                double y_flip = 0;

                if (tempX > AtWallHoriX) {
                    delta = tempX - AtWallHoriX;
                    x_flip = tempX - 2 * delta;
                } else if (tempX <= AtWallHoriX) {
                    delta = AtWallHoriX - tempX;
                    x_flip = tempX + 2 * delta;
                }

                if (tempY > AtWallVertiY) {
                    delta = tempY - AtWallVertiY;
                    y_flip = tempY - 2 * delta;
                } else if (tempY <= AtWallVertiY) {
                    delta = AtWallVertiY - tempY;
                    y_flip = tempY + 2 * delta;
                }

                tempDS.add(new DataSet(tempDS.get(i).iot_id, tempDS.get(i).timestamp, x_flip, tempY, tempDS.get(i).rssi));
                tempDS.add(new DataSet(tempDS.get(i).iot_id, tempDS.get(i).timestamp, tempX, y_flip, tempDS.get(i).rssi));
                tempDS.add(new DataSet(tempDS.get(i).iot_id, tempDS.get(i).timestamp, x_flip, y_flip, tempDS.get(i).rssi));
            }
            xsum = 0;
            ysum = 0;
            //그렇게 추가시킨 좌표를 이용하여, 다시 초기 예상위치를 적음.
            for (int i = 0; i < tempDS.size(); i++) {
                xsum += tempDS.get(i).xpos;
                ysum += tempDS.get(i).ypos;
            }
            initX = xsum / tempDS.size();
            initY = ysum / tempDS.size();
        }//corner


        else if (is_wall == 1) {// wall

            //벽의 경우 가장 센 5개의 좌표를 벽을 기준으로 사영시켜서 이용할 것.
            for (int i = 0; i < 5; i++) {
                tempDS.add(curIot.get(i));
            }
            //얻어낸 5개의 좌표에 대해 사영시켜 dataset에 추가
            for (int i = 0; i < 5; i++) {
                double tempX = tempDS.get(i).xpos;
                double tempY = tempDS.get(i).ypos;

                double delta = 0;
                double x_flip = 0;
                double y_flip = 0;

                if (Wall_at_hori) {
                    if (tempX > AtWallHoriX) {
                        delta = tempX - AtWallHoriX;
                        x_flip = tempX - 2 * delta;
                    } else if (tempX <= AtWallHoriX) {
                        delta = AtWallHoriX - tempX;
                        x_flip = tempX + 2 * delta;
                    }
                    tempDS.add(new DataSet(tempDS.get(i).iot_id, tempDS.get(i).timestamp, x_flip, tempY, tempDS.get(i).rssi));
                }

                if (Wall_at_ver) {
                    if (tempY > AtWallVertiY) {
                        delta = tempY - AtWallVertiY;
                        y_flip = tempY - 2 * delta;
                    } else if (tempY <= AtWallVertiY) {
                        delta = AtWallVertiY - tempY;
                        y_flip = tempY + 2 * delta;
                    }
                    tempDS.add(new DataSet(tempDS.get(i).iot_id, tempDS.get(i).timestamp, tempX, y_flip, tempDS.get(i).rssi));
                }

            }
            //다시 얻어낸 좌표를 기준으로 초기예상값 계산.
            xsum = 0;
            ysum = 0;
            for (int i = 0; i < tempDS.size(); i++) {
                xsum += tempDS.get(i).xpos;
                ysum += tempDS.get(i).ypos;
            }
            initX = xsum / tempDS.size();
            initY = ysum / tempDS.size();
        }
        else {
            //openspace 의 경우 가장 강한 10개의 점 그대로 이용
            for (int i = 0; i < window; i++) {
                //if(ds.size()<=i-1) break;
                tempDS.add(new DataSet(curIot.get(i).iot_id, curIot.get(i).timestamp, curIot.get(i).xpos, curIot.get(i).ypos, curIot.get(i).rssi));
            }
        }
        //초기 위치를 기준으로 추정시 어떤 모습으로 되는지 보기위해 넣은 코드
        //ImageView s = (ImageView) findViewById(R.id.user);
        //s.setX((float) initX);
        //s.setY((float) initY);
        //////////////////



        Log.e("funcLog", "사이즈" + tempDS.size());
        //이제 wall 과 개방의 경우 tempDS 사이즈는 10, 코너는 12. tempDS에는 가장 rssi 좋은 좌표들이 들어있다.
        //Power-distance 테이블을 만들어야함.

        ArrayList<PoDisTable> podisTable = new ArrayList<>();

        for (int i = 0; i < tempDS.size(); i++) {
            double power = tempDS.get(i).rssi;
            double distance = Math.sqrt(Math.pow(tempDS.get(i).xpos - initX, 2) + Math.pow(tempDS.get(i).ypos - initY, 2));

            double tempSum = 0;
            int left_count = 0;
            int right_count = 0;
            for (int j = i + 1; j < tempDS.size(); j++) { // 오른쪽 5개 합
                if (tempDS.size() <= j || right_count >= 5) break;
                tempSum += Math.sqrt(Math.pow(tempDS.get(j).xpos - initX, 2) + Math.pow(tempDS.get(j).ypos - initY, 2));
                right_count++;
            }
            for (int j = i - 1; j >= 0; j--) { // 왼쪽 4개 합
                if (j < 0 || left_count >= 4) break;
                tempSum += Math.sqrt(Math.pow(tempDS.get(j).xpos - initX, 2) + Math.pow(tempDS.get(j).ypos - initY, 2));
                left_count++;
            }
            tempSum += distance;//자신 또한 평균에 포함
            double avgDis = tempSum / (left_count + right_count + 1);

            //평균을 구했으니 표준편차를 구해보자
            tempSum = 0;
            left_count = 0;
            right_count = 0;

            for (int j = i + 1; j < tempDS.size(); j++) { // 오른쪽 5개 편차제곱 합
                if (tempDS.size() <= j || right_count >= 5) break;
                double tempDis = Math.sqrt(Math.pow(tempDS.get(j).xpos - initX, 2) + Math.pow(tempDS.get(j).ypos - initY, 2));
                tempSum += Math.pow(tempDis - avgDis, 2);
                right_count++;
            }
            for (int j = i - 1; j >= 0; j--) { // 왼쪽 4개 합
                if (j < 0 || left_count >= 4) break;
                double tempDis = Math.sqrt(Math.pow(tempDS.get(j).xpos - initX, 2) + Math.pow(tempDS.get(j).ypos - initY, 2));
                tempSum += Math.pow(tempDis - avgDis, 2);
                left_count++;
            }
            tempSum += Math.pow(distance - avgDis, 2); // 자신의 편차 마저 더해줌
            double stdDis = Math.sqrt(tempSum / (left_count + right_count + 1));

            podisTable.add(new PoDisTable(power, distance, avgDis, stdDis));
        }

        //podisTable 에 이제 power-distance table entry 들이 담겨있다.
        //Log.e("LOG", "테이블 사이즈" + podisTable.size());

        ////////
        //이제 power-distance table을 만들었으니 보정을 위해 Grid-Weight map 을 만들자.
        //tempDS 와 podisTable을 이용하자~

        //Log.e("LOG", "avgDis:  " + podisTable.get(0).avgDis + "      stdDis:  "+ podisTable.get(0).stdDis);
        //Log.e("LOG", "Norm:   "+ CalNorm(0,0,1));
        int grid_hori = map_hori / 10; //128
        int grid_verti = map_verti / 10; //72
        double gridmap[][] = new double[grid_hori][grid_verti];

        //initialize grid_map
        for (int i = 0; i < grid_hori; i++) {
            for (int j = 0; j < grid_verti; j++) {
                gridmap[i][j] = 0;

            }
        }
        for (int k = 0; k < tempDS.size(); k++) {

            for (int i = 0; i < grid_hori; i++) {
                for (int j = 0; j < grid_verti; j++) {
                    double Dab = Math.sqrt(Math.pow(tempDS.get(k).xpos - i * 10, 2) + Math.pow(tempDS.get(k).ypos - j * 10, 2));
                    double norm = (Dab - podisTable.get(k).avgDis) / podisTable.get(k).stdDis;
                    double Fab = CalNorm(norm, 0, 1);
                    gridmap[i][j] += Fab;
                }
            }
        }
        double max_val = 0;
        int indexX = -1;
        int indexY = -1;
        for (int i = 0; i < grid_hori; i++) {
            for (int j = 0; j < grid_verti; j++) {
                if (max_val < gridmap[i][j]) {
                    max_val = gridmap[i][j];
                    indexX = i;
                    indexY = j;
                }

            }
        }


        //indexX 와 indexY는 보정 후 예상 좌표값
        Log.e("LOG", "예상X:  " + indexX*10 + "      예상Y:  "+ indexY*10);

        //이제 최종 구해낸 보정 후 좌표값을 갖고, 각 iot1 기기들의 위치를 표시
        if(curIot.get(0).iot_id.equals("iot1")){
            ImageView m = (ImageView) findViewById(R.id.iot1);
            m.setX((float) indexX*10+5);
            m.setY((float) indexY*10+5);
            m.setVisibility(View.VISIBLE);
        }
        else if(curIot.get(0).iot_id.equals("iot2")){
            ImageView m = (ImageView) findViewById(R.id.iot2);
            m.setX((float) indexX*10+5);
            m.setY((float) indexY*10+5);
            m.setVisibility(View.VISIBLE);
        }
        else if(curIot.get(0).iot_id.equals("iot3")){
            ImageView m = (ImageView) findViewById(R.id.iot3);
            m.setX((float) indexX*10+5);
            m.setY((float) indexY*10+5);
            m.setVisibility(View.VISIBLE);
        }

        /*
        ImageView m = (ImageView) findViewById(R.id.modify);
        m.setImageDrawable(getResources().getDrawable(R.drawable.modify));
        m.setX((float) indexX*10+5);
        m.setY((float) indexY*10+5);
        */
        //시험용
        /*
        if(curIot.get(0).iot_id.equals("hyun")){
            ImageView m = (ImageView) findViewById(R.id.iot3);
            m.setImageDrawable(getResources().getDrawable(R.drawable.modify));
            m.setX((float) indexX*10+5);
            m.setY((float) indexY*10+5);
        }
        */

    }

    @Override
    public boolean onTouch(View v, MotionEvent event){
        if(v.getId() == R.id.btnMove) {
            //btnMove 버튼이 눌릴때 각 센서들 센서 등록 버튼이 다시 한번 눌리면 등록 해제.
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                //case MotionEvent.ACTION_MOVE:
                //case MotionEvent.ACTION_UP:
                    if(!running){
                        running = true;
                        mSensorManager.registerListener(userSensorListner, mGyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
                        mSensorManager.registerListener(userSensorListner, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                        mSensorManager.registerListener(userSensorListner, mOrientatioin,SensorManager.SENSOR_DELAY_UI);
                    }

                    //실행 중일 때 -> 중지
                    else if(running)
                    {
                        ImageView ok = findViewById(R.id.ok_sign);
                        ok.setVisibility(View.INVISIBLE);
                        running = false;
                        mSensorManager.unregisterListener(userSensorListner);

                    }
            }
        }
        else if (v.getId() == R.id.btnList){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                //case MotionEvent.ACTION_MOVE:
                //case MotionEvent.ACTION_UP:
                    Log.d(TAG, "OnClick() btnScanStart()");
                    printToast("Turn on List");
                    Intent intent = new Intent(
                            getApplicationContext(), // 현재 화면의 제어권자
                            WIFIScanner.class); // 다음 넘어갈 클래스 지정
                    startActivity(intent); // 다음 화면으로 넘어간다

            }
        }
        //좌표수집을 마치고 iot 출력 시작
        else if (v.getId() == R.id.btnStart){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //case MotionEvent.ACTION_MOVE:
                    //case MotionEvent.ACTION_UP:
                    //Descending descending = new Descending();
                    //Collections.sort(ds, descending);

                    //if (ds.size() < 10) break;
                    //본래 rssi 가장 높은 순으로 써야하지만, 지금은 distance 임시로 쓰므로 ascending
                    //Ascending ascending = new Ascending();
                    //Collections.sort(ds, ascending);

                    //-2018.11.25 추가 실제 데이터로 위치탐색과정

                    Descending descending = new Descending();

                    //개방된 경우 10개 이상일때 계산 시작
                    if(iot1ds.size()>= 10){
                        Collections.sort(iot1ds, descending);
                        Calposition(iot1ds);
                    }
                    //벽에 있는 경우 5개 이상일때 계산 시작
                    if(iot2ds.size()>=5){
                        Collections.sort(iot2ds, descending);
                        Calposition(iot2ds);
                    }
                    //코너에 있는 경우 3개 이상일때 계산 시작.
                    if(iot3ds.size()>=3){
                        Collections.sort(iot3ds, descending);
                        Calposition(iot3ds);
                    }
            }
        }
        //touch iot image
        else if(v.getId() == R.id.iot1){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    String iotName = "iot1";
                    String iotInformation = "iot1";
                    Toast.makeText(this, "show information of " + iotName, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(
                            getApplicationContext(), // 현재 화면의 제어권자
                            TouchFragment.class); // 다음 넘어갈 클래스 지정
                    intent.putExtra("TouchFragment_iotName", "" + iotName);
                    intent.putExtra("TouchFragment_iotInformation", "" + iotInformation);
                    Log.d("iot_touch", "run");
                    startActivity(intent);
            }
        }else if(v.getId() == R.id.iot2){//touch image
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    String iotName = "iot2";
                    String iotInformation = "iot2";
                    Toast.makeText(this, "show information of " + iotName, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(
                            getApplicationContext(), // 현재 화면의 제어권자
                            TouchFragment.class); // 다음 넘어갈 클래스 지정
                    intent.putExtra("TouchFragment_iotName", "" + iotName);
                    intent.putExtra("TouchFragment_iotInformation", "" + iotInformation);
                    startActivity(intent);
            }
        }else if(v.getId() == R.id.iot3){//touch image
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    String iotName = "iot3";
                    String iotInformation = "iot3";
                    Toast.makeText(this, "show information of " + iotName, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(
                            getApplicationContext(), // 현재 화면의 제어권자
                            TouchFragment.class); // 다음 넘어갈 클래스 지정
                    intent.putExtra("TouchFragment_iotName", "" + iotName);
                    intent.putExtra("TouchFragment_iotInformation", "" + iotInformation);
                    startActivity(intent);
            }
        }
        else{
            //시험용. 터치시마다 터치이벤트를 받아 좌표를 받고, rssi는 일단 랜덤 정수로.
            // 이걸 실제 움직일때 쌓였던 데이터마냥 쓸거임!
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    double corx = event.getRawX();
                    double cory = event.getRawY();
                    //Random rd = new Random();


                    //Log.d("cordi", "x,y : "+corx + " , "+ cory);
                    if(corx>=map_hori) corx = map_hori-10;
                    else if(corx<=0) corx = 10;

                    if(cory>=map_verti) cory = map_verti-10;
                    else if(cory<=0) cory = 10;


                    ImageView iot1 = (ImageView) findViewById(R.id.iot1);
                    ImageView iot2 = (ImageView) findViewById(R.id.iot2);
                    ImageView iot3 = (ImageView) findViewById(R.id.iot3);

                    //if(pixel_map[(int)corx][(int)cory] == 1) Log.e("LOG", "벽이다~");
                    /*
                    Log.e("LOG", "Iot1 [X]:" + String.format("%.4f", iot1.getX())
                        + "           [Y]:" + String.format("%.4f", iot1.getY()));
                       */
                    double dist1 = Math.pow(iot1.getX()-corx,2)+Math.pow(iot1.getY()-cory,2);
                    double dist2 = Math.pow(iot2.getX()-corx,2)+Math.pow(iot2.getY()-cory,2);
                    double dist3 = Math.pow(iot3.getX()-corx,2)+Math.pow(iot3.getY()-cory,2);

                    double mindist = dist1;
                    if(dist2<mindist) mindist = dist2;
                    if(dist3<mindist) mindist = dist3;

                    mindist = Math.sqrt(mindist);

                    //double rssi = rd.nextInt(100);
                    //ds.add(new DataSet("", "",corx,cory,mindist));

                    /*
                    Log.e("LOG", " [X]:" + String.format("%.4f", corx)
                            + "           [Y]:" + String.format("%.4f", cory)
                            + "          벽인가?" + pixel_map[(int)corx][(int)cory]);

                    */
                    ImageView s = (ImageView) findViewById(R.id.user);
                    s.setX((float)corx);
                    s.setY((float)cory);

            }
        }
        return true;
    }

    // TouchListenerClass TouchListener = new TouchListenerClass();
    public void printToast(String messageToast) {
        Toast.makeText(this, messageToast, Toast.LENGTH_LONG).show();
    }

    //////////////////////////////////////////////////
    private void setAnim (ImageView s, boolean is_move){
        s.setVisibility(View.VISIBLE);
        if(is_move) {
            if(anim_change>=0) {
                if (anim_count == 1) {
                    s.setImageDrawable(getResources().getDrawable(R.drawable.walk2));
                    anim_count = 2;
                } else if (anim_count == 2) {
                    s.setImageDrawable(getResources().getDrawable(R.drawable.walk3));
                    anim_count = 3;
                } else if (anim_count == 3) {
                    s.setImageDrawable(getResources().getDrawable(R.drawable.walk4));
                    anim_count = 4;
                } else if (anim_count == 4) {
                    s.setImageDrawable(getResources().getDrawable(R.drawable.walk1));
                    anim_count = 1;
                }
                anim_change =0;
            }
            anim_change++;
        }
        else{
            s.setImageDrawable(getResources().getDrawable(R.drawable.oksign));
        }
    }
    //센서가 등록될때 불리는 함수
    private class UserSensorListner implements SensorEventListener {
        //센서값 변경시마다불릴 함수
        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()){

                /** GYROSCOPE */
                case Sensor.TYPE_GYROSCOPE:

                    /*센서 값을 mGyroValues에 저장*/
                    mGyroValues = event.values;

                    if(!gyroRunning)
                        gyroRunning = true;

                    break;

                /** ACCELEROMETER */
                case Sensor.TYPE_ACCELEROMETER:

                    /*센서 값을 mAccValues에 저장*/
                    mAccValues = event.values;

                    if(!accRunning)
                        accRunning = true;

                    double tempX = cor_accX;
                    double tempY = cor_accY;
                    accX = event.values[0];
                    accY = event.values[1];
                    accZ = event.values[2];

                    double deter = Math.sqrt(Math.pow(accX,2)+Math.pow(accY,2)+Math.pow(accZ,2));

                    //if(deter < 9.4 && deter>15) deter = 9.4;
                    //초기 step_count 값은 0 .
                    if(step_count> 30){ //when motion stop detect// && calib_count >=240){
                        accG = deter;
                        veloX =0;
                        veloY =0;
                        ImageView ok_sign=  findViewById(R.id.ok_sign);
                        //ok_sign.setX(0);
                        //ok_sign.setY(0);
                        setAnim(ok_sign,false);
                    }
                    //각 축의 가속도 합이 일정이상 넘으면 움직이고 있다로 판별.
                    // 아닐경우 step_count++ 이것이 일정이상 넘으면 정지상태로 판별. 정지상태의 중력가속도 체크.
                    if(deter>11) {
                        is_step = true;
                        step_count =0;
                        ImageView ok_sign=  findViewById(R.id.ok_sign);
                        //ok_sign.setX(0);
                        //ok_sign.setY(0);
                        setAnim(ok_sign, true);
                    }
                    else step_count++;

                    ImageView s = (ImageView) findViewById(R.id.user);
                    //정지상태에서 체크한 중력가속도와 핸드폰이 기울어진 정도를 가지고, 각 축의 중력가속도 요소 제거.
                    cor_accX = accX + Math.sin(pitch*Math.PI/180)*accG;
                    cor_accY = accY + Math.sin(roll*Math.PI/180)*accG;

                    //그렇게 얻어낸 X,Y축의 가속도를 가지고 적분
                    //가속도를 이용해 먼저 속도를 얻어냄
                    double tempvX = veloX;
                    double tempvY = veloY;

                    double RtoP = 8; // 현실에서 pixel 세계, 스마트폰 세계로 변환될때 보정값.

                    veloX = tempvX+ (tempX+ ((cor_accX-tempX)/2))*RtoP*dt;
                    veloY = tempvY+ (tempY+ ((cor_accY-tempY)/2))*RtoP*dt;


                    //Log.d("orien", "orival: "+ori_val + "   ori_first: " +ori_first);
                    ori_val -= ori_first;
                    //구한 속도를 이용해 이동 거리 얻어냄. 이 이동거리는 내가 바라보고 있는 방향의 이동거리
                    //이것을 오리엔테이션 센서의 방향값을 이용해 각 축에 보정.
                    double delta = (tempvX+ ((veloX-tempvX)/2))*RtoP*dt; // *5는 단순히 속도를 위한 보정값.
                    double deltaX = delta*Math.sin(ori_val*Math.PI/180);
                    double deltaY = delta*Math.cos(ori_val*Math.PI/180);

                    //그렇게 움직인 값을 가지고, 현재상태에서 위치 업데이트

                    double nY = s.getY()+deltaY;
                    double nX = s.getX()+deltaX;
                    int newX = (int)nX;
                    int newY = (int)nY;
                    //좌표를 정수화하여 새 위치의 해당 좌표가 벽이라면 움직이지 않음.
                    if(newX>0 && newX< map_hori && pixel_map[newX][(int)s.getY()] != 1){
                        s.setX((float)nX);
                    }
                    if(newY>0 && newY< map_verti && pixel_map[(int)s.getX()][newY] != 1){
                        s.setY((float)nY);
                    }
                    /*
                    Log.e("LOG", "ACCELOMETER           [X]:" + String.format("%.4f", event.values[0])
                            + "           [Y]:" + String.format("%.4f", event.values[1])
                            + "           [Z]:" + String.format("%.4f", event.values[2]));
                    */
                    break;
                default:
                    //if(Math.abs(ori_val - event.values[0]) <50)
                    if(!is_ori) {
                        ori_first = event.values[0];
                        is_ori = true;
                    }
                    ori_val = event.values[0];
                    break;

            }

            /**두 센서 새로운 값을 받으면 상보필터 적용*/
            if(gyroRunning && accRunning){
                complementaty(event.timestamp);

            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


    }
    //////////////////////////////
    ///////////////////////////////
    private void complementaty(double new_ts){

        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll= Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);

        temp = (1/a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp*dt);

        //tv_roll.setText("roll : "+roll);
        //tv_pitch.setText("pitch : "+pitch);

    }
    //////////////////////////////////////////////////////////////////////////////////////////////// for broadcast
    WifiManager wifimanager;
    int iot_level;
    String iot_name;
    private List<ScanResult> mScanResult; // ScanResult List
    ArrayList<Listviewitem> wifiData;
    String iot_result = "";
    String registered_iot_list[] = {"chang_room", "chang_room2", "fruit", "vegetable", "iot1", "iot2","iot3","ioT1","ioT2","ioT3"};
    List<String> iot_lstString = new ArrayList<String>(Arrays.asList(registered_iot_list));
    DateFormat df = new SimpleDateFormat("HH:mm:ss");
    Calendar calobj = Calendar.getInstance();
    String currentTime = df.format(calobj.getTime());
    private static final float FONT_SIZE = 15;
    private LinearLayout container;

    //file system
    String myText = "test";
    String FileName =  "";

    double user_x;
    double user_y;

    int delay=0;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//            try {//read iot with interval 1 sec
//                Thread.sleep(1000);
//            }catch(Exception e){
//                //pass
//            }
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                if(delay%3==0) {
//                Log.d("Broadcast1","work");
                getWIFIScanResult(); // get WIFISCanResult
                wifimanager.startScan(); // for refresh
                ImageView s = (ImageView) findViewById(R.id.user);
                user_x = s.getX();
                user_y = s.getY();
//                }
                delay++;
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };
    public void getWIFIScanResult() {

        mScanResult = wifimanager.getScanResults(); // ScanResult
        wifiData=new ArrayList<>();
        container.removeAllViews();
        ImageView s = (ImageView) findViewById(R.id.user);
        calobj = Calendar.getInstance();
        currentTime = df.format(calobj.getTime());
        // Scan count
        for (int i = 0; i < mScanResult.size(); i++) {
            iot_result = "";
            ScanResult result = mScanResult.get(i);
            iot_name = result.SSID.toString();

            //if name is null break
            if(iot_name == "" || iot_name == " "){
                continue;
            }
            //if SSID not in sales list
            if(!iot_lstString.contains(iot_name)){
                continue;
            }
            iot_level = result.level;
            FileName = result.SSID;

            myText = currentTime + " " + s.getX() + " " + s.getY() + " " + iot_level;//time xpos ypos rssi

            Listviewitem iot = new Listviewitem(iot_name, iot_level, myText);
            wifiData.add(iot);

            iot_result = " " + iot_name + " " + iot_level + " " + currentTime;


            Log.d("location_data", "x= "+user_x+" "+(double)s.getX()+"y= "+ user_y + " "+ (double)s.getY() );

            //pos_grid[(int)user_x/10][(int)user_y/10] = true;
//            Log.d("position_test", ""+pos_grid[(int)user_x/10][(int)user_y/10]);
//            if(!pos_grid[(int)user_x/10][(int)user_y/10] ) {
            if((int)user_x != (int)s.getX() && (int)user_y != (int)s.getY()) {
//                pos_grid[(int)user_x/10][(int)user_y/10] = true;
                writeToFile(myText, this, FileName);
                iot_result = readFromFile(this, FileName);
                Log.d("FILE_READ", FileName + " " + iot_result);
            }
//            textview(iot_result);
        }
    }
    //write file
    private void writeToFile(String data,Context context,String name) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name, Context.MODE_APPEND));
            outputStreamWriter.write(data + "\n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //read file
    private String readFromFile(Context context, String fileName) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    String [] temp = receiveString.split(" ");
//                    Log.d("read_temp",""+temp.length);
                    if(fileName.equals("hyun")){
                        Log.d("fileName", fileName);
                        Log.d("read_temp",""+temp[3]);
                        /*
                        for(int i=0;i<temp.length; i++){
                            Log.d("read_temp",""+temp[i]);
                        }
                        */
                    }

                    //
                    //ds.add(new DataSet(fileName, temp[0], Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));

                    //fileName is iot device's name.
                    //temp[1] and temp[2] is smartphone's position
                    //temp[3] is rssi
                    //Log.d("filename", "fileName is..: " +fileName);
                    if(fileName.equals("iot1")){
                        //Log.e("datainput", "iot1 추가");
                        iot1ds.add(new DataSet(fileName, temp[0], Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));
                    }
                    else if(fileName.equals("iot2")){
                        iot2ds.add(new DataSet(fileName, temp[0], Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));
                    }
                    else if(fileName.equals("iot3")){
                        iot3ds.add(new DataSet(fileName, temp[0], Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));
                    }

                    //시험용
                    /*
                    if(fileName.equals("hyun")){
                        iot3ds.add(new DataSet(fileName, temp[0], Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));
                        //Log.d("inputcheck", iot3ds.get(0).iot_id);
                    }
                    */

                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void textView(String str){
        TextView view1 = new TextView(this);
        view1.setText(str);
        view1.setTextSize(FONT_SIZE);
        view1.setTextColor(Color.BLACK);

        //layout_width, layout_height, gravity 설정
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view1.setLayoutParams(lp);

        //부모 뷰에 추가
//        container.addView(view1);
    }
    public void initWIFIScan() {
        // init WIFISCAN
        wifimanager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        wifimanager.startScan();
        Log.d(TAG, "initWIFIScan()");
    }
    @Override
    protected void onResume() {
        super.onResume();
        initWIFIScan();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();

    }

}
