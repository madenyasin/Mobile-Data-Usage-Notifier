package com.example.mobile_data_usage_notifier;



import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.telephony.SmsManager;
import android.widget.Toast;

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

        for (int i = 1; i <= 90; i++) {
            try {
                if (isStopped()) {
                    // İşlem iptal edildi. stopWorker metodu çalıştı.
                    return Result.failure();
                }
                if (i == 30) { // 5. dakikada sms gönder
                    Data data = getInputData();
                    String number = data.getString("phoneNumber");
                    sendSms(number, "ALERT ~ Mobile data usage alert!");
                }

                Thread.sleep(10 * 1000);
                createNotificationChannel();
                if (checkNetworkStatus().contains("Mobile Data")) {
                    sendNotification();
                }


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

    private String checkNetworkStatus() {
        String result = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(myContext.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities =
                    connectivityManager.getNetworkCapabilities(network);

            if (networkCapabilities != null) {
                StringBuilder status = new StringBuilder();

                status.append("Network Type: ");
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    status.append("Wi-Fi");
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    status.append("Mobile Data");
                } else {
                    status.append("Other");
                }

                status.append("\nIs Metered: ");
                status.append(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED));
                result = status.toString();
                System.out.println(status.toString());
            } else {
                result = "Network not available";
                System.out.println("Network not available");
            }
        } else {
            result = "ConnectivityManager not available";
            System.out.println("ConnectivityManager not available");
        }
        return result;

    }
    public void sendSms(String number, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            Toast.makeText(getApplicationContext(),"Message Sent",Toast.LENGTH_SHORT).show();
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Message could not be sent",Toast.LENGTH_LONG).show();
        }
    }



}
