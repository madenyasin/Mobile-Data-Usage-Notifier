package com.example.mobile_data_usage_notifier;



import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MonitoringWorker extends Worker {

    Context myContext;
    private static final String CHANNEL_ID = "data_usage";
    private static final int NOTIFICATION_ID = 112;
    public MonitoringWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.myContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        for (int i = 1; i <= 100; i++) {
            try {
                Thread.sleep(7000);
                createNotificationChannel();
                sendNotification();

            } catch (InterruptedException e) {
                e.printStackTrace();
                return Result.failure();
            }
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Mobile data usage alert notifications";
            String description = "It comes into play when mobile data is used effectively.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = myContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(myContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Mobile Data Usage Warning!")
                .setContentText("Mobile data is used effectively. Please turn off mobile data and connect to a WI-FI point.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(myContext);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }



}
