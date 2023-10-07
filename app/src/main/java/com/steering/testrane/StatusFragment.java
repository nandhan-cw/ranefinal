package com.steering.testrane;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class StatusFragment extends Fragment {

    Button steerControl;
    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        steerControl = view.findViewById(R.id.steercontrol);
        steerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with LockSteeringFragment
                Fragment fragTwo = new LockSteeringFragment();
                replaceFragment(fragTwo);
            }
        });

        return view;
    }

    // Method to replace a fragment
    private void replaceFragment(Fragment fragment) {
        if (getActivity() != null) {
            fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment,null);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    // Handle back press to navigate back to LockSteeringFragment



}
