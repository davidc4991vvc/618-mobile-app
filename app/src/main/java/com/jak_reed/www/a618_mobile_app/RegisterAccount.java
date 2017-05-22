package com.jak_reed.www.a618_mobile_app;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import org.w3c.dom.Text;

public class RegisterAccount extends AppCompatActivity {

    public VideoView backgroundVideo;
    public Button registerActButton, pickPhotoBtn;
    public EditText nameEditText, emailEditText;
    public TextInputLayout passwordLayout, confirmPasswordLayout;
    public EditText passwordEditText, confPasswordEditText;
    public ImageView profilePic;
    private static final int READ_EXT_STO_CD = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        backgroundVideo = (VideoView) findViewById(R.id.background_video);
        profilePic = (ImageView) findViewById(R.id.profile_picture);
        pickPhotoBtn = (Button) findViewById(R.id.edit_profile_picture);
        registerActButton = (Button) findViewById(R.id.create_account);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_edit_text);
        confirmPasswordLayout = (TextInputLayout) findViewById(R.id.confirm_password_edit_text);
        passwordEditText = passwordLayout.getEditText();
        confPasswordEditText = confirmPasswordLayout.getEditText();

        pickPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });

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
        startActivity(new Intent(this, LoginWithEmail.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0){
            if (resultCode == RESULT_OK){
                Uri imageSelected = intent.getData();
                profilePic.setImageURI(imageSelected);
            }
        } else if (requestCode == 1){
            if (ContextCompat.checkSelfPermission(RegisterAccount.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterAccount.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (resultCode == RESULT_OK){
                        Uri imageSelected = intent.getData();
                        profilePic.setImageURI(imageSelected);
                    }
                } else {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(RegisterAccount.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXT_STO_CD);
                    if (resultCode == RESULT_OK){
                        Uri imageSelected = intent.getData();
                        profilePic.setImageURI(imageSelected);
                    }
                }
            }
        }
    }

}
