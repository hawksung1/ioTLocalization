package org.WIFIScanner;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class
WIFIScanner extends Activity implements OnClickListener {

	private static final String TAG = "WIFIScanner";
	//sales list
	String sales_list[] = {"chang_room", "chang_room2", "fruit", "vegetable", "iot1", "iot2", "iot3", "ioT1", "ioT2", "ioT3"};

	List<String> lst = new ArrayList<String>(Arrays.asList(sales_list));
	public static final int MY_PERMISSIONS_REQUEST_MULTIPLE_PERMISSION = 123;

	// WifiManager variable
	WifiManager wifimanager;
	//GPS variable
	LocationManager locationManager;

	// UI variable
	TextView textStatus;
	Button btnScanStart;
	Button btnScanStop;
	Button btnMap;
	Button btnInsert;
	ListView listView;
	ArrayList<Listviewitem> data;
	EditText edittext;

	private int scanCount = 0;
	String text = "";
	String result = "";
	String insert = "";
	String iotInformation = "";

    DateFormat df = new SimpleDateFormat("HH:mm:ss");
    Calendar calobj = Calendar.getInstance();
    String currentTime = df.format(calobj.getTime());
    int iot_level;

	private List<ScanResult> mScanResult; // ScanResult List

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				getWIFIScanResult(); // get WIFISCanResult
				wifimanager.startScan(); // for refresh
			} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
			}
		}
	};

	public void getWIFIScanResult() {

		mScanResult = wifimanager.getScanResults(); // ScanResult
		data=new ArrayList<>();
//		printToast(""+mScanResult.size());
		// Scan count
		for (int i = 0; i < mScanResult.size(); i++) {
			ScanResult result = mScanResult.get(i);
			String wifi_name = result.SSID.toString();

			//if name is null break
			if(wifi_name == "" || wifi_name == " "){
				continue;
			}
			//if SSID not in sales list
			if(insert.length() == 0) {
				if (!lst.contains(wifi_name)) {
					continue;
				}
			} //search iot
			else{
				if(!insert.equals(wifi_name)){
					continue;
				}
			}
            currentTime = df.format(calobj.getTime());
            iot_level = result.level;
            iotInformation = currentTime + " " + iot_level;
			Listviewitem iot = new Listviewitem(wifi_name, iot_level, iotInformation);

			data.add(iot);
		}
		//list view
		ListviewAdapter adapter=new ListviewAdapter(this,R.layout.item,data);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			//listview touch event
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String wifi_name = data.get(position).getName();
				String iot_information = data.get(position).getIotInformation();
				Toast.makeText(WIFIScanner.this ,wifi_name,Toast.LENGTH_LONG).show();
				Intent intent = new Intent(
						getApplicationContext(), // 현재 화면의 제어권자
						TouchFragment.class); // 다음 넘어갈 클래스 지정
				intent.putExtra("TouchFragment_iotName",wifi_name);
                intent.putExtra("TouchFragment_iotInformation",iot_information);
				startActivity(intent); // 다음 화면으로 넘어간다
			}
		});
	}

	public void initWIFIScan() {
		// init WIFISCAN
		wifimanager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		text = "";
		final IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mReceiver, filter);
		wifimanager.startScan();
		Log.d(TAG, "initWIFIScan()");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Setup UI
		btnScanStart = (Button) findViewById(R.id.btnScanStart);
		btnScanStop = (Button) findViewById(R.id.btnScanStop);
		btnMap = (Button) findViewById(R.id.btnMap);
		btnInsert = (Button) findViewById(R.id.btnInsert);
        listView = (ListView)findViewById(R.id.listview);
		edittext=(EditText)findViewById(R.id.editText);

		// Setup OnClickListener
		btnScanStart.setOnClickListener(this);
		btnScanStop.setOnClickListener(this);
		btnMap.setOnClickListener(this);
        btnInsert.setOnClickListener(this);

		// Setup WIFI
		wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		Log.d(TAG, "Setup WIfiManager getSystemService");

		// if WIFIEnabled
		if (wifimanager.isWifiEnabled() == false)
			wifimanager.setWifiEnabled(true);

		//check is GPS enable
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager)getSystemService(context);
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			alertCheckGPS();
		}
	}

	public void printToast(String messageToast) {
		Toast.makeText(this, messageToast, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnScanStart) {
			Log.d(TAG, "OnClick() btnScanStart()");
			printToast("IOT SCAN !!!");
			initWIFIScan(); // start WIFIScan
		}
		if (v.getId() == R.id.btnScanStop) {
			Log.d(TAG, "OnClick() btnScanStop()");
			printToast("IOT STOP !!!");
			try {
				unregisterReceiver(mReceiver); // stop WIFISCan
			} catch(Exception e){

			}
		}
		if (v.getId() == R.id.btnMap) {
			Log.d(TAG, "OnClick() btnMap()");
			printToast("turn on Map !!!");
			Intent intent = new Intent(
					getApplicationContext(), // 현재 화면의 제어권자
					IOTMap.class); // 다음 넘어갈 클래스 지정
			startActivity(intent); // 다음 화면으로 넘어간다
		}
		if (v.getId() == R.id.btnInsert) {
			Log.d(TAG, "OnClick() btnInsert()");
			printToast("IoT SCAN !!!");
			insert = edittext.getText().toString();
			initWIFIScan(); // start WIFIScan
		}

	}

	//runtime get permission
	@Override
	public void onResume() {
		super.onResume();
		// No explanation needed, we can request the permission.
		int PERMISSION_ALL = 1;
		String[] PERMISSIONS = {
				android.Manifest.permission.ACCESS_WIFI_STATE,
				android.Manifest.permission.CHANGE_WIFI_STATE,
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
				android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
		};
		if(!hasPermissions(this, PERMISSIONS)){
			ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
		}
	}
	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}
	private void alertCheckGPS() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS is disabled! Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Enable GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								moveConfigGPS();
							}
						})
				.setNegativeButton("Do nothing",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	// GPS 설정화면으로 이동
	private void moveConfigGPS() {
		Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptionsIntent);
	}

	@Override
    public void onDestroy(){
	    super.onDestroy();
	    File f = this.getFilesDir();
	    File file1 = new File(f, "iot1");
        File file2 = new File(f, "iot2");
        File file3 = new File(f, "iot3");
	    if(file1.delete()){
	        Log.i("FILE_DELETE", ""+f.getName()+" deleted");
        }else{
            Log.i("FILE_DELETE", ""+f.getName()+" deletd failed");
        }
        if(file2.delete()){
            Log.i("FILE_DELETE", ""+f.getName()+" deleted");
        }else{
            Log.i("FILE_DELETE", ""+f.getName()+" deletd failed");
        }
        if(file3.delete()){
            Log.i("FILE_DELETE", ""+f.getName()+" deleted");
        }else{
            Log.i("FILE_DELETE", ""+f.getName()+" deletd failed");
        }
    }

}