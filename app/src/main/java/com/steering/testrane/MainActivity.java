package com.steering.testrane;

import static android.provider.Telephony.Mms.Part.TEXT;
import static android.service.controls.ControlsProviderService.TAG;

import static com.steering.testrane.SteeringVariables.sendReceive;
import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

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
    Slider slider;
    EditText tx,rx;
    Button savebtn;
    Spinner spinner;
    String[] items;
    int screenWidth;
    private GestureDetector gestureDetector;
    ArrayAdapter<String> adapter;
    boolean isChecked ;
    private float startY;
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

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();


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

        loadData();




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

        Log.d("maxangle","settings: "+SteeringVariables.max_angle);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        String temp = sharedPreferences.getString("max_angle", "180");
        if(temp=="0"){
            if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null && SteeringVariables.max_angle!="180"){
                sendData();
            }
            SteeringVariables.max_angle = "180";
        }else{
            if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null && SteeringVariables.max_angle!=temp){
                sendData();
            }
            SteeringVariables.max_angle = temp;
        }

        maxAngleEditText.setText(SteeringVariables.max_angle);
        slider = dialog.findViewById(R.id.vibrateSlider);
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
        short number = SteeringVariables.frameId1;
        String hexString = Integer.toHexString(number & 0xFFFF);
        tx.setText(hexString.toUpperCase());
        short number1 = SteeringVariables.frameIdRX;
        String hexString1 = Integer.toHexString(number1 & 0xFFFF);
        rx.setText(hexString1.toUpperCase());
//        maxAngleEditText.setText(SteeringVariables.max_angle);
        // Load slider value


        Log.d("popup", "vehicle 1st "+SteeringVariables.vehicle.toString());

        toggleButton.setChecked(SteeringVariables.steeringauto.equals("on"));
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                // Handle slider value change
                Log.d("slider", "onValueChange: "+value);
                SteeringVariables.vibration = String.valueOf(value);
            }
        });

        slider.setValue(Float.parseFloat(SteeringVariables.vibration));

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(maxAngleEditText.getText().toString().isEmpty()){
                    if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null && SteeringVariables.max_angle!="180"){
                        sendData();
                    }
                    SteeringVariables.max_angle = "180";
                }else{
                    if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null && SteeringVariables.max_angle!=maxAngleEditText.getText().toString().trim()){
                        sendData();
                    }
                    SteeringVariables.max_angle = maxAngleEditText.getText().toString().trim();
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
                Toast.makeText(context, "saved", Toast.LENGTH_SHORT).show();
                if(maxAngleEditText.getText().toString().isEmpty()){
                    if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null && SteeringVariables.max_angle!="180"){
                        sendData();
                    }

                    SteeringVariables.max_angle = "180";

                }else{
                    String ma  = maxAngleEditText.getText().toString();
                    if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null && SteeringVariables.max_angle!=ma){
                        sendData();
                    }

                    SteeringVariables.max_angle = ma;

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
        editor.putString("tx", tx.getText().toString());

        String hexString1 = Integer.toHexString(SteeringVariables.frameIdRX).toUpperCase();
        editor.putString("rx", rx.getText().toString());

        editor.putString("vibration", SteeringVariables.vibration); // Save the slider value


//        editor.putString("rx", rx.getText().toString());
        Log.d("sv", "saveData: "+sharedPreferences.getString("rx",""));
        Log.d("sv","save "+String.valueOf(SteeringVariables.frameIdRX));

        editor.apply();
        Log.d("maxangle","save: "+sharedPreferences.getString("max_angle", "")+" sv: "+SteeringVariables.max_angle);



    }

    private void sendData(){
        while(sendReceive==null){
            Log.d("status","sendreceive no");
            if(sendReceive!=null) break;
        }
        if(sendReceive!=null){
            Log.d("status","sendreceive yes");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("status","status run()");
                    try {
//                        HomeFragment.SteeringVariables.max_angle;
                        byte[] doublebyte = formatAndConvertData(Float.parseFloat(SteeringVariables.max_angle));
                        byte[] frameId = HomeFragment.convertShortToBytes(SteeringVariables.frameId1);
                        byte[] concatenatedArray02 = {SteeringVariables.startId,frameId[0],frameId[1], SteeringVariables.dlc,0x06,doublebyte[0],doublebyte[1],SteeringVariables.data5[0],SteeringVariables.data5[1],SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,SteeringVariables.endId1,SteeringVariables.endId2};

                        sendReceive.write(concatenatedArray02);
                        Thread.sleep(500); // Delay for 1 second (1000 milliseconds)
                    } catch (InterruptedException e) {
                        Log.d("status","thread ex "+e);
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    private byte[] formatAndConvertData(float angle) {
        int angleValue = (int) angle;
        Log.d("value11", "angle1 : " + angleValue);

        String hexAngle = Integer.toHexString(angleValue);

        Log.d("value11", "hexstring : " + hexAngle);

        if (angleValue <= 255) {
            byte[] byteArray = new byte[2];
            byteArray[0] = 0x00;
            byteArray[1] = (byte) Integer.parseInt(hexAngle, 16);
            Log.d("value11", "data1 : " + byteArray[0] + " data2: " + byteArray[1]);
            return byteArray;
        } else {
            // If angle is more than 255, store it in a double byte array
            byte[] doubleByteArray = new byte[2];
            doubleByteArray[0] = (byte) Integer.parseInt(hexAngle.substring(0, 2), 16);
            doubleByteArray[1] = (byte) Integer.parseInt(hexAngle.substring(2), 16);
            Log.d("value11", "data1 : " + doubleByteArray[0] + " data2: " + doubleByteArray[1]);
            return doubleByteArray;
        }

    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", "180"); // Load max angle
        SteeringVariables.steeringauto = sharedPreferences.getString("steering_auto","off"); // Load steering auto state
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringstatus","not_locked");
        SteeringVariables.vehicle = sharedPreferences.getString("vehicle","car");
        SteeringVariables.vibration = sharedPreferences.getString("vibration","0.4");

//        Toast.makeText(getApplicationContext(), ""+SteeringVariables.max_angle, Toast.LENGTH_SHORT).show();
        Log.d("maxangle","load: "+sharedPreferences.getString("max_angle", "")+" sv: "+SteeringVariables.max_angle);

        // Set text using SteeringVariables.max_angle
        // Check toggle button based on SteeringVariables.steeringauto value
        LockSteeringFragment fragment = (LockSteeringFragment) getSupportFragmentManager().findFragmentById(R.id.lock);

        String hexString = sharedPreferences.getString("tx","70E");
        int decimalValue = Integer.parseInt(hexString, 16);
        short shortValue = (short) decimalValue;
        SteeringVariables.frameId1 = shortValue;

        String hexString1 = sharedPreferences.getString("rx","71E");
        int decimalValue1 = Integer.parseInt(hexString1, 16);
        short shortValue1 = (short) decimalValue1;
        SteeringVariables.frameIdRX = shortValue1;


        // Check if the fragment is not null and set the max angle value to the fragment variable
        if (fragment != null) {
            fragment.lockedstatus.setText(SteeringVariables.max_angle);
        }
        // Set tx EditText if txValue is not null

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






}