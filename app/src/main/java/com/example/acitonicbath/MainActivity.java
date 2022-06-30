package com.example.acitonicbath;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static androidx.core.app.NotificationCompat.DEFAULT_ALL;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.TimePickerDialog;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    EditText editTextIP;
    TextView totalTime;
    Cooler cooler0;
    Cooler cooler1;

    private NotificationManager notificationManager;
    private static final int NOTIFY_ID = 1;
    private static final String CHANNEL_ID = "Done";

    SharedPreferences savePreference;
    private static final String KEYIP = "IP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        totalTime = findViewById(R.id.totalTime);
        savePreference = getSharedPreferences("MyPref", MODE_PRIVATE);
        cooler0 = new Cooler(R.id.time1);
        cooler1 = new Cooler(R.id.time2);
        editTextIP = findViewById(R.id.editIPid);
        editTextIP.setText(savePreference.getString(KEYIP, ""));
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Button saveBTN = findViewById(R.id.saveButtonID);
        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor ed = savePreference.edit();
                ed.putString(KEYIP, editTextIP.getText().toString());
                ed.apply();
            }
        });

        Button btn = findViewById(R.id.buttonSend);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTime();
            }
        });
    }

    public void sendNotification(String title, String text){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(PRIORITY_HIGH)
                        .setDefaults(DEFAULT_ALL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
    }

    public void sendTime() {
        try{
            String ipAddress = editTextIP.getText().toString();
            String portNumber = ipAddress.split(":")[1];
            ipAddress = ipAddress.split(":")[0];
            String requests = "{start,"+cooler0+"," + cooler1 + "}";
            //OpenConnectionSendRequests
            new HttpRequestAsyncTask(ipAddress, portNumber, requests).execute();

        }catch(Exception ex){
            System.out.println(ex);
        }
    }


    boolean last = true;
    public void startTimer(){
        last = true;
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(cooler0.workingTime.get(Calendar.MINUTE)<=0){
                    if(cooler0.workingTime.get(Calendar.HOUR_OF_DAY)<=0){
                        if(cooler1.workingTime.get(Calendar.MINUTE)<=0) {
                            if (cooler1.workingTime.get(Calendar.HOUR_OF_DAY) <= 0) {
                                last = false;
                                sendNotification("Готово", "Твоё время пришло...");
                                myTimer.cancel();
                            } else {
                                cooler1.workingTime.set(Calendar.HOUR_OF_DAY, cooler1.workingTime.get(Calendar.HOUR_OF_DAY) - 1);
                                cooler1.workingTime.set(Calendar.MINUTE,59);
                            }
                        } else{
                            cooler1.workingTime.set(Calendar.MINUTE, cooler1.workingTime.get(Calendar.MINUTE)-1);
                        }
                    }else{
                        cooler0.workingTime.set(Calendar.HOUR_OF_DAY, cooler0.workingTime.get(Calendar.HOUR_OF_DAY)-1);
                        cooler0.workingTime.set(Calendar.MINUTE,59);
                    }
                }else{
                    cooler0.workingTime.set(Calendar.MINUTE, cooler0.workingTime.get(Calendar.MINUTE)-1);
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTotalTime();

                        int hours = (cooler0.workingTime.get(Calendar.HOUR_OF_DAY) + cooler1.workingTime.get(Calendar.HOUR_OF_DAY));
                        int minutes = (cooler0.workingTime.get(Calendar.MINUTE) + cooler1.workingTime.get(Calendar.MINUTE));
                        hours += minutes/60;
                        minutes = minutes%60;
                        if(hours ==0 && minutes == 0 && last){
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                int time = 59;
                                @Override
                                public void run() {
                                    totalTime.setText(time--+"");
                                    if(time<=0){
                                        timer.cancel();
                                    }
                                }
                            },0,1000);

                        }
                    }
                });
            }
        }, 0, 60000);
    }


    /**
     * Description: Послать HTTP Get запрос на указанные ip адрес и порт.
     * Также послать параметр "parameterName" со значением "parameterValue".
     * @param ipAddress ip адрес, на который необходимо послать запрос
     * @param portNumber номер порта ip адреса
     * @return Текст ответа с ip адреса или сообщение ERROR, если не получилось получить ответ
     */
    public String sendRequest(String ipAddress, String portNumber, String command) {
        String serverResponse = "";
        try {
            URL website = new URL("http://"+ipAddress+":"+portNumber+"/send?command="+command);
            System.out.println(website);
            HttpURLConnection httpURLConnection = (HttpURLConnection)website.openConnection();
            httpURLConnection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            serverResponse = response.toString();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            serverResponse = response.toString();
        } catch (Exception ex){
            System.out.println();
            System.out.println("eroerororor"+ex);
            return null;
        }
        // вернуть текст отклика сервера
        System.out.println(serverResponse);
        return serverResponse;
    }

    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void>{
        String ipAddress;
        String portNumber;
        String requests;
        public HttpRequestAsyncTask(String ipAddress, String portNumber, String command){
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;
            this.requests = command;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String str = sendRequest(ipAddress, portNumber, requests);
            if(str != null){
                startTimer();
            }
            return null;
        }
    }


    public void setTotalTime() {
        int hours = (cooler0.workingTime.get(Calendar.HOUR_OF_DAY) + cooler1.workingTime.get(Calendar.HOUR_OF_DAY));
        int minutes = (cooler0.workingTime.get(Calendar.MINUTE) + cooler1.workingTime.get(Calendar.MINUTE));
        hours += minutes/60;
        minutes = minutes%60;
        cooler1.update();
        cooler0.update();
        totalTime.setText(String.format("%02d:%02d",hours, minutes));
        System.out.println(cooler0 + "\n" + cooler1);
    }


    class Cooler {
        Calendar workingTime = Calendar.getInstance();
        int id;
        TextView textView;

        {
            workingTime.set(Calendar.MINUTE, 0);
            workingTime.set(Calendar.HOUR_OF_DAY, 0);
        }


        public Cooler(int id){
            this.textView = findViewById(id);
            this.textView.setOnClickListener(new listener());
        }
        public void update(){
            textView.setText(String.format("%02d:%02d",  workingTime.get(Calendar.HOUR_OF_DAY),workingTime.get(Calendar.MINUTE)));
        }


        @Override
        public String toString() {
            int second = (workingTime.get(Calendar.HOUR_OF_DAY)*60+workingTime.get(Calendar.MINUTE))*60;
            return String.format("%d",second);
        }

        TimePickerDialog.OnTimeSetListener t = (view, hourOfDay, minute) -> {
            workingTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            workingTime.set(Calendar.MINUTE, minute);
            textView.setText(String.format("%02d:%02d",  workingTime.get(Calendar.HOUR_OF_DAY),workingTime.get(Calendar.MINUTE)));
            setTotalTime();

        };

        class listener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(MainActivity.this, t,
                        workingTime.get(Calendar.HOUR_OF_DAY),
                        workingTime.get(Calendar.MINUTE), true)
                        .show();
            }
        }
    }
}