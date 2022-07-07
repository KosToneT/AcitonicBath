package com.example.acitonicbath;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static androidx.core.app.NotificationCompat.DEFAULT_ALL;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.TimePickerDialog;
import android.widget.TimePicker;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFY_ID = 1;
    private static final String CHANNEL_ID = "Done";
    private static final String KEY_IP = "IP";

    EditText editTextIP;
    TextView totalTime;
    CheckBox checkBox;
    Button buttonSend;
    Button buttonPlayOrPause;
    Button buttonStop;
    Bath arduino = new Bath();
    TimeFragment timeFragment = new TimeFragment(arduino);
    TemperatureFragment tempFragment = new TemperatureFragment(this);
    SharedPreferences savePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        savePreference = getSharedPreferences("MyPref", MODE_PRIVATE);
        initUI();
        initConnect();
    }
    public void initUI(){
        setFragment(timeFragment);
        setFragment(tempFragment);
        setFragment(timeFragment);

        checkBox = findViewById(R.id.checkBox);
        editTextIP = findViewById(R.id.editIPid);
        editTextIP.setText(savePreference.getString(KEY_IP, ""));

        Button saveBTN = findViewById(R.id.saveButtonID);
        saveBTN.setOnClickListener((view)->{
            SharedPreferences.Editor ed = savePreference.edit();
            ed.putString(KEY_IP, editTextIP.getText().toString());
            ed.apply();
            if(!arduino.getConnection())
                initConnect();
        });

        buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener((view)->{
            sendTime();
            MainActivity.this.runOnUiThread(()->{
                buttonPlayOrPause.setBackgroundResource(R.drawable.icons8_play_32);
            });
        });

        buttonPlayOrPause = findViewById(R.id.buttonStart);
        buttonPlayOrPause.setOnClickListener((View view)->{
            if(arduino.getState().equals(Bath.STATE_READY)){
                sendTime();
                arduino.start();
                MainActivity.this.runOnUiThread(()->{
                    buttonPlayOrPause.setBackgroundResource(R.drawable.icons8_pause_32);
                });
            } else {
                arduino.pause();
                MainActivity.this.runOnUiThread(()->{
                    buttonPlayOrPause.setBackgroundResource(R.drawable.icons8_play_32);
                });
            }
        });
        buttonStop = findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener((View view)->{
            arduino.stop();
            MainActivity.this.runOnUiThread(()->{
                buttonPlayOrPause.setBackgroundResource(R.drawable.icons8_play_32);
            });
        });
        GestureDetectorCompat lSwipeDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener(){
                boolean frag = false;
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
                    if (Math.abs(e1.getY() - e2.getY()) > 100)
                        return false;
                    if (Math.abs(e2.getX() - e1.getX()) > 50 && Math.abs(velocityX) > 0) {
                        if(frag){
                            setFragment(timeFragment);
                        }else{
                            setFragment(tempFragment);
                        }
                        frag = !frag;
                    }
                    return false;
                }
            });
        RelativeLayout main_layout = findViewById(R.id.main_layout);
        main_layout.setOnTouchListener((View v, MotionEvent event)-> {
            return lSwipeDetector.onTouchEvent(event);
        });
    }
    public void setFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public class Bath{
        public static final String STATE_READY = "ready";
        public static final String STATE_STOP = "stop";
        public static final String STATE_TIME0 = "time0";
        public static final String STATE_TIME1 = "time1";

        private String state ="";
        private Cooler cooler0 = new Cooler();
        private Cooler cooler1 = new Cooler();
        private WebServer server = new WebServer();

        public void setState(String state){
            if(!this.state.equals(state)){
                this.state = state;
                switch (state){
                    case "stop":
                        stop();
                        break;
                    case "pause":
                        break;
                    case "ready":
                        break;
                    case "time0":
                        break;
                    case "time1":
                        break;
                    default:
                        break;
                }
            }
        }
        public String getState(){
            return state;
        }

        public void setConnection(boolean connection){
            if(server.connection == connection){
                //not first
            }else{
                //first
                server.connection = connection;
            }
        }

        public void setView(){
            timeFragment.setView(this);
        }

        public boolean getConnection(){
            return server.connection;
        }
        public void createCoolerAndSet(int id){
            setCooler(id, new Cooler());
        }
        public void clearTime(){
            cooler0.clearTime();
            cooler1.clearTime();
        }
        public Cooler getCooler(int id){
            if(id==0){
                return cooler0;
            }else{
                return cooler1;
            }
        }
        public void setCooler(int id, Cooler cooler){
            if(id==0){
                cooler0 = cooler;
            }else{
                cooler1 = cooler;
            }
        }
        public void setView(int id, TextView textView){
            switch (id){
                case 0:
                    cooler0.setView(textView);
                    break;
                case 1:
                    cooler1.setView(textView);
                default:
                    totalTime = textView;
                    setTotalTime();
            }
        }
        public WebServer getServer(){
            return  server;
        }
        public void setServer(WebServer server){
            this.server = server;
        }
        public void start(){
            String requests = "start,0";
            new HttpRequestAsyncTask(arduino.getServer(), requests).execute();
        }
        public void pause(){
            String requests = "stop,0";
            new HttpRequestAsyncTask(arduino.getServer(), requests).execute();
        }
        public void stop(){
            String requests = "stop,0";
            new HttpRequestAsyncTask(arduino.getServer(), requests).execute();
            arduino.clearTime();
            MainActivity.this.runOnUiThread(()->{
                buttonPlayOrPause.setBackgroundResource(R.drawable.icons8_play_32);
            });
            sendNotification("Готово", "Твоё время пришло...");
        }
        public class Cooler {
            Calendar workingTime = Calendar.getInstance();
            TextView textView;
            {
                workingTime.set(Calendar.MINUTE, 0);
                workingTime.set(Calendar.HOUR_OF_DAY, 0);
                workingTime.set(Calendar.SECOND, 0);
            }

            public void setView(TextView textView){
                this.textView = textView;
                this.textView.setOnClickListener(new listener());
                update();
            }
            public void setSecondTime(int second){
                int hour = second/3600;
                second = second%3600;
                int minute = second/60;
                second = second%60;
                workingTime.set(Calendar.SECOND, second);
                workingTime.set(Calendar.MINUTE, minute);
                workingTime.set(Calendar.HOUR_OF_DAY, hour);
            }
            public void clearTime(){
                workingTime.set(Calendar.MINUTE, 0);
                workingTime.set(Calendar.HOUR_OF_DAY, 0);
                workingTime.set(Calendar.SECOND, 0);
            }
            public void update() {
                if (workingTime.get(Calendar.HOUR_OF_DAY) == 0) {
                    textView.setText(String.format("%02d:%02d", workingTime.get(Calendar.MINUTE), workingTime.get(Calendar.SECOND)));
                } else {
                    textView.setText(String.format("%02d:%02d", workingTime.get(Calendar.HOUR_OF_DAY), workingTime.get(Calendar.MINUTE)));
                }
            }
            @Override
            public String toString() {
                int second = ((workingTime.get(Calendar.HOUR_OF_DAY)*60+workingTime.get(Calendar.MINUTE))*60)+workingTime.get(Calendar.SECOND);
                return String.format("%d", second);
            }
            class listener implements View.OnClickListener {
                @Override
                public void onClick(View view) {
                    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            workingTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            workingTime.set(Calendar.MINUTE, minute);
                            textView.setText(String.format("%02d:%02d",  workingTime.get(Calendar.HOUR_OF_DAY),workingTime.get(Calendar.MINUTE)));
                            setTotalTime();
                        }
                    };
                    new TimePickerDialog(MainActivity.this, t,
                            workingTime.get(Calendar.HOUR_OF_DAY),
                            workingTime.get(Calendar.MINUTE), true)
                            .show();
                }
            }
        }
    }
    public void initConnect() {
        String ipAddress = editTextIP.getText().toString();
        if(ipAddress.indexOf(":")<0)return;
        String portNumber = ipAddress.split(":")[1];
        ipAddress = ipAddress.split(":")[0];
        arduino.setServer(new WebServer(ipAddress, portNumber));
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //{command, args}
                boolean connection = false;
                String str = "";
                try {
                    String random = (int) (Math.random() * 10000) + "";
                    str = arduino.getServer().sendRequest("connection," + random);
                    if (str.split(",")[0].equals("connection")) {
                        if (str.split(",")[1].equals(random)) {
                            connection = true;
                        }
                    }
                }catch(Exception ex) {
                    Log.wtf("ERROR", ex);
                }
                arduino.setConnection(connection);
                try{
                    if(arduino.getConnection()){
                        str = arduino.getServer().sendRequest("state,0");
                        if(str.split(",")[0].equals("state")){
                            if(!arduino.getState().equals(str.split(",")[1]))
                                arduino.setState(str.split(",")[1]);
                            MainActivity.this.runOnUiThread(()->{
                            });
                            tempFragment.setTemperature((int)Float.parseFloat(str.split(",")[2]));
                        }
                        if(arduino.getState().equals(Bath.STATE_TIME0)){
                            str = arduino.getServer().sendRequest("getTime,0");
                            if(str.split(",")[0].equals("getTime")){
                                arduino.getCooler(0).setSecondTime(Integer.parseInt(str.split(",")[1]));
                            }
                        }
                        if(arduino.getState().equals(Bath.STATE_TIME1)){
                            str = arduino.getServer().sendRequest("getTime,1");
                            if(str.split(",")[0].equals("getTime")){
                                arduino.getCooler(1).setSecondTime(Integer.parseInt(str.split(",")[1]));
                            }
                        }
                    } else {

                    }
                } catch (Exception ex) {
                    Log.i("initConnect", "" + Arrays.toString(ex.getStackTrace()));
                }
                MainActivity.this.runOnUiThread(()->{
                    checkBox.setChecked(arduino.getConnection());
                    setTotalTime();
                });
                if(!arduino.getConnection()){
                    myTimer.cancel();
                    myTimer.purge();
                }
            }
        }, 0, 1000);
    }
    class WebServer{
        boolean connection;
        String ipAddress, portNumber;
        public WebServer(){}
        public WebServer(String ipAddress, String portNumber){
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;
        }
        /**
         * Description: Послать HTTP Get запрос на указанные ip адрес и порт.
         * Также послать параметр "parameterName" со значением "parameterValue".
         * @return Текст ответа с ip адреса или сообщение ERROR, если не получилось получить ответ
         */
        public String sendRequest(String command) {
            String serverResponse = "";
            try {
                URL website = new URL("http://"+ipAddress+":"+portNumber+"/send?command="+command);
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
                System.out.println(Arrays.toString(ex.getStackTrace()));
                Log.i("sendRequest", "" + ex);
                return null;
            }
            return serverResponse;
        }
    }
    public void sendTime() {
        try{
            String requests = "";
            //OpenConnectionSendRequests
            if(arduino.getState().equals(Bath.STATE_READY)){
                requests = "setTime0," + arduino.cooler0.toString();
                new HttpRequestAsyncTask(arduino.getServer(), requests).execute();
                requests = "setTime1," + arduino.cooler1.toString();
                new HttpRequestAsyncTask(arduino.getServer(), requests).execute();
            }
        }catch(Exception ex){
            Log.i("sendTime", "" + ex);
        }
    }

    public void setTotalTime() {
        int hours = (arduino.cooler0.workingTime.get(Calendar.HOUR_OF_DAY) + arduino.cooler1.workingTime.get(Calendar.HOUR_OF_DAY));
        int minutes = (arduino.cooler0.workingTime.get(Calendar.MINUTE) + arduino.cooler1.workingTime.get(Calendar.MINUTE));
        int seconds = (arduino.cooler0.workingTime.get(Calendar.SECOND) + arduino.cooler1.workingTime.get(Calendar.SECOND));
        hours += minutes/60;
        minutes = minutes%60;
        arduino.cooler1.update();
        arduino.cooler0.update();
        totalTime.setText(String.format("%02d:%02d:%02d",hours, minutes, seconds));
    }

    public void sendNotification(String title, String text){
        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
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

    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void>{
        WebServer server;
        String requests;
        public HttpRequestAsyncTask(WebServer server, String command){
            this.server = server;
            this.requests = command;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            String str = server.sendRequest(requests);
            return null;
        }
    }
}