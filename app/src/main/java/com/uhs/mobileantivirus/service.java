package com.uhs.mobileantivirus;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class service extends Service {
    private static final String TAG = "UHS Mobile Antivirus";
    private boolean isRunning  = false;
    private Looper looper;
    private MyServiceHandler myServiceHandler;
    NotificationManager notificationManager;
    @Override
    public void onCreate() {
        HandlerThread handlerthread = new HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerthread.start();
        looper = handlerthread.getLooper();
        myServiceHandler = new MyServiceHandler(looper);
        isRunning = true;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = myServiceHandler.obtainMessage();
        msg.arg1 = startId;
        myServiceHandler.sendMessage(msg);
        Toast.makeText(this, "Protection ON", Toast.LENGTH_SHORT).show();
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "UHS_AV")
                .setSmallIcon(R.drawable.ic_uhs48dp)
                .setContentTitle("UHS Mobile Antivirus")
                .setContentText("Protection ON")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100,builder.build());
        return START_STICKY;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "UHS Channel";
            String description = "Channel for notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("UHS_AV", name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        isRunning = false;
        Toast.makeText(this, "Protection OFF", Toast.LENGTH_SHORT).show();
    }
    private final class MyServiceHandler extends Handler {
        public MyServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                for (int i = 0; i < 10; i++) {
                    try {
                        Log.i(TAG, "Protection ON");
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                    }
                    if(!isRunning){
                        break;
                    }
                }
            }
            stopSelfResult(msg.arg1);
            notificationManager.cancel(100);
        }
    }
}