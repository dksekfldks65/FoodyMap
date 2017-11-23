package fudi.fudimap;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class FoodListViewActivity extends AppCompatActivity {

    static DBManager dbManager;
    SQLiteDatabase db;
    byte[] food_image=null;
    Bitmap foodBit=null;
    static int id;
    private final int REQ_CODE_GALLERY = 100;
    static byte[][] foodpicture_save;
    Bitmap [] foodBit1;
    Bitmap [] foodBit2;
    int i=0;
    int j= 0;
    Gallery gallery;
    ImageView iv2;
    Button update;
    Button addPicture;
    static byte[][] foodpicture_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list_view);


        TextView title = (TextView) findViewById(R.id.title);
        TextView memo = (TextView) findViewById(R.id.memo);
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);

        update = (Button) findViewById(R.id.update);
        addPicture = (Button) findViewById(R.id.addPicture);

        Intent intent = getIntent();

        //현재 id값 받음
        id = intent.getExtras().getInt("itemi");

        //db열기
        dbManager= new DBManager(getApplicationContext(), "Food.db", null, 1);
        db = dbManager.getReadableDatabase();

        //cursor 지정
        Cursor eateryCursor = db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);
        Cursor pictureCursor = db.rawQuery("SELECT _id, food_id, picture FROM FOOD_PICTURE", null);

        //db에 접근하여 id가 같으면 현재 리스트뷰 내용 화면에 출력
        while(eateryCursor.moveToNext()) {
            if (eateryCursor.getInt(0) == id) {
                title.setText(eateryCursor.getString(1));
                memo.setText(eateryCursor.getString(3));
                //spinner setting
                if(eateryCursor.getString(2).equals("한식"))
                    spinner.setSelection(0);
                else if(eateryCursor.getString(2).equals("중식"))
                    spinner.setSelection(1);
                else if(eateryCursor.getString(2).equals("일식"))
                    spinner.setSelection(2);
                else if(eateryCursor.getString(2).equals("양식"))
                    spinner.setSelection(3);
                else if(eateryCursor.getString(2).equals("카페"))
                    spinner.setSelection(4);
                else if(eateryCursor.getString(2).equals("고기"))
                    spinner.setSelection(5);
                else if(eateryCursor.getString(2).equals("분식"))
                    spinner.setSelection(6);
                else if(eateryCursor.getString(2).equals("술집"))
                    spinner.setSelection(7);
                break;
            }
        }

        foodpicture_save = new byte [100][];
        foodpicture_add = new byte[100][];
        foodBit1 = new Bitmap[100];
        foodBit2 = new Bitmap[100];

        gallery = (Gallery) findViewById(R.id.gallery1);
        iv2 = (ImageView)findViewById(R.id.imageView1);

        while(pictureCursor.moveToNext()){
            if(pictureCursor.getInt(1) == id) {
                gallery.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.VISIBLE);

                foodpicture_save[i] = pictureCursor.getBlob(2);
                foodBit1[i]  = byteArrayToResizedBitmap(foodpicture_save[i]);
                foodBit2[i]  = byteArrayToBitmap(foodpicture_save[i]);
                i++;
            }
        }

        // adapter
        MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.row, foodBit1);

        gallery.setAdapter(adapter);

        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 선택되었을 때 콜백메서드
                iv2.setImageBitmap(foodBit2[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //데이터 저장 및 업데이트 기능 구현
    public void onclickedupdate(View v)
    {
        TextView title = (TextView) findViewById(R.id.title);
        Spinner spinner = (Spinner)findViewById(R.id.spinner1);
        TextView memo = (TextView) findViewById(R.id.memo);

        //커서지정
        Cursor update_eatery_cursor = db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);

        //db에 접근하여 id가 같으면 db수정 후 intent 종료
        while(update_eatery_cursor.moveToNext())
        {
            if(update_eatery_cursor.getInt(0) == id)
            {

                String update_title = title.getText().toString();
                String update_spinner = spinner.getSelectedItem().toString();
                String update_memo = memo.getText().toString();
                String sql1 = "update FOOD set name = '"+update_title+"' where _id = "+id;
                String sql2 = "update FOOD set category = '"+update_spinner+"' where _id = "+id;
                String sql3 = "update FOOD set memo = '"+update_memo+"' where _id = "+id;

                dbManager.update(sql1);
                dbManager.update(sql2);
                dbManager.update(sql3);


            }
        }

        for(int t=0; t<100;t++) {
            if(foodpicture_add[t] == null) {
                break;
            }

            else if(foodpicture_add[t] != null) {
                dbManager.insert(id, foodpicture_add[t]);
                Toast.makeText(getApplicationContext(), "사진 db에 저장됨", Toast.LENGTH_SHORT).show();
            }
        }

        for(int i=0;i<100;i++) {
            if(foodpicture_add[i] != null) {
                foodpicture_add[i] = null;
            }

            else{
                break;
            }
        }
        food_image = null;

        j=0;

        finish();
    }


    //줄인크기로의 비트맵으로 재변환
    public Bitmap byteArrayToResizedBitmap(byte[] byteArray){
        Bitmap bitmap;
        //Toast.makeText(getApplicationContext(), byteArray.length, Toast.LENGTH_LONG).show();
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        Bitmap resized = null;

        while (height > 118) {
            resized = Bitmap.createScaledBitmap(bitmap, (width * 118) / height, 118, true);
            height = resized.getHeight();
            width = resized.getWidth();
        }

        return resized;
    }


    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100 , stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    //비트맵으로 재변환
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

    //gallery에서 사진을 선택하여 불러올 수 있게 해주는 함수
    public void onclickedAddPicture(View v) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, REQ_CODE_GALLERY);
    }


    //겔러리로부터 이미지 경로를 받아와 비트맵을 byteArray로 전환후 db에 저장
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

        if(requestCode == REQ_CODE_GALLERY){
            Toast.makeText(getApplicationContext(), "gallery받음", Toast.LENGTH_SHORT).show();
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(), "result ok임", Toast.LENGTH_SHORT).show();
                Uri uri = data.getData();
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap resized = null;
                    int height = bm.getHeight();
                    int width = bm.getWidth();
                    resized = Bitmap.createScaledBitmap(bm, width/4, height/4, true);

                    if(height > 2000){
                        resized = imgRotate(resized);
                        food_image = bitmapToByteArray(resized);
                    }
                    else{
                        food_image = bitmapToByteArray(bm);
                    }

                    foodpicture_add[j] = food_image;
                    Toast.makeText(getApplicationContext(), "사진이 추가 되었습니다.", Toast.LENGTH_SHORT).show();
                    j++;
                }  catch (Exception e){
                    Toast.makeText(getApplicationContext(), "사진 추가 실패", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override protected void onDestroy() {
        iv2.setImageBitmap(null);
        super.onDestroy();
        }
}
