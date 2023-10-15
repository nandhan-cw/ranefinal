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

import android.annotation.SuppressLint;
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
    String[] items;
    BottomNavigationView bottom_navigation;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    NavigationView side_navigation;
    Toolbar toolbar;

    ImageView settings;
    EditText maxAngleEditText ,tx,rx ;
    ToggleButton toggleButton ;

    Button savebtn;
    Spinner spinner;
    ArrayAdapter<String> adapter;
    String savedTxValue;
    boolean isChecked ;
    public static final String SHARED_PREFS = "sharedPrefs";

    @SuppressLint("MissingInflatedId")
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
        SteeringVariables.vehicle = sharedPreferences.getString("vehicle","");
        savedTxValue = sharedPreferences.getString("tx", "");
        Log.d("sv", "savedtx: "+savedTxValue.toString());

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
        tx =dialog.findViewById(R.id.tx);
        rx =dialog.findViewById(R.id.rx);
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


        Log.d("vehicle", "vehicle 1st "+SteeringVariables.vehicle.toString());


        Log.d("vehicle", "vehicle 4st "+SteeringVariables.vehicle.toString());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SteeringVariables.max_angle = maxAngleEditText.getText().toString();
                SteeringVariables.steeringauto = toggleButton.isChecked() ? "on" : "off";
                saveData();
                loadData();
                vehicleChoose();
                txrxChoose();
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
                vehicleChoose();
                txrxChoose();
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


    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("max_angle", SteeringVariables.max_angle.toString());
        editor.putString("steering_auto", SteeringVariables.steeringauto.toString());
        editor.putString("steeringstatus", SteeringVariables.steeringStatus.toString());
        editor.putString("vehicle", SteeringVariables.vehicle.toString());

        // Get the entered hexadecimal values for tx and rx EditTexts
        String txValue = tx.getText().toString();
        String rxValue = rx.getText().toString();

        try {
            // Parse the entered hexadecimal strings to integers
            int txIntValue = Integer.parseInt(txValue, 16);
            int rxIntValue = Integer.parseInt(rxValue, 16);

            // Check if the entered values are within the range of a two-byte value
            if (txIntValue >= 0 && txIntValue <= 0xFFFF) {
                // Valid two-byte value for tx, save the data
                editor.putString("tx", txValue.toUpperCase());
            } else {
                // Invalid input for tx, show error message
                tx.setError("Invalid value! Please enter a valid two-byte hexadecimal value.");
                return; // Exit the method without saving the data
            }

            // Check if the entered value for rx is within the range of a two-byte value
            if (rxIntValue >= 0 && rxIntValue <= 0xFFFF) {
                // Valid two-byte value for rx, save the data
                editor.putString("rx", rxValue.toUpperCase());
            } else {
                // Invalid input for rx, show error message
                rx.setError("Invalid value! Please enter a valid two-byte hexadecimal value.");
                return; // Exit the method without saving the data
            }

            // Save other data if validation passes
            editor.apply();
        } catch (NumberFormatException e) {
            // Invalid input format, show error message
            tx.setError("Invalid input! Please enter a valid hexadecimal value.");
            rx.setError("Invalid input! Please enter a valid hexadecimal value.");
            Log.e("SaveData", "Error parsing hex string: " + e.getMessage());
        }
    }

    public void vehicleChoose(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item from the adapter
                String selectedItem = (String) parentView.getItemAtPosition(position);
                // Do something with the selected item
                if (selectedItem.equals("truck")) {
                    SteeringVariables.vehicle = "truck";
                    HomeFragment.vehicleChange();
                    Log.d("vehicle", "vehicle 2st "+SteeringVariables.vehicle.toString());

                }else if (selectedItem.equals("tractor")){
                    SteeringVariables.vehicle="tractor";
                    HomeFragment.vehicleChange();
                    Log.d("vehicle", "vehicle 3st "+SteeringVariables.vehicle.toString());
                }else {
                    SteeringVariables.vehicle="car";
                    HomeFragment.vehicleChange();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle situation when nothing is selected
                SteeringVariables.vehicle = "car";
            }
        });
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
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", ""); // Load max angle
        SteeringVariables.steeringauto = sharedPreferences.getString("steering_auto",""); // Load steering auto state
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringstatus","");
        SteeringVariables.vehicle = sharedPreferences.getString("vehicle","");
        maxAngleEditText.setText(SteeringVariables.max_angle);  // Set text using SteeringVariables.max_angle
        toggleButton.setChecked(SteeringVariables.steeringauto.equals("on"));  // Check toggle button based on SteeringVariables.steeringauto value
        LockSteeringFragment fragment = (LockSteeringFragment) getSupportFragmentManager().findFragmentById(R.id.lock);



        String hexString = sharedPreferences.getString("tx","");
        int decimalValue = Integer.parseInt(hexString, 16);
        short shortValue = (short) decimalValue;
        SteeringVariables.frameId1 = shortValue;

        String hexString1 = sharedPreferences.getString("rx","");
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
        String txValue = sharedPreferences.getString("tx", "0");
        if ( txValue!= null) {
            tx.setText(txValue.toUpperCase());
        }

        String rxValue = sharedPreferences.getString("rx", "0");
        if ( rxValue!= null) {
            rx.setText(rxValue.toUpperCase());
        }
    }

}