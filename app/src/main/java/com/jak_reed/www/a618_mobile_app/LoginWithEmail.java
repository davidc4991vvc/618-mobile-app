package com.jak_reed.www.a618_mobile_app;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class LoginWithEmail extends AppCompatActivity {

    public VideoView backgroundVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_email);

        backgroundVideo = (VideoView) findViewById(R.id.backgroundVideo);

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.vapevidback);

        backgroundVideo.setVideoURI(uri);
        backgroundVideo.start();

        backgroundVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0, 0);
                mp.setLooping(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginView.class));
    }
}
