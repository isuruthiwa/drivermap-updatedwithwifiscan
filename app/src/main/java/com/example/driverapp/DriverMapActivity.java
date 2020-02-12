package com.example.driverapp;
// Danuka

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.lang.Double.parseDouble;
import static java.lang.Math.abs;


public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener, com.google.android.gms.location.LocationListener, android.location.LocationListener { //, com.google.android.gms.location.LocationListener

    TextToSpeech tts;                        //Speech variables
    public DatabaseHelper myDb;                    //Local database ghost
    public static DatabaseTable1 table1Db;                //Local database table1 ghost
    public static TemporaryTable tempTable;               //Local database tempTable ghost
    QuarryArea Qarea;                       //Quary area ghost

    private GoogleMap mMap;                 //Map instance
    GoogleApiClient mGoogleApiClient;       //Google API caller
    Location mLastLocation;                 //Last location variable
    LocationRequest mLocationRequest;       //Location request variable

    private ImageView mLogout;                 //Logout button
    private TextView mTime, mHeading;
    private Boolean isLoggingout;           //Logout status variable
    public LatLng preLocation;          //a location variable
    public LatLng currentLocation;
    public LatLng targetSignLocation;
    private TextView signTxt;               //TextView for the Road sign
    //private TextView scanDetails;           //Scan Details on the bottom
    //private TextView timer1, timer2, timer3;
    private TextView distanceTxt;
    private ConstraintLayout signLayout;


    private LocationManager locationManager;
    private static final long MIN_TIME = 1000;
    private static final float MIN_DISTANCE = 0;


    public int audio = 1;
    private ImageView signView1, imageView2, signView2, signView3, mSignImage1, mSignImage2;
    private Button mAngry;

    //compass
    private SensorManager sensorManager;    //initiate sensors

    public PackageManager PM;
    private int slot = 0;
    public static Integer areaprev = 0;     //this variable to store previous area value.
    public static Integer tempID = 0;       //this id is used to keep colsest road sign id
    public static Integer tempID2 = 0;

    public double GeneralHeading = 0.0;
    public double GeneranHeading2 = 0.0;
    private String preShout = "";
    private String Shout = "";

    public double hL = 0.0;
    public double headiL = 0.0;
    public double htm1L = 0.0;
    public double htm2L = 0.0;

    public double compassH = 0.0;
    public String RoadSignToShow = "nosign";
    public String RoadSignToShow2 = "nosign";
    public Integer speed = 0;
    Location targetLocation = new Location("");         //provider name is unnecessary

    //WiFi Scanning
    private WifiManager wifiManager;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    public static List<RoadSign> roadSignList = new ArrayList<>();

    Thread task;
    Thread ShowImage;
    Thread ShowImage2;

    //SSID Decryption
    double unixTime;
    double beacontime, prevbeacontime, beaconhead;

    //Decryption
    String decryptedString;

    String key1 = "2b7e151628aed2a6abf7158809cf4f3c"; //hexkey
    String iv1 = "000102030405060708090a0b0c0d0e0f"; //hexiv
    String key2 = "K34VFiiu0qar9xWICc9PPA==";//base64key
    String iv2 = "AAECAwQFBgcICQoLDA0ODw==";//base64iv

    String[] alnum = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "1", "2", "3", "4", "5", "6", "7", "8"};
    ArrayList<String> obj = new ArrayList<String>(Arrays.asList(alnum));
    Map<String, String> signList = new HashMap<String, String>();
    Map<String, Integer> drawableList = new HashMap<String, Integer>();
    LatLng lastSignLocation;
    String lastSignMessage;

    Calendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        signList = new ImageData().getSignList();
        drawableList = new ImageData().getDrawableList();
        myDb = new DatabaseHelper(this);                        //DatabaseHelper object create
        table1Db = new DatabaseTable1(this);                    //DatabaseTable1 object create
        tempTable = new TemporaryTable(this);                   //TemporaryTable object create
        Qarea = new QuarryArea();

        mLogout = findViewById(R.id.logout);
        signView1 = findViewById(R.id.signView1);
        signView2 = findViewById(R.id.signView2);
        signView3 =  findViewById(R.id.signView3);
        mSignImage1 =  findViewById(R.id.imageView);
        mSignImage2 =  findViewById(R.id.imageView2);
        mTime =  findViewById(R.id.time);
        mHeading =  findViewById(R.id.head);
        imageView2 =  findViewById(R.id.audio);
        signTxt = findViewById(R.id.signTxt);
        //timer1 = findViewById(R.id.timer1);
        //timer2 = findViewById(R.id.timer2);
        //timer3 = findViewById(R.id.timer3);
        distanceTxt = findViewById(R.id.distanceTxt);
        mAngry = findViewById(R.id.angry_btn);
        signLayout = findViewById(R.id.signLayout);

        //compass
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        PM = (PackageManager) getPackageManager();

        //Location
        //final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        preLocation = new LatLng(0.0, 0.0);
        imageView2.setImageResource(R.drawable.volume_up);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio == 1) {
                    imageView2.setImageResource(R.drawable.volume_off);
                    audio = 0;
                    mTime.setText(String.valueOf(audio));
                } else {
                    imageView2.setImageResource(R.drawable.volume_up);
                    audio = 1;
                    mTime.setText(String.valueOf(audio));
                }
            }
        });



        // Text to speech
        tts = new TextToSpeech(DriverMapActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                /* TODO Auto-generated method stub */
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        if (audio == 1) {
                            ConvertTextToSpeech("");
                        }
                    }
                } else
                    Log.e("error", "Initilization Failed!");

            }

        });

        //Enabling wifi service
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
//        listView.setAdapter(adapter);
//        scanWifi();


        //Initialize the previous area which the driver stop the vehicle
        Cursor res = table1Db.fetchprev("1");
        if (res == null) {
            table1Db.insertPrev("1", "1");
            areaprev = 1;
            System.out.println("areaPrev 1");
        } else {
            if (res.getCount() == 0) {
                table1Db.insertPrev("1", "1");
                areaprev = 1;
                System.out.println("areaPrev 2");
            } else {
                while (res.moveToNext()) {
                    int TABLE_ID = Integer.parseInt(res.getString(0));
                    areaprev = Integer.parseInt(res.getString(1));
                    System.out.println("areaPrev " + areaprev);

                }
            }
        }

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingout = true;
                Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
                System.out.println("success");
                startActivity(intent);
                finish();
                return;
            }
        });

        //Initialize area data

        ShowImage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mSignImage2.setImageResource(drawableList.get(RoadSignToShow));
