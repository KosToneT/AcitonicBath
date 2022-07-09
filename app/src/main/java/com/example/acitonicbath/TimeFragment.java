package com.example.acitonicbath;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TimeFragment extends Fragment {
    private TextView totalTime2;
    private TextView time3;
    private TextView time4;
    private MainActivity.Bath bath;
    public TimeFragment(MainActivity.Bath bath) {
        this.bath = bath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time, container, false);

        TextView totalTime2 = (TextView) rootView.findViewById(R.id.totalTime2);
        TextView time3 = (TextView) rootView.findViewById(R.id.time3);
        TextView time4 = (TextView) rootView.findViewById(R.id.time4);

        bath.setView(0, time3);
        bath.setView(1, time4);
        bath.setView(2,totalTime2);
        return  rootView;
    }
}