package com.steering.testrane;

import static android.provider.Telephony.Mms.Part.TEXT;
import static android.service.controls.ControlsProviderService.TAG;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

    ImageView settings,connectStatus;
    EditText maxAngleEditText  ;
    ToggleButton toggleButton ;
    EditText tx,rx;
    Button savebtn;
    Spinner spinner;
    String[] items;
    ArrayAdapter<String> adapter;
    boolean isChecked ;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frame_layout = findViewById(R.id.frame_layout);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        connectStatus = findViewById(R.id.connectStatus);
        side_navigation = findViewById(R.id.side_navigation);
        NavigationView navigationView = findViewById(R.id.side_navigation);
        settings = findViewById(R.id.settings);

        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SteeringVariables.loginstatus = sharedPreferences.getString("loginstatus", "off");
        SteeringVariables.steeringauto=sharedPreferences.getString("steering_auto","off");
        SteeringVariables.max_angle=sharedPreferences.getString("max_angle","180");
        SteeringVariables.vehicle=sharedPreferences.getString("vehicle","car");

//        if(sharedPreferences.contains("steering_auto")){
//            SteeringVariables.steeringauto=sharedPreferences.getString("steering_auto","off");
//        }else{
//            SteeringVariables.steeringauto="off";
//        }
//        if(sharedPreferences.contains("max_angle")){
//            SteeringVariables.max_angle=sharedPreferences.getString("max_angle","180");
//        }else{
//            SteeringVariables.max_angle="180";
//        }
//        if(sharedPreferences.contains("vehicle")){
//            SteeringVariables.vehicle=sharedPreferences.getString("vehicle","car");
//        }else{
//            SteeringVariables.vehicle="car";
//        }

        if(sharedPreferences.contains("tx") && sharedPreferences.contains("rx")) {
            String hexString = sharedPreferences.getString("tx", "70E");
            int decimalValue = Integer.parseInt(hexString, 16);
            short shortValue = (short) decimalValue;
            SteeringVariables.frameId1 = shortValue;

            String hexString1 = sharedPreferences.getString("rx", "71E");
            int decimalValue1 = Integer.parseInt(hexString1, 16);
            short shortValue1 = (short) decimalValue1;
            SteeringVariables.frameIdRX = shortValue1;
        }else{
            SteeringVariables.frameId1 = 0x70E;
            SteeringVariables.frameIdRX = 0x71E;
        }

//        saveData();

//        SteeringVariables.frameId1 = sharedPreferences.get("tx",0x70E);
//        SteeringVariables.frameIdRx = sharedPreferences.getString("rx",)
//        if (sharedPreferences.contains("max_angle")) {
//            Log.d("",sharedPreferences.getString("max_angle", "180"));
//            SteeringVariables.max_angle = sharedPreferences.getString("max_angle", "180");
//        }
//        else SteeringVariables.max_angle="180";
//        loadData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if (SteeringVariables.bluetoothstatus == true) connectStatus.setImageResource(R.drawable.grnbtn);
                    else connectStatus.setImageResource(R.drawable.redbtn);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingspopup(MainActivity.this);
                Log.d("popup", "Settings button clicked");
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

//        HomeFragment.vehicleChange();

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
                SteeringVariables.home_thread_flag=true;
                SteeringVariables.status_thread_flag=false;
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if(item.getItemId() == R.id.status){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new StatusFragment(),null)
                        .addToBackStack(null)
                        .commit();
                SteeringVariables.home_thread_flag=false;
                SteeringVariables.status_thread_flag=true;
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
            SteeringVariables.home_thread_flag=true;
            SteeringVariables.status_thread_flag=false;
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if(item.getItemId() == R.id.status){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new StatusFragment(),null)
                    .addToBackStack(null)
                    .commit();
            SteeringVariables.home_thread_flag=false;
            SteeringVariables.status_thread_flag=true;
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
        tx = dialog.findViewById(R.id.tx);
        rx = dialog.findViewById(R.id.rx);
        spinner = dialog.findViewById(R.id.spinner);
        items = getResources().getStringArray(R.array.default_options);
        adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(SteeringVariables.vehicle.equals("truck")){
            spinner.setSelection(1);
        }else if (SteeringVariables.vehicle.equals("tractor")){
            spinner.setSelection(2);
        }else{
            spinner.setSelection(0);
        }
        Log.d("popup","6");
        tx.setText(SteeringVariables.frameId1+"");
        rx.setText(SteeringVariables.frameIdRX+"");

        Log.d("popup", "vehicle 1st "+SteeringVariables.vehicle.toString());

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(maxAngleEditText.getText().toString().isEmpty()){
                    SteeringVariables.max_angle = "180";
                }else{
                    SteeringVariables.max_angle = maxAngleEditText.getText().toString();

                }
                SteeringVariables.steeringauto = toggleButton.isChecked() ? "on" : "off";
                SteeringVariables.vehicle = spinner.getSelectedItem().toString();
                saveData();
                loadData();
                HomeFragment.vehicleChange();
