package com.jak_reed.www.a618_mobile_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginView extends AppCompatActivity {

    private final String TAG = "LOGIN_VIEW";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public VideoView backgroundVideo;
    public Button loginWithEmail, loginWithFacebook, loginWithGoogle;
    public CallbackManager cbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        cbManager = CallbackManager.Factory.create();

        loginWithEmail = (Button) findViewById(R.id.loginWitthEmail);
        loginWithFacebook = (Button) findViewById(R.id.btn_fb_login);
        loginWithGoogle = (Button) findViewById(R.id.googleLogin);
        backgroundVideo = (VideoView) findViewById(R.id.vapeVideoView);

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.vapevidback);

        backgroundVideo.setVideoURI(uri);
        backgroundVideo.start();

        LoginManager.getInstance().registerCallback(cbManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "::SUCCESS_LOGIN");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginView.this, "Log In Canceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "::EXC_THROWN::"+ error.getMessage());
            }
        });

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

        loginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginView.this, Arrays.asList("email", "public_profile"));
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginView.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cbManager.onActivityResult(requestCode, resultCode, data);
    }
}
