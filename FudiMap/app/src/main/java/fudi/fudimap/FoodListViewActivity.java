package fudi.fudimap;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FoodListViewActivity extends AppCompatActivity {

    static DBManager dbManager;
    SQLiteDatabase db;
    static byte[] food_image=null;
    Bitmap foodBit=null;
    static int id;

    static byte[][] foodpicture_save;
    Bitmap [] foodBit2;
    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list_view);


        TextView title = (TextView) findViewById(R.id.title);
        TextView memo = (TextView) findViewById(R.id.memo);
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);

        Intent intent = getIntent();

        //현재 id값 받음
        id = intent.getExtras().getInt("itemi");

        //db열기
        dbManager= new DBManager(getApplicationContext(), "Food.db", null, 1);
        db = dbManager.getReadableDatabase();

        //cursor 지정
        Cursor eateryCursor =db.rawQuery("SELECT _id, name, category, memo, date, lati,longi FROM FOOD", null);
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
        foodBit2 = new Bitmap[100];

        Gallery gallery = (Gallery) findViewById(R.id.gallery1);
        ImageView iv2 = (ImageView)findViewById(R.id.imageView1);


        while(pictureCursor.moveToNext()){
            if(pictureCursor.getInt(1) == id) {
                gallery.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.VISIBLE);

                foodpicture_save[i] = pictureCursor.getBlob(2);
                foodBit2[i]  = byteArrayToBitmap(foodpicture_save[i]);
                i++;
            }
        }

        // adapter
        MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.row, foodBit2);

        // adapterView
        Gallery g = (Gallery)findViewById(R.id.gallery1);
        g.setAdapter(adapter);

        final ImageView iv = (ImageView)findViewById(R.id.imageView1);

        g.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 선택되었을 때 콜백메서드
                iv.setImageBitmap(foodBit2[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //비트맵으로 재변환
    public Bitmap byteArrayToBitmap(byte[] byteArray){
        Bitmap bitmap;
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
}
