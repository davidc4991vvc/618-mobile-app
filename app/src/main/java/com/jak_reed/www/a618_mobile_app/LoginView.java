package com.jak_reed.www.a618_mobile_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.net.URI;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginView extends AppCompatActivity {

    public VideoView backgroundVideo;
    public Button loginWithEmail, loginWithFacebook, loginWithGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login_view);

        loginWithEmail = (Button) findViewById(R.id.loginWitthEmail);
        loginWithFacebook = (Button) findViewById(R.id.facebookLogin);
        loginWithGoogle = (Button) findViewById(R.id.googleLogin);
        backgroundVideo = (VideoView) findViewById(R.id.vapeVideoView);

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

        loginWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginView.this, LoginWithEmail.class));
            }
        });
    }
}
