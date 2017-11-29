package fudi.fudimap;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Button save;
    Button picture_btn;
    ListView listview = null;
    ListViewAdapter adapter;
    static DBManager dbManager;
    private final int REQ_CODE_GALLERY = 100;
    static byte[] food;
    static byte[][] foodpicture_save;
    private GoogleMap mMap;
    static double longi;
    static double lati;
    SQLiteDatabase db;
    static int i = 0;
    int cnt = 0;
    ArrayList<Story> story = new ArrayList<Story>();
    StoryAdapter Story_adapter;
    private GpsInfo gps;

    public MapsActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //DBManager객체 생성
        dbManager = new DBManager(getApplicationContext(), "Food.db", null, 1);

        // 화면을 portrait 세로화면으로 고정
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        save = (Button) findViewById(R.id.save);
        picture_btn = (Button) findViewById(R.id.registerpicture);

        // Adapter 생성, 리스트뷰 참조 및 Adapter달기
        adapter = new ListViewAdapter() ;
        listview = (ListView) findViewById(R.id.foodlist);
        listview.setAdapter(adapter);

        EditText editTextFilter = (EditText)findViewById(R.id.editTextFilter);
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) {
                String filterText = edit.toString();
                if (filterText.length() > 0) {
                    listview.setFilterText(filterText);
                } else {
                    listview.clearTextFilter();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        foodpicture_save = new byte [30][];

        //Tab 메뉴바 생성
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("맛집 지도");

        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("스토리");
        spec2.setContent(R.id.tab2);

        TabHost.TabSpec spec3 = tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("리스트");
        spec3.setContent(R.id.tab3);

        TabHost.TabSpec spec4 = tabHost.newTabSpec("Tab 4");
        spec4.setIndicator("맛집 추가");
        spec4.setContent(R.id.tab4);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);

        //db접근 및 테이블 지정
        db = dbManager.getReadableDatabase();
        Cursor eateryCursor =db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);
        Cursor pictureCursor = db.rawQuery("SELECT _id, food_id, picture FROM FOOD_PICTURE", null);

        //데이터 베이스로 부터 불러온 맛집리스트 목록 출력
        while(eateryCursor.moveToNext()) {
            //리스트뷰 초기화
            int eatery_key = eateryCursor.getInt(0);
            String eatery_title = eateryCursor.getString(1);
            String eatery_category = eateryCursor.getString(2);
            String eatery_memo = eateryCursor.getString(3);
            String eatery_date = eateryCursor.getString(4);
            double initLatitute = eateryCursor.getDouble(5);
            double initLongitute = eateryCursor.getDouble(6);

            while(pictureCursor.moveToNext()){
                if(pictureCursor.getInt(1) == eatery_key){
                    byte[] food_image = pictureCursor.getBlob(2);
                    Bitmap b = byteArrayToBitmap(food_image);
                    story.add(new Story(eatery_date,eatery_title, b, eatery_memo));
                }
            }
            pictureCursor.moveToFirst();
            pictureCursor.moveToPrevious();



            if (eatery_category.equals("한식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.korean_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("중식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.chinese_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("일식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.japanese_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("양식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.wastern_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("카페"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.cafe), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("고기"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.meat), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("분식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.snack), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("술집"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.drink), eatery_title, eatery_category, eatery_key);
        }

        Story_adapter = new StoryAdapter(getApplicationContext(), R.layout.story, story);
        ListView lv = (ListView)findViewById(R.id.storylistView);
        lv.setAdapter(Story_adapter);

        eateryCursor.close();
        pictureCursor.close();

        // 맛집리스트 클릭시 맛집에 대한 화면으로 이동
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListItem item = (ListItem) parent.getItemAtPosition(position) ;
                Intent intent = new Intent(getApplicationContext(), FoodListViewActivity.class);
                intent.putExtra("itemi", item.getId());
                startActivity(intent);

                // TODO : use item data.
            }
        }) ;

        //롱클릭시 데이터 삭제
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {//길게 클릭했을 때
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, final long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("삭제");
                alert.setMessage("이 리스트를 삭제하시겠습니까?");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();

                        long get_id;
                        get_id = adapter.getItemId(position);

                        //DB delete하는 명령, 리스트뷰 갱신
                        dbManager.delete("delete from FOOD where _id = '" + get_id + "';");
                        dbManager.delete("delete from FOOD_PICTURE where food_id = '" + get_id + "';");

                        adapter.deleteItem(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                alert.show();
                return false;
            }
        });

    }


    //식당에 대한 정보 데이타 베이스에 저장해주는 함수
    public void onclickedsave(View v)
    {
        int food_id=0;

        final EditText editTitle = (EditText) findViewById(R.id.title);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        final EditText editMemo = (EditText) findViewById(R.id.grade);

        String title = editTitle.getText().toString();
        String spinnertext = spinner.getSelectedItem().toString();
        String memo = editMemo.getText().toString();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        if(!title.equals("")){
            dbManager.insert("insert into FOOD values(null, '" + title + "', '" + spinnertext + "', '" +memo + "', '" + currentDateTimeString + "' , "+lati+", "+longi+");");

            db = dbManager.getReadableDatabase();
            Cursor eateryCursor =db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);

            eateryCursor.moveToLast();
            food_id = eateryCursor.getInt(0);

            int eatery_key = eateryCursor.getInt(0);
            String eatery_title = eateryCursor.getString(1);
            String eatery_category = eateryCursor.getString(2);
            String eatery_memo = eateryCursor.getString(3);
            String eatery_date = eateryCursor.getString(4);

            if (eatery_category.equals("한식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.korean_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("중식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.chinese_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("일식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.japanese_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("양식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.wastern_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("카페"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.cafe), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("고기"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.meat), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("분식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.snack), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("술집"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.drink), eatery_title, eatery_category, eatery_key);

            adapter.notifyDataSetChanged();

            eateryCursor.close();

            for(int j=0; j<30;j++) {
                if(foodpicture_save[j] == null) {
                    break;
                }

                else if(foodpicture_save[j] != null) {
                    dbManager.insert(food_id, foodpicture_save[j]);
                    byte[] food_image = foodpicture_save[j];
                    Bitmap b = byteArrayToBitmap(food_image);
                    story.add(new Story(eatery_date,eatery_title, b, eatery_memo));
                    Toast.makeText(getApplicationContext(), "사진 db에 저장됨", Toast.LENGTH_SHORT).show();
                }
            }

            editTitle.setText("");
            editMemo.setText("");
            spinner.setSelection(0);

            i = 0;

            for(int i=0;i<30;i++) {
                if(foodpicture_save[i] != null) {
                    foodpicture_save[i] = null;
                }

                else{
                    break;
                }
            }
            food = null;

            //마커객체 생성
            MarkerOptions optSecond = new MarkerOptions();
            optSecond.position(new LatLng(lati, longi));// 위도 • 경도
            optSecond.title(title); // 제목 미리보기
            mMap.addMarker(optSecond).showInfoWindow();
        }

    }

    //gallery에서 사진을 선택하여 불러올 수 있게 해주는 함수
    public void onclickedpicture(View v) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, REQ_CODE_GALLERY);
        //Toast.makeText(getApplicationContext(), "onclickedpicture", Toast.LENGTH_SHORT).show();
    }

    //불러온 사진을 비트맵으로 구성후 byteArray에 저장하여 데이타 베이스에 저장해주는 함수
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100 , stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    //데이타 베이스에 byteArray형태로 저장된 사진을 다시 bitmap으로 변환해주는 함수
    public Bitmap byteArrayToBitmap(byte[] byteArray){
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }

    //이미지 오른쪽으로 90도 회전해주는 함수
    private Bitmap imgRotate(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        bmp.recycle();

        return resizedBitmap;
    }


    //겔러리로부터 이미지 경로를 받아와 비트맵을 byteArray로 전환후 db에 저장
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

        if(requestCode == REQ_CODE_GALLERY){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();

                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap resized = null;
                    int height = bm.getHeight();
                    int width = bm.getWidth();
                    resized = Bitmap.createScaledBitmap(bm, width/4, height/4, true);
                    if(height > 2000){
                        resized = imgRotate(resized);
                        food = bitmapToByteArray(resized);
                    }
                    else{
                        food = bitmapToByteArray(bm);
                    }

                    foodpicture_save[i] = food;
                    Toast.makeText(getApplicationContext(), "사진이 추가 되었습니다.", Toast.LENGTH_SHORT).show();
                    i++;
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "한번에 추가 할 수 있는 사진의 갯수를 초과했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gps = new GpsInfo(MapsActivity.this);

        if (gps.isGetLocation()) {

            lati = gps.getLatitude();
            longi = gps.getLongitude();

            Toast.makeText(getApplicationContext(), "당신의 위치 - \n위도: " + lati + "\n경도: " + longi, Toast.LENGTH_LONG).show();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }

        db = dbManager.getReadableDatabase();
        Cursor eateryCursor =db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);

        //데이터 베이스로 부터 불러온 맛집리스트 목록 출력
        while(eateryCursor.moveToNext()) {
            String eatery_title = eateryCursor.getString(1);
            double initLatitute = eateryCursor.getDouble(5);
            double initLongitute = eateryCursor.getDouble(6);
            //마커객체 생성
            MarkerOptions optSecond = new MarkerOptions();
            optSecond.position(new LatLng(initLatitute, initLongitute));// 위도 • 경도
            optSecond.title(eatery_title); // 제목 미리보기
            mMap.addMarker(optSecond).showInfoWindow();
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //위치값이 갱신되면 위도와 경도를 갱신
            longi = location.getLongitude(); //경도
            lati = location.getLatitude();   //위도
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
        }
    };

    private void showCurrentLocation(final Double latitude, final Double longitude) {

        LatLng curPoint = new LatLng(latitude, longitude);

        if(cnt == 0) {
            //마커객체 생성
            MarkerOptions optSecond = new MarkerOptions();
            optSecond.position(new LatLng(latitude, longitude));// 위도 • 경도
            optSecond.title("현재위치"); // 제목 미리보기
            mMap.addMarker(optSecond).showInfoWindow();
            cnt++;
        }

        //currentPosition 위치로 카메라 중심을 옮기고 화면 줌을 조정한다. 줌범위는 2~21, 숫자클수록 확대
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

        // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public class GpsInfo extends Service implements LocationListener {

        private final Context mContext;

        // 현재 GPS 사용유무
        boolean isGPSEnabled = false;

        // 네트워크 사용유무
        boolean isNetworkEnabled = false;

        // GPS 상태값
        boolean isGetLocation = false;

        Location location;
        double lat; // 위도
        double lon; // 경도

        // 최소 GPS 정보 업데이트 거리 10미터
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

        // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
        private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1;

        protected LocationManager locationManager;

        public GpsInfo(Context context) {
            this.mContext = context;
            getLocation();
        }



        public Location getLocation() {
            try {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // GPS 정보 가져오기
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // 현재 네트워크 상태 값 알아오기
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
                } else {
                    this.isGetLocation = true;
                    // 네트워크 정보로 부터 위치값 가져오기
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                // 위도 경도 저장
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                    }

                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    lat = location.getLatitude();
                                    lon = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return location;
        }

        /**
         * GPS 종료
         * */
        public void stopUsingGPS(){
            if(locationManager != null){
                locationManager.removeUpdates((LocationListener) GpsInfo.this);
            }
        }

        /**
         * 위도값을 가져옵니다.
         * */
        public double getLatitude(){
            if(location != null){
                lat = location.getLatitude();
            }
            return lat;
        }

        /**
         * 경도값을 가져옵니다.
         * */
        public double getLongitude(){
            if(location != null){
                lon = location.getLongitude();
            }
            return lon;
        }

        /**
         * GPS 나 wife 정보가 켜져있는지 확인합니다.
         * */
        public boolean isGetLocation() {
            return this.isGetLocation;
        }

        /**
         * GPS 정보를 가져오지 못했을때
         * 설정값으로 갈지 물어보는 alert 창
         * */
        public void showSettingsAlert(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            alertDialog.setTitle("GPS 사용유무셋팅");
            alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다.\n 설정창으로 가시겠습니까?");

            // OK 를 누르게 되면 설정창으로 이동합니다.
            alertDialog.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mContext.startActivity(intent);
                        }
                    });
            // Cancle 하면 종료 합니다.
            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            showCurrentLocation(latitude, longitude);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }
    }




    //맛집리스트 update를 대비하여, restart시 리스트뷰를 갱신
    @Override
    protected void onRestart(){
        super.onRestart();
        adapter = new ListViewAdapter();
        listview = (ListView) findViewById(R.id.foodlist);
        listview.setAdapter(adapter);

        //스토리 adapter 구현
        Story_adapter = new StoryAdapter(getApplicationContext(), R.layout.story, story);
        ListView lv = (ListView)findViewById(R.id.storylistView);
        lv.setAdapter(Story_adapter);
        story = new ArrayList<Story>();

        db = dbManager.getReadableDatabase();
        Cursor eateryCursor2 =db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);
        Cursor pictureCursor2 = db.rawQuery("SELECT _id, food_id, picture FROM FOOD_PICTURE", null);
        //데이터 베이스로 부터 불러온 맛집리스트 목록 출력
        while(eateryCursor2.moveToNext())
        {

            //리스트뷰 초기화
            int eatery_key = eateryCursor2.getInt(0);
            String eatery_title = eateryCursor2.getString(1);
            String eatery_category = eateryCursor2.getString(2);
            String eatery_memo = eateryCursor2.getString(3);
            String eatery_date = eateryCursor2.getString(4);


            while(pictureCursor2.moveToNext()){
                if(pictureCursor2.getInt(1) == eatery_key){
                    byte[] food_image = pictureCursor2.getBlob(2);
                    Bitmap b = byteArrayToBitmap(food_image);
                    story.add(new Story(eatery_date,eatery_title, b, eatery_memo));
                }
            }
            pictureCursor2.moveToFirst();
            pictureCursor2.moveToPrevious();


            if (eatery_category.equals("한식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.korean_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("중식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.chinese_food), eatery_title, eatery_category, eatery_key);
                else if (eatery_category.equals("일식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.japanese_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("양식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.wastern_food), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("카페"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.cafe), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("고기"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.meat), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("분식"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.snack), eatery_title, eatery_category, eatery_key);
            else if (eatery_category.equals("술집"))
                adapter.addItem(ContextCompat.getDrawable(this, R.drawable.drink), eatery_title, eatery_category, eatery_key);
        }

        eateryCursor2.close();
    }
}
