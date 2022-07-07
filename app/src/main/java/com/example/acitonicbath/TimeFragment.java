package com.example.acitonicbath;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TimeFragment extends Fragment {
    public TextView totalTime2;
    public TextView time3;
    public TextView time4;
    public MainActivity.Bath bath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_time, container, false);

        totalTime2 = (TextView) rootView.findViewById(R.id.totalTime2);
        time3 = (TextView) rootView.findViewById(R.id.time3);
        time4 = (TextView) rootView.findViewById(R.id.time4);

        bath.setView();
        return  rootView;
    }

    public void setView(MainActivity.Bath bath){
        bath.setView(0, time3);
        bath.setView(1, time4);
        bath.setView(2,totalTime2);
    }

    public TimeFragment(MainActivity.Bath abth) {
        bath = abth;
    }

    public void sendTime(int id, String data) {
        if (data != null){
            if(id==0){
                time3.setText(data);
            }else{
                time4.setText(data);
            }
        }
    }

    public void sendTotalTime(String data){
        totalTime2.setText(data);
    }
}