package fudi.fudimap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager extends SQLiteOpenHelper {


    public DBManager(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);
        db.execSQL("CREATE TABLE FOOD( _id INTEGER PRIMARY KEY AUTOINCREMENT,  name VARCHAR(30), memo TEXT);");
        db.execSQL("CREATE TABLE FOOD_CATEGORY( _id INTEGER PRIMARY KEY AUTOINCREMENT, food_id INTEGER REFERENCES FOOD(_id) on delete cascade, category VARCHAR(30));");
        db.execSQL("CREATE TABLE FOOD_MAP(_id INTEGER PRIMARY KEY AUTOINCREMENT, food_id INTEGER REFERENCES FOOD(_id) on delete cascade,lati REAL, longi REAL);");
        db.execSQL("CREATE TABLE FOOD_PICTURE(_id INTEGER PRIMARY KEY AUTOINCREMENT, food_id INTEGER REFERENCES FOOD(_id) on delete cascade, picture BLOB);");
        db.execSQL("CREATE TABLE FOOD_DATE(_id INTEGER PRIMARY KEY AUTOINCREMENT, food_id INTEGER REFERENCES FOOD(_id) on delete cascade,date VARCHAR(30));");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public long getLastId(){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT _id from FOOD order by _id DESC limit 1";
        long lastId=0;
        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            lastId = c.getLong(0); //The 0 is the column index, we only have 1 column, so the index is 0
        }
        db.close();
        return lastId;
    }

    public void insert(int food_id, byte[] picture) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();

        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO FOOD_PICTURE(food_id, picture) VALUES(?,?);",new Object[]{food_id, picture});

        db.close();
    }
}