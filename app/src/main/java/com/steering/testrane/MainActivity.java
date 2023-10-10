package com.steering.testrane;

import static android.provider.Telephony.Mms.Part.TEXT;

import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FrameLayout frame_layout;
    BottomNavigationView bottom_navigation;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    NavigationView side_navigation;
    Toolbar toolbar;

    ImageView settings;
    EditText maxAngleEditText  ;
    ToggleButton toggleButton ;

    Button savebtn;
    boolean isChecked ;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frame_layout = findViewById(R.id.frame_layout);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        side_navigation = findViewById(R.id.side_navigation);
        NavigationView navigationView = findViewById(R.id.side_navigation);
        settings = findViewById(R.id.settings);
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SteeringVariables.loginstatus = sharedPreferences.getString("loginstatus", "off");
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SettingsPopup", "Settings button clicked");
                settingspopup(MainActivity.this);
                loadData();
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, new HomeFragment())
                .commit();


        bottom_navigation
                .setOnNavigationItemSelectedListener(item -> {

                    if(item.getItemId() == R.id.home){
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_layout, new HomeFragment())
                                .commit();
                        return true;
                    }
                    else if(item.getItemId() == R.id.status){
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_layout, new StatusFragment())
                                .commit();
                        return true;
                    }
                    else if(item.getItemId() == R.id.lock){
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_layout, new LockSteeringFragment())
                                .commit();
                        return true;
                    }
                    return false;
                });

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.getDrawerArrowDrawable().setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        side_navigation.setNavigationItemSelectedListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            if(item.getItemId() == R.id.home){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new HomeFragment(),null)
                        .addToBackStack(null)
                        .commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if(item.getItemId() == R.id.status){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new StatusFragment(),null)
                        .addToBackStack(null)
                        .commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if(item.getItemId() == R.id.lock){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new LockSteeringFragment(),null)
                        .addToBackStack(null)
                        .commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }else if(item.getItemId() == R.id.logout){
                saveloginstatus("off");
                startActivity(new Intent(MainActivity.this, LoadingPage.class));
                finish();
                return true;
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.home){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new HomeFragment(),null)
                    .addToBackStack(null)
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if(item.getItemId() == R.id.status){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new StatusFragment(),null)
                    .addToBackStack(null)
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if(item.getItemId() == R.id.lock){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new LockSteeringFragment(),null)
                    .addToBackStack(null)
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if(item.getItemId() == R.id.logout){
            saveloginstatus("off");
            startActivity(new Intent(MainActivity.this, LoadingPage.class));
            finish();
            return true;
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).showExitConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }


    private void saveloginstatus(String status) {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("loginstatus", status);
        editor.apply();
    }



    public void settingspopup(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.settings_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // Set window background to null
        dialog.setCancelable(true); // Set to true if you want to close the dialog when touching outside
        ImageView closeBtn = dialog.findViewById(R.id.cancelButton);
        maxAngleEditText = dialog.findViewById(R.id.max_angle);
        toggleButton = dialog.findViewById(R.id.toggle_button);
        savebtn = dialog.findViewById(R.id.savebtn);
        maxAngleEditText.setText(SteeringVariables.max_angle);
        toggleButton.setChecked(SteeringVariables.steeringauto.equals("on"));

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SteeringVariables.max_angle = maxAngleEditText.getText().toString();
                SteeringVariables.steeringauto = toggleButton.isChecked() ? "on" : "off";
                saveData();
                loadData();
                HomeFragment.updAngle();
                dialog.dismiss();
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SteeringVariables.max_angle = maxAngleEditText.getText().toString();
                SteeringVariables.steeringauto = toggleButton.isChecked() ? "on" : "off";
                saveData();
                loadData();
                HomeFragment.updAngle();
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                saveData();
                loadData();
            }
        });

        dialog.show();
    }


    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("max_angle",SteeringVariables.max_angle.toString());
        editor.putString("steering_auto",SteeringVariables.steeringauto.toString());
        editor.putString("steeringstatus",SteeringVariables.steeringStatus.toString());
        editor.apply();


    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", ""); // Load max angle
        SteeringVariables.steeringauto = sharedPreferences.getString("steering_auto",""); // Load steering auto state
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringstatus","");


        maxAngleEditText.setText(SteeringVariables.max_angle);  // Set text using SteeringVariables.max_angle
        toggleButton.setChecked(SteeringVariables.steeringauto.equals("on"));  // Check toggle button based on SteeringVariables.steeringauto value

        LockSteeringFragment fragment = (LockSteeringFragment) getSupportFragmentManager().findFragmentById(R.id.lock);

        // Check if the fragment is not null and set the max angle value to the fragment variable
        if (fragment != null) {
            fragment.lockedstatus.setText(SteeringVariables.max_angle);
        }
    }

}