package com.jak_reed.www.a618_mobile_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
 //
    private ImageView profilePic;
    private TextView profileName;
    private Uri photoUrl;
    private String uid, name, email, providerID;
    private final static String TAG = "MAIN_MENU_ACTIVITY";

    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        profilePic = (ImageView)  navView.getHeaderView(0).findViewById(R.id.profile_pic);
        profileName = (TextView) navView.getHeaderView(0).findViewById(R.id.profile_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);

        if(user != null){
            for(UserInfo profile : user.getProviderData()){
                //Log.d(TAG, "AUTH_PROVIDER::"+profile.getProviderId());
                if(profile.getProviderId().equals("password")){
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "USING_EMAIL::");
                            name = dataSnapshot.child("name").getValue().toString();
                            photoUrl = Uri.parse(dataSnapshot.child("profilePictureUrl").getValue().toString());

                            setUserDisplayInfo(name, photoUrl);
                            Log.d(TAG, "DATA::"+" NAME::"+name+"profilePictureUrl::"+photoUrl);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "FAILED_TO_CREATE_CONNECTION");
                        }
                    });
                } else {
                    Log.d(TAG, "USING_OTHER::");
                    name = profile.getDisplayName();
                    photoUrl = profile.getPhotoUrl();

                    Log.d(TAG, "DATA::"+" NAME::"+name+"profilePictureUrl::"+photoUrl);
                    setUserDisplayInfo(name, photoUrl);
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUserDisplayInfo(final String disName, final Uri disPhoto){
        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1)
                .cornerRadiusDp(50)
                .oval(false)
                .build();

        Picasso.with(this.getApplicationContext())
                .load(disPhoto)
                .resize(200,200)
                .transform(transformation)
                .into(profilePic);

        profileName.setText(disName);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragmentToUse = null;
        Class fragClass;
        int id = item.getItemId();

        if (id == R.id.nav_account) {

        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(MainMenuActivity.this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you wish to log out?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton){
                            signOut();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton){
                            // Do nothing
                        }
            }).show();
        } else if (id == R.id.nav_locations) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MainMenuActivity.this, "Logged Out", Toast.LENGTH_LONG).show();

        // Start the login activity
        startActivity(new Intent(MainMenuActivity.this, LoginView.class));
    }
}
