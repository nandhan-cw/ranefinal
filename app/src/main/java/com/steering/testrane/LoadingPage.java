package com.steering.testrane;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoadingPage extends AppCompatActivity {
    Dialog loginpopup;
    Button accessButton;
    CheckBox checkbox;
    ProgressDialog progressDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_page);

        checkbox = findViewById(R.id.checkbox);
        accessButton = findViewById(R.id.accessButton);
        progressDialog = new ProgressDialog(this);
        // Set an OnCheckedChangeListener to react to checkbox state changes
        setContentView(R.layout.activity_loading_page);

        checkbox = findViewById(R.id.checkbox);
        accessButton = findViewById(R.id.accessButton);
        loginpopup = new Dialog(this);
        SharedPreferences sharedPreferences = LoadingPage.this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SteeringVariables.loginstatus = sharedPreferences.getString("loginstatus", "off");
        Log.d(TAG, "onCreate:login "+SteeringVariables.loginstatus.toString());
//        Toast.makeText(this, "login", Toast.LENGTH_SHORT).show();
        if (SteeringVariables.loginstatus.equals("on")) {

            startActivity(new Intent(LoadingPage.this, MainActivity.class));

        }
        else{
            Log.d(TAG, "onCreate:else "+SteeringVariables.loginstatus.toString());
        }
        // Set an OnClickListener for the button
        accessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SteeringVariables.loginstatus.equals("off")) {

                        Loginpopup(LoadingPage.this);


                }else{
                    startActivity(new Intent(LoadingPage.this, MainActivity.class));

                }
                Loginpopup(LoadingPage.this);
                // startActivity(new Intent(LoadingPage.this, Login.class));
            }
        });

        // Initially set the background drawable and state based on the checkbox state
        updateButtonBackground(checkbox.isChecked());

        // Set an OnCheckedChangeListener to react to checkbox state changes
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the button background and state when the checkbox state changes
                updateButtonBackground(isChecked);
            }
        });
    }


    private void showExitConfirmationDialog() {
        final Dialog exitDialog = new Dialog(this);
        exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        exitDialog.setContentView(R.layout.exit_confirmation_dialog);
        exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        exitDialog.setCancelable(true);
        Button confirmButton = exitDialog.findViewById(R.id.confirmButton);
        Button cancelButton = exitDialog.findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
                finish(); // Close the app
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
            }
        });

        exitDialog.show();
    }

    private void updateButtonBackground(boolean isChecked) {
        if (isChecked) {
            // Checkbox is checked
            accessButton.setBackgroundResource(R.drawable.button); // Set the enabled state background
            accessButton.setEnabled(true);
            accessButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            // Checkbox is not checked
            accessButton.setBackgroundColor(getResources().getColor(R.color.grey));
            accessButton.setBackgroundResource(R.drawable.buttondis); // Set the disabled state background
            accessButton.setEnabled(false);
            accessButton.setTextColor(getResources().getColor(R.color.dark_grey));
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (loginpopup != null && loginpopup.isShowing()) {
            loginpopup.dismiss();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    public void Loginpopup(Context context){
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.login_popup);
                ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
                EditText username = dialog.findViewById(R.id.username);
                EditText password = dialog.findViewById(R.id.password);
                Button loginBtn = dialog.findViewById(R.id.loginBtn);
                ImageView closeBtn = dialog.findViewById(R.id.cancelButton);
                loginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isFinishing()) {
                            if (username.getText().toString().trim().equals("rane_user") && password.getText().toString().trim().equals("12345")) {
                                progressDialog.setMessage("Please wait Logging in");
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                Toast.makeText(LoadingPage.this, "Login successfull", Toast.LENGTH_SHORT).show();
                                saveloginstatus("on");
                                dialog.dismiss();
                                startActivity(new Intent(LoadingPage.this, MainActivity.class));
                                finish();
                            } else if (!username.getText().toString().equals("rane_user")) {
                                username.setError("username not found");
                            } else if (!password.getText().toString().equals("12345") || password.getText().toString().matches("\\d+")) {
                                password.setError("please check your password");

                            } else {
                                username.setError("username is empty");
                                password.setError("password is empty");
                            }
                        }
                    }
                });

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }}
    }
    private void saveloginstatus(String status) {
        SharedPreferences sharedPreferences = LoadingPage.this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("loginstatus", status);
        editor.apply();
    }
    private void showLoader() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Set your loading message here
        progressDialog.setCancelable(false); // Prevent the user from dismissing the dialog by pressing back button
        progressDialog.show();
    }
}