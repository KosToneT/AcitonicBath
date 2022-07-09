package com.example.acitonicbath;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.broooapps.graphview.CurveGraphConfig;
import com.broooapps.graphview.CurveGraphView;
import com.broooapps.graphview.models.GraphData;
import com.broooapps.graphview.models.PointMap;

import java.util.LinkedList;

public class TemperatureFragment extends Fragment {
    public TextView temp;
    private int temperature;
    private LinkedList<Integer> tempList = new LinkedList<>();
    private CurveGraphView curveGraphView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);
        temp = rootView.findViewById(R.id.temp);
        curveGraphView = rootView.findViewById(R.id.cgv);
        curveGraphView.configure(
                new CurveGraphConfig.Builder(this.getContext())
                        .setAxisColor(Color.rgb(227,37,107))                                             // Set number of values to be displayed in X ax
                        .setVerticalGuideline(4)                                                // Set number of background guidelines to be shown.
                        .setHorizontalGuideline(2)
                        .setGuidelineColor(Color.RED)                                         // Set color of the visible guidelines.
                        .setNoDataMsg(" Нет данных ")                                              // Message when no data is provided to the view.
                        .setxAxisScaleTextColor(Color.WHITE)                                  // Set X axis scale text color.
                        .setyAxisScaleTextColor(Color.WHITE)                                  // Set Y axis scale text color
                        .setAnimationDuration(2000)                                             // Set Animation Duration
                        .build()
        );
        PointMap pointMap = new PointMap();
        int max=0;
        for(int i=0; i<tempList.size(); i++){
            int value = tempList.get(i);
            pointMap.addPoint(i, value);
            if(value>max){
                max = value;
            }
        }


        GraphData gd2 = GraphData.builder(this.getContext())
                .setPointMap(pointMap)
                .setGraphStroke(Color.GRAY)
                .setGraphGradient(Color.BLUE, Color.CYAN)
                .build();
        int finalMax = max;
        TemperatureFragment.this.getActivity().runOnUiThread(()->{
            curveGraphView.setData(tempList.size(), finalMax, gd2);
        });
        return  rootView;
    }
    public void DrawGraphic(){
        PointMap pointMap = new PointMap();
        int max=0;
        for(int i=0; i<tempList.size(); i++){
            int value = tempList.get(i);
            pointMap.addPoint(i, value);
            if(value>max){
                max = value;
            }
        }
        GraphData gd2 = GraphData.builder(this.getContext())
                .setPointMap(pointMap)
                .setGraphStroke(Color.GRAY)
                .setGraphGradient(Color.BLUE, Color.CYAN)
                .build();
        int finalMax = max;
        TemperatureFragment.this.getActivity().runOnUiThread(()->{
            curveGraphView.setData(tempList.size(), finalMax, gd2);
            this.temp.setText(String.format("%d°С", temperature));
        });
    }


    public void setTemperature(int temp){
        temperature = temp;
        if(tempList.size()>25){
            tempList.removeFirst();
        }
        tempList.add(temp);
        DrawGraphic();
    }
    public int getTemperature(){
        return  temperature;
    }
}