//                vehicleChoose();
                txrxChoose();
                HomeFragment.updAngle();
                dialog.dismiss();
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(maxAngleEditText.getText().toString().isEmpty()){
                    SteeringVariables.max_angle = "180";
                }else{
                    SteeringVariables.max_angle = maxAngleEditText.getText().toString().trim();
                }
                SteeringVariables.steeringauto = toggleButton.isChecked() ? "on" : "off";
                SteeringVariables.vehicle = spinner.getSelectedItem().toString();
                saveData();
                loadData();
//                vehicleChoose();
                HomeFragment.vehicleChange();

                txrxChoose();
                HomeFragment.updAngle();
                dialog.dismiss();

            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                SteeringVariables.vehicle = spinner.getSelectedItem().toString();
                HomeFragment.vehicleChange();
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
        editor.putString("vehicle",SteeringVariables.vehicle.toString());

        String hexString = Integer.toHexString(SteeringVariables.frameId1).toUpperCase();
        editor.putString("tx", tx.getText().toString().trim());

        String hexString1 = Integer.toHexString(SteeringVariables.frameIdRX).toUpperCase();
        editor.putString("rx", rx.getText().toString());

//        editor.putString("rx", rx.getText().toString());
        Log.d("sv", "saveData: "+sharedPreferences.getString("rx",""));
        Log.d("sv","save "+String.valueOf(SteeringVariables.frameIdRX));

        editor.apply();
    }

    public void txrxChoose(){
        try {
            String hexString = tx.getText().toString().toUpperCase();
            int decimalValue = Integer.parseInt(hexString, 16);
            short shortValue = (short) decimalValue;
            SteeringVariables.frameId1 = shortValue;

            String hexString1 = rx.getText().toString().toUpperCase();
            int decimalValue1 = Integer.parseInt(hexString1, 16);
            short shortValue1 = (short) decimalValue1;
            SteeringVariables.frameIdRX = shortValue1;

            Log.d("sv", "rx: "+ hexString1);
            Log.d("sv", "txrxChoose: "+SteeringVariables.frameIdRX);
        } catch (NumberFormatException e) {
            // Handle the exception (invalid input format)
            Log.d("sv", "tx:no crct ");
            e.printStackTrace();
        }
    }



    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", "180"); // Load max angle
        SteeringVariables.steeringauto = sharedPreferences.getString("steering_auto","off"); // Load steering auto state
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringstatus","not_locked");
        SteeringVariables.vehicle = sharedPreferences.getString("vehicle","car");
        maxAngleEditText.setText(SteeringVariables.max_angle);  // Set text using SteeringVariables.max_angle
        toggleButton.setChecked(SteeringVariables.steeringauto.equals("on"));  // Check toggle button based on SteeringVariables.steeringauto value
        LockSteeringFragment fragment = (LockSteeringFragment) getSupportFragmentManager().findFragmentById(R.id.lock);

        String hexString = sharedPreferences.getString("tx","70E");
        int decimalValue = Integer.parseInt(hexString, 16);
        short shortValue = (short) decimalValue;
        SteeringVariables.frameId1 = shortValue;

        String hexString1 = sharedPreferences.getString("rx","71E");
        int decimalValue1 = Integer.parseInt(hexString1, 16);
        short shortValue1 = (short) decimalValue1;
        SteeringVariables.frameIdRX = shortValue1;

        maxAngleEditText.setText(SteeringVariables.max_angle);
        toggleButton.setChecked(SteeringVariables.steeringauto.equals("on"));

        // Check if the fragment is not null and set the max angle value to the fragment variable
        if (fragment != null) {
            fragment.lockedstatus.setText(SteeringVariables.max_angle);
        }
        // Set tx EditText if txValue is not null
        String txValue = sharedPreferences.getString("tx", "70E");
        if ( txValue!= null) {
            tx.setText(txValue.toUpperCase());
        }

        String rxValue = sharedPreferences.getString("rx", "71E");
        if ( rxValue!= null) {
            rx.setText(rxValue.toUpperCase());
        }
    }

}