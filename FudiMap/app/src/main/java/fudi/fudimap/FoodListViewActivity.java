package fudi.fudimap;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

public class FoodListViewActivity extends AppCompatActivity {

    static DBManager dbManager;
    SQLiteDatabase db;
    static byte[] food_image=null;
    Bitmap foodBit=null;
    static int id;

    static byte[][] foodpicture_save;
    Bitmap [] foodBit2;
    static int i=0;

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
    }
}
