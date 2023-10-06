package com.steering.testrane;

import static com.steering.testrane.SteeringVariables.steeringStatus;

import android.annotation.SuppressLint;
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


        if (steeringStatus == "not_locked") {
            steeringStatus = "locked";
            lockedstatus.setText("LOCKED");
            lockicon.setImageResource(R.drawable.baseline_lock_24);
            lockedstatus.setBackgroundColor(R.color.grey);
            lockedstatus.setBackgroundResource(R.drawable.button);

        }else{
            steeringStatus = "not_locked";
            lockedstatus.setText("LOCK");
            lockicon.setImageResource(R.drawable.baseline_lock_open_24);
            lockicon.setColorFilter(getResources().getColor(R.color.white));
            lockedstatus.setBackgroundColor(R.color.grey);
            lockedstatus.setBackgroundResource(R.drawable.button);
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
                if (steeringStatus == "not_locked") {
                    steeringStatus = "locked";
                    lockedstatus.setText("LOCKED");
                    lockicon.setImageResource(R.drawable.baseline_lock_24);
                    lockedstatus.setBackgroundColor(R.color.grey);
                    lockedstatus.setBackgroundResource(R.drawable.button);

                }else{
                    steeringStatus = "not_locked";
                    lockedstatus.setText("LOCK");
                    lockicon.setImageResource(R.drawable.baseline_lock_open_24);
                    lockicon.setColorFilter(getResources().getColor(R.color.white));
                    lockedstatus.setBackgroundColor(R.color.grey);
                    lockedstatus.setBackgroundResource(R.drawable.button);
                }
            }

        });




        return view;
    }
}