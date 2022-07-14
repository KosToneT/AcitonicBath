package com.example.acitonicbath;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class TimeFragment extends Fragment {
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

        TextView totalTime2 = rootView.findViewById(R.id.totalTime2);
        TextView time3 =  rootView.findViewById(R.id.time3);
        TextView time4 = rootView.findViewById(R.id.time4);
        TextView textViewState = rootView.findViewById(R.id.txtStateViewTime);

        bath.setView(0, time3);
        bath.setView(1, time4);
        bath.setView(2, textViewState);
        bath.setView(3,totalTime2);

        TextView temp = rootView.findViewById(R.id.txtTemp);
        temp.setText(String.format("%d°С", bath.getTemp()));
        temp.setOnClickListener(view -> {
            Dialog d= new Dialog(TimeFragment.this.getContext());
            d.setContentView(R.layout.dialog_set_temp);
            EditText edt = d.findViewById(R.id.edtTempurature);
            edt.setText(bath.getTemp()+"");
            d.show();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    int temperature = Integer.parseInt(edt.getText().toString());
                    bath.setTemp(temperature);
                    temp.setText(String.format("%d°С", temperature));
                }
            });
        });
        return  rootView;
    }
}