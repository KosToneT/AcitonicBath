package com.example.acitonicbath;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ConnectionService extends Service {
    private final String TAG = "ConnectionService";
    private final String ID = "ConnectionID";

    public static MainActivity.Bath bath;
    public static boolean view;

    public ConnectionService(){}
    public ConnectionService(MainActivity.Bath bath){
        this.bath = bath;
        view = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        stateUpdateNotification();
        createImmortalNottification("Connection", "Start");
        Log.wtf(TAG, "onStartCommand");
        return START_STICKY;
    }
    /**
     * thread update state and send notification on stop
     */
    private void stateUpdateNotification(){
        new Thread(() -> {
            while(true){
                String str =  bath.getServer().sendRequest("state,0");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(str != null && str.split(",")[1].equals("stop")){
                            if(!view) {
                                sendNotification("Готово", "Твоё время пришло...");
                            }
                            bath.setReady();
                            stopForeground(true);
                            stopSelf();

                        }
                    }
                });
                try{
                    Thread.sleep(500L);
                }catch(Exception ex){

                }
            }
        }).start();
    }
    /**
     * send immortal notification on phone
     * @param title title which show
     * @param text text which show
     */
    private void createImmortalNottification(String title, String text){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    ID, "2", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this, ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();
        startForeground(1001, notification);
    }
    /**
     * send notification on phone
     * @param title title which show
     * @param text text which show
     */
    public void sendNotification(String title, String text){
        final int NOTIFY_ID = 1;
        final String CHANNEL_ID = "Done";
        final int PRIORITY_HIGH = 1;
        NotificationManager notificationManager =
                (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(PRIORITY_HIGH)
                        .setDefaults(~0);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        Log.wtf(TAG, "onDestroy");
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        view = false;
        super.onTaskRemoved(rootIntent);
    }
}