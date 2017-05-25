package com.jak_reed.www.a618_mobile_app;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterAccount extends AppCompatActivity {

    public VideoView backgroundVideo;
    public Button registerActButton, pickPhotoBtn;
    public EditText nameEditText, emailEditText;
    public TextInputLayout passwordLayout, confirmPasswordLayout;
    public EditText passwordEditText, confPasswordEditText;
    public ImageView profilePic;
    private ProgressDialog progressDialog;

    private static final String TAG = "REGISTER_ACT";
    private boolean hasImageChanged = false; // Has imaged changed for checking?

    // Firebase variables and references needed
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference mProfilePicRef;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        // Configure the storage and database
        mRef = FirebaseDatabase.getInstance().getReference().child("users");


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

        profilePic.setDrawingCacheEnabled(true);

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
                progressDialog = ProgressDialog.show(RegisterAccount.this, "Creating Account", "Creating your account.", true);

                final String name = nameEditText.getText().toString().trim();
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();
                final String confPass = confPasswordEditText.getText().toString().trim();

                if (verifyEmail(email)){
                    if(verifyPassword(password, confPass)){
                        if (verifyName(name)){
                            if (hasImageChanged){
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(RegisterAccount.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "createUserWithEmail:success");
                                                    writeUserToDBOnSuccess(name, email);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                                        Toast.makeText(RegisterAccount.this,
                                                                "User with this email already exist.", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                progressDialog.dismiss();
                            }
                        } else {
                            progressDialog.dismiss();
                        }
                    } else {
                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
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
                    hasImageChanged = true;
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Log.d(TAG, "::ERROR_CROPPING_IMAGE::"+result.getError());
                    Toast.makeText(RegisterAccount.this, "Error Cropping Image", Toast.LENGTH_LONG).show();
                }
                break;
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

    private void writeUserToDBOnSuccess(final String name, final String email){
        // Sign in success, update UI with the signed-in user's information
        FirebaseUser user = mAuth.getCurrentUser();

        mProfilePicRef = mStorage.getReference("profile-pictures/"+user.getUid());
        profilePic.buildDrawingCache();

        Bitmap profImageBM = profilePic.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profImageBM.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] profPicData = baos.toByteArray();

        UploadTask uploadTask = mProfilePicRef.putBytes(profPicData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();

                Log.d(TAG, "::UPLOAD_FAIL");
                Toast.makeText(RegisterAccount.this, "Error Registering", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            @SuppressWarnings("VisibleForTests")
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                HashMap<String, String> userDataMap = new HashMap<String, String>();
                FirebaseUser user = mAuth.getCurrentUser();
                Uri profilePicUrl = taskSnapshot.getDownloadUrl();

                Log.d(TAG, "::UPLOAD_SUCC");
                Log.d(TAG, "DOWNLOAD_URL"+profilePicUrl.toString());

                userDataMap.put("name", name);
                userDataMap.put("email", email);
                userDataMap.put("profilePictureUrl", profilePicUrl.toString());

                mRef.child(user.getUid()).setValue(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterAccount.this, "Account Created!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private boolean verifyEmail(String emailToVerify){
        if (TextUtils.isEmpty(emailToVerify)){
            Toast.makeText(RegisterAccount.this,
                    "Email field cannot be empty.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if(Patterns.EMAIL_ADDRESS.matcher(emailToVerify).matches()){
                return true;
            } else {
                Toast.makeText(RegisterAccount.this,
                        "Email must be the correct format", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }

    private boolean verifyName(String nameToVerify){
        Pattern pattern;
        Matcher matcher;

        final String NAME_PATTERN = "^([A-Z][a-z]*)+[\\s]*+([A-Z][a-z]*)*$";
        pattern = Pattern.compile(NAME_PATTERN);
        matcher = pattern.matcher(nameToVerify);

        if (TextUtils.isEmpty(nameToVerify)){ //
            Toast.makeText(RegisterAccount.this, "Name field cannot be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (matcher.matches()){
                return true;
            } else {
                Toast.makeText(RegisterAccount.this, "Name field can only consist of letters.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }

    private boolean verifyPassword(String password, String confirmPassword){
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)" +
                "(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        if (!matcher.matches()){
            Toast.makeText(RegisterAccount.this,
                    "Password must contain at least one uppercase letter, One special " +
                            "character ($@$!%*?&), one number, and be 8 characters long.",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (password.equals(confirmPassword)){
                return true;
            } else {
                Toast.makeText(RegisterAccount.this,
                        "Passwords do not match.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
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
