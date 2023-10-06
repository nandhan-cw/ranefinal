package com.steering.testrane;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LockSteeringFragment extends Fragment {

    Button locksteeringbtn,statusbtn,lockedstatus;
    ImageView lockicon;
    @SuppressLint({"MissingInflatedId", "ResourceAsColor"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock_steering,container,false);
        RelativeLayout lockSteeringLayout = view.findViewById(R.id.locksteeringlayout);
        lockedstatus = view.findViewById(R.id.lockedstatus);
        String lockvalue = (String) lockedstatus.getTag();
        statusbtn= view.findViewById(R.id.statusbtn);
        lockicon = view.findViewById(R.id.lockicon);
        Toast.makeText(getActivity(), "check - "+SteeringVariables.steeringStatus.toString(), Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringStatus", "not_locked");
        if (SteeringVariables.steeringStatus.equals("not_locked")) {

            lockicon.setImageResource(R.drawable.baseline_lock_open_24);
            lockedstatus.setText("LOCK");


        }else{
            lockicon.setImageResource(R.drawable.baseline_lock_24);
            lockedstatus.setText("UNLOCK");

        }

        statusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragtwo = new StatusFragment();
                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.frame_layout,fragtwo).commit();
            }
        });

        lockedstatus.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if (SteeringVariables.steeringStatus == "not_locked") {
                    Toast.makeText(getActivity(), "if steering status", Toast.LENGTH_SHORT).show();
                    SteeringVariables.steeringStatus = "locked";
                    lockedstatus.setText("UNLOCK");
                    lockicon.setImageResource(R.drawable.baseline_lock_24);
                    lockedstatus.setBackgroundColor(R.color.grey);
                    lockedstatus.setBackgroundResource(R.drawable.button);
                    saveSteeringStatus("locked");
                }else{
                    Toast.makeText(getActivity(), "else  steering status", Toast.LENGTH_SHORT).show();
                    SteeringVariables.steeringStatus = "not_locked";
                    lockedstatus.setText("LOCK");
                    lockicon.setImageResource(R.drawable.baseline_lock_open_24);
                    lockicon.setColorFilter(getResources().getColor(R.color.white));
                    lockedstatus.setBackgroundColor(R.color.grey);
                    saveSteeringStatus("not_locked");
                    lockedstatus.setBackgroundResource(R.drawable.button);

                }
            }

        });




        return view;
    }
    private void saveSteeringStatus(String status) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("steeringStatus", status);
        editor.apply();
    }
}