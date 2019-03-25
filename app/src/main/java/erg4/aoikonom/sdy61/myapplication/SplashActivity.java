package erg4.aoikonom.sdy61.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim);
        findViewById(R.id.logo).startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, StorageActivity.class);
                startActivity(intent);
            }
        }, 4000);

    }
}
