package fudi.fudimap;

import android.graphics.Bitmap;

/**
 * Created by Kobot on 2017-03-04.
 */

class Story { // 자바빈
    String date = "";
    String title = "";
    Bitmap img; // 이미지
    String memo = "";

    public Story(String date, String title, Bitmap img, String memo) {
        this.date = date;
        this.title = title;
        this.img = img;
        this.memo = memo;
    }

}