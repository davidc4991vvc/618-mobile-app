package com.jak_reed.www.a618_mobile_app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginWithEmail extends AppCompatActivity {

    public VideoView backgroundVideo;
    public Button registerButton, loginButton;
    public EditText email, editTextPassword, input;
    public TextInputLayout password;
    public TextView forgotPass;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    private final String TAG = "LOGIN_WITH_EMAIL_ACT::";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_email);

        backgroundVideo = (VideoView) findViewById(R.id.backgroundVideo);
        registerButton = (Button) findViewById(R.id.register_button);
        loginButton = (Button) findViewById(R.id.login_button);
        email = (EditText) findViewById(R.id.email_edit_text);
        password = (TextInputLayout) findViewById(R.id.login_password_edit_text);
        forgotPass = (TextView) findViewById(R.id.forgot_pass_text_view);
        editTextPassword = password.getEditText();

        forgotPass.setPaintFlags(forgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mAuth = FirebaseAuth.getInstance();

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.vapevidback);

        backgroundVideo.setVideoURI(uri);
        backgroundVideo.start();

        backgroundVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginWithEmail.this, RegisterAccount.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userEmail = email.getText().toString().trim();
                String userPassword = editTextPassword.getText().toString().trim();

                progressDialog = ProgressDialog.show(LoginWithEmail.this,
                        "Logging In", "Logging in to your account...", true);

                mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                handleEmailLogin(task);
                            }
                        });
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginWithEmail.this);

                input = new EditText(LoginWithEmail.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                builder.setView(input);

                builder.setMessage("Enter e-mail address to send the reset password link.")
                        .setTitle("Forgot Password?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                final String email = input.getText().toString();
                                sendResetEmail(email);
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                // Do nothing here
                            }
                });
                builder.show();
            }
        });
    }

    private void handleEmailLogin(Task<AuthResult> task){
        if (task.isSuccessful()){
            progressDialog.dismiss();
            Log.d(TAG, "LI_SUCCESS");
            FirebaseUser user = mAuth.getCurrentUser();
            Toast.makeText(LoginWithEmail.this, "Logged In.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginWithEmail.this, MainMenuActivity.class));
        } else {
            progressDialog.dismiss();
            Log.d(TAG, "LI_FAIL");
            email.setError("Incorrect email/password combination.");
        }
    }

    private void sendResetEmail(final String email){
        if (verifyEmail(email)) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginWithEmail.this,
                                        "Email sent to " + email, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private boolean verifyEmail(String emailToVerify){
        if (TextUtils.isEmpty(emailToVerify)){
            input.setError("Email field cannot be empty.");
            return false;
        } else {
            if(Patterns.EMAIL_ADDRESS.matcher(emailToVerify).matches()){
                return true;
            } else {
                input.setError("Email must be the correct format.");
                return false;
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginView.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
