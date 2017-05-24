package com.jak_reed.www.a618_mobile_app;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.File;

public class RegisterAccount extends AppCompatActivity {

    public VideoView backgroundVideo;
    public Button registerActButton, pickPhotoBtn;
    public EditText nameEditText, emailEditText;
    public TextInputLayout passwordLayout, confirmPasswordLayout;
    public EditText passwordEditText, confPasswordEditText;
    public ImageView profilePic;
    private static final String TAG = "REGISTER_ACT";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        mAuth = FirebaseAuth.getInstance();

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

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vapevidback);

        backgroundVideo.setVideoURI(uri);
        backgroundVideo.start();

        /*
        * LISTENERS TO BE CREATED/ADDED ON ACTIVITY START
        * */

        backgroundVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        pickPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RegisterAccount.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(RegisterAccount.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                } else if (ContextCompat.checkSelfPermission(RegisterAccount.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    startPhotoPickerActivity();
                }
            }
        });
        registerActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confPass = confPasswordEditText.getText().toString();

                if (!(email.equals("")) && !(password.equals("")) && (password.equals(confPass))){
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterAccount.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(RegisterAccount.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(RegisterAccount.this, "Make sure all fields match and the " +
                            "confirmed password matches the entered password", Toast.LENGTH_LONG).show();
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginWithEmail.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "REQ_CD "+requestCode);
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    startImageCrop(intent);
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    startImageCrop(intent);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(intent);
                if(resultCode == RESULT_OK){
                    Uri resultUri = result.getUri();
                    Picasso.with(this.getApplicationContext())
                        .load(resultUri)
                        .resize(150,150)
                        .into(profilePic);
                        backgroundVideo.start();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Log.d(TAG, "::ERROR_CROPPING_IMAGE::"+result.getError());
                    Toast.makeText(RegisterAccount.this, "Error Cropping Image", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPhotoPickerActivity();
                } else {
                    Toast.makeText(RegisterAccount.this,
                            "Permission to select image has been denied.", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // Other permissions can be added.  Will be refactored later if need be.
        }
    }

    private void startPhotoPickerActivity(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
    }

    private void startImageCrop(Intent intent){
        Uri selectedImage = intent.getData();
        CropImage.activity(selectedImage)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
