package fudi.fudimap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by kyungj on 2017. 10. 6..
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(4000); //대기 초 설정
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
