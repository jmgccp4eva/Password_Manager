package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity {

    ImageView logo;
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        logo = findViewById(R.id.logo);
        fadeOutAndHideImage(logo);
    }

    private void fadeOutAndHideImage(final ImageView img){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(SPLASH_DISPLAY_LENGTH); // How long it takes for the animation to complete in milliseconds

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation){
                img.setVisibility(View.GONE);

                new Handler().postDelayed(() -> {
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();
                }, 150);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });
        img.startAnimation(fadeOut);
    }
}