//                    mSignImage1.setImageResource(drawableList.get(RoadSignToShow2));
                    Log.i("On image load", "Road sign load" + "Thread "+ Thread.currentThread().getId());

                }
            }
        });

        ShowImage2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mSignImage1.setImageResource(drawableList.get(RoadSignToShow2));
                }
            }
        });


        task = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {

                    wifiManager = (WifiManager)
                            getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context c, Intent intent) {
                            boolean success = intent.getBooleanExtra(
                                    WifiManager.EXTRA_RESULTS_UPDATED, false);
                            if (success) {
                                arrayList.clear();
                                results = wifiManager.getScanResults();
                                //
                                // System.out.println(results);
                                try {
                                    calendar = Calendar.getInstance();
                                    for (ScanResult scanResult : results) {
                                        arrayList.add(scanResult.SSID + " ; " + scanResult.level);
                                        adapter.notifyDataSetChanged();
                                    }

                                    long hour=calendar.get(Calendar.HOUR_OF_DAY);
                                    long min=calendar.get(Calendar.MINUTE);
                                    long sec=calendar.get(Calendar.SECOND);

                                    unixTime= hour*3600+min*60+sec;

                                    //System.out.println("Array"+arrayList);
                                    int scanfound=0;
                                    for (int i = 0; i < arrayList.size(); i++) {
                                        String rsu[] = arrayList.get(i).split(";");
                                        if (rsu.length == 2) {
                                            String ssid = arrayList.get(i).split(";")[0];
                                            if (ssid.contains("RF")) {
                                                /**
                                                 All RSU signal message starts with "R2v" encrypted to "F5W"
                                                 **/

                                                //Flag to remind that sign was discovered
                                                scanfound=1;
                                                String rssi_val = arrayList.get(i).split(";")[1];
                                                //Decrypting Location, Heading, Timestamp and Ciphered message
                                                String dec = decrypt(ssid.substring(1));
                                                String dec_ssid = dec;
                                                System.out.println("Message: "+dec);

                                                if(unixTime-beacontime>600) {
                                                    System.out.println(unixTime-beacontime);
                                                    System.out.println("Timeout");
                                                    continue;
                                                }
                                                if(prevbeacontime>beacontime){
                                                    System.out.println("Previous Message");
                                                    continue;
                                                }
                                                prevbeacontime=beacontime;
                                                lastSignMessage=dec;
                                                signTxt.setText(dec.substring(0,2));
//                            System.out.println(dec);

                                                /**
                                                 * Sign Validation with
                                                 * TIMESTAMP
                                                 */

                                                //If 4-Way Junction
                                                if(dec.substring(0,2).equals("RF")){
                                                    signView1.setVisibility(View.VISIBLE);
                                                    signView2.setVisibility(View.VISIBLE);
                                                    signView3.setVisibility(View.VISIBLE);

                                                    signTxt.setText("4-Way Junction");

                                                    /**
                                                     * Get angle difference between RSU and driver
                                                     * Divide into 4 boundaries
                                                     * 000 000 000 000 :: bitstream :: area1 area2 area3 area4
                                                     * 000 means Left Forward Right
                                                     * select which signals are relevant and show
                                                     */
                                                    double Head = GeneralHeading;//getHeading();
                                                    System.out.println(beaconhead);
                                                    double signselect = (beaconhead-Head)/90;
                                                    System.out.println("signselect :"+signselect);
                                                    if((signselect<0.5) & (signselect>-0.5)){
                                                        signselect=0;
                                                    }else if((signselect<1.5) & (signselect>0.5)){
                                                        signselect=1;
                                                    }
                                                    else if((signselect<2.5) & (signselect>1.5)){
                                                        signselect=2;
                                                    }
                                                    else if((signselect<-2.5) & (signselect>-1.5)){
                                                        signselect=2;
                                                    }
                                                    else if((signselect<-0.5) & (signselect>-1.5)){
                                                        signselect=3;
                                                    }

                                                    String selectBeacon = dec.substring(4,6);

                                                    String a = selectBeacon.substring(0,  1);
                                                    String b = selectBeacon.substring(1,  2);
                                                    int l = obj.indexOf(a)+12;

                                                    int m = obj.indexOf(b)+12;
                                                    String signalStream= String.format("%6s",Integer.toBinaryString(l)).replace(" ","0")+String.format("%6s",Integer.toBinaryString(m)).replace(" ","0");
                                                    char[] n =(signalStream.substring((int)signselect*3,(int)signselect*3+3)).toCharArray();

//                                                    timer1.setVisibility(View.VISIBLE);
//                                                    timer2.setVisibility(View.VISIBLE);
//                                                    timer3.setVisibility(View.VISIBLE);

                                                    if(n[0]=='1'){
                                                        //timer1.setText("Go");
                                                        signView1.setImageResource(R.drawable.gap_01);
                                                    }else{
                                                        //timer1.setText("Stop");
                                                        signView1.setImageResource(R.drawable.gap_02);
                                                    }

                                                    if(n[1]=='1'){
                                                        //timer2.setText("Go");
                                                        signView2.setImageResource(R.drawable.gap_05);
                                                    }else{
                                                        //timer2.setText("Stop");
                                                        signView2.setImageResource(R.drawable.gap_06);
                                                    }

                                                    if(n[2]=='1'){
                                                        //timer3.setText("Go");
                                                        signView3.setImageResource(R.drawable.gap_03);
                                                    }else{
                                                        //timer3.setText("Stop");
                                                        signView3.setImageResource(R.drawable.gap_04);
                                                    }

                                                    ConvertTextToSpeech("Four way junction");

                                                }

                                                //If T Junction
                                                else if(dec.substring(0,2).equals("TJ")){
                                                    signLayout.setBackgroundResource(R.drawable.sign_back);
                                                    signTxt.setText("T Junction");
                                                    System.out.println("Inside T J");
                                                    /**
                                                     * Get angle difference between RSU and driver
                                                     * Divide into 4 boundaries
                                                     * 000 000 000 :: bitstream :: area1 area2 area3
                                                     * area1 is the left side in main road
                                                     * 000 means Left Forward Right
                                                     * select which signals are relevant and show
                                                     * TESTING REQUIRED
                                                     */

                                                    String timers=dec.substring(dec.length()-7);
                                                    //System.out.println("Timers T Junction: "+timers);
                                                    //double Head = 0;
                                                    double Head=0;

                                                    try {
                                                        Head= GeneralHeading;//getHeading();
                                                    }
                                                    catch (Exception e){
                                                        //Error from reading heading - Set to default heading
                                                        //headingTextView.setText("getHeading Exception");
                                                        Head=0;
                                                    }
                                                    //System.out.println(beaconhead);
                                                    double signselect = ((beaconhead-Head)/90);
                                                    System.out.println("signselect :"+signselect);
                                                    if(abs(signselect)<0.5){
                                                        signselect=0;
                                                    }else if((signselect<1.5) & (signselect>0.5)){
                                                        signselect=1;
                                                    }
                                                    else if(((signselect>-1.5) & (signselect<-0.5))|((signselect>2.5) & (signselect<3.5))){
                                                        signselect=2;
                                                    }
                                                    else{
                                                        signView2.setVisibility(View.INVISIBLE);
                                                        signView3.setVisibility(View.INVISIBLE);
                                                        signView1.setVisibility(View.INVISIBLE);
                                                        signLayout.setBackgroundResource(0);
                                                        continue;
                                                    }


                                                    String selectBeacon = dec.substring(2,3);

                                                    String a = selectBeacon.substring(0,  1);
                                                    //System.out.println("DEC:"+dec);
                                                    int l = obj.indexOf(a);
                                                    String signalStream= String.format("%6s",Integer.toBinaryString(l)).replace(" ","0");
                                                    char[] n =(signalStream.substring((int)signselect*2,(int)signselect*2+2)).toCharArray();
                                                    //System.out.println("SignalStream : "+signalStream);
                                                    String tim = timers.substring((int)signselect*2,(int)signselect*2+2);
                                                    int tim1= obj.indexOf(String.valueOf(timers.charAt(0)));
                                                    int tim2= obj.indexOf(String.valueOf(timers.charAt(1)));
                                                    //System.out.println("Tim Array: "+tim+"\nt1:"+tim1+", t2:"+tim2);


                                                    signView1.setVisibility(View.INVISIBLE);
                                                    signView2.setVisibility(View.INVISIBLE);
                                                    signView3.setVisibility(View.INVISIBLE);
//                                                    timer1.setVisibility(View.INVISIBLE);
//                                                    timer2.setVisibility(View.INVISIBLE);
//                                                    timer3.setVisibility(View.INVISIBLE);

                                                    if(n[0]=='1'){
                                                        if(signselect==0) {
                                                            //timer3.setVisibility(View.VISIBLE);
                                                            signView3.setVisibility(View.VISIBLE);
                                                            //timer3.setText(String.valueOf(tim1));
                                                            signView3.setImageResource(R.drawable.gap_03);
                                                            ConvertTextToSpeech("You can turn left");
                                                        }else if (signselect==1){
                                                            //timer3.setVisibility(View.VISIBLE);
                                                            signView3.setVisibility(View.VISIBLE);
                                                            //timer3.setText(String.valueOf(tim1));
                                                            signView3.setImageResource(R.drawable.gap_03);
                                                            ConvertTextToSpeech("You can turn left");
                                                        }else {
                                                            //timer1.setVisibility(View.VISIBLE);
                                                            signView1.setVisibility(View.VISIBLE);
                                                            //timer1.setText(String.valueOf(tim1));
                                                            signView1.setImageResource(R.drawable.gap_01);
                                                            ConvertTextToSpeech("You can go straight");
                                                        }

                                                    }else{
                                                        if(signselect==0) {
                                                            //timer3.setVisibility(View.VISIBLE);
                                                            signView3.setVisibility(View.VISIBLE);
                                                            //timer3.setText(String.valueOf(tim1));
                                                            signView3.setImageResource(R.drawable.gap_04);
                                                        }else if (signselect==1){
                                                            //timer3.setVisibility(View.VISIBLE);
                                                            signView3.setVisibility(View.VISIBLE);
                                                            //timer3.setText(String.valueOf(tim1));
                                                            signView3.setImageResource(R.drawable.gap_04);
                                                        }else {
                                                            //timer1.setVisibility(View.VISIBLE);
                                                            signView1.setVisibility(View.VISIBLE);
                                                            //timer1.setText(String.valueOf(tim1));
                                                            signView1.setImageResource(R.drawable.gap_02);
                                                        }
                                                    }

                                                    if(n[1]=='1'){
                                                        if(signselect==0) {
                                                            //timer2.setVisibility(View.VISIBLE);
                                                            signView2.setVisibility(View.VISIBLE);
                                                            //timer2.setText(String.valueOf(tim2));
                                                            signView2.setImageResource(R.drawable.gap_05);
                                                            ConvertTextToSpeech("You can turn right");
                                                        }else if (signselect==1){
                                                            //timer1.setVisibility(View.VISIBLE);
                                                            signView1.setVisibility(View.VISIBLE);
                                                            //timer1.setText(String.valueOf(tim2));
                                                            signView1.setImageResource(R.drawable.gap_01);
                                                            ConvertTextToSpeech("You can go straight");
                                                        }else {
                                                            //timer2.setVisibility(View.VISIBLE);
                                                            signView2.setVisibility(View.VISIBLE);
                                                            //timer2.setText(String.valueOf(tim2));
                                                            signView2.setImageResource(R.drawable.gap_05);
                                                            ConvertTextToSpeech("You can turn right");
                                                        }
                                                    }else{
                                                        if(signselect==0) {
                                                            //timer2.setVisibility(View.VISIBLE);
                                                            signView2.setVisibility(View.VISIBLE);
                                                            //timer2.setText(String.valueOf(tim2));
                                                            signView2.setImageResource(R.drawable.gap_06);

                                                        }else if (signselect==1){
                                                            //timer1.setVisibility(View.VISIBLE);
                                                            signView1.setVisibility(View.VISIBLE);
                                                            //timer1.setText(String.valueOf(tim2));
                                                            signView1.setImageResource(R.drawable.gap_02);
                                                        }else {
                                                            //timer2.setVisibility(View.VISIBLE);
                                                            signView2.setVisibility(View.VISIBLE);
                                                            //timer2.setText(String.valueOf(tim2));
                                                            signView2.setImageResource(R.drawable.gap_06);
                                                        }

                                                    }
                                                    //ConvertTextToSpeech("T Junction");
                                                }
                                                else if(dec.substring(0,2).equals("EM")) {

                                                    signView1.setVisibility(View.INVISIBLE);
                                                    signView2.setVisibility(View.INVISIBLE);
                                                    signView3.setVisibility(View.INVISIBLE);
//                                                    timer1.setVisibility(View.INVISIBLE);
//                                                    timer2.setVisibility(View.INVISIBLE);
//                                                    timer3.setVisibility(View.INVISIBLE);

                                                    signTxt.setText("Emergency Sign");
                                                    //System.out.println("DEC::"+dec);
                                                    String emergencymessage = dec.substring(2,3);
                                                    int l = obj.indexOf(emergencymessage)+11;
                                                    emergencymessage=obj.get(l);
                                                    //System.out.println("l:"+l +" M:::"+emergencymessage);
                                                    if(emergencymessage.equals("1")){
                                                        signTxt.setText("EM: Lane Merger");

                                                        signView1.setVisibility(View.VISIBLE);
                                                        signView1.setImageResource(R.drawable.em00);
                                                    }
                                                    else if(emergencymessage.equals("2")){
                                                        signTxt.setText("EM: Road Flood");
                                                        signView1.setVisibility(View.VISIBLE);
                                                        signView1.setImageResource(R.drawable.em01);
                                                    }
                                                    else if(emergencymessage.equals("3")){
                                                        signTxt.setText("EM: Road Fire");
                                                        signView1.setVisibility(View.VISIBLE);
                                                        signView1.setImageResource(R.drawable.em02);
                                                    }
                                                    else if(emergencymessage.equals("4")){
                                                        signTxt.setText("EM: Road Block");
                                                        signView1.setVisibility(View.VISIBLE);
                                                        signView1.setImageResource(R.drawable.em03);
                                                    }
                                                    else if(emergencymessage.equals("5")){
                                                        signTxt.setText("EM: Oil Leak");
                                                        signView1.setVisibility(View.VISIBLE);
                                                        signView1.setImageResource(R.drawable.em04);
                                                    }

                                                }
                                                else{
                                                    String dec_s = dec.toLowerCase();
                                                    dec_s = dec_s.replace('-', '_');
                                                    dec_s = dec_s.replace(" ", "");
                                                    signTxt.setText(signList.get(dec_s));
                                                    signView1.setVisibility(View.VISIBLE);
                                                    signView2.setVisibility(View.GONE);
                                                    signView3.setVisibility(View.GONE);
//                                                    timer1.setVisibility(View.GONE);
//                                                    timer2.setVisibility(View.GONE);
//                                                    timer3.setVisibility(View.GONE);
                                                    if(drawableList.get(dec_s)!=null) {
                                                        signView1.setImageResource(drawableList.get(dec_s));
                                                    }else{
                                                        signView1.setVisibility(View.INVISIBLE);
                                                    }
                                                    ConvertTextToSpeech(signList.get(dec_s));
                                                }

                                                /*
                                                 *
                                                 * Distance based on GPS and RSSI value (Need to generate a function)
                                                 * Current Function> distance = (RSSI in dBm)**2 * 0.01
                                                 */

                                                double distance = Math.pow(Double.valueOf(rssi_val),2)*0.01;
                                                distanceTxt.setText(String.valueOf((int)distance)+"m Ahead");

                                                //prevbeacontime=beacontime;
                                            }
                                        }
                                    }

                                    /**
                                     * Checks if a sign was found, if not reset all the labels
                                     */
                                    if (scanfound==1) {
                                        //scanDetails.setText("Found a nearby Road Signal");
                                        //Toast.makeText(DriverMapActivity.this, "Found a nearby Road Signal", Toast.LENGTH_SHORT).show();
                                    }else {
                                        //scanDetails.setText("Scanning for Road Signals");
                                        //Toast.makeText(DriverMapActivity.this, "Found a nearby Road Signal", Toast.LENGTH_SHORT).show();
                                        double d= SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),lastSignLocation);
                                        System.out.println("Print Distance: "+d);
                                        if(d>350) {
                                            distanceTxt.setText("No sign found");
                                            signView2.setVisibility(View.INVISIBLE);
                                            signView3.setVisibility(View.INVISIBLE);
                                            signView1.setVisibility(View.INVISIBLE);
//                                            timer1.setVisibility(View.INVISIBLE);
//                                            timer2.setVisibility(View.INVISIBLE);
//                                            timer3.setVisibility(View.INVISIBLE);
                                            signTxt.setText("Road Sign Details");
                                            signLayout.setBackgroundResource(0);
                                        }

                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                } catch (ConcurrentModificationException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                //scanSuccess();
                            } else {
                                // scan failure handling
                                //scanFailure();
                                System.out.println("Scan failed");
                            }
                        }
                    };

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                    getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
                    boolean success = wifiManager.startScan();

                    if(!success){
                        System.out.println("Scan failure");
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

//        ShowImage.start();
//        ShowImage2.start();
        task.start();
//        Logout.start();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME,
                    MIN_DISTANCE, this);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME,
                    MIN_DISTANCE, this);
        }


    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isLocationServiceAvailable() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    //Compass
    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        double degree = Math.round(event.values[0]);
        compassH = degree;


    }

    //Compass
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



    //Wifi scaning

    private String decryptCFB(String encryptedtext, String key, String iv) throws Exception{


        byte[] bytekey = Base64.decode(String.valueOf(key), Base64.DEFAULT);
        byte[] byteiv = Base64.decode(String.valueOf(iv), Base64.DEFAULT);

        SecretKeySpec keySpec = new SecretKeySpec(bytekey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(byteiv);

        Cipher c = Cipher.getInstance("AES/CFB/NoPadding");
        c.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

        byte[] decodedValue = Base64.decode(String.valueOf(encryptedtext), Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);

        String decryptedValue = new String(decValue);
        decryptedString = Base64.encodeToString(decValue, Base64.DEFAULT);
        return decryptedString;
    }

    private String decrypt(String ssid) {
        String timers="";
        try {
            String encryptedSSID= ssid.substring(0,24);
            timers=ssid.substring(24);
            ssid=decryptCFB(encryptedSSID, key2, iv2);
            ssid = ssid.substring(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        int key = 11;
        String message = "";

        String[] alnum = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "1", "2", "3", "4", "5", "6", "7", "8"};

        String timest = ssid.substring(0,4);
        String lat_new = ssid.substring(4, 10);
        String lon_new = ssid.substring(10, 16);
        String headin = ssid.substring(16,18);

        double n = 0;
        double btimestamp = 0;
        for (int i = 1; i < timest.length(); i++) {
            String a = timest.substring(i, i + 1);
            n = obj.indexOf(a);
            //System.out.println(n);
            if(i==1){
                btimestamp += n * Math.pow(60, 2);
            }
            else {
                btimestamp += n * Math.pow(60, 3 - i);
            }
        }
        beacontime= btimestamp;

        double p = 0;
        double bhead = 0;
        for (int i = 0; i < headin.length(); i++) {
            String a = headin.substring(i, i + 1);
            p = obj.indexOf(a);
            bhead += p * Math.pow(60, 1 - i);
        }
        beaconhead = bhead;

        ArrayList<String> obj = new ArrayList<String>(Arrays.asList(alnum));
        String lat_new_d = lat_new.substring(0, 2);
        String lat_new_b = lat_new.substring(2, 6);
        double m = 0;
        double lat_i = 0;
        for (int i = 0; i < lat_new_d.length(); i++) {
            String a = lat_new_d.substring(i, i + 1);
            m = obj.indexOf(a);
            lat_i += m * Math.pow(60, 1 - i);
        }
        double lat_f = 0;
        for (int i = 0; i < lat_new_b.length(); i++) {
            String a = lat_new_b.substring(i, i + 1);
            lat_f += Math.pow(60, 3 - i) * obj.indexOf(a);
        }
        lat_f = lat_f * 0.0000001;

        String lon_new_d = lon_new.substring(0, 2);
        String lon_new_b = lon_new.substring(2, 6);
        double lon_i = 0;
        for (int i = 0; i < lon_new_d.length(); i++) {
            String a = lon_new_d.substring(i, i + 1);
            lon_i += (Math.pow(60, 1 - i)) * obj.indexOf(a);
        }
        double lon_f = 0;
        for (int i = 0; i < lon_new_b.length(); i++) {
            String a = lon_new_b.substring(i, i + 1);
            lon_f += (Math.pow(60, 3 - i)) * obj.indexOf(a);
        }
        lon_f = lon_f * 0.0000001;

        double sign_lat = lat_i + lat_f;
        double sign_lon = lon_i + lon_f;

        lastSignLocation = new LatLng(sign_lat, sign_lon);


        String signLocTxt= "Lat: " + sign_lat + "\n" + "Lon: " + sign_lon;
        String encrypted = ssid.substring(18);
        char a[] = encrypted.toCharArray();
        //System.out.println(a);
        String symbol_new="";
        for(int i=0; i<encrypted.length();i++){
            int k=obj.indexOf(Character.toString(a[i]))-key;
            if(k<0){
                k=60+k;
            }
            symbol_new+=obj.get(k);
        }
        symbol_new+=timers;

        return symbol_new;
    }
    //    /*On pause text speech object stoped and shutdown the speech
//     * */
//    @Override
//    protected void onPause() {
//        // TODO Auto-generated method stub
//
//        if (tts != null) {
//
//            tts.stop();
//            tts.shutdown();
//        }
//        super.onPause();
//   }

    /*
    getAreaAppendToTemp
    Collect area code from "getMyArea()"
    Collect heading from GeneralHeading"
    if driver is in new area, first delete whole table entries and enter new area data to temporary table
    then move to getColsestRoadSign()
    else driver is in the same area we directly move to the getClosestRoadSign();
    Find the corresponding table match to the area code
    Fetch data from corresponding Table and append to the Temporary database
    */
    private void getAreaAppendToTemp() {
        System.out.println("Here");
        loc();
        int areacode = getMyArea(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        speed = (int) (mLastLocation.getSpeed() * (18 / 5));

        mAngry.setText(String.valueOf(speed));

        Double heading = GeneralHeading;
        mHeading.setText(String.valueOf(heading)); //Display heading angle value
        //updateCameraBearing(mMap, Float.parseFloat(String.valueOf(heading)));

        preLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()); //Update current value to get next direction

        if (areacode != areaprev) {
            table1Db.updateprev("1", String.valueOf(areacode));     //add new area code to the data base
            areaprev = areacode;
            tempTable.deleteAll();                  //delete all entries in temporary database
            Qarea.quarryarea(String.valueOf(areacode));
        }

        Qarea.quarryarea(String.valueOf(areacode));
        getClosestRoadSign(heading, areacode);
    }


    /*
    getMyArea
    First we collect our Longitude and Latitude
    Then we compare our coordinates with area coordinates which given in Area Database
    Area code added to the global variable "slot"
    Return Integer "slot" value
    */
    private int getMyArea(double lati, double longi) {

        if (lati < 10.065380 & lati > 5.904049) {
            if (longi > 79.563740 & longi < 81.939274) {
                slot = 21;
            } else slot = 2;
        } else slot = 2;
        return slot;
    }

//    /* This is the original function
//   getHeading
//   Within this method we use accelerometerReading and magnetometerReading to find our heading
//   Return double "heading"
//   */
//    private Double parseHeading() {
//        LatLng CurrentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
//        Double head = SphericalUtil.computeHeading(preLocation,CurrentLocation);
//        if (head < 0) {
//            return head + 360;
//        } else return head;
//    }
//
//    public Double getHeading () {
//        Double hd = 0.0;
//        if(speed < 10){
//            if(PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS) == true){
//                hd = compassH;
//            }else{
//                hd = parseHeading();
//            }
//        }else{
//            hd = parseHeading();
//        }
//
//        if (abs(htm2 - htm1)<20 || abs(htm2 - htm1)>340) {
//            if (abs(htm1 -hd)<20 || abs(hd - htm1)>340) {
//                h = hd;
//            } else {
//                htm2 = htm1;
//                htm1 = hd;
//
//                }
//            } else {
//                htm2 = htm1;
//                htm1 = hd;
//            }
//        return h;
//    }


    /*
 getHeadingLocation
 Within this method we use accelerometerReading and magnetometerReading to find our heading
 Return double "heading"
 */
    private Double parseHeadingL() {
        LatLng CurrentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Double head = SphericalUtil.computeHeading(preLocation, CurrentLocation);
        if (head < 0) {
            return head + 360;
        } else return head;
    }

    public Double getHeadingL() {
        if (speed < 10) {
            if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)) {
                hL = compassH;
                headiL = hL;
                htm2L = htm1L;
                htm1L = hL;
            } else {
                hL = filterHeadL(parseHeadingL());
            }
        } else {
            hL = filterHeadL(parseHeadingL());
        }
        return hL;
    }

    public Double filterHeadL(double hd) {

        if (abs(htm2L - htm1L) < 20 || abs(htm2L - htm1L) > 340) {
            if (abs(htm1L - hd) < 20 || abs(hd - htm1L) > 340) {
                headiL = hd;
            } else {
                htm2L = htm1L;
                htm1L = hd;

            }
        } else {
            htm2L = htm1L;
            htm1L = hd;
        }
        return headiL;
    }


    /*
        getClosestRoadSign
        In this section we calculate the distance to each road sign in temporary table and store in the table.
        Then compare each and get closest road sign
        if the distance to the road sign is greater than 10m , send it to dispaly
    */
    private void getClosestRoadSign(Double heading, Integer area) {

        float lowestdistance = 100;
        float lowestdistance2 = 100;
        RoadSign targetSign = null;
        RoadSign secondClosestSign = null;

        for (RoadSign roadSign : roadSignList) {

            try {
                targetLocation.setLatitude(parseDouble(roadSign.getLatitude()));       //your co-ords of course
                targetLocation.setLongitude(parseDouble(roadSign.getLongitude()));
                targetSignLocation = new LatLng(parseDouble(roadSign.getLatitude()),parseDouble(roadSign.getLongitude()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            float distanceInMeters = mLastLocation.distanceTo(targetLocation);
            currentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            Double headingToSign = SphericalUtil.computeHeading(currentLocation,targetSignLocation);

            if (headingToSign < 0) {
                headingToSign =  headingToSign + 360;
            }

            if (distanceInMeters <= roadSign.getDistance())
            {                                     //Compare new distance to the RS with previous distance
                if ((abs(heading - headingToSign) < 89) ||
                    (abs(heading - headingToSign) > 271))
                {
                    if ((abs(heading - roadSign.getHeading()) < 45) ||
                            (abs(heading - roadSign.getHeading()) > 315))
                    {
                        if(distanceInMeters < lowestdistance && distanceInMeters > 5.0)
                        {
                            System.out.println("yahooooooooooooooooooooooooo");
                            targetSign = roadSign;
                            lowestdistance = distanceInMeters;
                        }else if (lowestdistance < distanceInMeters && distanceInMeters < 100)
                        {
                            if(distanceInMeters < lowestdistance2 && distanceInMeters > 5.0)
                            {
                                System.out.println("yahooooooooooooooooooooooooo2222222222222");
                                secondClosestSign = roadSign;
                                lowestdistance2 = distanceInMeters;
                            }

                        }
                    }
                }
            }

            roadSign.setDistance(distanceInMeters);
        }

        if (lowestdistance < 100) {
            distanceTxt.setText(lowestdistance + "m Ahead D");
            mTime.setText(targetSign.getName());

        } else {
            distanceTxt.setText("More than 100m Ahead D");
        }

        //mSignImage1.setImageResource(images[Temp_sign]);


        String targetSignName = (targetSign != null) ? targetSign.getName() : null;

        if (targetSignName != null && (drawableList.get(targetSignName) != null)) {
            RoadSignToShow = targetSignName;
            signTxt.setText(signList.get(targetSignName));
            if (targetSign.getId() != tempID) {
                Log.i("OH Id changed",String.valueOf(targetSign.getId()));
                if (audio == 1) {
                    ConvertTextToSpeech(signList.get(targetSignName));
                    tempID = targetSign.getId();
                }
            }

        } else {
            RoadSignToShow = "nosign";
        }
        mSignImage2.setImageResource(drawableList.get(RoadSignToShow));


        if ((secondClosestSign != null) && (secondClosestSign.getName() != null) &&
                (drawableList.get(secondClosestSign.getName()) != null))
        {
            tempID2 = secondClosestSign.getId();
            if (!tempID2.equals(tempID)) {
                RoadSignToShow2 = secondClosestSign.getName();
            }
        } else {
            RoadSignToShow2 = "nosign";
        }

        mSignImage1.setImageResource(drawableList.get(RoadSignToShow2));


    }

    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!isLocationServiceAvailable()) {
            buildAlertMessage("Enable Location Service", MobileServiceEnum.LOCATION, false);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17)); //zoom level
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
        table1Db.close();
        tempTable.close();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest(); // Requesting Location
        mLocationRequest.setInterval(1000); // Location updating interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Highest accuracy for loading location


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void ConvertTextToSpeech(String RoadSign) {
        // TODO Auto-generated method stub

        if (!Objects.equals(preShout, RoadSign)) {
            Shout = RoadSign;
            preShout = RoadSign;
        } else {
            Shout = "";
        }

        if (audio == 1) {
            if (Shout == null || "".equals(Shout)) {
                Shout = "";
            } else {
                tts.speak(Shout, TextToSpeech.QUEUE_FLUSH, null);
                System.out.println("Shout " + Shout);
            }
        } else System.out.println("Shut up please " + Shout);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        if (!isLocationServiceAvailable()) {
            buildAlertMessage("Turn on GPS", MobileServiceEnum.LOCATION, false);
        }
        if (!isNetworkAvailable()) {
            buildAlertMessage("Turn on Internet", MobileServiceEnum.INTERNET, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void buildAlertMessage(String message, final MobileServiceEnum serviceType, boolean cancelable) {
        AlertDialog alertDialog = new AlertDialog.Builder(DriverMapActivity.this)
                .setCancelable(cancelable)
                .create();
        alertDialog.setTitle("Cannot Load Results");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (serviceType == MobileServiceEnum.LOCATION
                                && !isLocationServiceAvailable()) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void loc() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME,
                    MIN_DISTANCE, this);

        }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME,
                    MIN_DISTANCE, this);
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17)); //zoom level
        GeneralHeading = getHeadingL();
        updateCameraBearing(mMap,(float) GeneralHeading);
        GeneranHeading2 = GeneralHeading;
        preLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()); //Update current value to get next direction

        Log.i("On Location", String.valueOf(latLng.latitude)
                +","+ String.valueOf(latLng.longitude)  + " Thread "+ Thread.currentThread().getId());

        getAreaAppendToTemp();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private enum MobileServiceEnum {LOCATION, INTERNET}
}

